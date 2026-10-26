package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.test.ui.theme.TestTheme

data class Task(
    val id: Int,
    val text: String,
    val completed: Boolean = false
)

class TaskListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestTheme {
                TaskListScreen()
            }
        }
    }
}

@Composable
fun TaskListScreen() {
    var nextId by remember { mutableStateOf(4) }
    val tasks = remember {
        mutableStateListOf(
            Task(1, "学习 Jetpack Compose", completed = true),
            Task(2, "完成 LinearLayout 实验"),
            Task(3, "完成 ConstraintLayout 实验")
        )
    }
    var inputText by remember { mutableStateOf("") }

    val completedCount = tasks.count { it.completed }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题（不用 TopAppBar，PDF 原图是白底普通标题）
        Text(
            text = "课程学习任务",
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 已完成计数
        Text(
            text = "已完成: $completedCount / ${tasks.size}",
            style = TextStyle(fontSize = 14.sp),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 输入框 + 添加按钮
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("输入新任务...") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        tasks.add(Task(nextId++, inputText.trim()))
                        inputText = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("添加")
            }
        }

        // 任务列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task ->
                TaskItem(
                    task = task,
                    onCheckedChange = { checked ->
                        val index = tasks.indexOfFirst { it.id == task.id }
                        if (index >= 0) {
                            tasks[index] = tasks[index].copy(completed = checked)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = task.completed,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = task.text,
            style = TextStyle(
                fontSize = 16.sp,
                textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                color = if (task.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        )
    }
}
