package org.koorm.ocpd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koorm.ocpd.core.OCPDAssistantManager
import org.koorm.ocpd.services.initializeAndroidFileOperations

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize platform-specific file operations
        initializeAndroidFileOperations(applicationContext)

        setContent {
            OCPDAssistantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OCPDAssistantApp()
                }
            }
        }
    }
}

@Composable
fun OCPDAssistantApp() {
    var isLoading by remember { mutableStateOf(true) }
    var assistant by remember { mutableStateOf<OCPDAssistantManager?>(null) }

    LaunchedEffect(Unit) {
        assistant = OCPDAssistantManager()
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading OCPD Assistant...")
        } else {
            Text(
                text = "🧠 OCPD Assistant",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your Compassionate Productivity Companion",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Demo features section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "✅ Features Available:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Smart Task Breakdown")
                    Text("• Time-Boxed Scheduling")
                    Text("• 'Good Enough' Mode")
                    Text("• Anti-Procrastination Tools")
                    Text("• Behavioral Insights")
                    Text("• Natural Language Processing")
                    Text("• Cross-Platform Sync")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // TODO: Navigate to main app interface
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Get Started")
            }
        }
    }
}

@Composable
fun OCPDAssistantTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF6750A4),
            secondary = androidx.compose.ui.graphics.Color(0xFF625B71)
        ),
        content = content
    )
}
