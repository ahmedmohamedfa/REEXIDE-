package com.example.runtime

import kotlinx.coroutines.flow.StateFlow

enum class RuntimeStatus {
    STOPPED,
    COMPILING,
    RUNNING,
    HOT_RELOADING,
    ERROR
}

sealed class ParsedWidget {
    data class Scaffold(
        val appBar: AppBar? = null,
        val body: ParsedWidget? = null,
        val floatingActionButton: FloatingActionButton? = null,
        val bottomNavigationBar: BottomNavBar? = null,
        val drawer: Drawer? = null,
        val backgroundColorHex: String? = null
    ) : ParsedWidget()

    data class AppBar(
        val title: String,
        val backgroundColorHex: String? = null,
        val centerTitle: Boolean = true,
        val actions: List<String> = emptyList()
    ) : ParsedWidget()

    data class Drawer(
        val headerTitle: String = "Flutter Drawer",
        val items: List<String> = emptyList()
    ) : ParsedWidget()

    data class FloatingActionButton(
        val tooltip: String = "Action",
        val iconName: String = "add",
        val action: String = "increment",
        val label: String? = null
    ) : ParsedWidget()

    data class BottomNavBar(
        val items: List<BottomNavItem> = emptyList(),
        val selectedIndex: Int = 0
    ) : ParsedWidget()

    data class BottomNavItem(
        val label: String,
        val iconName: String
    )

    data class Column(
        val children: List<ParsedWidget>,
        val mainAxisAlignment: String = "start",
        val crossAxisAlignment: String = "center"
    ) : ParsedWidget()

    data class Row(
        val children: List<ParsedWidget>,
        val mainAxisAlignment: String = "start",
        val crossAxisAlignment: String = "center"
    ) : ParsedWidget()

    data class Stack(
        val children: List<ParsedWidget>
    ) : ParsedWidget()

    data class Wrap(
        val children: List<ParsedWidget>,
        val spacing: Float = 8f
    ) : ParsedWidget()

    data class Center(
        val child: ParsedWidget
    ) : ParsedWidget()

    data class Align(
        val alignment: String = "center",
        val child: ParsedWidget
    ) : ParsedWidget()

    data class Padding(
        val all: Float = 16f,
        val horizontal: Float = 0f,
        val vertical: Float = 0f,
        val child: ParsedWidget
    ) : ParsedWidget()

    data class SizedBox(
        val width: Float = 0f,
        val height: Float = 0f,
        val child: ParsedWidget? = null
    ) : ParsedWidget()

    data class Spacer(
        val flex: Int = 1
    ) : ParsedWidget()

    data class Expanded(
        val child: ParsedWidget,
        val flex: Int = 1
    ) : ParsedWidget()

    data class Text(
        val content: String,
        val fontSize: Float = 14f,
        val isBold: Boolean = false,
        val colorHex: String? = null,
        val textAlign: String = "start",
        val maxLines: Int? = null
    ) : ParsedWidget()

    data class ElevatedButton(
        val label: String,
        val iconName: String? = null,
        val action: String = "action"
    ) : ParsedWidget()

    data class TextButton(
        val label: String,
        val action: String = "action"
    ) : ParsedWidget()

    data class OutlinedButton(
        val label: String,
        val action: String = "action"
    ) : ParsedWidget()

    data class IconButton(
        val iconName: String = "add",
        val action: String = "action",
        val colorHex: String? = null
    ) : ParsedWidget()

    data class Card(
        val child: ParsedWidget,
        val elevation: Float = 2f,
        val colorHex: String? = null
    ) : ParsedWidget()

    data class Container(
        val width: Float? = null,
        val height: Float? = null,
        val colorHex: String? = null,
        val borderRadius: Float = 0f,
        val child: ParsedWidget? = null
    ) : ParsedWidget()

    data class ListTile(
        val title: String,
        val subtitle: String? = null,
        val leadingIcon: String? = null,
        val trailingIcon: String? = null,
        val action: String? = null
    ) : ParsedWidget()

    data class Divider(
        val thickness: Float = 1f,
        val colorHex: String? = null
    ) : ParsedWidget()

    data class ListView(
        val itemCount: Int = 5,
        val itemLabels: List<String> = emptyList(),
        val children: List<ParsedWidget> = emptyList()
    ) : ParsedWidget()

    data class GridView(
        val crossAxisCount: Int = 2,
        val children: List<ParsedWidget> = emptyList()
    ) : ParsedWidget()

    data class SingleChildScrollView(
        val child: ParsedWidget
    ) : ParsedWidget()

    data class TextField(
        val label: String = "Enter text",
        val placeholder: String = "",
        val stateKey: String = "_text"
    ) : ParsedWidget()

    data class Switch(
        val stateKey: String = "_switchState",
        val value: Boolean = false
    ) : ParsedWidget()

    data class Checkbox(
        val stateKey: String = "_checkboxState",
        val value: Boolean = false,
        val label: String = ""
    ) : ParsedWidget()

    data class Slider(
        val stateKey: String = "_sliderValue",
        val min: Float = 0f,
        val max: Float = 100f,
        val value: Float = 50f
    ) : ParsedWidget()

    data class CircularProgressIndicator(
        val colorHex: String? = null
    ) : ParsedWidget()

    data class LinearProgressIndicator(
        val progress: Float? = null,
        val colorHex: String? = null
    ) : ParsedWidget()

    data class CircleAvatar(
        val radius: Float = 20f,
        val text: String = "R",
        val backgroundColorHex: String? = null
    ) : ParsedWidget()

    data class Icon(
        val name: String = "star",
        val colorHex: String? = null,
        val size: Float = 24f
    ) : ParsedWidget()

    data class Unknown(
        val rawName: String
    ) : ParsedWidget()
}

data class RuntimeState(
    val status: RuntimeStatus = RuntimeStatus.STOPPED,
    val rootWidget: ParsedWidget? = null,
    val stateVariables: Map<String, Any> = emptyMap(),
    val appTitle: String = "Flutter App",
    val errorMessage: String? = null,
    val errorStackTrace: String? = null,
    val fps: Int = 60,
    val reloadDurationMs: Long = 0,
    val isHotReloading: Boolean = false
)

interface IFlutterRuntime {
    val state: StateFlow<RuntimeState>
    val logs: StateFlow<List<String>>

    suspend fun start(code: String)
    suspend fun stop()
    suspend fun hotReload(newCode: String)
    suspend fun hotRestart()
    fun triggerAction(actionName: String)
    fun updateStateVariable(key: String, value: Any)
    fun sendLog(log: String)
}
