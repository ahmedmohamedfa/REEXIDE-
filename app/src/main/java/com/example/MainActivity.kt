package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import com.example.model.Project
import com.example.model.ProjectTemplate
import com.example.service.BuildManagerService
import com.example.service.CodeFormatterService
import com.example.service.DartAnalyzerService
import com.example.service.GitService
import com.example.service.LocalizationManager
import com.example.service.PackageManagerService
import com.example.service.ProjectManager
import com.example.service.SdkDoctorService
import com.example.service.TerminalService
import com.example.ui.components.SplashScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IdeScreen
import com.example.ui.theme.MyApplicationTheme

enum class ScreenState {
    SPLASH,
    HOME,
    IDE
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val projectManager = ProjectManager(this)
        val analyzerService = DartAnalyzerService()
        val formatterService = CodeFormatterService()
        val terminalService = TerminalService()
        val packageService = PackageManagerService()
        val buildService = BuildManagerService(this)
        val doctorService = SdkDoctorService(this)
        val gitService = GitService()

        // Seed initial project if none exist so the user can immediately edit and code
        if (projectManager.getProjects().isEmpty()) {
            projectManager.createProject("reex_starter", ProjectTemplate.COUNTER)
        }

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LocalizationManager.currentLanguage.layoutDirection) {
                MyApplicationTheme {
                    var currentScreen by remember { mutableStateOf(ScreenState.SPLASH) }
                    var activeProject by remember { mutableStateOf<Project?>(null) }

                    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                        when (screen) {
                            ScreenState.SPLASH -> {
                                SplashScreen(
                                    onFinished = {
                                        currentScreen = ScreenState.HOME
                                    }
                                )
                            }

                            ScreenState.HOME -> {
                                HomeScreen(
                                    projectManager = projectManager,
                                    doctorService = doctorService,
                                    onOpenProject = { project ->
                                        activeProject = project
                                        currentScreen = ScreenState.IDE
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            ScreenState.IDE -> {
                                activeProject?.let { proj ->
                                    IdeScreen(
                                        project = proj,
                                        projectManager = projectManager,
                                        analyzerService = analyzerService,
                                        formatterService = formatterService,
                                        terminalService = terminalService,
                                        packageService = packageService,
                                        buildService = buildService,
                                        doctorService = doctorService,
                                        gitService = gitService,
                                        onBackToHome = {
                                            currentScreen = ScreenState.HOME
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
