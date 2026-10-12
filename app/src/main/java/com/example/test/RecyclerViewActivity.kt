package com.example.test

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class AnimalCard(val name: String, val desc: String, val imageRes: Int)

class AnimalAdapter(
    private val animals: MutableList<AnimalCard>,
    private val onItemClick: (AnimalCard) -> Unit
) : RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder>() {

    inner class AnimalViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val ivAnimal: ImageView = itemView.findViewById(R.id.ivAnimal)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvDesc)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(animals[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return AnimalViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        val animal = animals[position]
        holder.ivAnimal.setImageResource(animal.imageRes)
        holder.tvName.text = animal.name
        holder.tvDesc.text = animal.desc
    }

    override fun getItemCount() = animals.size

    // 添加新项
    fun addItem(animal: AnimalCard) {
        animals.add(animal)
        notifyItemInserted(animals.size - 1)
    }
}

class RecyclerViewActivity : Activity() {

    private val animals = mutableListOf(
        AnimalCard("猫", "一种常见的家养宠物，喜欢睡觉和玩耍", R.drawable.cat),
        AnimalCard("狗", "人类最忠实的朋友，聪明又忠诚", R.drawable.dog),
        AnimalCard("大象", "陆地上最大的哺乳动物，记忆力惊人", R.drawable.elephant),
        AnimalCard("狮子", "草原之王，群居生活的大型猫科动物", R.drawable.lion),
        AnimalCard("猴子", "灵长目动物，聪明活泼善于攀爬", R.drawable.monkey),
        AnimalCard("老虎", "森林之王，独居的大型猫科动物", R.drawable.tiger)
    )

    private lateinit var adapter: AnimalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)
        actionBar?.title = "5. RecyclerView + CardView"

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val etNewItem = findViewById<EditText>(R.id.etNewItem)
        val btnAdd = findViewById<Button>(R.id.btnAdd)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AnimalAdapter(animals) { animal ->
            Toast.makeText(this, "点击了：${animal.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        // 添加新项
        btnAdd.setOnClickListener {
            val name = etNewItem.text.toString().trim()
            if (name.isNotEmpty()) {
                adapter.addItem(AnimalCard(name, "新添加的列表项", R.drawable.cat))
                etNewItem.text.clear()
                Toast.makeText(this, "已添加：$name", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "请输入名称", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
