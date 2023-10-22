package jp.ta7sus4.healthcareai

import android.os.StrictMode
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val FIRST_MESSAGE = "こんにちは！何かお困りですか？"
private const val ERROR_MESSAGE = "問題が発生しました。再度お試しください。"
private const val THINKING = "考えています..."


class ChatViewModel: ViewModel() {
    private val viewModelScope = CoroutineScope(Dispatchers.IO)
    var messages = mutableStateOf(listOf(ChatMessage(text = FIRST_MESSAGE, isMe = false)))

    private fun addMessage(message: ChatMessage) {
        messages.value = messages.value + message
    }

    private fun deleteLastMessage() {
        messages.value = messages.value.dropLast(1)
    }

    fun sendMessage(message: ChatMessage) {
        addMessage(message)
        addMessage(ChatMessage(text = THINKING, isMe = false))
        viewModelScope.launch {
            makeHttpRequest()
        }
    }

    private fun makeHttpRequest(){
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
                    "Bearer " + BuildConfig.aki_key
                )
                doOutput = true
            }

            var jsonInputStringContent = ""
            messages.value.dropLast(1).forEach {
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
                        [${jsonInputStringContent.dropLast(1)}],
                        "stream": true
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
                if (messages.value.last().text == THINKING) {
                    deleteLastMessage()
                }
                addMessage(ChatMessage(text = ERROR_MESSAGE + "(${stream.bufferedReader().use { it.readText() }})", isMe = false))
                return
            }

            val bufferedReader = stream.bufferedReader()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {

                println("Received line: $line")

                if (line!!.length > 5 && line!!.contains("content")) {
                    val jsonObject = JSONObject(line?.substring(5) ?: "")
                    val choices = jsonObject.getJSONArray("choices")
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.getJSONObject("delta")
                    val content = message.getString("content")
                    var lastMessage = messages.value.last().text
                    if (lastMessage == THINKING){
                        lastMessage = ""
                    }
                    messages.value = messages.value.dropLast(1) + ChatMessage(text = lastMessage + content, isMe = false)
                }
            }
        } catch (e: Exception) {
            println(e)
            if (messages.value.last().text == THINKING) {
                deleteLastMessage()
            }
            addMessage(ChatMessage(text = ERROR_MESSAGE + "(${e.message})", isMe = false))
            return
        }
    }
}

data class ChatMessage(
    val text: String,
    val isMe: Boolean,
)