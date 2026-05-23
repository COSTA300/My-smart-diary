package com.example

import android.os.Bundle
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.CompanionPersonality
import com.example.data.DecryptedChatMessage
import com.example.data.DecryptedDiaryEntry
import com.example.viewmodel.DiaryViewModel
import com.example.viewmodel.PasscodeState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: DiaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val locksState by viewModel.locksState.collectAsStateWithLifecycle()
            val isDecoySession by viewModel.isDecoySession.collectAsStateWithLifecycle()
            val activeThemeId by viewModel.activeTheme.collectAsStateWithLifecycle()
            
            // Dynamic Light/Dark Mode State
            var isDarkMode by remember { mutableStateOf(true) }
            
            // Generate the design palette matching selection
            val palette = getCozyPalette(activeThemeId, isDarkMode)

            MaterialTheme(
                typography = MaterialTheme.typography // defaults are excellent for base scaling
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = palette.background
                ) {
                    when (locksState) {
                        PasscodeState.NOT_SET -> {
                            PasscodeSetupScreen(
                                viewModel = viewModel,
                                palette = palette
                            )
                        }
                        PasscodeState.LOCKED -> {
                            PasscodeUnlockScreen(
                                viewModel = viewModel,
                                palette = palette
                            )
                        }
                        PasscodeState.UNLOCKED, PasscodeState.DECOY_UNLOCKED -> {
                            DiaryHomeMainScreen(
                                viewModel = viewModel,
                                isDecoy = isDecoySession,
                                palette = palette,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { isDarkMode = !isDarkMode }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== DESIGN PALETTE DEFINITION ====================

data class ColorSanctuary(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val secondary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onPrimary: Color,
    val cardBg: Color,
    val lightAccent: Color,
    val extraAccent: Color
)

fun getCozyPalette(themeId: String, isDarkMode: Boolean): ColorSanctuary {
    return if (isDarkMode) {
        when (themeId) {
            "SAGE_CALM" -> ColorSanctuary(
                background = Color(0xFF131A16),
                surface = Color(0xFF1A241F),
                primary = Color(0xFF8DC0A2),
                secondary = Color(0xFF536E5D),
                onBackground = Color(0xFFEAF2ED),
                onSurface = Color(0xFFD2E0D7),
                onPrimary = Color(0xFF133220),
                cardBg = Color(0xFF222F28),
                lightAccent = Color(0xFF32453B),
                extraAccent = Color(0xFF9CCC9C)
            )
            "MIDNIGHT_VELVET" -> ColorSanctuary(
                background = Color(0xFF0F0F1A),
                surface = Color(0xFF151629),
                primary = Color(0xFF909BFF),
                secondary = Color(0xFF575BB5),
                onBackground = Color(0xFFECEDFF),
                onSurface = Color(0xFFD0D3FF),
                onPrimary = Color(0xFF0E1045),
                cardBg = Color(0xFF1F2140),
                lightAccent = Color(0xFF2E3161),
                extraAccent = Color(0xFFB5BAFF)
            )
            "EMBER_GLOW" -> ColorSanctuary(
                background = Color(0xFF1C1613),
                surface = Color(0xFF281E19),
                primary = Color(0xFFFF9B73),
                secondary = Color(0xFFAB593A),
                onBackground = Color(0xFFFFF0EB),
                onSurface = Color(0xFFFFDDD0),
                onPrimary = Color(0xFF4C1802),
                cardBg = Color(0xFF362821),
                lightAccent = Color(0xFF4C362E),
                extraAccent = Color(0xFFFFB294)
            )
            "AMBER_ROSE" -> ColorSanctuary(
                background = Color(0xFF1B141B),
                surface = Color(0xFF271A26),
                primary = Color(0xFFFF9CE1),
                secondary = Color(0xFFA15A8E),
                onBackground = Color(0xFFFFF0FA),
                onSurface = Color(0xFFFFD9F5),
                onPrimary = Color(0xFF4D023E),
                cardBg = Color(0xFF362435),
                lightAccent = Color(0xFF4E304C),
                extraAccent = Color(0xFFFFB4E9)
            )
            else -> ColorSanctuary(
                background = Color(0xFF131A16),
                surface = Color(0xFF1A241F),
                primary = Color(0xFF8DC0A2),
                secondary = Color(0xFF536E5D),
                onBackground = Color(0xFFEAF2ED),
                onSurface = Color(0xFFD2E0D7),
                onPrimary = Color(0xFF133220),
                cardBg = Color(0xFF222F28),
                lightAccent = Color(0xFF32453B),
                extraAccent = Color(0xFF9CCC9C)
            )
        }
    } else {
        // Light Mode Options
        when (themeId) {
            "SAGE_CALM" -> ColorSanctuary(
                background = Color(0xFFF3F7F5),
                surface = Color(0xFFFFFFFF),
                primary = Color(0xFF285C3A),
                secondary = Color(0xFF538C68),
                onBackground = Color(0xFF1F2421),
                onSurface = Color(0xFF2B332F),
                onPrimary = Color(0xFFFFFFFF),
                cardBg = Color(0xFFE5ECE7),
                lightAccent = Color(0xFFD1E0D6),
                extraAccent = Color(0xFF1A3D25)
            )
            "MIDNIGHT_VELVET" -> ColorSanctuary(
                background = Color(0xFFF2F4FB),
                surface = Color(0xFFFFFFFF),
                primary = Color(0xFF3743A3),
                secondary = Color(0xFF5E6BCD),
                onBackground = Color(0xFF151622),
                onSurface = Color(0xFF21233F),
                onPrimary = Color(0xFFFFFFFF),
                cardBg = Color(0xFFE1E5F7),
                lightAccent = Color(0xFFCDD3F2),
                extraAccent = Color(0xFF1E2660)
            )
            "EMBER_GLOW" -> ColorSanctuary(
                background = Color(0xFFFAF4F1),
                surface = Color(0xFFFFFFFF),
                primary = Color(0xFF94340A),
                secondary = Color(0xFFCD5F33),
                onBackground = Color(0xFF251A15),
                onSurface = Color(0xFF392720),
                onPrimary = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFCEBE3),
                lightAccent = Color(0xFFFBDAD0),
                extraAccent = Color(0xFF632104)
            )
            "AMBER_ROSE" -> ColorSanctuary(
                background = Color(0xFFFAF3F8),
                surface = Color(0xFFFFFFFF),
                primary = Color(0xFF912F7D),
                secondary = Color(0xFFC059A7),
                onBackground = Color(0xFF251623),
                onSurface = Color(0xFF392135),
                onPrimary = Color(0xFFFFFFFF),
                cardBg = Color(0xFFFCE6F8),
                lightAccent = Color(0xFFFAD1F3),
                extraAccent = Color(0xFF611C53)
            )
            else -> ColorSanctuary(
                background = Color(0xFFF3F7F5),
                surface = Color(0xFFFFFFFF),
                primary = Color(0xFF285C3A),
                secondary = Color(0xFF538C68),
                onBackground = Color(0xFF1F2421),
                onSurface = Color(0xFF2B332F),
                onPrimary = Color(0xFFFFFFFF),
                cardBg = Color(0xFFE5ECE7),
                lightAccent = Color(0xFFD1E0D6),
                extraAccent = Color(0xFF1A3D25)
            )
        }
    }
}

// ==================== SCREEN 1: PASSCODE SETUP ====================

@Composable
fun PasscodeSetupScreen(
    viewModel: DiaryViewModel,
    palette: ColorSanctuary
) {
    var primaryPin by remember { mutableStateOf("") }
    var decoyPin by remember { mutableStateOf("") }
    var enableDecoy by remember { mutableStateOf(false) }
    
    val pinSetupError by viewModel.pinSetupError.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Header
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Sanctuary Lock",
                tint = palette.primary,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "DearDiary",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = palette.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Establish your zero-knowledge secure sanctuary. All data stays fully encrypted on your device and never leaves.",
                fontSize = 14.sp,
                color = palette.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Inputs
            Card(
                colors = CardDefaults.cardColors(containerColor = palette.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Core Security PIN",
                        fontWeight = FontWeight.Bold,
                        color = palette.primary,
                        fontSize = 15.sp
                    )

                    OutlinedTextField(
                        value = primaryPin,
                        onValueChange = { if (it.length <= 8) primaryPin = it.filter { char -> char.isDigit() } },
                        label = { Text("Enter 4-8 Digit Master PIN", color = palette.onSurface.copy(alpha = 0.6f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.primary,
                            unfocusedBorderColor = palette.onSurface.copy(alpha = 0.2f),
                            focusedTextColor = palette.onSurface,
                            unfocusedTextColor = palette.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = enableDecoy,
                            onCheckedChange = { enableDecoy = it },
                            colors = CheckboxDefaults.colors(checkedColor = palette.primary)
                        )
                        Text(
                            text = "Add a Decoy PIN (Opens blank sanctuary)",
                            fontSize = 13.sp,
                            color = palette.onSurface
                        )
                    }

                    AnimatedVisibility(visible = enableDecoy) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Decoy Sanctuary PIN",
                                fontWeight = FontWeight.Bold,
                                color = palette.secondary,
                                fontSize = 15.sp
                            )
                            OutlinedTextField(
                                value = decoyPin,
                                onValueChange = { if (it.length <= 8) decoyPin = it.filter { char -> char.isDigit() } },
                                label = { Text("Enter 4-8 Digit Decoy PIN", color = palette.onSurface.copy(alpha = 0.6f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = palette.secondary,
                                    unfocusedBorderColor = palette.onSurface.copy(alpha = 0.2f),
                                    focusedTextColor = palette.onSurface,
                                    unfocusedTextColor = palette.onSurface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            if (pinSetupError != null) {
                Text(
                    text = pinSetupError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = {
                    viewModel.setupPasscodes(
                        primaryPin = primaryPin,
                        decoyPin = if (enableDecoy) decoyPin else null,
                        onComplete = {}
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.onPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Lock & Launch Vault", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==================== SCREEN 2: PASSCODE UNLOCK (KEYPAD) ====================

@Composable
fun PasscodeUnlockScreen(
    viewModel: DiaryViewModel,
    palette: ColorSanctuary
) {
    var pinAttempts by remember { mutableStateOf("") }
    val unlockError by viewModel.unlockError.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    // Custom Shake Animation offset on unlock failure
    val shakeOffset = remember { Animatable(0f) }

    // Rattle helper
    val triggerShake: () -> Unit = {
        scope.launch {
            repeat(4) {
                shakeOffset.animateTo(20f, spring(stiffness = Spring.StiffnessHigh))
                shakeOffset.animateTo(-20f, spring(stiffness = Spring.StiffnessHigh))
            }
            shakeOffset.animateTo(0f, spring(stiffness = Spring.StiffnessHigh))
        }
    }

    // React to PIN build up
    LaunchedEffect(pinAttempts) {
        if (pinAttempts.length >= 4) {
            val unlocked = viewModel.unlockWithPin(pinAttempts)
            if (!unlocked) {
                triggerShake()
                pinAttempts = ""
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .offset(x = shakeOffset.value.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Locked Symbol
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .background(palette.surface, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Safe locked",
                    tint = palette.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Unlock Sanctuary",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.onBackground
                )
                Text(
                    text = "Please enter your 4-8 digit secure passcode",
                    fontSize = 13.sp,
                    color = palette.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Display PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ensure we handle length
                val totalDots = if (pinAttempts.isEmpty()) 4 else pinAttempts.length.coerceAtLeast(4)
                for (i in 0 until totalDots) {
                    val active = i < pinAttempts.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = if (active) palette.primary else palette.surface,
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (active) palette.primary else palette.onBackground.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }

            if (unlockError) {
                Text(
                    text = "Incorrect Sanctuary Passcode",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Interactive Finger-friendly Keypad Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val row1 = listOf("1", "2", "3")
                val row2 = listOf("4", "5", "6")
                val row3 = listOf("7", "8", "9")

                KeypadRow(buttons = row1, palette = palette, onClick = { pinAttempts += it })
                KeypadRow(buttons = row2, palette = palette, onClick = { pinAttempts += it })
                KeypadRow(buttons = row3, palette = palette, onClick = { pinAttempts += it })

                // Bottom keypad row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Biometric Anchor Display (Visual check)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.3f)
                            .background(Color.Transparent, CircleShape)
                            .clickable {
                                // Simulate Biometric prompt click inside sandbox beautifully
                                scope.launch {
                                    val fallbackUnlocked = viewModel.unlockWithPin("1234") // simple emulator debug trigger or informative
                                    if (!fallbackUnlocked) {
                                        triggerShake()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Biometric Bypass",
                                tint = palette.primary.copy(alpha = 0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Mock Bio",
                                fontSize = 9.sp,
                                color = palette.onBackground.copy(alpha = 0.4f)
                            )
                        }
                    }

                    // Key 0
                    KeypadButton(
                        num = "0",
                        palette = palette,
                        modifier = Modifier.weight(1f),
                        onClick = { pinAttempts += "0" }
                    )

                    // Clear button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.3f)
                            .background(palette.surface, CircleShape)
                            .clickable { if (pinAttempts.isNotEmpty()) pinAttempts = pinAttempts.dropLast(1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Backspace",
                            tint = palette.onBackground,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadRow(
    buttons: List<String>,
    palette: ColorSanctuary,
    onClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (num in buttons) {
            KeypadButton(
                num = num,
                palette = palette,
                modifier = Modifier.weight(1f),
                onClick = onClick
            )
        }
    }
}

@Composable
fun KeypadButton(
    num: String,
    palette: ColorSanctuary,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1.3f)
            .background(palette.surface, CircleShape)
            .clickable { onClick(num) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = num,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = palette.onBackground
        )
    }
}

// ==================== SCREEN 3: MAIN SANCTUARY WORKSPACE ====================

@Composable
fun DiaryHomeMainScreen(
    viewModel: DiaryViewModel,
    isDecoy: Boolean,
    palette: ColorSanctuary,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val allEntries by viewModel.allEntries.collectAsStateWithLifecycle()
    val selectedEntry by viewModel.selectedEntry.collectAsStateWithLifecycle()
    
    // Navigation index: 0 = Today's Entry, 1 = Previous Sanctuary Timber, 2 = Sanctuary Settings
    var navIndex by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = palette.surface,
                contentColor = palette.onSurface
            ) {
                NavigationBarItem(
                    selected = navIndex == 0,
                    onClick = { 
                        navIndex = 0 
                        viewModel.selectEntry(null) // return to fresh daily check-in
                    },
                    icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Daily Check-in") },
                    label = { Text("Daily Check") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = palette.primary,
                        unselectedIconColor = palette.onSurface.copy(alpha = 0.5f),
                        selectedTextColor = palette.primary,
                        indicatorColor = palette.lightAccent
                    )
                )

                NavigationBarItem(
                    selected = navIndex == 1,
                    onClick = { navIndex = 1 },
                    icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = "Past Sanctuary") },
                    label = { Text("My Library") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = palette.primary,
                        unselectedIconColor = palette.onSurface.copy(alpha = 0.5f),
                        selectedTextColor = palette.primary,
                        indicatorColor = palette.lightAccent
                    )
                )

                NavigationBarItem(
                    selected = navIndex == 2,
                    onClick = { navIndex = 2 },
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Sanctuary Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = palette.primary,
                        unselectedIconColor = palette.onSurface.copy(alpha = 0.5f),
                        selectedTextColor = palette.primary,
                        indicatorColor = palette.lightAccent
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.background)
                .padding(innerPadding)
        ) {
            // Direct Screen flow rendering based on Navigation tab index
            when {
                selectedEntry != null -> {
                    // Chat Companion entry is selected (either previously saved today, or historical)
                    ChatCompanionScreen(
                        viewModel = viewModel,
                        entry = selectedEntry!!,
                        palette = palette
                    )
                }
                navIndex == 0 -> {
                    DailyCheckinScreen(
                        viewModel = viewModel,
                        palette = palette
                    )
                }
                navIndex == 1 -> {
                    MyLibraryTimelineScreen(
                        viewModel = viewModel,
                        allEntries = allEntries,
                        palette = palette
                    )
                }
                navIndex == 2 -> {
                    SanctuarySettingsScreen(
                        viewModel = viewModel,
                        palette = palette,
                        isDecoy = isDecoy,
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = onToggleDarkMode
                    )
                }
            }
        }
    }
}

// ==================== PANEL A: DAILY CHECK-IN WRITER ====================

@Composable
fun DailyCheckinScreen(
    viewModel: DiaryViewModel,
    palette: ColorSanctuary
) {
    val thoughtsText by viewModel.inputThoughts.collectAsStateWithLifecycle()
    val selectedMood by viewModel.inputMood.collectAsStateWithLifecycle()
    val companionSelected by viewModel.selectedRepositoryCompanion()
    
    val moods = listOf(
        Pair("Peaceful", "😇"),
        Pair("Heavy", "😔"),
        Pair("Anxious", "😰"),
        Pair("Lonely", "🥺"),
        Pair("Hopeful", "🌟"),
        Pair("Secretive", "🤫")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming header prompt
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "DearDiary Sanctuary",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = palette.primary,
                letterSpacing = 1.sp
            )
            Text(
                text = "How are you feeling today?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = palette.onBackground,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Mood Chips Layout
        Column {
            Text(
                text = "My Active State:",
                fontWeight = FontWeight.SemiBold,
                color = palette.onBackground,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                spacing = 8.dp
            ) {
                for (mood in moods) {
                    val active = selectedMood == mood.first
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (active) palette.primary else palette.surface)
                            .clickable { viewModel.inputMood.value = mood.first }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = mood.second, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = mood.first,
                                color = if (active) palette.onPrimary else palette.onSurface,
                                fontSize = 13.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // AI Companion Personality Picker
        Column {
            Text(
                text = "Choose Your Emotional Guide Companion:",
                fontWeight = FontWeight.SemiBold,
                color = palette.onBackground,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CompanionPersonality.values().forEach { comp ->
                    val active = companionSelected == comp
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (active) palette.primary.copy(alpha = 0.15f) else palette.surface
                        ),
                        border = BorderStroke(
                            width = if (active) 2.dp else 1.dp,
                            color = if (active) palette.primary else palette.onSurface.copy(alpha = 0.10f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectedCompanion.value = comp }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = if (active) palette.primary else palette.lightAccent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (active) palette.onPrimary else palette.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = comp.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = palette.onSurface
                                )
                                Text(
                                    text = comp.tagline,
                                    fontSize = 12.sp,
                                    color = palette.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            if (active) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = palette.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Multiline Entry Pad
        OutlinedTextField(
            value = thoughtsText,
            onValueChange = { viewModel.inputThoughts.value = it },
            placeholder = { 
                Text(
                    "Pour your heart out here... write about relationships, intimacies, doubts with faith, financial stress, or raw, unspoken secrets. This is a secure, encrypted void.",
                    color = palette.onBackground.copy(alpha = 0.4f),
                    fontSize = 14.sp
                ) 
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = palette.primary,
                unfocusedBorderColor = palette.onSurface.copy(alpha = 0.15f),
                focusedTextColor = palette.onBackground,
                unfocusedTextColor = palette.onBackground,
                focusedContainerColor = palette.surface,
                unfocusedContainerColor = palette.surface
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp)
        )

        Button(
            onClick = { viewModel.saveEntryAndRespond() },
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.primary,
                contentColor = palette.onPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Publish thoughts")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pour & Seek Understanding", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// FlowRow layout helper for mood chips without external grid failures
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        var rowWidth = 0
        var rowHeight = 0
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentOffset = 0
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var maxWidth = 0
        var totalHeight = 0

        placeables.forEach { placeable ->
            if (currentOffset + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                maxWidth = maxOf(maxWidth, currentOffset)
                totalHeight += rowHeight + spacing.roundToPx()
                currentRow = mutableListOf()
                currentOffset = 0
                rowHeight = 0
            }
            currentRow.add(placeable)
            currentOffset += placeable.width + spacing.roundToPx()
            rowHeight = maxOf(rowHeight, placeable.height)
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            maxWidth = maxOf(maxWidth, currentOffset)
            totalHeight += rowHeight
        }

        layout(maxWidth, totalHeight) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                var maxRowH = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + spacing.roundToPx()
                    maxRowH = maxOf(maxRowH, placeable.height)
                }
                y += maxRowH + spacing.roundToPx()
            }
        }
    }
}

// Helper extension to keep view model bindings elegant
@Composable
fun DiaryViewModel.selectedRepositoryCompanion() = this.selectedCompanion.collectAsStateWithLifecycle()

// ==================== PANEL B: MY LIBRARY / TIMELINE HISTORY ====================

@Composable
fun MyLibraryTimelineScreen(
    viewModel: DiaryViewModel,
    allEntries: List<DecryptedDiaryEntry>,
    palette: ColorSanctuary
) {
    var entryToDelete by remember { mutableStateOf<DecryptedDiaryEntry?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Secure Sanctuary Archives",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = palette.primary,
                letterSpacing = 1.sp
            )
            Text(
                text = "My Journal Timber",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = palette.onBackground,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (allEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Empty sanctuary timeline",
                        tint = palette.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "The sanctuary pages are quiet.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Your written entries and AI guide dialogues will be securely categorized here.",
                        fontSize = 13.sp,
                        color = palette.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(allEntries) { entry ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = palette.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectEntry(entry) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Date / Mood Badge tag
                                Surface(
                                    color = palette.lightAccent,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = entry.mood.uppercase(Locale.getDefault()),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = entry.companionName,
                                        fontSize = 12.sp,
                                        color = palette.onSurface.copy(alpha = 0.6f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { entryToDelete = entry },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete sanctuary entry",
                                            tint = palette.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = entry.dateStr,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.onSurface,
                                modifier = Modifier.padding(top = 10.dp)
                            )

                            // Preview cut mainThoughts
                            Text(
                                text = entry.mainText,
                                fontSize = 14.sp,
                                color = palette.onSurface.copy(alpha = 0.7f),
                                maxLines = 2,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Deletion Dialog
    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        entryToDelete?.let { viewModel.deleteEntry(it.id) }
                        entryToDelete = null
                    }
                ) {
                    Text("Permanently Erase", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Keep Safe", color = palette.onSurface)
                }
            },
            title = { Text("Erase Thoughts Page") },
            text = { Text("Are you absolutely sure you want to permanently erase this entry and its associated AI conversation from local storage? This action cannot be undone.") },
            containerColor = palette.surface,
            titleContentColor = palette.onSurface,
            textContentColor = palette.onSurface.copy(alpha = 0.8f)
        )
    }
}

// ==================== PANEL C: EMOTIONAL DIALOGUE / CHAT SCREENS ====================

@Composable
fun ChatCompanionScreen(
    viewModel: DiaryViewModel,
    entry: DecryptedDiaryEntry,
    palette: ColorSanctuary
) {
    val messages by viewModel.activeChatMessages.collectAsStateWithLifecycle()
    var innerTextReply by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val listState = rememberScrollState()

    // Keep scrolling to the base on reply additions
    LaunchedEffect(messages.size) {
        listState.animateScrollTo(listState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Conversation Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.surface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.selectEntry(null) }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go back",
                    tint = palette.primary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Dialogue with ${entry.companionName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = palette.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = palette.lightAccent,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = entry.mood,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = entry.dateStr,
                    fontSize = 12.sp,
                    color = palette.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        // Scrollable dialogue layout
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(listState)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // First display original user thoughts journal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.End)
                    .padding(start = 48.dp)
            ) {
                Surface(
                    color = palette.lightAccent,
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Pour Entry:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = entry.mainText,
                            fontSize = 14.sp,
                            color = palette.onSurface
                        )
                    }
                }
            }

            // Loop chat messages below
            messages.forEach { msg ->
                val isUser = msg.sender == "user"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = if (isUser) 48.dp else 0.dp,
                            end = if (isUser) 0.dp else 48.dp
                        ),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        color = if (isUser) palette.lightAccent else palette.surface,
                        shape = if (isUser) {
                            RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                        } else {
                            RoundedCornerShape(topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                        },
                        border = if (!isUser) BorderStroke(1.dp, palette.primary.copy(alpha = 0.15f)) else null
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isUser) "You:" else entry.companionName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) palette.primary else palette.extraAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                fontSize = 14.sp,
                                color = palette.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        // Bottom input row inside active dialog to recurse chats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.surface)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = innerTextReply,
                onValueChange = { innerTextReply = it },
                placeholder = { Text("Contribute to dialogue, ask advice...", fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.primary,
                    unfocusedBorderColor = palette.onSurface.copy(alpha = 0.1f),
                    focusedTextColor = palette.onSurface,
                    unfocusedTextColor = palette.onSurface
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (innerTextReply.isNotBlank()) {
                        viewModel.sendChatMessage(innerTextReply)
                        innerTextReply = ""
                    }
                },
                modifier = Modifier
                    .background(palette.primary, CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Submit dialog reply",
                    tint = palette.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ==================== PANEL D: SANCTUARY SETTINGS ====================

@Composable
fun SanctuarySettingsScreen(
    viewModel: DiaryViewModel,
    palette: ColorSanctuary,
    isDecoy: Boolean,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val activeThemeId by viewModel.activeTheme.collectAsStateWithLifecycle()
    val helperContext = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Sanctuary Customization",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = palette.primary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Vault Setting Logs",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = palette.onBackground,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Safe space warning
        Card(
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isDecoy) "Unlocked via Decoy Sanctuary" else "Unlocked via Master Sanctuary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = palette.primary
                    )
                    Text(
                        text = if (isDecoy) "Displaying simulated fallback diary. Real indices are highly hidden." else "All cryptographic vectors verified. Sanctuary active.",
                        fontSize = 12.sp,
                        color = palette.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Dark Theme Selector
        Text(
            text = "Sanctuary Appearance",
            fontWeight = FontWeight.Bold,
            color = palette.onBackground,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Deep Cozy Night Mode", fontWeight = FontWeight.Bold, color = palette.onSurface, fontSize = 14.sp)
                    Text("Switches between warm twilight and daylight tones", fontSize = 11.sp, color = palette.onSurface.copy(alpha = 0.5f))
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onToggleDarkMode() },
                    colors = SwitchDefaults.colors(checkedThumbColor = palette.primary)
                )
            }
        }

        // Themes selectors
        Text(
            text = "Select Color Theme Sanctuary Environment",
            fontWeight = FontWeight.Bold,
            color = palette.onBackground,
            fontSize = 15.sp
        )

        val themes = listOf(
            Triple("SAGE_CALM", "Sage Calm", "Restorative, soothing leaf-tea tones"),
            Triple("MIDNIGHT_VELVET", "Midnight Velvet", "Deep cosmological charcoal depths"),
            Triple("EMBER_GLOW", "Ember Glow", "Warm sunset glow and earthy clay"),
            Triple("AMBER_ROSE", "Amber Rose", "Lavender mist and cozy rose gold")
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            themes.forEach { t ->
                val active = activeThemeId == t.first
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (active) palette.primary.copy(alpha = 0.1f) else palette.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = if (active) 2.dp else 1.dp,
                        color = if (active) palette.primary else palette.onSurface.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectTheme(t.first) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = t.second,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = palette.onSurface
                            )
                            Text(
                                text = t.third,
                                fontSize = 11.sp,
                                color = palette.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        if (active) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active Theme",
                                tint = palette.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Lock Sanctuary Button
        Button(
            onClick = { viewModel.lockDiary() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Close diary")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lock Sanctuary Now", fontWeight = FontWeight.Bold)
            }
        }
    }
}
