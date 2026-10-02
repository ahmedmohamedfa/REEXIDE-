package com.example.service

import android.content.Context
import com.example.model.Project
import com.example.model.ProjectFile
import com.example.model.ProjectTemplate
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ProjectManager(private val context: Context) {

    private val projectsRoot: File
        get() {
            val dir = File(context.filesDir, "flutter_projects")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun getProjects(): List<Project> {
        val root = projectsRoot
        val projectDirs = root.listFiles { file -> file.isDirectory } ?: return emptyList()

        return projectDirs.map { dir ->
            val pubspec = File(dir, "pubspec.yaml")
            val template = detectTemplate(pubspec)
            Project(
                id = dir.name,
                name = dir.name,
                path = dir.absolutePath,
                template = template,
                lastModified = dir.lastModified(),
                isOfflineReady = true
            )
        }.sortedByDescending { it.lastModified }
    }

    private fun detectTemplate(pubspec: File): ProjectTemplate {
        if (!pubspec.exists()) return ProjectTemplate.COUNTER
        val content = pubspec.readText()
        return when {
            content.contains("provider:") -> ProjectTemplate.STATE_MANAGEMENT
            content.contains("http:") -> ProjectTemplate.HTTP_CLIENT
            content.contains("shared_preferences:") -> ProjectTemplate.LOCAL_STORAGE
            content.contains("Empty App") -> ProjectTemplate.EMPTY
            content.contains("Tabs & Navigation") -> ProjectTemplate.NAVIGATION
            else -> ProjectTemplate.COUNTER
        }
    }

    fun createProject(name: String, template: ProjectTemplate): Project {
        val sanitizedName = name.trim().replace(Regex("[^a-zA-Z0-9_]"), "_").lowercase()
        val projectDir = File(projectsRoot, sanitizedName)
        if (projectDir.exists()) {
            throw IllegalArgumentException("A project with name '$sanitizedName' already exists.")
        }
        projectDir.mkdirs()

        // Create standard Flutter layout
        val libDir = File(projectDir, "lib").apply { mkdirs() }
        File(projectDir, "test").apply { mkdirs() }
        File(projectDir, "assets").apply { mkdirs() }
        File(projectDir, "android/app/src/main").apply { mkdirs() }

        // Create pubspec.yaml
        val pubspecContent = generatePubspec(sanitizedName, template)
        File(projectDir, "pubspec.yaml").writeText(pubspecContent)

        // Create analysis_options.yaml
        File(projectDir, "analysis_options.yaml").writeText(
            """
            include: package:flutter_lints/flutter.yaml

            linter:
              rules:
                prefer_const_constructors: true
                prefer_const_literals_to_create_immutables: true
                avoid_print: false
            """.trimIndent()
        )

        // Create main.dart
        val mainDartContent = generateMainDart(sanitizedName, template)
        File(libDir, "main.dart").writeText(mainDartContent)

        // Create widget_test.dart
        File(projectDir, "test/widget_test.dart").writeText(
            """
            import 'package:flutter/material.dart';
            import 'package:flutter_test/flutter_test.dart';
            import 'package:$sanitizedName/main.dart';

            void main() {
              testWidgets('App smoke test', (WidgetTester tester) async {
                await tester.pumpWidget(const MyApp());
                expect(find.byType(MaterialApp), findsOneWidget);
              });
            }
            """.trimIndent()
        )

        // Create README.md
        File(projectDir, "README.md").writeText(
            """
            # $sanitizedName

            A Flutter application created with **REEX IDE**.

            ## Architecture
            - Target: arm64-v8a / Flutter 3.24.3
            - Template: ${template.displayName}
            - Mode: Offline-capable
            """.trimIndent()
        )

        // Android Manifest stub
        File(projectDir, "android/app/src/main/AndroidManifest.xml").writeText(
            """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.reex.$sanitizedName">
                <application
                    android:label="$sanitizedName"
                    android:name="io.flutter.app.FlutterApplication"
                    android:icon="@mipmap/ic_launcher">
                    <activity
                        android:name=".MainActivity"
                        android:exported="true"
                        android:launchMode="singleTop"
                        android:theme="@android:style/Theme.Black.NoTitleBar">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN"/>
                            <category android:name="android.intent.category.LAUNCHER"/>
                        </intent-filter>
                    </activity>
                </application>
            </manifest>
            """.trimIndent()
        )

        return Project(
            id = sanitizedName,
            name = sanitizedName,
            path = projectDir.absolutePath,
            template = template,
            lastModified = System.currentTimeMillis()
        )
    }

    fun deleteProject(projectName: String): Boolean {
        val dir = File(projectsRoot, projectName)
        return if (dir.exists()) dir.deleteRecursively() else false
    }

    fun getFileTree(directory: File, rootPath: String): ProjectFile {
        val children = directory.listFiles()?.sortedWith(
            compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() }
        )?.map { file ->
            if (file.isDirectory) {
                getFileTree(file, rootPath)
            } else {
                ProjectFile(
                    name = file.name,
                    path = file.absolutePath,
                    relativePath = file.absolutePath.removePrefix(rootPath).removePrefix(File.separator),
                    isDirectory = false,
                    size = file.length(),
                    lastModified = file.lastModified()
                )
            }
        } ?: emptyList()

        return ProjectFile(
            name = directory.name,
            path = directory.absolutePath,
            relativePath = directory.absolutePath.removePrefix(rootPath).removePrefix(File.separator),
            isDirectory = true,
            children = children,
            lastModified = directory.lastModified(),
            isExpanded = true
        )
    }

    fun exportProjectZip(project: Project): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val zipFile = File(exportDir, "${project.name}.zip")
        if (zipFile.exists()) zipFile.delete()

        val sourceDir = File(project.path)
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            sourceDir.walkTopDown().forEach { file ->
                val relPath = file.relativeTo(sourceDir).path
                if (relPath.isNotEmpty() && !relPath.startsWith(".git") && !relPath.startsWith("build")) {
                    if (file.isDirectory) {
                        zos.putNextEntry(ZipEntry("$relPath/"))
                        zos.closeEntry()
                    } else {
                        zos.putNextEntry(ZipEntry(relPath))
                        FileInputStream(file).use { fis -> fis.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
        }
        return zipFile
    }

    private fun generatePubspec(name: String, template: ProjectTemplate): String {
        val extraDeps = when (template) {
            ProjectTemplate.STATE_MANAGEMENT -> "  provider: ^6.1.2\n"
            ProjectTemplate.LOCAL_STORAGE -> "  shared_preferences: ^2.3.2\n"
            ProjectTemplate.HTTP_CLIENT -> "  http: ^1.2.2\n"
            else -> ""
        }

        return """
            name: $name
            description: "A Flutter application built with REEX IDE (${template.displayName})"
            publish_to: "none"
            version: 1.0.0+1

            environment:
              sdk: ">=3.0.0 <4.0.0"

            dependencies:
              flutter:
                sdk: flutter
              cupertino_icons: ^1.0.8
            $extraDeps
            dev_dependencies:
              flutter_test:
                sdk: flutter
              flutter_lints: ^4.0.0

            flutter:
              uses-material-design: true
        """.trimIndent()
    }

    private fun generateMainDart(name: String, template: ProjectTemplate): String {
        return when (template) {
            ProjectTemplate.COUNTER -> """
                import 'package:flutter/material.dart';

                void main() {
                  runApp(const MyApp());
                }

                class MyApp extends StatelessWidget {
                  const MyApp({super.key});

                  @override
                  Widget build(BuildContext context) {
                    return MaterialApp(
                      title: '$name',
                      debugShowCheckedModeBanner: false,
                      theme: ThemeData(
                        colorScheme: ColorScheme.fromSeed(
                          seedColor: Colors.deepPurple,
                          brightness: Brightness.dark,
                        ),
                        useMaterial3: true,
                      ),
                      home: const MyHomePage(title: '$name Home'),
                    );
                  }
                }

                class MyHomePage extends StatefulWidget {
                  const MyHomePage({super.key, required this.title});
                  final String title;

                  @override
                  State<MyHomePage> createState() => _MyHomePageState();
                }

                class _MyHomePageState extends State<MyHomePage> {
                  int _counter = 0;

                  void _incrementCounter() {
                    setState(() {
                      _counter++;
                    });
                  }

                  @override
                  Widget build(BuildContext context) {
                    return Scaffold(
                      appBar: AppBar(
                        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
                        title: Text(widget.title),
                      ),
                      body: Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: <Widget>[
                            const Text(
                              'You have pushed the button this many times:',
                              style: TextStyle(fontSize: 16),
                            ),
                            Text(
                              '${'$'}_counter',
                              style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                                fontWeight: FontWeight.bold,
                                color: Colors.cyanAccent,
                              ),
                            ),
                          ],
                        ),
                      ),
                      floatingActionButton: FloatingActionButton(
                        onPressed: _incrementCounter,
                        tooltip: 'Increment',
                        child: const Icon(Icons.add),
                      ),
                    );
                  }
                }
            """.trimIndent()

            ProjectTemplate.EMPTY -> """
                import 'package:flutter/material.dart';

                void main() {
                  runApp(const MyApp());
                }

                class MyApp extends StatelessWidget {
                  const MyApp({super.key});

                  @override
                  Widget build(BuildContext context) {
                    return MaterialApp(
                      debugShowCheckedModeBanner: false,
                      home: Scaffold(
                        appBar: AppBar(
                          title: const Text('$name'),
                        ),
                        body: const Center(
                          child: Text(
                            'Hello from REEX IDE!',
                            style: TextStyle(fontSize: 20),
                          ),
                        ),
                      ),
                    );
                  }
                }
            """.trimIndent()

            ProjectTemplate.NAVIGATION -> """
                import 'package:flutter/material.dart';

                void main() => runApp(const MyApp());

                class MyApp extends StatelessWidget {
                  const MyApp({super.key});

                  @override
                  Widget build(BuildContext context) {
                    return MaterialApp(
                      debugShowCheckedModeBanner: false,
                      theme: ThemeData.dark(useMaterial3: true),
                      home: const MainNavigationScreen(),
                    );
                  }
                }

                class MainNavigationScreen extends StatefulWidget {
                  const MainNavigationScreen({super.key});

                  @override
                  State<MainNavigationScreen> createState() => _MainNavigationScreenState();
                }

                class _MainNavigationScreenState extends State<MainNavigationScreen> {
                  int _selectedIndex = 0;

                  static const List<Widget> _pages = <Widget>[
                    Center(child: Text('Dashboard', style: TextStyle(fontSize: 24))),
                    Center(child: Text('Explore Code', style: TextStyle(fontSize: 24))),
                    Center(child: Text('Settings', style: TextStyle(fontSize: 24))),
                  ];

                  @override
                  Widget build(BuildContext context) {
                    return Scaffold(
                      appBar: AppBar(title: const Text('$name')),
                      body: _pages[_selectedIndex],
                      bottomNavigationBar: NavigationBar(
                        selectedIndex: _selectedIndex,
                        onDestinationSelected: (int index) {
                          setState(() => _selectedIndex = index);
                        },
                        destinations: const [
                          NavigationDestination(icon: Icon(Icons.home), label: 'Home'),
                          NavigationDestination(icon: Icon(Icons.code), label: 'Code'),
                          NavigationDestination(icon: Icon(Icons.settings), label: 'Settings'),
                        ],
                      ),
                    );
                  }
                }
            """.trimIndent()

            ProjectTemplate.LOCAL_STORAGE -> """
                import 'package:flutter/material.dart';
                import 'package:shared_preferences/shared_preferences.dart';

                void main() => runApp(const MyApp());

                class MyApp extends StatelessWidget {
                  const MyApp({super.key});

                  @override
                  Widget build(BuildContext context) {
                    return MaterialApp(
                      debugShowCheckedModeBanner: false,
                      theme: ThemeData.dark(useMaterial3: true),
                      home: const StorageScreen(),
                    );
                  }
                }

                class StorageScreen extends StatefulWidget {
                  const StorageScreen({super.key});

                  @override
                  State<StorageScreen> createState() => _StorageScreenState();
                }

                class _StorageScreenState extends State<StorageScreen> {
                  final TextEditingController _controller = TextEditingController();
                  String _savedText = 'No data saved yet';

                  @override
                  void initState() {
                    super.initState();
                    _loadData();
                  }

                  Future<void> _loadData() async {
                    final prefs = await SharedPreferences.getInstance();
                    setState(() {
                      _savedText = prefs.getString('saved_note') ?? 'No data saved yet';
                    });
                  }

                  Future<void> _saveData() async {
                    final prefs = await SharedPreferences.getInstance();
                    await prefs.setString('saved_note', _controller.text);
                    _controller.clear();
                    _loadData();
                  }

                  @override
                  Widget build(BuildContext context) {
                    return Scaffold(
                      appBar: AppBar(title: const Text('Offline Local Storage')),
                      body: Padding(
                        padding: const EdgeInsets.all(16.0),
                        child: Column(
                          children: [
                            TextField(
                              controller: _controller,
                              decoration: const InputDecoration(
                                labelText: 'Enter Note to Save Offline',
                                border: OutlineInputBorder(),
                              ),
                            ),
                            const SizedBox(height: 12),
                            ElevatedButton.icon(
                              onPressed: _saveData,
                              icon: const Icon(Icons.save),
                              label: const Text('Save Locally'),
                            ),
                            const SizedBox(height: 24),
                            Card(
                              child: Padding(
                                padding: const EdgeInsets.all(16.0),
                                child: Text('Stored Value: ${'$'}_savedText'),
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  }
                }
            """.trimIndent()

            ProjectTemplate.HTTP_CLIENT -> """
                import 'dart:convert';
                import 'package:flutter/material.dart';
                import 'package:http/http.dart' as http;

                void main() => runApp(const MyApp());

                class MyApp extends StatelessWidget {
                  const MyApp({super.key});

                  @override
                  Widget build(BuildContext context) {
                    return MaterialApp(
                      debugShowCheckedModeBanner: false,
                      theme: ThemeData.dark(useMaterial3: true),
                      home: const ApiScreen(),
                    );
                  }
                }

                class ApiScreen extends StatefulWidget {
                  const ApiScreen({super.key});

                  @override
                  State<ApiScreen> createState() => _ApiScreenState();
                }

                class _ApiScreenState extends State<ApiScreen> {
                  bool _loading = false;
                  String _response = 'Click fetch to request public API';

                  Future<void> _fetchData() async {
                    setState(() => _loading = true);
                    try {
                      final res = await http.get(Uri.parse('https://httpbin.org/get'));
                      if (res.statusCode == 200) {
                        setState(() => _response = res.body);
                      } else {
                        setState(() => _response = 'Error: ${'$'}{res.statusCode}');
                      }
                    } catch (e) {
                      setState(() => _response = 'Request Failed: ${'$'}e');
                    } finally {
                      setState(() => _loading = false);
                    }
                  }

                  @override
                  Widget build(BuildContext context) {
                    return Scaffold(
                      appBar: AppBar(title: const Text('HTTP REST Client')),
                      body: Padding(
                        padding: const EdgeInsets.all(16.0),
                        child: Column(
                          children: [
                            ElevatedButton.icon(
                              onPressed: _loading ? null : _fetchData,
                              icon: const Icon(Icons.refresh),
                              label: Text(_loading ? 'Requesting...' : 'Fetch API Data'),
                            ),
                            const SizedBox(height: 16),
                            Expanded(
                              child: SingleChildScrollView(
                                child: Text(_response, style: const TextStyle(fontFamily: 'monospace')),
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  }
                }
            """.trimIndent()

            ProjectTemplate.STATE_MANAGEMENT -> """
                import 'package:flutter/material.dart';
                import 'package:provider/provider.dart';

                void main() {
                  runApp(
                    ChangeNotifierProvider(
                      create: (context) => TaskModel(),
                      child: const MyApp(),
                    ),
                  );
                }

                class TaskModel extends ChangeNotifier {
                  final List<String> _tasks = ['Install REEX IDE', 'Create Flutter App', 'Build APK Offline'];
                  List<String> get tasks => List.unmodifiable(_tasks);

                  void addTask(String task) {
                    _tasks.add(task);
                    notifyListeners();
                  }

                  void removeTask(int index) {
                    _tasks.removeAt(index);
                    notifyListeners();
                  }
                }

                class MyApp extends StatelessWidget {
                  const MyApp({super.key});

                  @override
                  Widget build(BuildContext context) {
                    return MaterialApp(
                      debugShowCheckedModeBanner: false,
                      theme: ThemeData.dark(useMaterial3: true),
                      home: const TaskListScreen(),
                    );
                  }
                }

                class TaskListScreen extends StatelessWidget {
                  const TaskListScreen({super.key});

                  @override
                  Widget build(BuildContext context) {
                    final taskModel = Provider.of<TaskModel>(context);
                    final controller = TextEditingController();

                    return Scaffold(
                      appBar: AppBar(title: const Text('Provider State Management')),
                      body: ListView.builder(
                        itemCount: taskModel.tasks.length,
                        itemBuilder: (context, index) {
                          return ListTile(
                            title: Text(taskModel.tasks[index]),
                            trailing: IconButton(
                              icon: const Icon(Icons.delete, color: Colors.redAccent),
                              onPressed: () => taskModel.removeTask(index),
                            ),
                          );
                        },
                      ),
                      floatingActionButton: FloatingActionButton(
                        onPressed: () {
                          showDialog(
                            context: context,
                            builder: (ctx) => AlertDialog(
                              title: const Text('New Task'),
                              content: TextField(controller: controller),
                              actions: [
                                TextButton(
                                  onPressed: () => Navigator.pop(ctx),
                                  child: const Text('Cancel'),
                                ),
                                ElevatedButton(
                                  onPressed: () {
                                    if (controller.text.isNotEmpty) {
                                      taskModel.addTask(controller.text);
                                      Navigator.pop(ctx);
                                    }
                                  },
                                  child: const Text('Add'),
                                ),
                              ],
                            ),
                          );
                        },
                        child: const Icon(Icons.add),
                      ),
                    );
                  }
                }
            """.trimIndent()
        }
    }
}
