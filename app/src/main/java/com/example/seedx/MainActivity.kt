package com.example.seedx

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.seedx.mc.BlockPos
import com.example.seedx.mc.SeedSearcher
import com.example.seedx.mc.StructureType
import com.example.seedx.ui.theme.SeedXTheme
import com.example.seedx.ui.theme.SolarizedBase02
import com.example.seedx.ui.theme.SolarizedBase03
import com.example.seedx.ui.theme.SolarizedBase0
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeedXTheme {
                SeedXApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedXApp() {
    var seed by remember { mutableStateOf("") }
    var centerX by remember { mutableStateOf("0") }
    var centerZ by remember { mutableStateOf("0") }
    var range by remember { mutableStateOf("10000") }
    var playerDist by remember { mutableStateOf("64") }
    var selectedType by remember { mutableStateOf(StructureType.SWAMP_HUT) }
    var expanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    var results by remember { mutableStateOf(listOf<BlockPos>()) }
    var searching by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SeedX") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("使用说明") },
                            onClick = {
                                menuExpanded = false
                                Toast.makeText(context, "输入种子和范围，点击计算搜索四连女巫小屋或十字路口", Toast.LENGTH_LONG).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("关于项目") },
                            onClick = {
                                menuExpanded = false
                                Toast.makeText(context, "SeedX v1.0\n基于 Minecraft 1.21 逻辑", Toast.LENGTH_LONG).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("退出应用") },
                            onClick = {
                                menuExpanded = false
                                (context as? android.app.Activity)?.finish()
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
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = seed,
                onValueChange = { seed = it },
                label = { Text("种子") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = range,
                    onValueChange = { range = it },
                    label = { Text("搜索范围") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = playerDist,
                    onValueChange = { playerDist = it },
                    label = { Text("玩家距离") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (selectedType == StructureType.SWAMP_HUT) "四连女巫小屋" else "下界四连十字路口")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("四连女巫小屋") },
                        onClick = { selectedType = StructureType.SWAMP_HUT; expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("下界四连十字路口") },
                        onClick = { selectedType = StructureType.FORTRESS; expanded = false }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (seed.isEmpty()) {
                        Toast.makeText(context, "请输入种子", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    searching = true
                    results = emptyList()
                    progress = 0f
                    scope.launch(Dispatchers.Default) {
                        val s = seed.toLongOrNull() ?: seed.hashCode().toLong()
                        val searcher = SeedSearcher(
                            seed = s,
                            centerX = centerX.toIntOrNull() ?: 0,
                            centerZ = centerZ.toIntOrNull() ?: 0,
                            range = range.toIntOrNull() ?: 10000,
                            playerDistance = playerDist.toIntOrNull() ?: 64,
                            type = selectedType,
                            onResult = { res ->
                                results = results + res
                            },
                            onProgress = { p ->
                                progress = p
                            }
                        )
                        searcher.search()
                        searching = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !searching
            ) {
                Text(if (searching) "正在计算..." else "点击计算")
            }

            if (searching) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("搜索结果 (${results.size}):", style = MaterialTheme.typography.titleMedium)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(results) { pos ->
                    ResultItem(pos)
                }
            }
        }
    }
}

@Composable
fun ResultItem(pos: BlockPos) {
    val context = LocalContext.current
    val coordText = "X: ${pos.x}, Z: ${pos.z}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SolarizedBase02)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(coordText, color = SolarizedBase0)
            IconButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("SeedX Result", "${pos.x} ${pos.z}")
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SolarizedBase0)
            }
        }
    }
}
