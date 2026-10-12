package com.example.test

import android.app.Activity
import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast

class ActionModeActivity : Activity() {

    private val items = arrayListOf(
        "苹果", "香蕉", "橙子", "葡萄", "西瓜",
        "草莓", "芒果", "菠萝", "樱桃", "蓝莓",
        "桃子", "梨子", "柚子", "柠檬", "猕猴桃"
    )
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_mode)
        actionBar?.title = "4. ActionMode (多选)"

        val listView = findViewById<ListView>(R.id.listView)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, items)
        listView.adapter = adapter

        // 设置多选模式监听器（ActionMode）
        listView.setMultiChoiceModeListener(object : AbsListView.MultiChoiceModeListener {
            override fun onItemCheckedStateChanged(
                mode: ActionMode,
                position: Int,
                id: Long,
                checked: Boolean
            ) {
                val count = listView.checkedItemCount
                mode.title = "已选中 $count 项"
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                menuInflater.inflate(R.menu.menu_action_mode, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                val checkedPositions = listView.checkedItemPositions
                when (item.itemId) {
                    R.id.action_delete -> {
                        // 从后往前删除，避免索引错乱
                        for (i in items.size - 1 downTo 0) {
                            if (checkedPositions.get(i)) {
                                items.removeAt(i)
                            }
                        }
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

            override fun onDestroyActionMode(mode: ActionMode) {}
        })
    }
}
