package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.calculator.CalculationHistory
import com.example.calculator.CalculatorViewModel
import com.example.ui.theme.BlueMultiply
import com.example.ui.theme.CelebrationPink
import com.example.ui.theme.CoralClear
import com.example.ui.theme.LilacDelete
import com.example.ui.theme.MintDivide
import com.example.ui.theme.PeachSubtract
import com.example.ui.theme.YellowAdd
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiddhyasCalculatorScreen(
  modifier: Modifier = Modifier,
  calculatorViewModel: CalculatorViewModel = viewModel()
) {
  val state by calculatorViewModel.uiState.collectAsState()
  val sheetState = rememberModalBottomSheetState()

  // Background subtle gradient: Soft warm pink to blush cream
  val bgBrush = Brush.verticalGradient(
    colors = listOf(
      Color(0xFFFFEEF3),
      Color(0xFFFFF7F9),
      Color(0xFFFFEBF0)
    )
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(bgBrush)
      .statusBarsPadding()
      .navigationBarsPadding()
  ) {
    // Floating celebratory particles container
    SparkleOverlay(trigger = state.celebrationCount)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Header with Riddhya's Name and Quick Controls
      TopHeader(
        soundEnabled = state.soundEnabled,
        onToggleSound = { calculatorViewModel.toggleSound() },
        onOpenHistory = { calculatorViewModel.toggleHistory() }
      )

      Spacer(modifier = Modifier.height(10.dp))

      // 2. Main Calculator Display Card
      CalculatorDisplay(
        displayText = state.displayText,
        expressionText = state.expressionText,
        errorMessage = state.errorMessage,
        visualCount = state.visualCount,
        isResult = state.isResultState
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 3. Simple, Large, Kid-Friendly Keypad
      KeypadGrid(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        onDigit = { calculatorViewModel.onDigit(it) },
        onDot = { calculatorViewModel.onDot() },
        onOperator = { calculatorViewModel.onOperator(it) },
        onEquals = { calculatorViewModel.onEquals() },
        onClear = { calculatorViewModel.onClear() },
        onBackspace = { calculatorViewModel.onBackspace() },
        onToggleSign = { calculatorViewModel.onToggleSign() }
      )
    }

    // Calculation History Modal Sheet
    if (state.showHistory) {
      ModalBottomSheet(
        onDismissRequest = { calculatorViewModel.toggleHistory() },
        sheetState = sheetState,
        containerColor = Color(0xFFFFF0F5)
      ) {
        HistorySheetContent(
          history = state.history,
          onClear = { calculatorViewModel.clearHistory() },
          onClose = { calculatorViewModel.toggleHistory() }
        )
      }
    }
  }
}

