package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.layout)

        val editText = findViewById<EditText>(R.id.e1)
        val buttondel = findViewById<Button>(R.id.dele)
        val but1 = findViewById<Button>(R.id.b1)
        val but2 = findViewById<Button>(R.id.b2)
        val but3 = findViewById<Button>(R.id.b3)
        val butadd = findViewById<Button>(R.id.badd)
        val but4 = findViewById<Button>(R.id.b4)
        val but5 = findViewById<Button>(R.id.b5)
        val but6 = findViewById<Button>(R.id.b6)
        val butSub = findViewById<Button>(R.id.bsub)
        val but7 = findViewById<Button>(R.id.b7)
        val but8 = findViewById<Button>(R.id.b8)
        val but9 = findViewById<Button>(R.id.b9)
        val butmul = findViewById<Button>(R.id.bmul)
        val but0 = findViewById<Button>(R.id.b0)
        val butCl = findViewById<Button>(R.id.C)
        val butdiv = findViewById<Button>(R.id.divs)
        val butEq = findViewById<Button>(R.id.ans)

        but1.setOnClickListener { editText.append("1") }
        but2.setOnClickListener { editText.append("2") }
        but3.setOnClickListener { editText.append("3") }
        but4.setOnClickListener { editText.append("4") }
        but5.setOnClickListener { editText.append("5") }
        but6.setOnClickListener { editText.append("6") }
        but7.setOnClickListener { editText.append("7") }
        but8.setOnClickListener { editText.append("8") }
        but9.setOnClickListener { editText.append("9") }
        but0.setOnClickListener { editText.append("0") }

        butadd.setOnClickListener { editText.append("+") }
        butSub.setOnClickListener { editText.append("-") }
        butmul.setOnClickListener { editText.append("*") }
        butdiv.setOnClickListener { editText.append("/") }

        var operator: String? = null
        var firstOperand: Double? = null

        butCl.setOnClickListener {
            editText.text.clear()
            firstOperand = null
            operator = null
        }

        butadd.setOnClickListener {
            operator = "+"
            firstOperand = editText.text.toString().toDoubleOrNull()
            editText.text.clear()
        }

        butSub.setOnClickListener {
            operator = "-"
            firstOperand = editText.text.toString().toDoubleOrNull()
            editText.text.clear()
        }

        butmul.setOnClickListener {
            operator = "*"
            firstOperand = editText.text.toString().toDoubleOrNull()
            editText.text.clear()
        }

        butdiv.setOnClickListener {
            operator = "/"
            firstOperand = editText.text.toString().toDoubleOrNull()
            editText.text.clear()
        }


        buttondel.setOnClickListener {
            val curtext = editText.text.toString()
            if (curtext.isNotEmpty()) {
                editText.setText(curtext.dropLast(1))
                editText.setSelection(editText.text.length)
            }
        }

        butEq.setOnClickListener {
            val secondOperand = editText.text.toString().toDoubleOrNull()
            if (firstOperand != null && secondOperand != null) {
                val result = when (operator) {
                    "+" -> firstOperand!! + secondOperand
                    "-" -> firstOperand!! - secondOperand
                    "*" -> firstOperand!! * secondOperand
                    "/" -> firstOperand!! / secondOperand

                    else -> {
                        null
                    }
                }
                editText.setText(result.toString())
            }


        }
    }
}

