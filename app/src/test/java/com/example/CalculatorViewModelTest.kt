package com.example

import com.example.calculator.CalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CalculatorViewModelTest {

  private lateinit var viewModel: CalculatorViewModel

  @Before
  fun setUp() {
    viewModel = CalculatorViewModel()
  }

  @Test
  fun `simple addition calculation works`() {
    viewModel.onDigit(3)
    viewModel.onOperator("+")
    viewModel.onDigit(5)
    viewModel.onEquals()

    val state = viewModel.uiState.value
    assertEquals("8", state.displayText)
    assertEquals(8, state.visualCount)
    assertEquals(1, state.history.size)
  }

  @Test
  fun `multiplication and visual count computation works`() {
    viewModel.onDigit(4)
    viewModel.onOperator("×")
    viewModel.onDigit(3)
    viewModel.onEquals()

    val state = viewModel.uiState.value
    assertEquals("12", state.displayText)
    assertEquals(12, state.visualCount)
  }

  @Test
  fun `division by zero handles safely without crashing`() {
    viewModel.onDigit(9)
    viewModel.onOperator("÷")
    viewModel.onDigit(0)
    viewModel.onEquals()

    val state = viewModel.uiState.value
    assertEquals("Oops!", state.displayText)
    assertNotNull(state.errorMessage)
  }

  @Test
  fun `clear resets state back to zero`() {
    viewModel.onDigit(7)
    viewModel.onOperator("+")
    viewModel.onDigit(2)
    viewModel.onClear()

    val state = viewModel.uiState.value
    assertEquals("0", state.displayText)
    assertEquals("", state.expressionText)
    assertNull(state.errorMessage)
  }

  @Test
  fun `backspace removes last character`() {
    viewModel.onDigit(1)
    viewModel.onDigit(5)
    viewModel.onBackspace()

    val state = viewModel.uiState.value
    assertEquals("1", state.displayText)
  }
}