@Composable
private fun TopHeader(
  soundEnabled: Boolean,
  onToggleSound: () -> Unit,
  onOpenHistory: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Large, kid-friendly prominent Title without emojis or badges
    Text(
      text = "Riddhya's Calculator",
      fontSize = 25.sp,
      fontWeight = FontWeight.ExtraBold,
      fontFamily = FontFamily.Cursive,
      color = Color(0xFF880E4F),
      letterSpacing = 0.5.sp,
      textAlign = TextAlign.Start,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier
        .weight(1f)
        .padding(end = 8.dp)
        .testTag("app_title_text")
    )

    // Quick Action Buttons (Sound Toggle & History)
    Row(verticalAlignment = Alignment.CenterVertically) {
      // Sound Toggle Button
      IconButton(
        onClick = onToggleSound,
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(if (soundEnabled) Color(0xFFFFD1DC) else Color(0xFFEEEEEE))
          .testTag("sound_toggle_button")
      ) {
        Icon(
          imageVector = if (soundEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
          contentDescription = if (soundEnabled) "Sound On" else "Sound Muted",
          tint = if (soundEnabled) Color(0xFFE91E63) else Color.Gray,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(6.dp))

      // History Button
      IconButton(
        onClick = onOpenHistory,
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(Color(0xFFFFE4EC))
          .testTag("history_button")
      ) {
        Icon(
          imageVector = Icons.Rounded.History,
          contentDescription = "Show History",
          tint = Color(0xFFAD1457),
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalculatorDisplay(
  displayText: String,
  expressionText: String,
  errorMessage: String?,
  visualCount: Int?,
  isResult: Boolean
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp), ambientColor = Color(0xFFFF80AB), spotColor = Color(0xFFFF4081))
      .border(2.dp, Color(0xFFFFB2D1), RoundedCornerShape(28.dp))
      .testTag("calculator_display_card"),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFFFFFFFF)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.End
    ) {
      // Top Expression Row (e.g. 5 + 3 =)
      Text(
        text = expressionText.ifEmpty { " " },
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFFE91E63).copy(alpha = 0.85f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.End,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Main Large Number Display
      val mainFontSize = when {
        displayText.length > 8 -> 34.sp
        displayText.length > 6 -> 42.sp
        else -> 50.sp
      }

      val textColor = if (errorMessage != null) {
        Color(0xFFFF1744)
      } else if (isResult) {
        Color(0xFF880E4F)
      } else {
        Color(0xFF2B1920)
      }

      Text(
        text = errorMessage ?: displayText,
        fontSize = mainFontSize,
        fontWeight = FontWeight.Black,
        color = textColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.End,
        fontFamily = FontFamily.Default,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("display_text")
      )

      // Kid-friendly visual counter (shows bouncing candies/stars if result is 1..20)
      AnimatedVisibility(
        visible = visualCount != null && visualCount in 1..20,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
      ) {
        visualCount?.let { count ->
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = "Counting Fun ($count):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD81B60)
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Cheerful grid of counting items (stars and candies)
            FlowRow(
              horizontalArrangement = Arrangement.Center,
              verticalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              val emojis = listOf("🍬", "⭐", "🎈", "💖", "🍎", "🌸")
              val chosenEmoji = emojis[(count - 1) % emojis.size]
              for (i in 1..count) {
                Text(
                  text = chosenEmoji,
                  fontSize = 18.sp,
                  modifier = Modifier.padding(horizontal = 2.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun KeypadGrid(
  modifier: Modifier = Modifier,
  onDigit: (Int) -> Unit,
  onDot: () -> Unit,
  onOperator: (String) -> Unit,
  onEquals: () -> Unit,
  onClear: () -> Unit,
  onBackspace: () -> Unit,
  onToggleSign: () -> Unit
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Row 1: Clear, Backspace, Plus/Minus, Divide
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      CalcButton(
        text = "C",
        bgColor = CoralClear,
        textColor = Color.White,
        modifier = Modifier.weight(1f),
        onClick = onClear,
        testTag = "btn_clear"
      )
      CalcButton(
        icon = {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.Backspace,
            contentDescription = "Backspace",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
          )
        },
        bgColor = LilacDelete,
        modifier = Modifier.weight(1f),
        onClick = onBackspace,
        testTag = "btn_backspace"
      )
      CalcButton(
        text = "±",
        bgColor = Color(0xFF9D4EDD),
        textColor = Color.White,
        modifier = Modifier.weight(1f),
        onClick = onToggleSign,
        testTag = "btn_toggle_sign"
      )
      CalcButton(
        text = "÷",
        bgColor = MintDivide,
        textColor = Color.White,
        modifier = Modifier.weight(1f),
        onClick = { onOperator("÷") },
        testTag = "btn_divide"
      )
    }

    // Row 2: 7, 8, 9, Multiply
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      NumberButton(text = "7", modifier = Modifier.weight(1f), onClick = { onDigit(7) }, testTag = "btn_7")
      NumberButton(text = "8", modifier = Modifier.weight(1f), onClick = { onDigit(8) }, testTag = "btn_8")
      NumberButton(text = "9", modifier = Modifier.weight(1f), onClick = { onDigit(9) }, testTag = "btn_9")
      CalcButton(
        text = "×",
        bgColor = BlueMultiply,
        textColor = Color.White,
        modifier = Modifier.weight(1f),
        onClick = { onOperator("×") },
        testTag = "btn_multiply"
      )
    }

    // Row 3: 4, 5, 6, Subtract
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      NumberButton(text = "4", modifier = Modifier.weight(1f), onClick = { onDigit(4) }, testTag = "btn_4")
      NumberButton(text = "5", modifier = Modifier.weight(1f), onClick = { onDigit(5) }, testTag = "btn_5")
      NumberButton(text = "6", modifier = Modifier.weight(1f), onClick = { onDigit(6) }, testTag = "btn_6")
      CalcButton(
        text = "−",
        bgColor = PeachSubtract,
        textColor = Color.White,
        modifier = Modifier.weight(1f),
        onClick = { onOperator("−") },
        testTag = "btn_subtract"
      )
    }

    // Row 4: 1, 2, 3, Add
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      NumberButton(text = "1", modifier = Modifier.weight(1f), onClick = { onDigit(1) }, testTag = "btn_1")
      NumberButton(text = "2", modifier = Modifier.weight(1f), onClick = { onDigit(2) }, testTag = "btn_2")
      NumberButton(text = "3", modifier = Modifier.weight(1f), onClick = { onDigit(3) }, testTag = "btn_3")
      CalcButton(
        text = "+",
        bgColor = YellowAdd,
        textColor = Color.White,
        modifier = Modifier.weight(1f),
        onClick = { onOperator("+") },
        testTag = "btn_add"
      )
    }

    // Row 5: 0, Dot, Equals (Equals is double/large celebration button)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      NumberButton(
        text = "0",
        modifier = Modifier.weight(2f),
        onClick = { onDigit(0) },
        testTag = "btn_0"
      )
      NumberButton(
        text = ".",
        modifier = Modifier.weight(1f),
        onClick = onDot,
        testTag = "btn_dot"
      )
      // Celebration Equals Button
      CalcButton(
        text = "=",
        bgColor = CelebrationPink,
        textColor = Color.White,
        isEquals = true,
        modifier = Modifier.weight(1f),
        onClick = onEquals,
        testTag = "btn_equals"
      )
    }
  }
}

@Composable
private fun NumberButton(
  text: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  testTag: String
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.90f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "numScale"
  )

  Surface(
    onClick = onClick,
    interactionSource = interactionSource,
    modifier = modifier
      .fillMaxHeight()
      .scale(scale)
      .shadow(elevation = if (isPressed) 2.dp else 4.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0xFFFFD6E0), spotColor = Color(0xFFFF80AB))
      .border(1.5.dp, Color(0xFFFFD6E0), RoundedCornerShape(24.dp))
      .testTag(testTag),
    shape = RoundedCornerShape(24.dp),
    color = Color.White
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.fillMaxSize()
    ) {
      Text(
        text = text,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF2B1920)
      )
    }
  }
}

