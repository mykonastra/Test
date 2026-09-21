package com.example.test

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import com.example.test.ui.theme.TestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("实验2 Android界面布局") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "选择实验项目",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    context.startActivity(Intent(context, LinearLayoutActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("1. 线性布局 (LinearLayout)")
            }

            Button(
                onClick = {
                    context.startActivity(Intent(context, TableLayoutActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("2. 表格布局 (TableLayout)")
            }

            Button(
                onClick = {
                    context.startActivity(Intent(context, CalculatorActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("3. 约束布局 - 计算器 (ConstraintLayout)")
            }

            Button(
                onClick = {
                    context.startActivity(Intent(context, SpaceTravelActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("4. 约束布局 - 太空旅行 (ConstraintLayout)")
            }

            Button(
                onClick = {
                    context.startActivity(Intent(context, TaskListActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("5. Compose 任务列表")
            }
        }
    }
}
