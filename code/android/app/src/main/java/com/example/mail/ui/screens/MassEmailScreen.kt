package com.example.mail.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mail.viewmodel.EmailViewModel
import com.example.mail.data.model.EmailRequest
import com.example.mail.data.model.User
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MassEmailScreen(navController: NavHostController) {
    val emailViewModel: EmailViewModel = EmailViewModel.getInstance()
    val broadcastEmailState by emailViewModel.broadcastEmailState.collectAsState()
    val userListState by emailViewModel.userListState.collectAsState()
    
    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showUserSelection by remember { mutableStateOf(false) }
    var selectedUsers by remember { mutableStateOf(emptySet<Long>()) } // 保存选中的用户ID
    
    val users = remember(userListState) {
        userListState?.getOrNull() ?: emptyList()
    }

    // 监听发送邮件结果
    LaunchedEffect(broadcastEmailState) {
        if (broadcastEmailState?.isSuccess == true) {
            // 发送成功，返回上一页
            navController.popBackStack()
        }
    }
    
    // 初始加载用户列表
    LaunchedEffect(Unit) {
        emailViewModel.getAllUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("群发邮件") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                } },
                actions = {
                    if (!showUserSelection) {
                        IconButton(
                            onClick = {
                                showUserSelection = true
                            },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "选择用户")
                        }
                        IconButton(
                            onClick = {
                                isLoading = true
                                // 将选中的用户ID转换为逗号分隔字符串
                                val selectedUserIds = if (selectedUsers.isEmpty()) "" else selectedUsers.joinToString(",")
                                val emailRequest = EmailRequest(
                                    subject = subject,
                                    from = "",  // 群发邮件会自动使用管理员邮箱
                                    to = selectedUserIds,  // 如果为空字符串表示发送给所有用户，否则发送给指定用户
                                    content = content,
                                    attachments = null
                                )
                                emailViewModel.broadcastEmail(emailRequest)
                            },
                            enabled = content.isNotEmpty() && !isLoading
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "发送")
                        }
                    } else {
                        IconButton(
                            onClick = {
                                showUserSelection = false
                            },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回邮件编辑")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (!showUserSelection) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = if (selectedUsers.isEmpty()) "群发邮件将发送给所有用户" 
                           else "将发送邮件给 ${selectedUsers.size} 个选中的用户",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("主题") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("正文") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    minLines = 15,
                    maxLines = Int.MAX_VALUE
                )

                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                // 显示发送失败信息
                if (broadcastEmailState?.isFailure == true) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "发送失败：${broadcastEmailState?.exceptionOrNull()?.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        } else {
            // 用户选择界面
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 选择操作栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("选择收件人", style = MaterialTheme.typography.headlineSmall)
                    Row {
                        IconButton(
                            onClick = {
                                selectedUsers = users.map { it.id }.toSet()
                            }
                        ) {
                            Icon(Icons.Default.SelectAll, contentDescription = "全选")
                        }
                        IconButton(
                            onClick = {
                                selectedUsers = emptySet()
                            }
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = "取消全选")
                        }
                    }
                }
                
                // 用户列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(users) { user ->
                        UserSelectionItem(
                            user = user,
                            isSelected = selectedUsers.contains(user.id),
                            onToggle = {
                                selectedUsers = if (selectedUsers.contains(user.id)) {
                                    selectedUsers - user.id
                                } else {
                                    selectedUsers + user.id
                                }
                            }
                        )
                    }
                }
                
                // 底部选择信息
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(color = MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "已选择 ${selectedUsers.size} 个用户",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun UserSelectionItem(user: User, isSelected: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onToggle),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                contentDescription = if (isSelected) "已选择" else "未选择",
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
