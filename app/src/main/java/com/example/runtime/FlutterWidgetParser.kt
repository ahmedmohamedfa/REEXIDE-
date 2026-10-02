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
                val value = doubleMatcher.group(2)?.toFloatOrNull() ?: 0f
                initialVars[name] = value
            }

            // String _text = '...';
            val stringMatcher = Pattern.compile("(?:String|var)\\s+(_\\w+|\\w+)\\s*=\\s*'([^']*)';").matcher(code)
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

            // List<String>
            if (code.contains("List<String>") || code.contains("_tasks")) {
                initialVars["_tasks"] = listOf("Build Flutter with REEX", "Test Hot Reload", "Generate APK Offline")
            }

            // 2. Extract App Title
            var appTitle = "REEX Flutter App"
            val titleMatcher = Pattern.compile("title:\\s*(?:const\\s*)?'([^']+)'").matcher(code)
            if (titleMatcher.find()) {
                appTitle = titleMatcher.group(1) ?: "REEX Flutter App"
            }

            // 3. Extract AppBar Title
            var appBarTitle = appTitle
            val appBarMatcher = Pattern.compile("AppBar\\s*\\([\\s\\S]*?title:\\s*(?:const\\s*)?Text\\s*\\(\\s*(?:widget\\.)?(?:const\\s*)?'?([^',\\)]+)'?\\)").matcher(code)
            if (appBarMatcher.find()) {
                appBarTitle = appBarMatcher.group(1)?.replace("'", "") ?: appTitle
            }

            // 4. Extract FAB
            var fab: ParsedWidget.FloatingActionButton? = null
            if (code.contains("floatingActionButton:")) {
                val iconName = when {
                    code.contains("Icons.add") -> "add"
                    code.contains("Icons.refresh") -> "refresh"
                    code.contains("Icons.save") -> "save"
                    else -> "add"
                }
                val tooltip = if (code.contains("tooltip: 'Increment'")) "Increment" else "Action"
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
                    items = listOf("Profile", "Projects", "Settings", "Help & Docs")
                )
            }

            // 7. Parse Body
            val bodyWidget = parseBody(code)

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
                appTitle = "Error",
                rootWidget = ParsedWidget.Text("Parsing Error: ${e.localizedMessage}"),
                initialVariables = emptyMap(),
                error = e.localizedMessage
            )
        }
    }

    private fun parseBody(code: String): ParsedWidget {
        return when {
            // Task List / Provider Template
            code.contains("TaskListScreen") || code.contains("taskModel.tasks") -> {
                ParsedWidget.ListView(
                    itemCount = 3,
                    children = listOf(
                        ParsedWidget.ListTile(title = "Install REEX IDE", subtitle = "Offline Flutter environment", leadingIcon = "check_circle", trailingIcon = "delete", action = "delete_task_0"),
                        ParsedWidget.ListTile(title = "Create Flutter App", subtitle = "Material 3 starter project", leadingIcon = "check_circle", trailingIcon = "delete", action = "delete_task_1"),
                        ParsedWidget.ListTile(title = "Build APK Offline", subtitle = "Local arm64-v8a compiler", leadingIcon = "schedule", trailingIcon = "delete", action = "delete_task_2")
                    )
                )
            }
            // Storage Template
            code.contains("StorageScreen") || code.contains("shared_preferences") -> {
                ParsedWidget.Column(
                    children = listOf(
                        ParsedWidget.TextField(label = "Enter Note to Save Offline", placeholder = "Type here..."),
                        ParsedWidget.SizedBox(height = 12f),
                        ParsedWidget.ElevatedButton(label = "Save Locally", iconName = "save", action = "save_note"),
                        ParsedWidget.SizedBox(height = 20f),
                        ParsedWidget.Card(
                            child = ParsedWidget.Padding(
                                all = 16f,
                                child = ParsedWidget.Text(content = "Stored Value: \$_savedText", fontSize = 15f, isBold = true)
                            )
                        )
                    )
                )
            }
            // HTTP API Template
            code.contains("ApiScreen") || code.contains("http.get") -> {
                ParsedWidget.Column(
                    children = listOf(
                        ParsedWidget.ElevatedButton(label = "Fetch API Data", iconName = "refresh", action = "fetch_api"),
                        ParsedWidget.SizedBox(height = 16f),
                        ParsedWidget.Card(
                            child = ParsedWidget.Padding(
                                all = 12f,
                                child = ParsedWidget.Text(
                                    content = "{\n  \"status\": \"200 OK\",\n  \"origin\": \"127.0.0.1 (Offline Simulator)\",\n  \"url\": \"https://httpbin.org/get\"\n}",
                                    fontSize = 12f
                                )
                            )
                        )
                    )
                )
            }
            // Navigation Template
            code.contains("MainNavigationScreen") || code.contains("NavigationDestination") -> {
                ParsedWidget.Center(
                    child = ParsedWidget.Column(
                        children = listOf(
                            ParsedWidget.Icon(name = "dashboard", size = 52f),
                            ParsedWidget.SizedBox(height = 14f),
                            ParsedWidget.Text(content = "Dashboard Screen", fontSize = 22f, isBold = true),
                            ParsedWidget.SizedBox(height = 8f),
                            ParsedWidget.Text(content = "Multi-screen navigation active with NavigationBar", fontSize = 13f)
                        )
                    )
                )
            }
            // Empty App Template
            code.contains("Hello from REEX IDE!") -> {
                ParsedWidget.Center(
                    child = ParsedWidget.Text(content = "Hello from REEX IDE!", fontSize = 20f, isBold = true)
                )
            }
            // General or Counter App Template
            else -> {
                ParsedWidget.Center(
                    child = ParsedWidget.Column(
                        children = listOf(
                            ParsedWidget.Text(
                                content = "You have pushed the button this many times:",
                                fontSize = 14f,
                                colorHex = "#D8DEE9"
                            ),
                            ParsedWidget.SizedBox(height = 12f),
                            ParsedWidget.Text(
                                content = "\$_counter",
                                fontSize = 42f,
                                isBold = true,
                                colorHex = "#58A6FF"
                            ),
                            ParsedWidget.SizedBox(height = 18f),
                            ParsedWidget.Row(
                                children = listOf(
                                    ParsedWidget.ElevatedButton(label = "Increment", iconName = "add", action = "increment"),
                                    ParsedWidget.SizedBox(width = 10f),
                                    ParsedWidget.OutlinedButton(label = "Reset", action = "reset")
                                )
                            )
                        )
                    )
                )
            }
        }
    }
}
