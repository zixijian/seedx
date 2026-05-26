package com.jules.seedx

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jules.seedx.ui.theme.SeedXTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeedXTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsManager = remember { SettingsManager(context) }

    var seed by remember { mutableStateOf("") }
    var centerX by remember { mutableStateOf("0") }
    var centerZ by remember { mutableStateOf("0") }
    var range by remember { mutableStateOf("10000") }
    var maxDist by remember { mutableStateOf("64") }
    var minCount by remember { mutableStateOf("4") }
    var structureType by remember { mutableStateOf(StructureType.SWAMP_HUT) }

    var results by remember { mutableStateOf<List<BlockPos>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }

    // Load settings
    LaunchedEffect(Unit) {
        settingsManager.seed.collect { if (it.isNotEmpty()) seed = it }
        settingsManager.centerX.collect { centerX = it }
        settingsManager.centerZ.collect { centerZ = it }
        settingsManager.range.collect { range = it }
        settingsManager.maxDist.collect { maxDist = it }
        settingsManager.minCount.collect { minCount = it }
        settingsManager.structureType.collect { typeName ->
            structureType = StructureType.entries.find { it.name == typeName } ?: StructureType.SWAMP_HUT
        }
    }

    var showMenu by remember { mutableStateOf(false) }
    var showInstructions by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SeedX") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "菜单")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("使用说明") },
                            onClick = {
                                showMenu = false
                                showInstructions = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("关于项目") },
                            onClick = {
                                showMenu = false
                                showAbout = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("退出应用") },
                            onClick = {
                                (context as? ComponentActivity)?.finish()
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = seed,
                onValueChange = { seed = it },
                label = { Text("种子") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = centerX,
                    onValueChange = { centerX = it },
                    label = { Text("中心 X") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = centerZ,
                    onValueChange = { centerZ = it },
                    label = { Text("中心 Z") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            OutlinedTextField(
                value = range,
                onValueChange = { range = it },
                label = { Text("搜索范围") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = maxDist,
                onValueChange = { maxDist = it },
                label = { Text("最大距离 (玩家到点位)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = minCount,
                onValueChange = { minCount = it },
                label = { Text("连接数量") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = structureType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("结构类型") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    StructureType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                structureType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    searching = true
                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        val s = seed.toLongOrNull() ?: seed.hashCode().toLong()
                        val cx = centerX.toIntOrNull() ?: 0
                        val cz = centerZ.toIntOrNull() ?: 0
                        val r = range.toIntOrNull() ?: 10000
                        val md = maxDist.toIntOrNull() ?: 64
                        val mc = minCount.toIntOrNull() ?: 4

                        settingsManager.saveSettings(seed, range, centerX, centerZ, maxDist, minCount, structureType.name)

                        val finder = Finder()
                        val foundResults = finder.findClusters(s, cx, cz, r, md, mc, structureType)

                        launch(kotlinx.coroutines.Dispatchers.Main) {
                            results = foundResults
                            searching = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !searching
            ) {
                if (searching) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("计算")
                }
            }

            if (results.isNotEmpty()) {
                Text("搜索结果 (${results.size}):", style = MaterialTheme.typography.titleMedium)
                results.forEach { pos ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("BlockPos", "/tp @s ${pos.x} ~ ${pos.z}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "已复制传送命令: ${pos.x}, ${pos.z}", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("玩家中心挂机位置: ${pos.x}, ${pos.z}")
                            Text("点击复制传送命令", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            } else if (!searching && seed.isNotEmpty()) {
                Text("未找到符合条件的结构聚集地。")
            }
        }
    }

    if (showInstructions) {
        AlertDialog(
            onDismissRequest = { showInstructions = false },
            title = { Text("使用说明") },
            text = { Text("1. 输入 Minecraft 种子 (1.21+)\n2. 设置搜索中心和半径\n3. 设置玩家到结构的距离阈值 (默认64)\n4. 设置所需结构的最小聚集数量\n5. 选择结构类型并点击计算\n6. 点击结果可复制 /tp 命令。") },
            confirmButton = {
                TextButton(onClick = { showInstructions = false }) { Text("确定") }
            }
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("关于项目") },
            text = { Text("SeedX - Minecraft 结构聚集地搜索工具。\n版本: 1.0\n基于 Kotlin 和 Jetpack Compose 开发。") },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("确定") }
            }
        )
    }
}
