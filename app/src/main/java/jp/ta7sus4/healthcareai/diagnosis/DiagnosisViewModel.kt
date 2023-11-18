package jp.ta7sus4.healthcareai.diagnosis

import android.os.StrictMode
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import jp.ta7sus4.healthcareai.BuildConfig
import jp.ta7sus4.healthcareai.chat.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

enum class DiagnosisState {
    INIT,
    LOADING,
    STARTED,
    RESULT,
    HISTORY,
}

class DiagnosisViewModel: ViewModel(){
    companion object {
        private const val TRY_MAX_COUNT = 5
        private const val TEXT_LOADING = "読み込み中..."
        private const val TEXT_REQUEST_QUESTION = "今の気持ちの健康を数値化するはい/いいえで答えられる質問を9個考えて"
        private const val TEXT_REQUEST_SCORE = "以下のユーザに対する評価の文からスコアを0-9999の範囲で一の位まで点数をつけて推測して必ず「XXXX」のように表して:"
        private const val TEXT_REQUEST_ADVICE = "100文字以内でアドバイスして"
        private const val TEXT_ERROR = "問題が発生しました。再度お試しください"
    }

    private val dao = RoomApplication.database.diagnosisDao()
    var historyList = mutableStateListOf<DiagnosisEntity>()

    private val viewModelScope = CoroutineScope(Dispatchers.IO)

    private var _diagnosisState = mutableStateOf(DiagnosisState.INIT)
    val diagnosisState: DiagnosisState by _diagnosisState

    private var questions = mutableListOf<Question>()
    private var count by mutableStateOf(0)

    private var _resultMessage = mutableStateOf(TEXT_LOADING)
    val resultMessage: String by _resultMessage

    private var _resultScore = mutableStateOf(-1)
    val resultScore by _resultScore


    private var aiResponse = ""

    fun startButtonPressed() {
        if (diagnosisState == DiagnosisState.LOADING) { return }
        viewModelScope.launch {
            _diagnosisState.value = DiagnosisState.LOADING
            questions = mutableListOf()
            createQuestion()
            count = 0
            _diagnosisState.value = DiagnosisState.STARTED
            _resultMessage.value = TEXT_LOADING
            _resultScore.value = -1
        }
    }

    fun historyButtonPressed() {
        if (historyList.isEmpty()) viewModelScope.launch { loadHistory() }
        _diagnosisState.value = DiagnosisState.HISTORY
    }

    fun historyDeleteButtonPressed() {
        viewModelScope.launch {
            deleteAllHistory()
        }
    }

    fun endButtonPressed() {
        endDiagnosis()
    }

    fun onClickYes() {
        if (questions.size <= count) { return }
        questions.getOrNull(count)?.answer = true
        changeQuestion()
    }
    fun onClickNo() {
        if (questions.size <= count) { return }
        questions.getOrNull(count)?.answer = false
        changeQuestion()
    }

    private fun changeQuestion() {
        count++
    }

    fun currentQuestion(): String {
        if (diagnosisState == DiagnosisState.RESULT) return TEXT_LOADING
        if (questions.size <= count) { showResult() }
        return questions.getOrNull(count)?.question ?: TEXT_LOADING
    }

    private fun showResult() {
        _diagnosisState.value = DiagnosisState.RESULT
        resultString()
    }

