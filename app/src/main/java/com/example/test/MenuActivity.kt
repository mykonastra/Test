package com.example.test

import android.app.Activity
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast

class MenuActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        actionBar?.title = "3. XML 菜单 (OptionsMenu + ContextMenu)"

        val tvContext = findViewById<TextView>(R.id.tvContext)
        // 注册上下文菜单
        registerForContextMenu(tvContext)
    }

    // 创建选项菜单（右上角三点菜单）
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return true
    }

    // 选项菜单点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val msg = when (item.itemId) {
            R.id.menu_settings -> "点击了：设置"
            R.id.menu_about -> "点击了：关于"
            R.id.menu_exit -> "点击了：退出"
            else -> return super.onOptionsItemSelected(item)
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        return true
    }

    // 创建上下文菜单（长按弹出）
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.menu_context, menu)
        menu?.setHeaderTitle("上下文菜单")
    }

    // 上下文菜单点击
    override fun onContextItemSelected(item: MenuItem): Boolean {
        val msg = when (item.itemId) {
            R.id.ctx_copy -> "执行了：复制"
            R.id.ctx_cut -> "执行了：剪切"
            R.id.ctx_paste -> "执行了：粘贴"
            else -> return super.onContextItemSelected(item)
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        return true
    }
}
