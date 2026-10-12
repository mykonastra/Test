package com.example.test

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class AlertDialogActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_dialog)
        actionBar?.title = "2. AlertDialog (setView)"

        findViewById<Button>(R.id.btnShowDialog).setOnClickListener {
            showLoginDialog()
        }
    }

    private fun showLoginDialog() {
        // 加载自定义布局
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_login, null)
        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)

        AlertDialog.Builder(this)
            .setTitle("用户登录")
            .setIcon(R.mipmap.ic_launcher)
            .setView(dialogView)  // 设置自定义视图
            .setPositiveButton("登录") { _, _ ->
                val username = etUsername.text.toString()
                val password = etPassword.text.toString()
                Toast.makeText(
                    this,
                    "用户名：$username\n密码：$password",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("取消", null)
            .setCancelable(true)
            .show()
    }
}
