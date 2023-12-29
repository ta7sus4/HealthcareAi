package jp.ta7sus4.healthcareai.diagnosis

import android.os.StrictMode
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.ta7sus4.healthcareai.BuildConfig
import jp.ta7sus4.healthcareai.chat.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed class DiagnosisState {
    object Init : DiagnosisState()
    object Loading : DiagnosisState()
    object Started : DiagnosisState()

    object Result : DiagnosisState()
    object History : DiagnosisState()
}

class DiagnosisViewModel: ViewModel(){
    companion object {
        private const val TRY_MAX_COUNT = 5
    }

    private val dao = RoomApplication.database.diagnosisDao()
    var historyList = mutableStateListOf<DiagnosisEntity>()

    private var _diagnosisState = mutableStateOf<DiagnosisState>(DiagnosisState.Init)
    val diagnosisState: State<DiagnosisState> = _diagnosisState

    private var questions = mutableListOf<Question>()
    private var answerProgress by mutableStateOf(0)

    private var _resultMessage = mutableStateOf("")
    val resultMessage: String by _resultMessage

    private var _resultScore = mutableStateOf(-1)
    val resultScore by _resultScore

    private var aiResponse = ""

    fun startButtonPressed(
        query: String,
    ) {
        if (diagnosisState.value is DiagnosisState.Loading) { return }
        viewModelScope.launch {
            _diagnosisState.value = DiagnosisState.Loading
            withContext(Dispatchers.IO) {
                questions = mutableListOf()
                createQuestion(query)
            }
            _diagnosisState.value = DiagnosisState.Started
            _resultMessage.value = ""
            _resultScore.value = -1
        }
    }

    fun historyButtonPressed() {
        if (historyList.isEmpty()) viewModelScope.launch { withContext(Dispatchers.IO) { loadHistory() } }
        _diagnosisState.value = DiagnosisState.History
    }

    fun historyDeleteButtonPressed(diagnosis: DiagnosisEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                deleteHistory(diagnosis)
            }
        }
    }

    fun historyDeleteAllButtonPressed() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                deleteAllHistory()
            }
        }
    }

    fun endButtonPressed() {
        endDiagnosis()
    }

    fun onClickAnswer(
        bool: Boolean
    ) {
        if (questions.size <= answerProgress) { return }
        questions.getOrNull(answerProgress)?.answer = bool
        answerProgress++
    }

    fun currentQuestion(
        queryMakeAdvice: String,
        queryScoring: String,
    ): String {
        if (diagnosisState.value is DiagnosisState.Result) return ""
        if (questions.size <= answerProgress) { showResult(
            queryMakeAdvice = queryMakeAdvice,
            queryScoring = queryScoring
        ) }
        return questions.getOrNull(answerProgress)?.question ?: ""
    }

    private fun showResult(
        queryMakeAdvice: String,
        queryScoring: String,
    ) {
        _diagnosisState.value = DiagnosisState.Result
        resultString(
            queryMakeAdvice = queryMakeAdvice,
            queryScoring = queryScoring
        )
    }

    private fun resultScore(
        message: String,
        queryScoring: String,
    ) {
        val scores = mutableListOf(-1, -1, -1)
        for (i in 0..2) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    var tryCount = TRY_MAX_COUNT
                    while (scores[i] < 0) {
                        scores[i] = makeHttpRequest(
                            listOf(
                                ChatMessage(
                                    text = queryScoring + message,
                                    isMe = true
                                ),
                            )
                        ).filter { it.isDigit() }.toIntOrNull() ?: -2
                        tryCount--
                        if (tryCount <= 0) {
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
    }
    private fun resultString(
        queryMakeAdvice: String,
        queryScoring: String,
    ) {
        if (questions.isEmpty()) return
        val query = questions.mapIndexed { index, it ->
            "${index + 1}: Q:${it.question} A:${if (it.answer == true) "はい" else "いいえ"}\n"
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var tryCount = TRY_MAX_COUNT
                while (_resultMessage.value.length !in 21..400) {
                    _resultMessage.value = makeHttpRequest(
                        listOf(
                            ChatMessage(text = aiResponse, isMe = false),
                            ChatMessage(text = "$queryMakeAdvice:$query\n", isMe = true),
                        )
                    )
                    tryCount--
                    if (tryCount <= 0) break
                }
                resultScore(
                    message = _resultMessage.value,
                    queryScoring = queryScoring
                )
            }
        }
    }

    private fun endDiagnosis() {
        _diagnosisState.value = DiagnosisState.Init
        answerProgress = 0
        _resultMessage.value = ""
        _resultScore.value = -1
    }

    private fun createQuestion(
        query: String,
    ) {
        aiResponse = makeHttpRequest(
            listOf(
                ChatMessage(text = query, isMe = true)
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
                        "model": "gpt-3.5-turbo-0613",
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
                return ""
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
            return ""
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