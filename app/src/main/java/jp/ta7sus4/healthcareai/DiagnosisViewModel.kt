package jp.ta7sus4.healthcareai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class DiagnosisViewModel {
    private val _isStart = mutableStateOf(false)
    val isStart: Boolean by _isStart
    private var questions: List<Question> = List(4) { i ->
        Question("質問${i + 1}")
    }
    private var count by mutableStateOf(0)

    fun startButtonPressed() {
        count = 0
        _isStart.value = true
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
        if (questions.size <= count) { endDiagnosis() }
        return questions.getOrNull(count)?.question ?: ""
    }

    private fun endDiagnosis() {
        _isStart.value = false
        count = 0
    }
}

data class Question(
    var question: String,
    var answer: Boolean? = null,
)