package com.example.test

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class CalculatorActivity : Activity() {

    private lateinit var display: EditText

    // 计算器状态
    private var firstOperand: Double? = null   // 第一个操作数
    private var operator: String? = null        // 当前运算符 + - × ÷
    private var waitingForNewNumber = false     // 是否等待输入新数字

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        display = findViewById(R.id.display)

        // 数字按钮 0-9
        val numberButtons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )

        numberButtons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                val digit = (it as Button).text.toString()
                onNumberClick(digit)
            }
        }

        // 小数点
        findViewById<Button>(R.id.btnDot).setOnClickListener {
            onDotClick()
        }

        // 运算符
        findViewById<Button>(R.id.btnAdd).setOnClickListener { onOperatorClick("+") }
        findViewById<Button>(R.id.btnSub).setOnClickListener { onOperatorClick("-") }
        findViewById<Button>(R.id.btnMul).setOnClickListener { onOperatorClick("×") }
        findViewById<Button>(R.id.btnDiv).setOnClickListener { onOperatorClick("÷") }

        // 等号
        findViewById<Button>(R.id.btnEq).setOnClickListener { onEqualsClick() }
    }

    private fun onNumberClick(digit: String) {
        val current = display.text.toString()
        if (waitingForNewNumber) {
            display.setText(digit)
            waitingForNewNumber = false
        } else if (current == "0" || current == "0.0") {
            display.setText(digit)
        } else {
            display.setText(current + digit)
        }
    }

    private fun onDotClick() {
        val current = display.text.toString()
        if (waitingForNewNumber) {
            display.setText("0.")
            waitingForNewNumber = false
        } else if (!current.contains(".")) {
            display.setText("$current.")
        }
    }

    private fun onOperatorClick(op: String) {
        val currentValue = display.text.toString().toDoubleOrNull() ?: return

        if (firstOperand != null && operator != null && !waitingForNewNumber) {
            // 连续运算：先算前面的结果
            val result = calculate(firstOperand!!, currentValue, operator!!)
            display.setText(formatNumber(result))
            firstOperand = result
        } else {
            firstOperand = currentValue
        }

        operator = op
        waitingForNewNumber = true
    }

    private fun onEqualsClick() {
        val currentValue = display.text.toString().toDoubleOrNull() ?: return
        if (firstOperand != null && operator != null) {
            val result = calculate(firstOperand!!, currentValue, operator!!)
            display.setText(formatNumber(result))
            firstOperand = null
            operator = null
            waitingForNewNumber = true
        }
    }

    private fun calculate(a: Double, b: Double, op: String): Double {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "×" -> a * b
            "÷" -> if (b == 0.0) Double.NaN else a / b
            else -> b
        }
    }

    private fun formatNumber(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return "Error"
        // 整数去掉小数点
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.8f", value).trimEnd('0').trimEnd('.')
        }
    }
}
