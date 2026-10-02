package com.example.runtime

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FlutterRuntimeManager : IFlutterRuntime {

    private val parser = FlutterWidgetParser()

    private val _state = MutableStateFlow(RuntimeState())
    override val state: StateFlow<RuntimeState> = _state.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    override val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private var currentCode: String = ""

    override suspend fun start(code: String) = withContext(Dispatchers.Default) {
        currentCode = code
        _state.value = _state.value.copy(status = RuntimeStatus.COMPILING)
        log("Launching lib/main.dart on Android Emulator (arm64-v8a)...")
        delay(400)

        log("Syncing files to device Android arm64...")
        delay(300)

        val parseResult = parser.parse(code)
        if (parseResult.error != null) {
            _state.value = _state.value.copy(
                status = RuntimeStatus.ERROR,
                errorMessage = parseResult.error,
                errorStackTrace = "FlutterError (Widget building failed):\n  ${parseResult.error}\n  at ComponentElement.performRebuild(framework.dart:5508)"
            )
            log("❌ Compilation failed: ${parseResult.error}")
            return@withContext
        }

        _state.value = RuntimeState(
            status = RuntimeStatus.RUNNING,
            rootWidget = parseResult.rootWidget,
            stateVariables = parseResult.initialVariables,
            appTitle = parseResult.appTitle,
            fps = 60
        )
        log("Flutter engine connected. Observatory listening on http://127.0.0.1:8181/")
        log("⚡ Hot Reload is active. Press 'r' in terminal or tap Hot Reload ⚡.")
    }

    override suspend fun stop() = withContext(Dispatchers.Default) {
        _state.value = RuntimeState(status = RuntimeStatus.STOPPED)
        log("Application finished and Flutter engine detached.")
    }

    override suspend fun hotReload(newCode: String) = withContext(Dispatchers.Default) {
        if (_state.value.status != RuntimeStatus.RUNNING) return@withContext
        val startTime = System.currentTimeMillis()
        currentCode = newCode
        _state.value = _state.value.copy(isHotReloading = true)

        delay(120)
        val parseResult = parser.parse(newCode)
        val elapsed = System.currentTimeMillis() - startTime

        if (parseResult.error != null) {
            _state.value = _state.value.copy(
                isHotReloading = false,
                status = RuntimeStatus.ERROR,
                errorMessage = parseResult.error,
                errorStackTrace = "Hot Reload rejected due to syntax error:\n${parseResult.error}"
            )
            log("❌ Hot reload error: ${parseResult.error}")
            return@withContext
        }

        // Preserve current user state variables while updating widget tree
        val currentVars = _state.value.stateVariables.toMutableMap()
        for ((k, v) in parseResult.initialVariables) {
            if (!currentVars.containsKey(k)) {
                currentVars[k] = v
            }
        }

        _state.value = _state.value.copy(
            isHotReloading = false,
            rootWidget = parseResult.rootWidget,
            stateVariables = currentVars,
            appTitle = parseResult.appTitle,
            reloadDurationMs = elapsed,
            errorMessage = null,
            status = RuntimeStatus.RUNNING
        )
        log("⚡ Reloaded 1 of 540 libraries in ${elapsed}ms (Hot Reload).")
    }

    override suspend fun hotRestart() = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        _state.value = _state.value.copy(isHotReloading = true)
        delay(250)

        val parseResult = parser.parse(currentCode)
        val elapsed = System.currentTimeMillis() - startTime

        _state.value = RuntimeState(
            status = RuntimeStatus.RUNNING,
            rootWidget = parseResult.rootWidget,
            stateVariables = parseResult.initialVariables,
            appTitle = parseResult.appTitle,
            reloadDurationMs = elapsed,
            isHotReloading = false
        )
        log("🔄 Restarted application in ${elapsed}ms (Hot Restart).")
    }

    override fun triggerAction(actionName: String) {
        val currentVars = _state.value.stateVariables.toMutableMap()
        when (actionName) {
            "increment" -> {
                val current = (currentVars["_counter"] as? Int) ?: 0
                val next = current + 1
                currentVars["_counter"] = next
                _state.value = _state.value.copy(stateVariables = currentVars)
                log("I/flutter (1024): setState(() { _counter = $next; });")
            }
            "reset" -> {
                currentVars["_counter"] = 0
                _state.value = _state.value.copy(stateVariables = currentVars)
                log("I/flutter (1024): setState(() { _counter = 0; });")
            }
            "save_note" -> {
                val currentNotes = (currentVars["_savedText"] as? String) ?: ""
                log("I/flutter (1024): SharedPreferences.setString('saved_note', note);")
            }
            "fetch_api" -> {
                log("I/flutter (1024): HTTP GET https://httpbin.org/get -> 200 OK")
            }
        }
    }

    override fun updateStateVariable(key: String, value: Any) {
        val currentVars = _state.value.stateVariables.toMutableMap()
        currentVars[key] = value
        _state.value = _state.value.copy(stateVariables = currentVars)
    }

    override fun sendLog(log: String) {
        log(log)
    }

    private fun log(message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _logs.value = (_logs.value + "[$time] $message").takeLast(100)
    }
}
