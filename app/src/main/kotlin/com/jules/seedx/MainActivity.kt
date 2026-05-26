package com.jules.seedx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeedXTheme {
                val viewModel: MainViewModel = viewModel()
                MainScreen(viewModel = viewModel, onExit = {
                    finishAffinity()
                    exitProcess(0)
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, onExit: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var showInstructions by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SeedX") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
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
                                showMenu = false
                                onExit()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SolarizedDarkSurface,
                    titleContentColor = SolarizedDarkOnPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.seed,
                onValueChange = { viewModel.onSeedChange(it) },
                label = { Text("种子") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.centerX,
                    onValueChange = { viewModel.centerX = it },
                    label = { Text("起点 X") },
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                )
                OutlinedTextField(
                    value = viewModel.centerZ,
                    onValueChange = { viewModel.centerZ = it },
                    label = { Text("起点 Z") },
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.range,
                    onValueChange = { viewModel.range = it },
                    label = { Text("搜索半径") },
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                )
                OutlinedTextField(
                    value = viewModel.maxDist,
                    onValueChange = { viewModel.maxDist = it },
                    label = { Text("中心最大距离") },
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                )
            }

            var expandedType by remember { mutableStateOf(false) }
            val types = listOf(
                "四连女巫小屋（沼泽）",
                "三连女巫小屋（沼泽）",
                "二连女巫小屋（沼泽）",
                "女巫小屋（沼泽）",
                "四连十字路口（灵魂沙峡谷）",
                "三连十字路口（灵魂沙峡谷）",
                "二连十字路口（灵魂沙峡谷）",
                "十字路口（灵魂沙峡谷）"
            )
            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = !expandedType },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = types[viewModel.selectedType],
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("结构类型") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false }
                ) {
                    types.forEachIndexed { index, label ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                viewModel.selectedType = index
                                expandedType = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.search() },
                enabled = !viewModel.isSearching,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                if (viewModel.isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("计算结果")
                }
            }

            Text("结果列表:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            val clipboardManager = LocalClipboardManager.current
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(viewModel.results) { res ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(res, modifier = Modifier.weight(1f))
                        TextButton(onClick = { clipboardManager.setText(AnnotatedString(res)) }) {
                            Text("复制")
                        }
                    }
                }
            }
        }
    }

    if (showInstructions) {
        AlertDialog(
            onDismissRequest = { showInstructions = false },
            title = { Text("使用说明") },
            text = { Text("1. 输入您想要查询的种子（Long类型）。\n2. 设置搜索的起始中心坐标 XZ 及搜索半径。\n3. 设置玩家中心到结构的最大允许距离（默认 64）。\n4. 选择结构类型并点击计算。") },
            confirmButton = {
                TextButton(onClick = { showInstructions = false }) { Text("确定") }
            }
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("关于项目") },
            text = { Text("SeedX v1.0\n基于 cubiomes 核心计算，专为 Minecraft 1.21 设计。\n开发者：Jules") },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("确定") }
            }
        )
    }
}
