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
        addMessage(ChatMessage(text = "考えています...", isMe = false))
        viewModelScope.launch {
            val responseMessage = makeHttpRequest()
            deleteLastMessage()
            addMessage(ChatMessage(text = responseMessage, isMe = false))
        }
    }

    private fun makeHttpRequest(): String {
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
            println(code) // Print HTTP response code for debug purposes.

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