package com.example.test

import android.app.Activity
import android.os.Bundle
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast

class ListViewActivity : Activity() {

    private val animals = arrayOf(
        Animal("猫", R.drawable.cat),
        Animal("狗", R.drawable.dog),
        Animal("大象", R.drawable.elephant),
        Animal("狮子", R.drawable.lion),
        Animal("猴子", R.drawable.monkey),
        Animal("老虎", R.drawable.tiger)
    )

    data class Animal(val name: String, val icon: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_view)
        actionBar?.title = "1. ListView + SimpleAdapter"

        val listView = findViewById<ListView>(R.id.listView)

        // 构造数据列表
        val data = ArrayList<Map<String, Any>>()
        for (animal in animals) {
            val map = HashMap<String, Any>()
            map["icon"] = animal.icon
            map["title"] = animal.name
            data.add(map)
        }

        // SimpleAdapter 参数：上下文、数据、item布局、键数组、视图id数组
        val adapter = SimpleAdapter(
            this,
            data,
            R.layout.item_animal,
            arrayOf("icon", "title"),
            intArrayOf(R.id.icon, R.id.title)
        )
        listView.adapter = adapter

        // 点击列表项弹出 Toast
        listView.setOnItemClickListener { _, _, position, _ ->
            Toast.makeText(this, "你点击了：${animals[position].name}", Toast.LENGTH_SHORT).show()
        }
    }
}
