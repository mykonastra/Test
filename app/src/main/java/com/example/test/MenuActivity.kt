package com.example.test

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast

class MenuActivity : Activity() {

    private lateinit var tvContent: TextView
    private val fontSizes = intArrayOf(10, 16, 20)
    private var fontSizeIndex = 1  // 默认 16sp
    private val fontColors = intArrayOf(Color.BLACK, Color.RED, Color.BLUE)
    private var fontColorIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        actionBar?.title = "3. XML 菜单"

        tvContent = findViewById(R.id.tvContent)
    }

    // 创建选项菜单（右上角三点菜单）
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return true
    }

    // 选项菜单点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_font_size -> {
                // 循环切换字体大小 10 -> 16 -> 20 -> 10
                fontSizeIndex = (fontSizeIndex + 1) % fontSizes.size
                tvContent.textSize = fontSizes[fontSizeIndex].toFloat()
                Toast.makeText(this, "字体大小：${fontSizes[fontSizeIndex]}sp", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menu_normal -> {
                Toast.makeText(this, "这是普通菜单项", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menu_font_color -> {
                // 循环切换字体颜色 黑 -> 红 -> 蓝 -> 黑
                fontColorIndex = (fontColorIndex + 1) % fontColors.size
                tvContent.setTextColor(fontColors[fontColorIndex])
                Toast.makeText(this, "字体颜色已切换", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
