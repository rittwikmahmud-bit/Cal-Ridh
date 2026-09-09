package com.example.calculator

import androidx.lifecycle.ViewModel
import com.example.audio.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

data class CalculationHistory(
  val id: Long = System.currentTimeMillis(),
  val expression: String,
  val result: String,
  val visualCount: Int? = null
)

data class CalculatorUiState(
  val displayText: String = "0",
  val expressionText: String = "",
  val pendingOperator: String? = null,
  val firstOperand: Double? = null,
  val isResultState: Boolean = false,
  val errorMessage: String? = null,
  val celebrationCount: Int = 0,
  val visualCount: Int? = null,
  val soundEnabled: Boolean = true,
  val history: List<CalculationHistory> = emptyList(),
  val showHistory: Boolean = false
)

class CalculatorViewModel(
  private val soundManager: SoundManager = SoundManager()
) : ViewModel() {

  private val _uiState = MutableStateFlow(CalculatorUiState())
  val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

  private val decimalFormat = DecimalFormat("0.######", DecimalFormatSymbols(Locale.US))

  fun onDigit(digit: Int) {
    soundManager.playDigit(digit)
    _uiState.update { state ->
      if (state.errorMessage != null || state.isResultState) {
        state.copy(
          displayText = digit.toString(),
          isResultState = false,
          errorMessage = null,
          visualCount = computeVisualCount(digit.toDouble())
        )
      } else {
        val newText = if (state.displayText == "0") {
          digit.toString()
        } else if (state.displayText.replace("-", "").replace(".", "").length >= 10) {
          state.displayText
        } else {
          state.displayText + digit
        }
        state.copy(
          displayText = newText,
          visualCount = computeVisualCount(newText.toDoubleOrNull())
        )
      }
    }
  }

  fun onDot() {
    soundManager.playDot()
    _uiState.update { state ->
      if (state.errorMessage != null || state.isResultState) {
        state.copy(displayText = "0.", isResultState = false, errorMessage = null, visualCount = null)
      } else if (!state.displayText.contains(".")) {
        state.copy(displayText = state.displayText + ".", visualCount = null)
      } else {
        state
      }
    }
  }

  fun onOperator(operator: String) {
    soundManager.playOperator()
    _uiState.update { state ->
      if (state.errorMessage != null) return@update state

      val currentVal = state.displayText.toDoubleOrNull() ?: 0.0

      if (state.pendingOperator != null && !state.isResultState && state.firstOperand != null) {
        // Compute intermediate step
        val intermediate = calculate(state.firstOperand, currentVal, state.pendingOperator)
        if (intermediate == null) {
          state.copy(
            errorMessage = "Oops! Can't divide by 0 🎈",
            displayText = "Oops!",
            pendingOperator = null,
            firstOperand = null,
            visualCount = null
          )
        } else {
          val formatted = formatNumber(intermediate)
          state.copy(
            firstOperand = intermediate,
            pendingOperator = operator,
            expressionText = "$formatted $operator",
            displayText = formatted,
            isResultState = true,
            visualCount = computeVisualCount(intermediate)
          )
        }
      } else {
        val formatted = formatNumber(currentVal)
        state.copy(
          firstOperand = currentVal,
          pendingOperator = operator,
          expressionText = "$formatted $operator",
          isResultState = true,
          visualCount = computeVisualCount(currentVal)
        )
      }
    }
  }

  fun onEquals() {
    _uiState.update { state ->
      if (state.errorMessage != null) return@update state
      val op = state.pendingOperator ?: return@update state
      val first = state.firstOperand ?: return@update state
      val second = state.displayText.toDoubleOrNull() ?: return@update state

      val result = calculate(first, second, op)
      if (result == null) {
        soundManager.playClear()
        state.copy(
          errorMessage = "Oops! Can't divide by 0 🎈",
          displayText = "Oops!",
          expressionText = "${formatNumber(first)} $op ${formatNumber(second)} =",
          pendingOperator = null,
          firstOperand = null,
          visualCount = null
        )
      } else {
        soundManager.playFanfare()
        val formattedResult = formatNumber(result)
        val fullExpr = "${formatNumber(first)} $op ${formatNumber(second)} ="
        val count = computeVisualCount(result)
        val historyEntry = CalculationHistory(
          expression = fullExpr,
          result = formattedResult,
          visualCount = count
        )

        state.copy(
          displayText = formattedResult,
          expressionText = fullExpr,
          firstOperand = result,
          pendingOperator = null,
          isResultState = true,
          celebrationCount = state.celebrationCount + 1,
          visualCount = count,
          history = (listOf(historyEntry) + state.history).take(15)
        )
      }
    }
  }

  fun onToggleSign() {
    soundManager.playOperator()
    _uiState.update { state ->
      if (state.errorMessage != null) return@update state
      if (state.displayText == "0") return@update state

      val newText = if (state.displayText.startsWith("-")) {
        state.displayText.removePrefix("-")
      } else {
        "-${state.displayText}"
      }
      state.copy(
        displayText = newText,
        visualCount = computeVisualCount(newText.toDoubleOrNull())
      )
    }
  }

  fun onBackspace() {
    soundManager.playDelete()
    _uiState.update { state ->
      if (state.errorMessage != null || state.isResultState) {
        state.copy(displayText = "0", isResultState = false, errorMessage = null, visualCount = null)
      } else {
        val newText = if (state.displayText.length <= 1 || (state.displayText.length == 2 && state.displayText.startsWith("-"))) {
          "0"
        } else {
          state.displayText.dropLast(1)
        }
        state.copy(
          displayText = newText,
          visualCount = computeVisualCount(newText.toDoubleOrNull())
        )
      }
    }
  }

  fun onClear() {
    soundManager.playClear()
    _uiState.update { state ->
      state.copy(
        displayText = "0",
        expressionText = "",
        pendingOperator = null,
        firstOperand = null,
        isResultState = false,
        errorMessage = null,
        visualCount = null
      )
    }
  }

  fun toggleSound() {
    val newSound = !_uiState.value.soundEnabled
    soundManager.isSoundEnabled = newSound
    _uiState.update { it.copy(soundEnabled = newSound) }
    if (newSound) {
      soundManager.playDigit(7)
    }
  }

  fun toggleHistory() {
    _uiState.update { it.copy(showHistory = !it.showHistory) }
  }

  fun clearHistory() {
    soundManager.playClear()
    _uiState.update { it.copy(history = emptyList(), showHistory = false) }
  }

  private fun calculate(first: Double, second: Double, operator: String): Double? {
    return when (operator) {
      "+" -> first + second
      "−", "-" -> first - second
      "×", "*" -> first * second
      "÷", "/" -> if (second == 0.0) null else first / second
      else -> second
    }
  }

  private fun formatNumber(number: Double): String {
    if (number.isInfinite() || number.isNaN()) return "Error"
    // Handle integer display cleanly
    return if (number == number.toLong().toDouble() && kotlin.math.abs(number) < 1e12) {
      number.toLong().toString()
    } else {
      decimalFormat.format(number)
    }
  }

  private fun computeVisualCount(num: Double?): Int? {
    if (num == null) return null
    if (num > 0 && num <= 20 && num == num.toLong().toDouble()) {
      return num.toInt()
    }
    return null
  }
}
