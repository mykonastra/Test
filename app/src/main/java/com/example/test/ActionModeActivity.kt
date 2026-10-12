package com.example.test

import android.app.Activity
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class ActionModeActivity : Activity() {

    private val items = arrayListOf("One", "Two", "Three", "Four", "Five")
    // 记录选中的位置
    private val selectedPositions = HashSet<Int>()
    private var actionMode: ActionMode? = null
    private lateinit var adapter: SelectableAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_mode)
        actionBar?.title = "4. ActionMode"

        val listView = findViewById<ListView>(R.id.listView)
        adapter = SelectableAdapter()
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            toggleSelection(position)
        }
    }

    private fun toggleSelection(position: Int) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position)
        } else {
            selectedPositions.add(position)
        }

        if (selectedPositions.isNotEmpty() && actionMode == null) {
            actionMode = startActionMode(actionModeCallback)
        }
        actionMode?.title = "${selectedPositions.size} selected"

        if (selectedPositions.isEmpty()) {
            actionMode?.finish()
        }
        adapter.notifyDataSetChanged()
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            menuInflater.inflate(R.menu.menu_action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_delete -> {
                    // 从后往前删除
                    val sorted = selectedPositions.sortedDescending()
                    for (pos in sorted) {
                        items.removeAt(pos)
                    }
                    selectedPositions.clear()
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this@ActionModeActivity, "已删除选中项", Toast.LENGTH_SHORT).show()
                    mode.finish()
                    return true
                }
                R.id.action_share -> {
                    Toast.makeText(this@ActionModeActivity, "分享选中项", Toast.LENGTH_SHORT).show()
                    mode.finish()
                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            selectedPositions.clear()
            adapter.notifyDataSetChanged()
            actionMode = null
        }
    }

    inner class SelectableAdapter : BaseAdapter() {
        override fun getCount() = items.size
        override fun getItem(position: Int) = items[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@ActionModeActivity)
                .inflate(R.layout.item_action_mode, parent, false)

            view.findViewById<TextView>(android.R.id.text1).text = items[position]
            view.findViewById<ImageView>(android.R.id.icon)?.setImageResource(android.R.drawable.sym_def_app_icon)

            // 选中变蓝
            if (selectedPositions.contains(position)) {
                view.setBackgroundColor(android.graphics.Color.parseColor("#33B5E5"))
            } else {
                view.setBackgroundColor(android.graphics.Color.WHITE)
            }
            return view
        }
    }
}
