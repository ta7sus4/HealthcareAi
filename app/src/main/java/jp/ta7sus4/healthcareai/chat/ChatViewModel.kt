package jp.ta7sus4.healthcareai.chat

import android.os.StrictMode
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.ta7sus4.healthcareai.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ChatViewModel: ViewModel() {
    var messages = mutableStateOf(listOf<ChatMessage>())

    private fun addMessage(message: ChatMessage) {
        messages.value = messages.value + message
    }

    fun sendButtonPressed(query: String){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                sendMessage(ChatMessage(text = query, isMe = true))
            }
        }
    }

    private fun sendMessage(message: ChatMessage) {
        addMessage(message)
        addMessage(ChatMessage(text = "", isMe = false))
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                makeHttpRequest()
            }
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
                    "Bearer " + BuildConfig.api_key
                )
                doOutput = true
            }

            var jsonInputStringContent = ""
            messages.value.dropLast(1).forEach {
                jsonInputStringContent += """
                        { "role": "${if (it.isMe) "user" else "assistant"}",
                          "content": "${it.text.replace("\"", "'")}"
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

            println(connection.responseCode)

            val stream = if (connection.responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            if (connection.responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                Log.d("ChatViewModel", "Error: ${stream.bufferedReader().use { it.readText() }}")
                addMessage(ChatMessage(text = "(${stream.bufferedReader().use { it.readText() }})", isMe = false, isError = true))
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
                    val lastMessage = messages.value.last().text
                    messages.value = messages.value.dropLast(1) + ChatMessage(text = lastMessage + content, isMe = false)
                }
            }
        } catch (e: Exception) {
            println(e)
            addMessage(ChatMessage(text = "(${e.message})", isMe = false, isError = true))
            return
        }
    }
}

data class ChatMessage(
    val text: String,
    val isMe: Boolean,
    val isError: Boolean = false
)