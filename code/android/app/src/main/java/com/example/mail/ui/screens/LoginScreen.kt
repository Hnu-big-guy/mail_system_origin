package com.example.mail.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import com.example.mail.ui.Screen
import com.example.mail.viewmodel.EmailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    val emailViewModel: EmailViewModel = EmailViewModel.getInstance()
    val loginState by emailViewModel.loginState.collectAsState()
    
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 监听登录结果
    LaunchedEffect(loginState) {
        if (loginState?.isSuccess == true) {
            // 登录成功，导航到邮件列表
            navController.navigate(Screen.EmailList.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }
    
    // 监听登出结果
    LaunchedEffect(Unit) {
        // 重置登出状态，避免影响下次登录
        emailViewModel.resetLogoutState()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "邮件客户端",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("用户名") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        Button(
            onClick = {
                emailViewModel.login(username, password)
            },
            enabled = username.isNotEmpty() && password.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "登录")
        }

        // 显示登录失败信息
        if (loginState?.isFailure == true) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "登录失败：${loginState?.exceptionOrNull()?.message}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                navController.navigate(Screen.Register.route)
            }
        ) {
            Text(text = "还没有账号？注册")
        }
    }
}