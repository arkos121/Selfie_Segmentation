package com.example.easycalculator

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
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
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.easycalculator.ui.theme.EasyCalculatorTheme
import org.w3c.dom.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.layout)

        val buts:Button = findViewById<Button>(R.id.btn4)
        val ks : TextView = findViewById(R.id.TextView1)
        val zs : TextView = findViewById(R.id.textview2)
        val tt : EditText = findViewById(R.id.edittx)
        val mt : EditText = findViewById(R.id.edittx2)
        val so : Switch = findViewById<Switch>(R.id.switch1)
        val mainLayout = findViewById<ConstraintLayout>(R.id.relativeLayout)
        so.setOnCheckedChangeListener{_, isChecked ->
            if(isChecked){
                mainLayout.setBackgroundColor(Color.BLACK)
            }
            else{
                mainLayout.setBackgroundColor(Color.WHITE)
            }
        }

        buts.setOnClickListener{
            val firstName = tt.text.toString()
            val secondName = mt.text.toString()

            ks.text = "First name is $firstName"
            zs.text = "Second name is $secondName"
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EasyCalculatorTheme {
        Greeting("Android")
    }
}