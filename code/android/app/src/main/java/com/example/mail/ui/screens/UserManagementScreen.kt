package com.example.mail.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mail.ui.Screen
import com.example.mail.data.model.User
import com.example.mail.viewmodel.EmailViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(navController: NavHostController) {
    val emailViewModel: EmailViewModel = EmailViewModel.getInstance()
    val userListState by emailViewModel.userListState.collectAsState()
    val users = remember(userListState) {
        userListState?.getOrNull() ?: emptyList()
    }
    val isLoading = remember(userListState) {
        userListState == null
    }
    
    // 重置密码对话框状态
    val showResetPasswordDialog = remember { mutableStateOf(false) }
    val selectedUserId = remember { mutableStateOf<Long?>(null) }
    val newPassword = remember { mutableStateOf("") }

    // 初始加载用户列表
    LaunchedEffect(Unit) {
        emailViewModel.getAllUsers()
    }
    
    // 重置密码对话框
    if (showResetPasswordDialog.value) {
        AlertDialog(
            onDismissRequest = { showResetPasswordDialog.value = false },
            title = { Text(text = "重置密码") },
            text = {
                Column {
                    Text("请输入新密码")
                    OutlinedTextField(
                        value = newPassword.value,
                        onValueChange = { newPassword.value = it },
                        label = { Text("新密码") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = { 
                            if (newPassword.value.isNotEmpty() && (newPassword.value.length < 6 || newPassword.value.length > 120)) {
                                Text("密码长度必须在6-120之间", color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("密码长度必须在6-120之间")
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedUserId.value?.let {
                            emailViewModel.resetPassword(it, newPassword.value)
                            showResetPasswordDialog.value = false
                            newPassword.value = ""
                        }
                    },
                    enabled = newPassword.value.isNotEmpty() && newPassword.value.length >= 6 && newPassword.value.length <= 120
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showResetPasswordDialog.value = false
                        newPassword.value = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用户管理") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 可以添加搜索等操作
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
            userListState?.isFailure == true -> {
                // 错误状态
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "加载失败：${userListState?.exceptionOrNull()?.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { emailViewModel.getAllUsers() }) {
                            Text("重试")
                        }
                    }
                }
            }
            else -> {
                // 用户列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(users) { user ->
                        UserListItem(
                            user = user,
                            onDelete = {
                                // 调用删除用户方法
                                emailViewModel.deleteUser(user.id)
                            },
                            onResetPassword = {
                                selectedUserId.value = user.id
                                showResetPasswordDialog.value = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserListItem(user: User, onDelete: () -> Unit, onResetPassword: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.username,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = user.role,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Row {
                    IconButton(
                        onClick = onResetPassword,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "重置密码")
                    }
                    IconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "删除用户")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "昵称: ${user.nickname}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "状态: ${user.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "创建时间: ${user.createdAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
