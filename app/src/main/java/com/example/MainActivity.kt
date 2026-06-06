package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.SadaRepository
import com.example.ui.theme.SadaTheme
import com.example.ui.SadaMainView
import com.example.ui.SadaViewModel
import com.example.ui.SadaViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {

    private val viewModel: SadaViewModel by viewModels {
        val app = application as SadaApplication
        SadaViewModelFactory(SadaRepository(app.database.sadaDao()))
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SadaTheme {
                val micPermissionState = rememberPermissionState(
                    permission = Manifest.permission.RECORD_AUDIO
                )

                LaunchedEffect(Unit) {
                    if (!micPermissionState.status.isGranted) {
                        micPermissionState.launchPermissionRequest()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (micPermissionState.status.isGranted) {
                        SadaMainView(viewModel = viewModel)
                    } else {
                        // Minimalist fallback design if permission is denied
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(32.dp)) {
                            Text(
                                "تطبيق صدى يحتاج لصلاحية الميكروفون ليعمل كأداة صوتية ذكية. يرجى تفعيل الصلاحية من الإعدادات.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}
