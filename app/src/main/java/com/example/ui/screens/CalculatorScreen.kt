package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.MainViewModel

@Composable
fun CalculatorButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = CircleShape,
    isHighlighted: Boolean = false,
    fontSizeOverride: androidx.compose.ui.unit.TextUnit? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Tactile spring physics contraction on button compression
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.00f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMedium),
        label = "click_scale"
    )

    // Blend current button colors with minor darkening on compression
    val animatedColor by animateColorAsState(
        targetValue = if (isPressed) containerColor.copy(alpha = 0.78f) else containerColor,
        animationSpec = tween(durationMillis = 80),
        label = "click_color"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(animatedColor)
            .testTag(testTag)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disable default standard android gray click overlay to use custom reactive animatedColor
                onClick = onClick
            )
    ) {
        Text(
            text = text,
            fontSize = fontSizeOverride ?: (if (text.length > 3) 14.sp else if (text.length > 1) 18.sp else 24.sp),
            color = contentColor,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val calcInput by viewModel.calcInput.collectAsState()
    val calcResult by viewModel.calcResult.collectAsState()
    val isSosActive by viewModel.isSosActive.collectAsState()
    val navigateToSettings by viewModel.shouldNavigateToSettings.collectAsState()
    val historyList by viewModel.calcHistoryList.collectAsState()
    val isDegreeMode by viewModel.isDegreeMode.collectAsState()
    val animateResultTrigger by viewModel.animateResultTrigger.collectAsState()

    var showHistorySheet by remember { mutableStateOf(false) }
    var forceScientificByButton by remember { mutableStateOf(false) }

    // Color theme setups
    val amoledBg = Color(0xFF000000)
    // Operators/digits color mappings matching premium iOS styles
    val colorGray = Color(0xFFA5A5A5)       // Operations (C, DEL)
    val colorOnGray = Color(0xFF000000)
    val colorCharcoal = Color(0xFF333333)   // Digits
    val colorOnCharcoal = Color(0xFFFFFFFF)
    val colorOrange = Color(0xFFFF9F0A)     // Arithmetic symbols (+, -, x, x, =)
    val colorOnOrange = Color(0xFFFFFFFF)
    
    val colorScienceNormal = Color(0xFF212121) // Scientific functions normal container
    val colorSciOperator = Color(0xFF4B5563)   // Scientific operators like (), √
    
    // Sense layout orientation & desktop width configurations
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600
    val isScientificLayout = isLandscape || isTablet || forceScientificByButton
    val buttonRatio = if (isScientificLayout) 1.5f else 1.0f
    val buttonShape = if (isScientificLayout) RoundedCornerShape(20.dp) else CircleShape

    val focusRequester = remember { FocusRequester() }

    // Elastic pop scale animatable for answers
    val resultScale = remember { Animatable(1.0f) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Handle hidden settings navigation
    LaunchedEffect(navigateToSettings) {
        if (navigateToSettings) {
            onSettingsClick()
            viewModel.shouldNavigateToSettings.value = false
        }
    }

    // Perform organic scale expansion for computed results
    LaunchedEffect(animateResultTrigger) {
        if (animateResultTrigger > 0) {
            resultScale.animateTo(
                targetValue = 1.15f,
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
            )
            resultScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    val onKeyTap = { key: String ->
        when (key) {
            "sin" -> viewModel.onCalcPress("sin(", lifecycleOwner)
            "cos" -> viewModel.onCalcPress("cos(", lifecycleOwner)
            "tan" -> viewModel.onCalcPress("tan(", lifecycleOwner)
            "asin" -> viewModel.onCalcPress("asin(", lifecycleOwner)
            "acos" -> viewModel.onCalcPress("acos(", lifecycleOwner)
            "atan" -> viewModel.onCalcPress("atan(", lifecycleOwner)
            "log" -> viewModel.onCalcPress("log(", lifecycleOwner)
            "ln" -> viewModel.onCalcPress("ln(", lifecycleOwner)
            "√" -> viewModel.onCalcPress("√(", lifecycleOwner)
            "∛" -> viewModel.onCalcPress("∛(", lifecycleOwner)
            "x²" -> viewModel.onCalcPress("^2", lifecycleOwner)
            "x³" -> viewModel.onCalcPress("^3", lifecycleOwner)
            "xʸ" -> viewModel.onCalcPress("^", lifecycleOwner)
            "π" -> viewModel.onCalcPress("π", lifecycleOwner)
            "e" -> viewModel.onCalcPress("e", lifecycleOwner)
            "DEG" -> viewModel.setDegreeMode(true)
            "RAD" -> viewModel.setDegreeMode(false)
            else -> viewModel.onCalcPress(key, lifecycleOwner)
        }
    }

    val standardKeys = listOf(
        listOf("C", "DEL", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "#", "=")
    )

    val scientificKeys = listOf(
        listOf("(", ")", "∛", "x³"),
        listOf("sin", "cos", "tan", "xʸ"),
        listOf("asin", "acos", "atan", "√"),
        listOf("ln", "log", "π", "e"),
        listOf("x²", "!", "DEG", "RAD")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(amoledBg)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val char = when (keyEvent.key) {
                        Key.Zero -> "0"
                        Key.One -> "1"
                        Key.Two -> "2"
                        Key.Three -> "3"
                        Key.Four -> "4"
                        Key.Five -> "5"
                        Key.Six -> "6"
                        Key.Seven -> "7"
                        Key.Eight -> "8"
                        Key.Nine -> "9"
                        Key.Equals, Key.Enter -> "="
                        Key.Backspace -> "DEL"
                        Key.Escape -> "C"
                        Key.Plus -> "+"
                        Key.Minus -> "-"
                        Key.Period -> "."
                        else -> null
                    }
                    if (char != null) {
                        onKeyTap(char)
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Display Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                // Header row containing mode indicator and hidden SOS active status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2C2C2E))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isDegreeMode) "DEG" else "RAD",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    if (isSosActive) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FF3B30))
                        )
                    }
                }

                // Dynamic display typography resizing gracefully based on length
                val displayFontSize = when {
                    calcInput.length < 8 -> 56.sp
                    calcInput.length < 12 -> 40.sp
                    calcInput.length < 16 -> 32.sp
                    calcInput.length < 24 -> 24.sp
                    else -> 18.sp
                }

                Text(
                    text = calcInput.ifEmpty { "0" },
                    fontSize = displayFontSize,
                    color = Color.White,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    lineHeight = displayFontSize * 1.15f,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                val resultFontSize = when {
                    calcResult.length < 8 -> 52.sp
                    calcResult.length < 12 -> 38.sp
                    calcResult.length < 18 -> 26.sp
                    else -> 20.sp
                }

                Text(
                    text = calcResult,
                    fontSize = resultFontSize,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(resultScale.value)
                )
            }

            // Options Toolbar (History & Scientific Mode toggles)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showHistorySheet = true },
                    modifier = Modifier.testTag("btn_history")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Calculator History",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }

                IconButton(
                    onClick = { forceScientificByButton = !forceScientificByButton },
                    modifier = Modifier.testTag("btn_toggle_scientific")
                ) {
                    Icon(
                        imageVector = if (isScientificLayout) Icons.Default.Calculate else Icons.Default.Science,
                        contentDescription = "Toggle Scientific Mode",
                        tint = if (isScientificLayout) colorOrange else Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Interactive dynamic grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left Column: Scientific Keyboard
                AnimatedVisibility(
                    visible = isScientificLayout,
                    enter = fadeIn(animationSpec = spring()) + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally(),
                    modifier = Modifier.weight(1.22f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (rowIdx in 0..4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val scientificRow = scientificKeys[rowIdx]
                                scientificRow.forEach { char ->
                                    val isSciOp = char in listOf("(", ")", "∛", "x³", "xʸ", "√", "!", "%", "π", "e", "x²")
                                    val isSciFunc = char in listOf("sin", "cos", "tan", "asin", "acos", "atan", "ln", "log")
                                    val isDegRad = char in listOf("DEG", "RAD")
                                    
                                    val (bg, fg, isAct) = when {
                                        isDegRad -> {
                                            val isActive = (char == "DEG" && isDegreeMode) || (char == "RAD" && !isDegreeMode)
                                            if (isActive) Triple(colorOrange, colorOnOrange, true)
                                            else Triple(colorScienceNormal, Color.White.copy(alpha = 0.5f), false)
                                        }
                                        isSciOp -> Triple(colorSciOperator, Color.White, false)
                                        isSciFunc -> Triple(colorScienceNormal, Color(0xFFFFB03A), false)
                                        else -> Triple(colorScienceNormal, Color.White, false)
                                    }

                                    CalculatorButton(
                                        text = char,
                                        containerColor = bg,
                                        contentColor = fg,
                                        isHighlighted = isAct,
                                        shape = RoundedCornerShape(20.dp),
                                        testTag = "calc_scientific_$char",
                                        modifier = Modifier.weight(1f).aspectRatio(buttonRatio),
                                        onClick = { onKeyTap(char) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Column: Standard Keyboard
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (rowIdx in 0..4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val standardRow = standardKeys[rowIdx]
                            standardRow.forEach { char ->
                                val isOperator = char in listOf("÷", "×", "-", "+", "=")
                                val isClearOrDel = char in listOf("C", "DEL", "%")
                                
                                val bg = when {
                                    isOperator -> colorOrange
                                    isClearOrDel -> colorGray
                                    else -> colorCharcoal
                                }
                                val fg = when {
                                    isOperator -> colorOnOrange
                                    isClearOrDel -> colorOnGray
                                    else -> colorOnCharcoal
                                }

                                CalculatorButton(
                                    text = char,
                                    containerColor = bg,
                                    contentColor = fg,
                                    shape = buttonShape,
                                    testTag = "calc_key_$char",
                                    modifier = Modifier.weight(1f).aspectRatio(buttonRatio),
                                    onClick = { onKeyTap(char) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Calculation History Panel
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = Color(0xFF1C1C1E),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.4f)) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calculation History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    TextButton(onClick = { viewModel.clearHistory() }) {
                        Text("Clear All", color = colorOrange)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.12f))

                if (historyList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent calculations",
                            color = Color.White.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(historyList) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.restoreHistoryEntry(item.expression, item.result)
                                        showHistorySheet = false
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = item.expression,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.result,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = colorOrange
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
