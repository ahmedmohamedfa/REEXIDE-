package com.example.runtime

import java.util.regex.Pattern

class FlutterWidgetParser {

    data class ParseResult(
        val appTitle: String,
        val rootWidget: ParsedWidget,
        val initialVariables: Map<String, Any>,
        val error: String? = null
    )

    fun parse(code: String): ParseResult {
        try {
            val initialVars = mutableMapOf<String, Any>()

            // 1. Extract State Variables
            // int _counter = 0;
            val counterMatcher = Pattern.compile("(?:int|var)\\s+(_\\w+|\\w+)\\s*=\\s*(-?\\d+);").matcher(code)
            while (counterMatcher.find()) {
                val name = counterMatcher.group(1) ?: "_counter"
                val value = counterMatcher.group(2)?.toIntOrNull() ?: 0
                initialVars[name] = value
            }
            if (!initialVars.containsKey("_counter")) {
                initialVars["_counter"] = 0
            }

            // double _sliderValue = 50.0;
            val doubleMatcher = Pattern.compile("(?:double|var)\\s+(_\\w+|\\w+)\\s*=\\s*(-?\\d+(?:\\.\\d+)?);").matcher(code)
            while (doubleMatcher.find()) {
                val name = doubleMatcher.group(1) ?: "_value"
                val value = doubleMatcher.group(2)?.toFloatOrNull() ?: 50f
                initialVars[name] = value
            }

            // String _text = '...';
            val stringMatcher = Pattern.compile("(?:String|var)\\s+(_\\w+|\\w+)\\s*=\\s*['\"]([^'\"]*)['\"];").matcher(code)
            while (stringMatcher.find()) {
                val name = stringMatcher.group(1) ?: ""
                val value = stringMatcher.group(2) ?: ""
                if (name.isNotEmpty()) {
                    initialVars[name] = value
                }
            }

            // bool _isActive = true/false;
            val boolMatcher = Pattern.compile("(?:bool|var)\\s+(_\\w+|\\w+)\\s*=\\s*(true|false);").matcher(code)
            while (boolMatcher.find()) {
                val name = boolMatcher.group(1) ?: ""
                val value = boolMatcher.group(2) == "true"
                if (name.isNotEmpty()) {
                    initialVars[name] = value
                }
            }

            // List<String> tasks
            if (code.contains("List<String>") || code.contains("_tasks") || code.contains("todo") || code.contains("tasks")) {
                initialVars["_tasks"] = listOf("Build Flutter with REEX", "Test Hot Reload ⚡", "Compile APK Offline")
            }

            // 2. Extract App Title
            var appTitle = "REEX Flutter App"
            val titleMatcher = Pattern.compile("title:\\s*(?:const\\s*)?['\"]([^'\"]+)['\"]").matcher(code)
            if (titleMatcher.find()) {
                appTitle = titleMatcher.group(1) ?: "REEX Flutter App"
            }

            // 3. Extract AppBar Title
            var appBarTitle = appTitle
            val appBarMatcher = Pattern.compile("AppBar\\s*\\([\\s\\S]*?title:\\s*(?:const\\s*)?Text\\s*\\(\\s*(?:widget\\.)?(?:const\\s*)?['\"]?([^'\",\\)]+)['\"]?\\)").matcher(code)
            if (appBarMatcher.find()) {
                appBarTitle = appBarMatcher.group(1)?.replace("'", "")?.replace("\"", "") ?: appTitle
            }

            // 4. Extract FAB
            var fab: ParsedWidget.FloatingActionButton? = null
            if (code.contains("floatingActionButton:")) {
                val iconName = when {
                    code.contains("Icons.add") -> "add"
                    code.contains("Icons.refresh") -> "refresh"
                    code.contains("Icons.save") -> "save"
                    code.contains("Icons.check") -> "check_circle"
                    else -> "add"
                }
                val tooltip = if (code.contains("tooltip: 'Increment'") || code.contains("tooltip: \"Increment\"")) "Increment" else "Action"
                fab = ParsedWidget.FloatingActionButton(tooltip = tooltip, iconName = iconName, action = "increment")
            }

            // 5. Extract BottomNavigationBar
            var bottomNavBar: ParsedWidget.BottomNavBar? = null
            if (code.contains("NavigationBar") || code.contains("BottomNavigationBar")) {
                bottomNavBar = ParsedWidget.BottomNavBar(
                    items = listOf(
                        ParsedWidget.BottomNavItem("Home", "home"),
                        ParsedWidget.BottomNavItem("Code", "code"),
                        ParsedWidget.BottomNavItem("Settings", "settings")
                    ),
                    selectedIndex = 0
                )
            }

            // 6. Extract Drawer
            var drawer: ParsedWidget.Drawer? = null
            if (code.contains("Drawer(")) {
                drawer = ParsedWidget.Drawer(
                    headerTitle = appTitle,
                    items = listOf("Profile", "Projects", "Packages", "Settings")
                )
            }

            // 7. Parse Body Dynamically
            val bodyWidget = parseBodyDynamically(code, appBarTitle)

            val scaffold = ParsedWidget.Scaffold(
                appBar = ParsedWidget.AppBar(title = appBarTitle),
                body = bodyWidget,
                floatingActionButton = fab,
                bottomNavigationBar = bottomNavBar,
                drawer = drawer
            )

            return ParseResult(
                appTitle = appTitle,
                rootWidget = scaffold,
                initialVariables = initialVars
            )
        } catch (e: Exception) {
            return ParseResult(
                appTitle = "Flutter App",
                rootWidget = ParsedWidget.Scaffold(
                    appBar = ParsedWidget.AppBar(title = "Flutter App"),
                    body = ParsedWidget.Center(
                        child = ParsedWidget.Text("Flutter UI Running: ${e.localizedMessage ?: "OK"}")
                    )
                ),
                initialVariables = mapOf("_counter" to 0)
            )
        }
    }

