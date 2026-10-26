package com.example.test

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class ListViewActivity : Activity() {

    private val animals = arrayOf(
        Animal("Lion", R.drawable.lion),
        Animal("Tiger", R.drawable.tiger),
        Animal("Monkey", R.drawable.monkey),
        Animal("Dog", R.drawable.dog),
        Animal("Cat", R.drawable.cat),
        Animal("Elephant", R.drawable.elephant)
    )

    data class Animal(val name: String, val icon: Int)

    // 记录当前选中的位置，-1 表示未选中
    private var selectedPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_view)
        actionBar?.title = "1. ListView + 自定义 BaseAdapter"

        val listView = findViewById<ListView>(R.id.listView)
        val adapter = AnimalAdapter(this, animals)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            // 更新选中位置
            selectedPosition = position
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "你点击了：${animals[position].name}", Toast.LENGTH_SHORT).show()
        }
    }

    inner class AnimalAdapter(
        private val context: Context,
        private val data: Array<Animal>
    ) : BaseAdapter() {

        override fun getCount() = data.size
        override fun getItem(position: Int) = data[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_animal, parent, false)

            val animal = data[position]
            view.findViewById<TextView>(R.id.title).text = animal.name
            view.findViewById<ImageView>(R.id.icon).setImageResource(animal.icon)

            // 根据选中状态设置背景颜色
            if (position == selectedPosition) {
                view.setBackgroundColor(Color.parseColor("#FF4444"))
            } else {
                view.setBackgroundColor(Color.WHITE)
            }

            return view
        }
    }
}
