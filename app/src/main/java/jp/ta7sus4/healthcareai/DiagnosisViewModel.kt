package jp.ta7sus4.healthcareai

import android.os.StrictMode
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class DiagnosisViewModel: ViewModel(){
    private val viewModelScope = CoroutineScope(Dispatchers.IO)

    private val _isStart = mutableStateOf(false)
    val isStart: Boolean by _isStart

    private val _isLoading = mutableStateOf(false)
    val isLoading: Boolean by _isLoading

    private val _isResult = mutableStateOf(false)
    val isResult: Boolean by _isResult

    private var questions = mutableListOf<Question>()
    private var count by mutableStateOf(0)

    private var _resultMessage = mutableStateOf("読み込み中...")
    val resultMessage: String by _resultMessage

    private var _resultScore = mutableStateOf(-1)
    val resultScore by _resultScore


    private val requestMessage = "今の気持ちの健康を数値化するはい/いいえで答えられる質問を9個考えて"
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
            _resultMessage.value = "読み込み中..."
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
        if (_isResult.value) return "読み込み中..."
        if (questions.size <= count) { showResult() }
        return questions.getOrNull(count)?.question ?: "読み込み中..."
    }

    private fun showResult() {
        _isResult.value = true
        resultString()
    }

    private fun resultScore() {
        viewModelScope.launch {
            _resultScore.value = makeHttpRequest(
                listOf(
                    ChatMessage(
                        text = "以下のユーザに対する評価の文からスコアを0-9999の範囲で点数をつけて推測して「0000」のように表して\n\n" + _resultMessage.value,
                        isMe = true
                    ),
                )
            ).filter { it.isDigit() }.toIntOrNull() ?: -2
            if (_resultScore.value >= 10000) {
                _resultScore.value /= 10000
            }
        }
    }
    private fun resultString() {
        if (questions.isEmpty()) return
        val query = questions.mapIndexed { index, it ->
            "${index + 1}: Q:${it.question} A:${if (it.answer == true) "はい" else "いいえ"}\n"
        }
        viewModelScope.launch {
            _resultMessage.value = makeHttpRequest(
                listOf(
                    ChatMessage(text = requestMessage, isMe = true),
                    ChatMessage(text = aiResponse, isMe = false),
                    ChatMessage(text = "100文字以内でアドバイスして$query\n", isMe = true),
                )
            )
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
                ChatMessage(text = requestMessage, isMe = true)
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

            val code = connection.responseCode
            println(code)

            val stream = if (connection.responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            if (connection.responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                Log.d("ChatViewModel", "Error: ${stream.bufferedReader().use { it.readText() }}")
                return "問題が発生しました。再度お試しください。"
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
            return "問題が発生しました。再度お試しください。(${e.message})"
        }
    }
}

data class Question(
    var question: String,
    var answer: Boolean? = null,
)