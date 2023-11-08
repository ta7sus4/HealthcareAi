package jp.ta7sus4.healthcareai.diagnosis

import android.os.StrictMode
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import jp.ta7sus4.healthcareai.BuildConfig
import jp.ta7sus4.healthcareai.chat.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class DiagnosisViewModel: ViewModel(){
    companion object {
        private const val TRY_MAX_COUNT = 5
        private const val TEXT_LOADING = "読み込み中..."
        private const val TEXT_REQUEST_QUESTION = "今の気持ちの健康を数値化するはい/いいえで答えられる質問を9個考えて"
        private const val TEXT_REQUEST_SCORE = "以下のユーザに対する評価の文からスコアを0-9999の範囲で一の位まで点数をつけて推測して必ず「XXXX」のように表して:"
        private const val TEXT_REQUEST_ADVICE = "100文字以内でアドバイスして"
        private const val TEXT_ERROR = "問題が発生しました。再度お試しください"
    }

    private val viewModelScope = CoroutineScope(Dispatchers.IO)

    private val _isStart = mutableStateOf(false)
    val isStart: Boolean by _isStart

    private val _isLoading = mutableStateOf(false)
    val isLoading: Boolean by _isLoading

    private val _isResult = mutableStateOf(false)
    val isResult: Boolean by _isResult

    private var questions = mutableListOf<Question>()
    private var count by mutableStateOf(0)

    private var _resultMessage = mutableStateOf(TEXT_LOADING)
    val resultMessage: String by _resultMessage

    private var _resultScore = mutableStateOf(-1)
    val resultScore by _resultScore


    private var aiResponse = ""

    fun startButtonPressed() {
        if (_isLoading.value) { return }
        viewModelScope.launch {
            _isLoading.value = true
            questions = mutableListOf()
            createQuestion()
            count = 0
            _isStart.value = true
            _isLoading.value = false
            _resultMessage.value = TEXT_LOADING
            _resultScore.value = -1
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
        if (_isResult.value) return TEXT_LOADING
        if (questions.size <= count) { showResult() }
        return questions.getOrNull(count)?.question ?: TEXT_LOADING
    }

    private fun showResult() {
        _isResult.value = true
        resultString()
    }

    private fun resultScore() {
        val scores = mutableListOf<Int>(-1, -1, -1)
        for (i in 0..2) {
            viewModelScope.launch {
                var tryCount = TRY_MAX_COUNT
                while (scores[i] < 0) {
                    scores[i] = makeHttpRequest(
                        listOf(
                            ChatMessage(
                                text = TEXT_REQUEST_SCORE + _resultMessage.value,
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
                if (scores[i] >= 10000) scores[i] /= 10000
                if (scores.all { it >= 0 }) _resultScore.value = scores.average().toInt()
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
            resultScore()
        }
    }

    private fun endDiagnosis() {
        _isStart.value = false
        _isResult.value = false
        count = 0
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
}

data class Question(
    var question: String,
    var answer: Boolean? = null,
)