@file:OptIn(InternalSerializationApi::class)
package net.embizzolator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.embizzolator.ui.theme.EmbizzolatorTheme

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*
import net.embizzolator.MainViewModel

private const val TAG = "EmbizzolatorApp"

// Helper function to convert slider value to text
fun getSliderLabel(value: Float): String {
    return when {
        value < 0.2f -> "Low"
        value < 0.4f -> "Medium-Low"
        value < 0.6f -> "Medium"
        value < 0.8f -> "Medium-High"
        else -> "High"
    }
}

@Serializable
data class ApiRequest(val model: String, val messages: List<Message>)
@Serializable
data class Message(val role: String, val content: String)
@Serializable
data class ApiResponse(val choices: List<Choice>)
@Serializable
data class Choice(val message: Message)

object LlmApiService {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getEmbizzolatedText(userInput: String, settings: AppSettings, prefs: AppPreferences): String {
        return withContext(Dispatchers.IO) {
            try {
                val persona = if (prefs.corporateStyle.isNotBlank()) {
                    "Adopt the persona of a ${prefs.corporateStyle}."
                } else {
                    ""
                }
                val prompt = """
                    Translate the following user input into an equivalent phrase that uses heavy corporate and business 
                    jargon.
                    $persona
                    The response should have a jargon density level of "${getSliderLabel(prefs.jargonDensity)}".
                    The tone should reflect an urgency level of "${getSliderLabel(prefs.urgencyMeter)}".
                    The length of the response should reflect a verbosity level of "${getSliderLabel(prefs.verbosity)}".

                    User input: "$userInput"
                """.trimIndent()

                val requestPayload = ApiRequest(
                    model = settings.modelName,
                    messages = listOf(Message(role = "user", content = prompt))
                )
                val requestBody = Json.encodeToString(ApiRequest.serializer(), requestPayload)
                    .toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(settings.apiUrl)
                    .header("Authorization", "Bearer ${settings.apiKey}")
                    .header("Content-Type", "application/json")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext "Error: ${response.code} - ${response.message}"
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        json.decodeFromString(ApiResponse.serializer(), responseBody)
                            .choices.firstOrNull()?.message?.content ?: "No response content."
                    } else {
                        "Error: Empty response body."
                    }
                }
            } catch (e: Exception) { "Error: ${e.message}" }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val uiState by mainViewModel.uiState.collectAsState()

            EmbizzolatorTheme(brandGuideline = uiState.preferences.brandingTheme) {
                EmbizzolatorApp(mainViewModel)
            }
        }
    }
}

