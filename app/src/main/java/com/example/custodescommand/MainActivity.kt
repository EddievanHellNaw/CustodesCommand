
package com.example.custodescommand

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.custodescommand.ui.theme.CustodesTheme
import com.example.custodescommand.ui.CustodesApp


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CustodesTheme {
                CustodesApp()
            }
        }
    }
}
