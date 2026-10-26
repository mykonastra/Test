package com.example.test

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
                title = { Text("实验3 Android界面组件") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "实验3 Android界面组件",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    context.startActivity(Intent(context, ListViewActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("1. ListView + 自定义 BaseAdapter")
            }

            Button(
                onClick = {
                    context.startActivity(Intent(context, AlertDialogActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("2. AlertDialog (setView)")
            }

            Button(
                onClick = {
                    context.startActivity(Intent(context, MenuActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("3. XML 菜单 (OptionsMenu + 子菜单)")
            }

            Button(
                onClick = {
                    context.startActivity(Intent(context, ActionModeActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("4. ActionMode (多选)")
            }

            Button(
                onClick = {
                    context.startActivity(Intent(context, RecyclerViewActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("5. RecyclerView + CardView")
            }
        }
    }
}