@Composable
private fun CalcButton(
  modifier: Modifier = Modifier,
  text: String? = null,
  icon: (@Composable () -> Unit)? = null,
  bgColor: Color,
  textColor: Color = Color.White,
  isEquals: Boolean = false,
  onClick: () -> Unit,
  testTag: String
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.88f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "btnScale"
  )

  Surface(
    onClick = onClick,
    interactionSource = interactionSource,
    modifier = modifier
      .fillMaxHeight()
      .scale(scale)
      .shadow(
        elevation = if (isPressed) 2.dp else 6.dp,
        shape = RoundedCornerShape(24.dp),
        ambientColor = bgColor,
        spotColor = bgColor
      )
      .testTag(testTag),
    shape = RoundedCornerShape(24.dp),
    color = bgColor
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.fillMaxSize()
    ) {
      if (icon != null) {
        icon()
      } else if (text != null) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = text,
            fontSize = if (isEquals) 34.sp else 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
          )
          if (isEquals) {
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = "✨", fontSize = 14.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun HistorySheetContent(
  history: List<CalculationHistory>,
  onClear: () -> Unit,
  onClose: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Rounded.History,
          contentDescription = null,
          tint = Color(0xFFC2185B),
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Riddhya's Math History",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF880E4F)
        )
      }

      if (history.isNotEmpty()) {
        TextButton(onClick = onClear) {
          Icon(
            imageVector = Icons.Rounded.DeleteOutline,
            contentDescription = "Clear History",
            tint = Color(0xFFFF1744),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Clear", color = Color(0xFFFF1744), fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (history.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(text = "🌟", fontSize = 48.sp)
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No calculations yet!",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF880E4F)
          )
          Text(
            text = "Try solving some fun numbers with Riddhya!",
            fontSize = 14.sp,
            color = Color.Gray
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .height(300.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(history, key = { it.id }) { item ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD6E0))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = item.expression,
                  fontSize = 15.sp,
                  color = Color(0xFFE91E63),
                  fontWeight = FontWeight.Medium
                )
                if (item.visualCount != null && item.visualCount in 1..10) {
                  Text(
                    text = "🍬 ".repeat(item.visualCount),
                    fontSize = 12.sp
                  )
                }
              }

              Text(
                text = item.result,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF880E4F)
              )
            }
          }
        }
      }
    }
  }
}

// Sparkle Particle Data
private data class SparkleParticle(
  val id: Long,
  val initialX: Float,
  val initialY: Float,
  val emoji: String,
  val size: Float,
  val animX: Animatable<Float, *>,
  val animY: Animatable<Float, *>,
  val animAlpha: Animatable<Float, *>
)

@Composable
private fun SparkleOverlay(trigger: Int) {
  val particles = remember { mutableStateListOf<SparkleParticle>() }

  LaunchedEffect(trigger) {
    if (trigger <= 0) return@LaunchedEffect

    val emojis = listOf("⭐", "✨", "💖", "🎉", "🌟", "🍬")
    val random = Random(System.currentTimeMillis())

    // Spawn 12 colorful floating celebration particles
    val newParticles = (1..12).map { i ->
      val targetX = random.nextFloat() * 800f - 400f
      val targetY = -(random.nextFloat() * 500f + 200f)
      SparkleParticle(
        id = System.currentTimeMillis() + i,
        initialX = 400f + (random.nextFloat() * 200f - 100f),
        initialY = 700f,
        emoji = emojis[random.nextInt(emojis.size)],
        size = random.nextFloat() * 14f + 18f,
        animX = Animatable(0f),
        animY = Animatable(0f),
        animAlpha = Animatable(1f)
      )
    }

    particles.clear()
    particles.addAll(newParticles)

    newParticles.forEach { p ->
      val xOffset = (Random.nextFloat() * 600f - 300f)
      val yOffset = -(Random.nextFloat() * 450f + 150f)

      this.launch {
        p.animX.animateTo(xOffset, tween(900, easing = FastOutSlowInEasing))
      }
      this.launch {
        p.animY.animateTo(yOffset, tween(900, easing = FastOutSlowInEasing))
      }
      this.launch {
        p.animAlpha.animateTo(0f, tween(900, easing = LinearEasing))
      }
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    particles.forEach { p ->
      if (p.animAlpha.value > 0.05f) {
        Text(
          text = p.emoji,
          fontSize = p.size.sp,
          modifier = Modifier
            .align(Alignment.Center)
            .scale(p.animAlpha.value)
            .offset(x = p.animX.value.dp, y = p.animY.value.dp)
        )
      }
    }
  }
}

