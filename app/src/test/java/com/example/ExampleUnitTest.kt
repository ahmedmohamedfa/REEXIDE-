package com.example

import com.example.model.DiagnosticSeverity
import com.example.model.Project
import com.example.model.ProjectTemplate
import com.example.model.PubPackage
import com.example.runtime.FlutterRuntimeManager
import com.example.runtime.FlutterWidgetParser
import com.example.runtime.ParsedWidget
import com.example.runtime.RuntimeStatus
import com.example.service.AiProjectAgentService
import com.example.service.AppLanguage
import com.example.service.CodeFormatterService
import com.example.service.DartAnalyzerService
import com.example.service.LocalizationManager
import com.example.service.PackageManagerService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExampleUnitTest {

    @Test
    fun testDartAnalyzerDetectsMissingSemicolon() {
        val analyzer = DartAnalyzerService()
        val code = """
            void main() {
              int a = 10
              print(a);
            }
        """.trimIndent()

        val diagnostics = analyzer.analyze("main.dart", "main.dart", code)
        assertTrue(diagnostics.any { it.code == "missing_semicolon" && it.severity == DiagnosticSeverity.ERROR })
    }

    @Test
    fun testDartAnalyzerDetectsUnclosedBracket() {
        val analyzer = DartAnalyzerService()
        val code = """
            void main() {
              if (true) {
                print("test");
            }
        """.trimIndent()

        val diagnostics = analyzer.analyze("main.dart", "main.dart", code)
        assertTrue(diagnostics.any { it.code == "missing_matching_bracket" })
    }

    @Test
    fun testCodeFormatterIndentsProperly() {
        val formatter = CodeFormatterService()
        val code = """
            void main() {
            print("hello");
            }
        """.trimIndent()

        val formatted = formatter.format(code)
        assertTrue(formatted.contains("  print(\"hello\");"))
    }

    @Test
    fun testFlutterWidgetParserParsesScaffoldAndState() {
        val parser = FlutterWidgetParser()
        val code = """
            import 'package:flutter/material.dart';

            class _MyHomePageState extends State<MyHomePage> {
              int _counter = 0;

              @override
              Widget build(BuildContext context) {
                return Scaffold(
                  appBar: AppBar(title: const Text('My Flutter App')),
                  body: Center(
                    child: Text('Counter: ${'$'}_counter'),
                  ),
                );
              }
            }
        """.trimIndent()

        val result = parser.parse(code)
        assertNotNull(result.rootWidget)
        assertTrue(result.rootWidget is ParsedWidget.Scaffold)
        val scaffold = result.rootWidget as ParsedWidget.Scaffold
        assertEquals("My Flutter App", scaffold.appBar?.title)
        assertEquals(0, result.initialVariables["_counter"])
    }

    @Test
    fun testLocalizationManager() {
        LocalizationManager.currentLanguage = AppLanguage.ARABIC
        assertEquals("مرحبا", LocalizationManager.str("مرحبا", "Hello"))

        LocalizationManager.currentLanguage = AppLanguage.ENGLISH
        assertEquals("Hello", LocalizationManager.str("مرحبا", "Hello"))
    }

    @Test
    fun testAiProjectAgentServiceGeneratesCodeForLogin() = runBlocking {
        val aiService = AiProjectAgentService()
        val project = Project(id = "test_proj", name = "test_proj", path = "/tmp/test_proj", template = ProjectTemplate.COUNTER)
        val file = File("/tmp/test_proj/lib/main.dart")

        aiService.sendMessage("Create a login screen", project, file, "// empty", "No errors")
        val msgs = aiService.messages.value
        assertTrue(msgs.size >= 2)
        val aiMsg = msgs.last()
        assertNotNull(aiMsg.suggestedCode)
        assertTrue(aiMsg.suggestedCode!!.contains("LoginScreen"))
    }

    @Test
    fun testCustomApiKeySetAndClear() {
        val aiService = AiProjectAgentService()
        aiService.customApiKey = "AIzaSyTestKey12345"
        assertEquals("AIzaSyTestKey12345", aiService.getEffectiveApiKey())
        assertTrue(aiService.hasValidApiKey())

        aiService.customApiKey = ""
        assertEquals("", aiService.customApiKey)
    }

    @Test
    fun testPackageManagerAddsAndRemovesFromPubspec() {
        val packageService = PackageManagerService()
        val tempDir = File.createTempFile("reex_test_pkg", "").apply {
            delete()
            mkdirs()
        }
        val pubspec = File(tempDir, "pubspec.yaml")
        pubspec.writeText(
            """
            name: test_app
            dependencies:
              flutter:
                sdk: flutter
            """.trimIndent()
        )

        val pkg = PubPackage(
            name = "provider",
            version = "^6.1.2",
            description = "State management",
            category = "State",
            sha256 = "abc123sha"
        )
        val added = packageService.addPackageToProject(tempDir, pkg)
        assertTrue(added)
        assertTrue(pubspec.readText().contains("provider: ^6.1.2"))

        val catalog = packageService.getCatalog(tempDir)
        val providerInCatalog = catalog.find { it.name == "provider" }
        assertNotNull(providerInCatalog)
        assertTrue(providerInCatalog!!.isInstalledInCurrentProject)

        val removed = packageService.removePackageFromProject(tempDir, "provider")
        assertTrue(removed)
        assertFalse(pubspec.readText().contains("provider: ^6.1.2"))

        tempDir.deleteRecursively()
    }

    @Test
    fun testFlutterRuntimeStartsAndHotReloads() = runBlocking {
        val runtime = FlutterRuntimeManager()
        val code1 = """
            import 'package:flutter/material.dart';
            class _HomeState extends State<Home> {
              int _counter = 5;
              Widget build(BuildContext c) => Scaffold(appBar: AppBar(title: Text('V1')));
            }
        """.trimIndent()

        runtime.start(code1)
        assertEquals(RuntimeStatus.RUNNING, runtime.state.value.status)
        assertEquals(5, runtime.state.value.stateVariables["_counter"])

        val code2 = """
            import 'package:flutter/material.dart';
            class _HomeState extends State<Home> {
              int _counter = 5;
              Widget build(BuildContext c) => Scaffold(appBar: AppBar(title: Text('V2 Hot Reloaded')));
            }
        """.trimIndent()

        runtime.hotReload(code2)
        assertEquals(RuntimeStatus.RUNNING, runtime.state.value.status)
        val scaffold = runtime.state.value.rootWidget as? ParsedWidget.Scaffold
        assertEquals("V2 Hot Reloaded", scaffold?.appBar?.title)
        // Ensure state was preserved
        assertEquals(5, runtime.state.value.stateVariables["_counter"])

        runtime.stop()
        assertEquals(RuntimeStatus.STOPPED, runtime.state.value.status)
    }

    @Test
    fun testDartSyntaxHighlighterHighlightsKeywordsAndSearch() {
        val highlighter = com.example.ui.components.DartSyntaxHighlighter(searchQuery = "Scaffold")
        val source = androidx.compose.ui.text.AnnotatedString("class HomeScreen extends StatelessWidget { Widget build() => Scaffold(); }")
        val transformed = highlighter.filter(source)
        assertNotNull(transformed.text)
        assertEquals(source.text, transformed.text.text)
        assertTrue(transformed.text.spanStyles.isNotEmpty())
    }

    @Test
    fun testDynamicLoginScreenParsing() {
        val parser = FlutterWidgetParser()
        val loginCode = """
            import 'package:flutter/material.dart';
            class LoginScreen extends StatelessWidget {
              Widget build(BuildContext context) {
                return Scaffold(
                  appBar: AppBar(title: Text('Sign In')),
                  body: Column(
                    children: [
                      TextField(decoration: InputDecoration(labelText: 'Email')),
                      TextField(decoration: InputDecoration(labelText: 'Password')),
                      ElevatedButton(child: Text('Sign In'), onPressed: () {}),
                    ],
                  ),
                );
              }
            }
        """.trimIndent()

        val result = parser.parse(loginCode)
        assertNotNull(result.rootWidget)
        val scaffold = result.rootWidget as ParsedWidget.Scaffold
        assertEquals("Sign In", scaffold.appBar?.title)
        assertNotNull(scaffold.body)
    }

    @Test
    fun testDownloadCustomPackageFromPub() {
        val packageService = PackageManagerService()
        val tempDir = File.createTempFile("reex_pkg_dl", "").apply {
            delete()
            mkdirs()
        }
        val pubspec = File(tempDir, "pubspec.yaml")
        pubspec.writeText(
            """
            name: test_app
            dependencies:
              flutter:
                sdk: flutter
            """.trimIndent()
        )

        val (success, msg) = packageService.downloadAndInstallCustomPackage(tempDir, "camera", "^0.11.0")
        assertTrue(success)
        assertTrue(pubspec.readText().contains("camera: ^0.11.0"))
        tempDir.deleteRecursively()
    }
}