@Composable
fun EmbizzolatorApp(mainViewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController, mainViewModel) }
        composable("dashboard") { TPSDashboardScreen(navController, mainViewModel) }
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, modifier = Modifier.height(72.dp)) {
        NavigationBarItem(
            selected = currentRoute == "main",
            onClick = { navController.navigate("main") },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(24.dp))
                    // FIX 2: Changed label for the "Home" button
                    Text("KPI Dashboard", fontSize = 12.sp)
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "dashboard",
            onClick = { navController.navigate("dashboard") },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(24.dp))
                    // FIX 3: Changed label for the "TPS Dashboard" button
                    Text("TPS Configuration", fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: MainViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    val defaultTranslatedText = "Your synergistic output will appear here..."
    var translatedText by remember { mutableStateOf(defaultTranslatedText) }
    val context = LocalContext.current

    val textToSpeech = remember {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) { tts?.language = Locale.US }
        }
        tts
    }

    DisposableEffect(Unit) {
        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            inputText = results?.get(0) ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Embizzolator", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = { AppBottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(visible = !uiState.areSettingsConfigured) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFB00020))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Configuration Required", fontWeight = FontWeight.Bold, color = Color(0xFFB00020))
                            Text("Please set API credentials in the dashboard.", fontSize = 14.sp, color = Color.DarkGray)
                        }
                        Button(onClick = { navController.navigate("dashboard") }) {
                            Text("Dashboard")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 68.dp),
                label = { Text("Core Competency Input") },
                placeholder = { Text("Leverage your core competencies here...") },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (inputText.isNotEmpty()) {
                            IconButton(onClick = { inputText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Input")
                            }
                        }
                        IconButton(onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Begin Dictation...")
                            }
                            speechRecognizerLauncher.launch(intent)
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Dictation Sesh")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Button(
                onClick = {
                    if (inputText.isNotBlank() && uiState.settings != null) {
                        coroutineScope.launch {
                            isLoading = true
                            translatedText = "Synergizing..."
                            val result = LlmApiService.getEmbizzolatedText(inputText, uiState.settings!!, uiState.preferences)
                            translatedText = result
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && uiState.areSettingsConfigured,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(Icons.Default.FlashOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Synergize!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = translatedText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    label = { Text("Synergistic Output") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (translatedText.isNotEmpty() && translatedText != defaultTranslatedText) {
                        IconButton(onClick = { translatedText = defaultTranslatedText }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Output")
                        }
                    }
                    IconButton(onClick = {
                        if (translatedText.isNotBlank()) {
                            textToSpeech?.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, "")
                        }
                    }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play Output")
                    }
                    IconButton(onClick = { textToSpeech?.stop() }) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop Playback")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TPSDashboardScreen(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var apiUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    var isPasswordSet by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(true) }
    var showLockDialog by remember { mutableStateOf(false) }
    var showUnlockDialog by remember { mutableStateOf(false) }

    val defaultJargonDensity = 0.5f
    val defaultUrgencyMeter = 0.5f
    val defaultVerbosity = 0.5f
    val defaultCorporateStyle = "Business Executive"
    val defaultBrandingGuidelines = "General Business"

    var jargonDensity by remember { mutableFloatStateOf(defaultJargonDensity) }
    var urgencyMeter by remember { mutableFloatStateOf(defaultUrgencyMeter) }
    var verbosity by remember { mutableFloatStateOf(defaultVerbosity) }
    var corporateStyle by remember { mutableStateOf(defaultCorporateStyle) }
    var corporateStyleExpanded by remember { mutableStateOf(false) }
    var brandingGuidelines by remember { mutableStateOf(defaultBrandingGuidelines) }
    var brandingGuidelinesExpanded by remember { mutableStateOf(false) }

    val corporateStyleOptions = listOf("", "Business Executive", "Engineering Manager", "Agile Product Owner", "Marketing Executive")
    val brandingOptions = listOf("General Business", "Executive Mahogany", "Cube Farm Chic", "Marketing")

    LaunchedEffect(Unit) {
        isPasswordSet = SecureStorageManager.isPasswordSet(context)
        isLocked = isPasswordSet
        val settings = SecureStorageManager.getSettings(context)
        val prefs = SettingsManager.getPreferences(context)
        if (settings != null) {
            apiUrl = settings.apiUrl
            apiKey = settings.apiKey
            modelName = settings.modelName
        }
        jargonDensity = prefs.jargonDensity
        urgencyMeter = prefs.urgencyMeter
        verbosity = prefs.verbosity
        corporateStyle = prefs.corporateStyle
        brandingGuidelines = prefs.brandingTheme
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TPS Configuration", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = { AppBottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showLockDialog = true },
                    enabled = !isPasswordSet,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Lock")
                }
                Button(
                    onClick = { showUnlockDialog = true },
                    enabled = isPasswordSet,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Unlock")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("API Endpoint URL", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = apiUrl,
                    onValueChange = { apiUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = isLocked,
                    // FIX 1: Gray out the text field when locked
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                    ),
                    enabled = !isLocked
                )

                Text("API Key", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = isLocked,
                    visualTransformation = if (showApiKey || !isLocked) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(if (showApiKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, "Toggle visibility")
                        }
                    },
                    // FIX 1: Gray out the text field when locked
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                    ),
                    enabled = !isLocked
                )

                Text("Model Name", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = isLocked,
                    placeholder = { Text("e.g., llama3-8b-8192")},
                    // FIX 1: Gray out the text field when locked
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                    ),
                    enabled = !isLocked
                )
            }

            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Jargon Density", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Text(getSliderLabel(jargonDensity), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                }
                Slider(value = jargonDensity, onValueChange = { jargonDensity = it }, steps = 4)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Urgency Meter", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Text(getSliderLabel(urgencyMeter), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                }
                Slider(value = urgencyMeter, onValueChange = { urgencyMeter = it }, steps = 4)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Verbosity", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Text(getSliderLabel(verbosity), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                }
                Slider(value = verbosity, onValueChange = { verbosity = it }, steps = 4)
            }

            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Corporate Style", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = corporateStyleExpanded,
                        onExpandedChange = { corporateStyleExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = corporateStyle.ifEmpty { "None" }, onValueChange = {}, readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = corporateStyleExpanded) }
                        )
                        ExposedDropdownMenu(expanded = corporateStyleExpanded, onDismissRequest = { corporateStyleExpanded = false }) {
                            corporateStyleOptions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption.ifEmpty { "None" }) },
                                    onClick = { corporateStyle = selectionOption; corporateStyleExpanded = false }
                                )
                            }
                        }
                    }
                }
                Column {
                    Text("Branding Guidelines", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = brandingGuidelinesExpanded,
                        onExpandedChange = { brandingGuidelinesExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = brandingGuidelines, onValueChange = {}, readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandingGuidelinesExpanded) }
                        )
                        ExposedDropdownMenu(expanded = brandingGuidelinesExpanded, onDismissRequest = { brandingGuidelinesExpanded = false }) {
                            brandingOptions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = { brandingGuidelines = selectionOption; brandingGuidelinesExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        jargonDensity = defaultJargonDensity
                        urgencyMeter = defaultUrgencyMeter
                        verbosity = defaultVerbosity
                        corporateStyle = defaultCorporateStyle
                        brandingGuidelines = defaultBrandingGuidelines
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val newSettings = AppSettings(apiUrl, apiKey, modelName)
                            SecureStorageManager.saveSettings(context, newSettings)

                            val newPrefs = AppPreferences(jargonDensity, urgencyMeter, verbosity, corporateStyle, brandingGuidelines)
                            SettingsManager.savePreferences(context, newPrefs)

                            viewModel.checkSettings()
                            navController.navigateUp()
                        }
                    }, modifier = Modifier.weight(1f)
                ) { Text("Save") }
            }
        }
    }

    if (showLockDialog) {
        LockDialog(
            onDismiss = { showLockDialog = false },
            onConfirm = { password ->
                SecureStorageManager.savePassword(context, password)
                isPasswordSet = true
                isLocked = true
                showLockDialog = false
            }
        )
    }

    if (showUnlockDialog) {
        UnlockDialog(
            onDismiss = { showUnlockDialog = false },
            onConfirm = { password ->
                if (password == SecureStorageManager.getPassword(context)) {
                    isLocked = false
                    showUnlockDialog = false
                    true
                } else {
                    false
                }
            }
        )
    }
}

@Composable
fun LockDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set API Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter a password to lock your API credentials.")
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; error = null },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = error != null,
                    supportingText = { if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error) }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (password.isNotBlank() && password == confirmPassword) {
                    onConfirm(password)
                } else {
                    error = "Passwords do not match."
                }
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun UnlockDialog(onDismiss: () -> Unit, onConfirm: (String) -> Boolean) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unlock API Credentials") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter your password to edit API credentials.")
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = error != null,
                    supportingText = { if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error) }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (!onConfirm(password)) {
                    error = "Incorrect password."
                }
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}