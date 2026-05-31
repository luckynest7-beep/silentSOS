package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.entity.Contact
import com.example.data.entity.EmergencyEvent
import com.example.ui.viewmodels.MainViewModel
import com.example.utils.TimeFormatter
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onBackToCalc: () -> Unit
) {
    val context = LocalContext.current
    val contacts by viewModel.contactsList.collectAsState()
    val allEvents by viewModel.allEventsList.collectAsState()

    val sosCode by viewModel.sosTriggerCode.collectAsState()
    val setCode by viewModel.settingsCode.collectAsState()
    val stopPin by viewModel.safeStopPin.collectAsState()
    val isSafeMode by viewModel.isSafeModeEnabled.collectAsState()
    val isSelfie by viewModel.isSelfieEnabled.collectAsState()
    val isAudio by viewModel.isAudioEnabled.collectAsState()

    // Dialog trigger states
    var showAddContactDialog by remember { mutableStateOf(false) }
    var selectedEventForDetail by remember { mutableStateOf<EmergencyEvent?>(null) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    // Permissions check state reactively updated
    var fineLocationGranted by remember { mutableStateOf(false) }
    var cameraGranted by remember { mutableStateOf(false) }
    var audioGranted by remember { mutableStateOf(false) }
    var smsGranted by remember { mutableStateOf(false) }

    fun checkAllPermissions() {
        fineLocationGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        audioGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        smsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }

    // Checking permissions initially
    LaunchedEffect(Unit) {
        checkAllPermissions()
    }

    // Permission Request Launchers
    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        checkAllPermissions()
    }

    val permissionsListToRequest = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("SilentSOS Config Dashboard", fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToCalc) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back to Calculator")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Description banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "App is fully disguised",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Default view looks like a normal calculator. Type your secrete configurations / SOS passcode to trigger safety services.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Permissions Check Card
            item {
                Text("System Permissions Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PermissionRow("SMS Dispatch (SOS messaging)", smsGranted)
                        PermissionRow("Location Services (Maps Tracking link)", fineLocationGranted)
                        PermissionRow("Front Camera (Secret Selfie Snaps)", cameraGranted)
                        PermissionRow("Microphone (30-second silent Audio)", audioGranted)

                        Spacer(modifier = Modifier.height(16.dp))

                        val allGranted = smsGranted && fineLocationGranted && cameraGranted && audioGranted
                        Button(
                            onClick = { requestPermissionsLauncher.launch(permissionsListToRequest) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (allGranted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (allGranted) "All Permissions Granted" else "Grant Safety Permissions")
                        }
                    }
                }
            }

            // Code Configuration Table
            item {
                Text("Passcode Configuration (Safety Guard)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ConfigTextField("Calculator SOS Trigger Code (Enforced with #)", "Type this number + '#' + press '=' to trigger silent safety mode.", sosCode) {
                            viewModel.persistSetting("SOS_TRIGGER_CODE", it)
                        }
                        ConfigTextField("Settings Lock entrance (Enforced with #)", "Type this number + '#' + press '=' to enter configuration.", setCode) {
                            viewModel.persistSetting("SETTINGS_CODE", it)
                        }
                        ConfigTextField("Safe Stop PIN (I Am Safe) (Enforced with #)", "Type this number + '#' + press '=' to stop SOS safely.", stopPin) {
                            viewModel.persistSetting("SAFE_STOP_PIN", it)
                        }
                    }
                }
            }

            // General toggle features
            item {
                Text("Safety Triggers Customization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ToggleRow("Record front selfie on alert", "Take invisible selfie on activation", isSelfie) {
                            viewModel.persistSetting("IS_SELFIE_ENABLED", it.toString())
                        }
                        Divider()
                        ToggleRow("Record 30s Audio on alert", "Performs hidden ambient recording clip", isAudio) {
                            viewModel.persistSetting("IS_AUDIO_ENABLED", it.toString())
                        }
                        Divider()
                        ToggleRow("Enable \"I Am Safe\" messages", "Allows PIN entry to declare safe status", isSafeMode) {
                            viewModel.persistSetting("IS_SAFE_MODE_ENABLED", it.toString())
                        }
                    }
                }
            }

            // Emergency Contacts Setup
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Emergency Contacts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { showAddContactDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact")
                    }
                }
            }

            if (contacts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No contacts configured. Click + to add emergency responders.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            } else {
                items(contacts) { contact ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarIcon(contact.name)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(contact.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text(contact.phoneNumber, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                            }
                            IconButton(onClick = { viewModel.deleteContact(contact) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete contact", tint = Color.Red)
                            }
                        }
                    }
                }
            }

            // Historic Evidence Logs Container
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Evidence Archive / Incident Reports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (allEvents.isNotEmpty()) {
                        TextButton(onClick = { showClearConfirmDialog = true }) {
                            Text("Clear All", color = Color.Red)
                        }
                    }
                }
            }

            if (allEvents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No incidents logged. Calculator alert remains stable.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            } else {
                items(allEvents) { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedEventForDetail = event },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.formattedTime, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Battery state: ${event.batteryPercentage}% | GPS status: ${if (event.latitude != 0.0) "Ready" else "No GPS"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "View Details")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Add Contact Dialog
    if (showAddContactDialog) {
        var cName by remember { mutableStateOf("") }
        var cPhone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("Add Contact") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cName,
                        onValueChange = { cName = it },
                        label = { Text("Contact Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cPhone,
                        onValueChange = { cPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cName.isNotBlank() && cPhone.isNotBlank()) {
                            viewModel.addContact(cName, cPhone)
                            showAddContactDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Incident / Evidence Details Dialog viewer
    if (selectedEventForDetail != null) {
        val event = selectedEventForDetail!!
        AlertDialog(
            onDismissRequest = { selectedEventForDetail = null },
            title = {
                Text("Incident Report Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Text("Accurate Location: ${TimeFormatter.format(event.timestamp)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Battery context is recorded at ${event.batteryPercentage}% capacity.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    item {
                        Text("Stored SOS Distress Message Text:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(8.dp)
                        ) {
                            Text(event.distressMessage, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (event.latitude != 0.0) {
                        item {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.locationUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Map, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Google Maps Link")
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                val uris = ArrayList<Uri>()
                                try {
                                    event.selfieFilePath?.let { path ->
                                        val file = File(path)
                                        if (file.exists()) {
                                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                                context,
                                                "com.aistudio.silentsos.vkmqpz.fileprovider",
                                                file
                                            )
                                            uris.add(uri)
                                        }
                                    }
                                    event.backCameraFilePath?.let { path ->
                                        val file = File(path)
                                        if (file.exists()) {
                                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                                context,
                                                "com.aistudio.silentsos.vkmqpz.fileprovider",
                                                file
                                            )
                                            uris.add(uri)
                                        }
                                    }
                                    event.audioFilePath?.let { path ->
                                        val file = File(path)
                                        if (file.exists()) {
                                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                                context,
                                                "com.aistudio.silentsos.vkmqpz.fileprovider",
                                                file
                                            )
                                            uris.add(uri)
                                        }
                                    }
                                    
                                    val shareIntent = Intent().apply {
                                        if (uris.isNotEmpty()) {
                                            action = Intent.ACTION_SEND_MULTIPLE
                                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                                            type = "*/*"
                                        } else {
                                            action = Intent.ACTION_SEND
                                        }
                                        putExtra(Intent.EXTRA_SUBJECT, "SilentSOS Emergency Report")
                                        putExtra(Intent.EXTRA_TEXT, "Dispatched SOS Distress Alert details:\n${event.distressMessage}")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Safety Evidence (MMS/Chat)..."))
                                } catch (e: Exception) {
                                    android.util.Log.e("SettingsScreen", "Sharing error", e)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share / Send Evidence (MMS/Chat)")
                        }
                    }

                    event.selfieFilePath?.let { filePath ->
                        item {
                            Text("Captured Selfie Evidence:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            val selfieFile = File(filePath)
                            if (selfieFile.exists()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = selfieFile,
                                        contentDescription = "Selfie image capture evidence",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else {
                                Text("Selfie file was deleted or is missing.", style = MaterialTheme.typography.bodySmall, color = Color.Red)
                            }
                        }
                    }

                    event.backCameraFilePath?.let { filePath ->
                        item {
                            Text("Captured Env Photo (Back Camera):", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            val backFile = File(filePath)
                            if (backFile.exists()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = backFile,
                                        contentDescription = "Back camera environment photo evidence",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else {
                                Text("Environment photo was deleted or is missing.", style = MaterialTheme.typography.bodySmall, color = Color.Red)
                            }
                        }
                    }

                    event.audioFilePath?.let { filePath ->
                        item {
                            Text("Ambient Audio Recording Evidence:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            AudioPlayerCard(filePath)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedEventForDetail = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Clear Evidence confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Delete Evidence Reports?") },
            text = { Text("Are you absolutely sure you want to delete all stored incidents and media recordings? This action is irreversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearEvidenceLogs()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Confirm Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PermissionRow(name: String, granted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (granted) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                if (granted) "Allowed" else "Missing",
                style = MaterialTheme.typography.bodySmall,
                color = if (granted) Color(0xFF10B981) else Color(0xFFEF4444)
            )
        }
    }
}

@Composable
fun ConfigTextField(title: String, subtitle: String, value: String, onValueUpdate: (String) -> Unit) {
    // Strip trailing '#' for a clean and smooth editing experience
    val displayValue = value.removeSuffix("#")
    var textValue by remember(displayValue) { mutableStateOf(TextFieldValue(displayValue)) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = textValue,
            onValueChange = {
                // Keep only letters/digits to prevent accidental multiple hashes or invalid symbols
                val filteredText = it.text.filter { char -> char.isLetterOrDigit() }
                textValue = it.copy(text = filteredText)
                onValueUpdate(filteredText + "#")
            },
            trailingIcon = {
                Text(
                    text = "#",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(end = 12.dp)
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun AvatarIcon(name: String) {
    val initial = name.firstOrNull()?.toString()?.uppercase() ?: "?"
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(initial, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun AudioPlayerCard(filePath: String) {
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPaused by remember { mutableStateOf(false) }

    DisposableEffect(filePath) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    try {
                        val file = File(filePath)
                        if (!file.exists()) {
                            return@IconButton
                        }
                        if (isPlaying) {
                            if (isPaused) {
                                mediaPlayer?.start()
                                isPaused = false
                            } else {
                                mediaPlayer?.pause()
                                isPaused = true
                            }
                        } else {
                            mediaPlayer?.release()
                            mediaPlayer = MediaPlayer().apply {
                                setDataSource(filePath)
                                prepare()
                                start()
                                setOnCompletionListener {
                                    isPlaying = false
                                    isPaused = false
                                }
                            }
                            isPlaying = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            ) {
                Icon(
                    imageVector = if (isPlaying && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
            }
            Text(
                text = if (isPlaying && !isPaused) "Playing Ambient..." else if (isPlaying && isPaused) "Ambient Paused" else "Listen to Audio Recording",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
