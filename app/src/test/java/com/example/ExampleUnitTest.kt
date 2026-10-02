package com.example

import com.example.model.DiagnosticSeverity
import com.example.model.Project
import com.example.model.ProjectTemplate
import com.example.runtime.FlutterWidgetParser
import com.example.runtime.ParsedWidget
import com.example.service.AiProjectAgentService
import com.example.service.AppLanguage
import com.example.service.CodeFormatterService
import com.example.service.DartAnalyzerService
import com.example.service.LocalizationManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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
        val project = Project("test_proj", "/tmp/test_proj", ProjectTemplate.COUNTER, System.currentTimeMillis(), "3.24.3")
        val file = File("/tmp/test_proj/lib/main.dart")

        aiService.sendMessage("Create a login screen", project, file, "// empty", "No errors")
        val msgs = aiService.messages.value
        assertTrue(msgs.size >= 2)
        val aiMsg = msgs.last()
        assertNotNull(aiMsg.suggestedCode)
        assertTrue(aiMsg.suggestedCode!!.contains("LoginScreen"))
    }
}
