package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesRepository
import com.example.engine.SamplePdfGenerator
import com.example.ui.MainScaffold
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.theme.PDFOmniTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "pdfomni.db"
        ).fallbackToDestructiveMigration().build()

        preferencesRepository = PreferencesRepository(applicationContext)

        setContent {
            val isDarkMode by preferencesRepository.isDarkMode.collectAsState(initial = true)
            val hasCompletedOnboarding by preferencesRepository.hasCompletedOnboarding.collectAsState(initial = false)
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                scope.launch(Dispatchers.IO) {
                    SamplePdfGenerator.cleanupDefaultSamples(applicationContext, database.documentDao())
                }
            }

            PDFOmniTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(targetState = hasCompletedOnboarding, label = "onboarding_crossfade") { completed ->
                        if (!completed) {
                            OnboardingScreen(
                                onFinish = {
                                    scope.launch {
                                        preferencesRepository.setOnboardingCompleted(true)
                                    }
                                }
                            )
                        } else {
                            MainScaffold(
                                database = database,
                                preferencesRepository = preferencesRepository
                            )
                        }
                    }
                }
            }
        }
    }
}
