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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        actionBar?.title = "3. XML 菜单"

        tvContent = findViewById(R.id.tvContent)
    }

    // 创建选项菜单（右上角三点菜单，含子菜单）
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return true
    }

    // 选项菜单点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 字体大小子菜单
            R.id.font_size_10 -> {
                tvContent.textSize = 10f
                Toast.makeText(this, "字体大小：10sp", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.font_size_16 -> {
                tvContent.textSize = 16f
                Toast.makeText(this, "字体大小：16sp", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.font_size_20 -> {
                tvContent.textSize = 20f
                Toast.makeText(this, "字体大小：20sp", Toast.LENGTH_SHORT).show()
                return true
            }
            // 普通菜单项
            R.id.menu_normal -> {
                Toast.makeText(this, "这是普通菜单项", Toast.LENGTH_SHORT).show()
                return true
            }
            // 字体颜色子菜单
            R.id.font_color_black -> {
                tvContent.setTextColor(Color.BLACK)
                Toast.makeText(this, "字体颜色：黑色", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.font_color_red -> {
                tvContent.setTextColor(Color.RED)
                Toast.makeText(this, "字体颜色：红色", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.font_color_blue -> {
                tvContent.setTextColor(Color.BLUE)
                Toast.makeText(this, "字体颜色：蓝色", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
