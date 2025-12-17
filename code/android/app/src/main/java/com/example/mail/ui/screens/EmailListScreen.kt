package com.example.mail.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Save
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mail.ui.Screen
import com.example.mail.data.model.Email
import com.example.mail.viewmodel.EmailViewModel
import com.example.mail.utils.formatTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// 邮件类型枚举
enum class EmailType {
    INBOX, SENT, DRAFT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailListScreen(navController: NavHostController, initialEmailType: EmailType = EmailType.INBOX) {
    val emailViewModel: EmailViewModel = EmailViewModel.getInstance()
    val emailListState by emailViewModel.emailListState.collectAsState()
    val sentMailsState by emailViewModel.sentMailsState.collectAsState()
    val draftMailsState by emailViewModel.draftMailsState.collectAsState()
    val loginState by emailViewModel.loginState.collectAsState()
    val logoutState by emailViewModel.logoutState.collectAsState()
    
    var selectedEmailType by remember { mutableStateOf(initialEmailType) }
    
    val emails = remember(selectedEmailType, emailListState, sentMailsState, draftMailsState) {
        when (selectedEmailType) {
            EmailType.INBOX -> emailListState?.getOrNull()?.content ?: emptyList()
            EmailType.SENT -> sentMailsState?.getOrNull()?.content ?: emptyList()
            EmailType.DRAFT -> draftMailsState?.getOrNull()?.content ?: emptyList()
        }
    }
    
    val isLoading = remember(selectedEmailType, loginState, emailListState, sentMailsState, draftMailsState) {
        loginState?.isSuccess == true && when (selectedEmailType) {
            EmailType.INBOX -> emailListState == null
            EmailType.SENT -> sentMailsState == null
            EmailType.DRAFT -> draftMailsState == null
        }
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    
    // 检查用户是否为管理员
    val isAdmin = remember(loginState) {
        loginState?.getOrNull()?.role == "ROLE_ADMIN"
    }

    // 加载邮件列表，根据选中的邮件类型
    LaunchedEffect(loginState, selectedEmailType) {
        if (loginState?.isSuccess == true) {
            when (selectedEmailType) {
                EmailType.INBOX -> emailViewModel.getInbox(page = 0, size = 20)
                EmailType.SENT -> emailViewModel.getSentMails(page = 0, size = 20)
                EmailType.DRAFT -> emailViewModel.getDraftMails(page = 0, size = 20)
            }
        }
    }
    
    // 监听登出结果
    LaunchedEffect(logoutState) {
        if (logoutState?.isSuccess == true) {
            // 登出成功，导航到登录界面
            navController.navigate(Screen.Login.route) {
                popUpTo(0)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                navController = navController,
                isAdmin = isAdmin,
                onCloseDrawer = { 
                    coroutineScope.launch {
                        drawerState.close()
                    }
                },
                onLogout = { emailViewModel.logout() }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            when (selectedEmailType) {
                                EmailType.INBOX -> "收件箱"
                                EmailType.SENT -> "已发送"
                                EmailType.DRAFT -> "草稿箱"
                            }
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            // 打开抽屉菜单
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "菜单")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate(Screen.ComposeEmail.route)
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "写邮件")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Menu, contentDescription = "收件箱") },
                        label = { Text("收件箱") },
                        selected = selectedEmailType == EmailType.INBOX,
                        onClick = { selectedEmailType = EmailType.INBOX }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Send, contentDescription = "已发送") },
                        label = { Text("已发送") },
                        selected = selectedEmailType == EmailType.SENT,
                        onClick = { selectedEmailType = EmailType.SENT }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Save, contentDescription = "草稿箱") },
                        label = { Text("草稿箱") },
                        selected = selectedEmailType == EmailType.DRAFT,
                        onClick = { selectedEmailType = EmailType.DRAFT }
                    )
                }
            },
            contentColor = MaterialTheme.colorScheme.onSurface,
            containerColor = MaterialTheme.colorScheme.surface
        ) { paddingValues ->
            when {
                isLoading -> {
                    // 加载状态
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                emailListState?.isFailure == true -> {
                    // 错误状态
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "加载失败：${emailListState?.exceptionOrNull()?.message}",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { emailViewModel.getInbox(page = 0, size = 20) }) {
                                Text("重试")
                            }
                        }
                    }
                }
                else -> {
                    // 邮件列表
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(emails) { email ->
                            EmailListItem(
                                email = email,
                                onClick = {
                                    if (email.folder == "DRAFT") {
                                        navController.navigate(Screen.ComposeEmail.createRoute(email.id))
                                    } else {
                                        navController.navigate(Screen.EmailDetail.createRoute(email.id))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmailListItem(email: Email, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = email.from,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatTime(email.sentAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = email.subject,
                fontWeight = if (!email.isRead) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = email.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DrawerContent(
    navController: NavHostController,
    isAdmin: Boolean,
    onCloseDrawer: () -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        // 抽屉头部：显示用户信息
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "用户头像",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "用户",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Divider()
        
        // 普通用户菜单选项
        DrawerItem(
            icon = Icons.Default.Menu,
            label = "收件箱",
            onClick = {
                navController.navigate(Screen.EmailList.route)
                onCloseDrawer()
            }
        )
        
        DrawerItem(
            icon = Icons.Default.Send,
            label = "已发送",
            onClick = {
                navController.navigate(Screen.EmailList.route)
                onCloseDrawer()
            }
        )
        
        DrawerItem(
            icon = Icons.Default.Save,
            label = "草稿箱",
            onClick = {
                navController.navigate(Screen.ComposeEmail.route)
                onCloseDrawer()
            }
        )
        
        DrawerItem(
            icon = Icons.Default.Add,
            label = "写邮件",
            onClick = {
                navController.navigate(Screen.ComposeEmail.route)
                onCloseDrawer()
            }
        )
        
        DrawerItem(
            icon = Icons.Default.Person,
            label = "个人信息",
            onClick = {
                navController.navigate(Screen.UserProfile.route)
                onCloseDrawer()
            }
        )
        
        // 管理员特有的菜单选项
        if (isAdmin) {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                "管理员功能",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            
            DrawerItem(
                icon = Icons.Default.Person,
                label = "用户管理",
                onClick = {
                    navController.navigate(Screen.UserManagement.route)
                    onCloseDrawer()
                }
            )
            
            DrawerItem(
                icon = Icons.Default.Send,
                label = "群发邮件",
                onClick = {
                    navController.navigate(Screen.MassEmail.route)
                    onCloseDrawer()
                }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Divider()
        
        // 登出选项
        DrawerItem(
            icon = Icons.Default.Logout,
            label = "登出",
            onClick = {
                onLogout()
                onCloseDrawer()
            },
            isLogout = true
        )
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector, 
    label: String, 
    onClick: () -> Unit,
    isLogout: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, 
            contentDescription = label,
            tint = if (isLogout) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = if (isLogout) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}