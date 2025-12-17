package com.example.mail.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import com.example.mail.viewmodel.EmailViewModel
import com.example.mail.utils.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailDetailScreen(navController: NavHostController, emailId: Long) {
    val emailViewModel: EmailViewModel = viewModel()
    val emailDetailState by emailViewModel.emailDetailState.collectAsState()
    val email = emailDetailState?.getOrNull()
    val isLoading = emailDetailState == null

    // 加载邮件详情
    LaunchedEffect(emailId) {
        emailViewModel.getEmailDetail(emailId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("邮件详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { /* 添加星标功能 */ }) {
                        Icon(
                            Icons.Default.StarBorder,
                            contentDescription = "添加星标"
                        )
                    }
                    IconButton(onClick = { 
                        emailViewModel.deleteEmail(emailId)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                // 加载状态
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            emailDetailState?.isFailure == true -> {
                // 错误状态
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "加载失败：${emailDetailState?.exceptionOrNull()?.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { emailViewModel.getEmailDetail(emailId) }) {
                            Text("重试")
                        }
                    }
                }
            }
            email != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // 发件人和时间
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                        text = "发件人: ${email.from}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                        Text(
                            text = formatTime(email.sentAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 邮件主题
                    Text(
                        text = email.subject,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // 收件人
                    Text(
                        text = "收件人: ${email.to}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 分割线
                    Divider(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))

                    // 邮件内容
                    Text(
                        text = email.content,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            else -> {
                // 空状态
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "邮件不存在或已被删除")
                }
            }
        }
    }
}

