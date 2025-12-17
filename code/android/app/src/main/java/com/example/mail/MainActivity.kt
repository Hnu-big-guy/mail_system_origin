package com.example.mail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.mail.ui.NavigationGraph
import com.example.mail.ui.theme.MailTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MailTheme {
                // 创建导航控制器
                val navController = rememberNavController()
                
                // 应用主界面
                Surface {
                    NavigationGraph(navController = navController)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    MailTheme {
        Text("邮件客户端")
    }
}