    private fun resultScore(message: String) {
        val scores = mutableListOf(-1, -1, -1)
        for (i in 0..2) {
            viewModelScope.launch {
                var tryCount = TRY_MAX_COUNT
                while (scores[i] < 0) {
                    scores[i] = makeHttpRequest(
                        listOf(
                            ChatMessage(
                                text = TEXT_REQUEST_SCORE + message,
                                isMe = true
                            ),
                        )
                    ).filter { it.isDigit() }.toIntOrNull() ?: -2
                    tryCount--
                    if (tryCount <= 0){
                        scores[i] = 5000
                        break
                    }
                }
                if (scores[i] >= 10000) scores[i] %= 10000
                if (scores.all { it >= 0 }) {
                    val averageScore = scores.average().toInt()
                    _resultScore.value = averageScore
                    postHistory(averageScore, message)
                }
            }
        }
    }
    private fun resultString() {
        if (questions.isEmpty()) return
        val query = questions.mapIndexed { index, it ->
            "${index + 1}: Q:${it.question} A:${if (it.answer == true) "はい" else "いいえ"}\n"
        }
        viewModelScope.launch {
            var tryCount = TRY_MAX_COUNT
            while (_resultMessage.value.length !in 21..400) {
                _resultMessage.value = makeHttpRequest(
                    listOf(
                        ChatMessage(text = TEXT_REQUEST_QUESTION, isMe = true),
                        ChatMessage(text = aiResponse, isMe = false),
                        ChatMessage(text = "$TEXT_REQUEST_ADVICE:$query\n", isMe = true),
                    )
                )
                tryCount--
                if (tryCount <= 0) break
            }
            resultScore(_resultMessage.value)
        }
    }

    private fun endDiagnosis() {
        _diagnosisState.value = DiagnosisState.INIT
        count = 0
        _resultMessage.value = TEXT_LOADING
        _resultScore.value = -1
    }

    private fun createQuestion() {
        aiResponse = makeHttpRequest(
            listOf(
                ChatMessage(text = TEXT_REQUEST_QUESTION, isMe = true)
            )
        )
        val questionListResponse = aiResponse.split("\n")
        var number = 0
        for (q in questionListResponse) {
            if (q.getOrNull(0)?.isDigit() == true) {
                questions.add(Question(q))
                number++
            }
        }
    }

    private fun makeHttpRequest(queryMessage: List<ChatMessage>): String {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            val url = URL("https://api.openai.iniad.org/api/v1/chat/completions")
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty(
                    "Authorization",
                    "Bearer " + BuildConfig.api_key
                )
                doOutput = true
            }

            var jsonInputStringContent = ""
            queryMessage.forEach {
                jsonInputStringContent += """
                        { "role": "${if (it.isMe) "user" else "assistant"}",
                          "content": "${it.text}"
                        },
                    """.trimIndent()
            }
            println(jsonInputStringContent)

            val jsonInputString = """
                    {
                        "model": "gpt-3.5-turbo",
                        "messages": 
                        [${jsonInputStringContent.dropLast(1)}]
                        
                    }
                """.trimIndent().replace(" ", "").replace("\n", "")
            println(jsonInputString)


            connection.outputStream.use { os ->
                val input = jsonInputString.toByteArray(charset("utf-8"))
                os.write(input, 0, input.size)
            }

            connection.connect()

            println(connection.responseCode)

            val stream = if (connection.responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            if (connection.responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                Log.d("ChatViewModel", "Error: ${stream.bufferedReader().use { it.readText() }}")
                return TEXT_ERROR
            }

            val result = stream.bufferedReader().use { it.readText() }
            println(result)
            val jsonObject = JSONObject(result)
            val choices = jsonObject.getJSONArray("choices")
            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.getJSONObject("message")
            return message.getString("content")
        } catch (e: Exception) {
            println(e)
            return "$TEXT_ERROR(${e.message})"
        }
    }

    private suspend fun loadHistory() {
        withContext(Dispatchers.Default) {
            dao.getAll().forEach { history ->
                historyList.add(history)
            }
        }
    }

    private suspend fun postHistory(score: Int, comment: String) {
        withContext(Dispatchers.Default) {
            dao.post(DiagnosisEntity(score = score, comment = comment))
            historyList.clear()
            loadHistory()
        }
    }

    private suspend fun deleteHistory(diagnosis: DiagnosisEntity) {
        withContext(Dispatchers.Default) {
            dao.delete(diagnosis)
            historyList.clear()
            loadHistory()
        }
    }

    private suspend fun deleteAllHistory() {
        withContext(Dispatchers.Default) {
            dao.deleteAll()
            historyList.clear()
            loadHistory()
        }
    }
}

data class Question(
    var question: String,
    var answer: Boolean? = null,
)