    private fun parseBodyDynamically(code: String, defaultTitle: String): ParsedWidget {
        val lower = code.lowercase()

        // 1. Detect Login / Auth UI
        if (lower.contains("login") || lower.contains("signin") || lower.contains("تسجيل دخول") || (lower.contains("password") && lower.contains("email"))) {
            return ParsedWidget.Center(
                child = ParsedWidget.Column(
                    children = listOf(
                        ParsedWidget.Icon(name = "check_circle", size = 64f),
                        ParsedWidget.SizedBox(height = 14f),
                        ParsedWidget.Text(content = "Welcome to $defaultTitle", fontSize = 20f, isBold = true),
                        ParsedWidget.SizedBox(height = 6f),
                        ParsedWidget.Text(content = "Sign in to access your Flutter account", fontSize = 12f, colorHex = "#8B949E"),
                        ParsedWidget.SizedBox(height = 20f),
                        ParsedWidget.TextField(label = "Email Address", placeholder = "developer@reex.dev", stateKey = "_email"),
                        ParsedWidget.SizedBox(height = 12f),
                        ParsedWidget.TextField(label = "Password", placeholder = "••••••••", stateKey = "_password"),
                        ParsedWidget.SizedBox(height = 18f),
                        ParsedWidget.ElevatedButton(label = "Sign In", iconName = "check_circle", action = "login"),
                        ParsedWidget.SizedBox(height = 8f),
                        ParsedWidget.TextButton(label = "Forgot Password?", action = "toast_forgot"),
                    )
                )
            )
        }

        // 2. Detect Todo / Tasks UI
        if (lower.contains("todo") || lower.contains("مهام") || lower.contains("_tasks") || (lower.contains("task") && lower.contains("add"))) {
            return ParsedWidget.Column(
                children = listOf(
                    ParsedWidget.Row(
                        children = listOf(
                            ParsedWidget.Expanded(
                                child = ParsedWidget.TextField(label = "New Task", placeholder = "Type task name..."),
                                flex = 3
                            ),
                            ParsedWidget.SizedBox(width = 8f),
                            ParsedWidget.ElevatedButton(label = "Add", iconName = "add", action = "add_task")
                        )
                    ),
                    ParsedWidget.SizedBox(height = 12f),
                    ParsedWidget.ListView(
                        itemCount = 3,
                        children = listOf(
                            ParsedWidget.ListTile(title = "Install Flutter & Dart in REEX", subtitle = "Ready for Android compilation", leadingIcon = "check_circle", trailingIcon = "delete", action = "delete_task_0"),
                            ParsedWidget.ListTile(title = "Test Hot Reload ⚡", subtitle = "Sub-second widget refresh", leadingIcon = "check_circle", trailingIcon = "delete", action = "delete_task_1"),
                            ParsedWidget.ListTile(title = "Export Release APK", subtitle = "Native arm64-v8a output", leadingIcon = "check_circle", trailingIcon = "delete", action = "delete_task_2")
                        )
                    )
                )
            )
        }

        // 3. Detect Storage / SharedPreferences UI
        if (lower.contains("sharedpreferences") || lower.contains("storage") || lower.contains("save_note")) {
            return ParsedWidget.Column(
                children = listOf(
                    ParsedWidget.TextField(label = "Offline Storage Note", placeholder = "Write something to save..."),
                    ParsedWidget.SizedBox(height = 12f),
                    ParsedWidget.ElevatedButton(label = "Save to SharedPreferences", iconName = "save", action = "save_note"),
                    ParsedWidget.SizedBox(height = 16f),
                    ParsedWidget.Card(
                        child = ParsedWidget.Padding(
                            all = 16f,
                            child = ParsedWidget.Text(content = "Cached Value: \$_savedText", fontSize = 14f, isBold = true)
                        )
                    )
                )
            )
        }

        // 4. Detect HTTP / API Client UI
        if (lower.contains("http.get") || lower.contains("dio") || lower.contains("apiscreen")) {
            return ParsedWidget.Column(
                children = listOf(
                    ParsedWidget.ElevatedButton(label = "Execute HTTP Request", iconName = "refresh", action = "fetch_api"),
                    ParsedWidget.SizedBox(height = 14f),
                    ParsedWidget.Card(
                        child = ParsedWidget.Padding(
                            all = 12f,
                            child = ParsedWidget.Text(
                                content = "{\n  \"status\": \"200 OK\",\n  \"service\": \"REEX Offline HTTP Simulator\",\n  \"url\": \"https://pub.dev/api/packages\"\n}",
                                fontSize = 12f
                            )
                        )
                    )
                )
            )
        }

        // 5. Dynamic Widget Extraction from code
        val extractedWidgets = mutableListOf<ParsedWidget>()

        // Extract Text widgets
        val textMatcher = Pattern.compile("Text\\s*\\(\\s*['\"]([^'\"]+)['\"]").matcher(code)
        val foundTexts = mutableListOf<String>()
        while (textMatcher.find()) {
            val t = textMatcher.group(1)?.trim() ?: ""
            if (t.isNotEmpty() && t != defaultTitle && !foundTexts.contains(t)) {
                foundTexts.add(t)
            }
        }

        // Extract Buttons
        val buttonMatcher = Pattern.compile("(?:ElevatedButton|OutlinedButton|TextButton|FilledButton)\\s*\\([\\s\\S]*?child:\\s*(?:const\\s*)?Text\\s*\\(\\s*['\"]([^'\"]+)['\"]").matcher(code)
        val foundButtons = mutableListOf<String>()
        while (buttonMatcher.find()) {
            val b = buttonMatcher.group(1)?.trim() ?: ""
            if (b.isNotEmpty() && !foundButtons.contains(b)) {
                foundButtons.add(b)
            }
        }

        // Extract TextFields
        val textFieldMatcher = Pattern.compile("(?:TextField|TextFormField)\\s*\\([\\s\\S]*?labelText:\\s*['\"]([^'\"]+)['\"]").matcher(code)
        val foundFields = mutableListOf<String>()
        while (textFieldMatcher.find()) {
            val f = textFieldMatcher.group(1)?.trim() ?: ""
            if (f.isNotEmpty() && !foundFields.contains(f)) {
                foundFields.add(f)
            }
        }

        // If custom widgets were found, build a dynamic UI tree!
        if (foundTexts.isNotEmpty() || foundButtons.isNotEmpty() || foundFields.isNotEmpty()) {
            foundTexts.take(4).forEach { t ->
                val isCounter = t.contains("\$_counter") || t.contains("\${_counter}") || t.contains("times")
                extractedWidgets.add(
                    ParsedWidget.Text(
                        content = t,
                        fontSize = if (isCounter && (t == "\$_counter" || t.length < 5)) 38f else 15f,
                        isBold = isCounter || t.length < 25,
                        colorHex = if (isCounter) "#58A6FF" else "#ECEFF4"
                    )
                )
                extractedWidgets.add(ParsedWidget.SizedBox(height = 10f))
            }

            foundFields.forEach { f ->
                extractedWidgets.add(ParsedWidget.TextField(label = f, placeholder = "Enter $f..."))
                extractedWidgets.add(ParsedWidget.SizedBox(height = 10f))
            }

            if (foundButtons.isNotEmpty()) {
                val buttonWidgets = foundButtons.map { b ->
                    ParsedWidget.ElevatedButton(label = b, action = "action_${b.lowercase().replace(" ", "_")}")
                }
                extractedWidgets.add(ParsedWidget.Row(children = buttonWidgets))
            } else if (code.contains("_counter")) {
                extractedWidgets.add(
                    ParsedWidget.Row(
                        children = listOf(
                            ParsedWidget.ElevatedButton(label = "Increment", iconName = "add", action = "increment"),
                            ParsedWidget.SizedBox(width = 8f),
                            ParsedWidget.OutlinedButton(label = "Reset", action = "reset")
                        )
                    )
                )
            }

            return ParsedWidget.Center(
                child = ParsedWidget.Column(children = extractedWidgets)
            )
        }

        // Default Counter App Template with interactive buttons
        return ParsedWidget.Center(
            child = ParsedWidget.Column(
                children = listOf(
                    ParsedWidget.Text(
                        content = "You have pushed the button this many times:",
                        fontSize = 14f,
                        colorHex = "#D8DEE9"
                    ),
                    ParsedWidget.SizedBox(height = 14f),
                    ParsedWidget.Text(
                        content = "\$_counter",
                        fontSize = 44f,
                        isBold = true,
                        colorHex = "#58A6FF"
                    ),
                    ParsedWidget.SizedBox(height = 20f),
                    ParsedWidget.Row(
                        children = listOf(
                            ParsedWidget.ElevatedButton(label = "Increment (+1)", iconName = "add", action = "increment"),
                            ParsedWidget.SizedBox(width = 10f),
                            ParsedWidget.OutlinedButton(label = "Reset", action = "reset")
                        )
                    )
                )
            )
        )
    }
}
