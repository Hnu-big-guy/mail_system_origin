package com.example.mail.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mail.data.model.UserProfile
import com.example.mail.viewmodel.EmailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(navController: NavHostController) {
    val emailViewModel: EmailViewModel = EmailViewModel.getInstance()
    val userProfileState by emailViewModel.userProfileState.collectAsState()
    val updateUserProfileState by emailViewModel.updateUserProfileState.collectAsState()
    val changePasswordState by emailViewModel.changePasswordState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // 表单状态
    var nickname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    
    // 修改密码对话框状态
    val showChangePasswordDialog = remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // 加载状态
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    
    // 初始加载用户信息
    LaunchedEffect(Unit) {
        emailViewModel.getUserProfile()
    }
    
    // 当用户信息加载完成后，填充表单
    LaunchedEffect(userProfileState) {
        userProfileState?.onSuccess { profile ->
            nickname = profile.nickname ?: ""
            phone = profile.phone ?: ""
            username = profile.username
            email = profile.email
            role = profile.role
            isLoading = false
        }
        
        userProfileState?.onFailure {
            errorMessage = "加载用户信息失败: ${it.message}"
            isLoading = false
        }
    }
    
    // 监听更新结果
    LaunchedEffect(updateUserProfileState) {
        updateUserProfileState?.onSuccess { 
            successMessage = "个人信息更新成功"
            isSubmitting = false
            // 清除成功消息
            launch {
                kotlinx.coroutines.delay(2000)
                successMessage = ""
            }
        }
        
        updateUserProfileState?.onFailure { 
            errorMessage = "更新个人信息失败: ${it.message}"
            isSubmitting = false
        }
    }
    
    // 监听修改密码结果
    LaunchedEffect(changePasswordState) {
        changePasswordState?.onSuccess { 
            successMessage = "密码修改成功"
            isChangingPassword = false
            showChangePasswordDialog.value = false
            oldPassword = ""
            newPassword = ""
            confirmPassword = ""
            // 清除成功消息
            launch {
                kotlinx.coroutines.delay(2000)
                successMessage = ""
            }
        }
        
        changePasswordState?.onFailure { 
            errorMessage = "修改密码失败: ${it.message}"
            isChangingPassword = false
        }
    }
    
    // 修改密码对话框
    if (showChangePasswordDialog.value) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog.value = false },
            title = { Text(text = "修改密码") },
            text = {
                Column {
                    // 旧密码
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("旧密码") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 新密码
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("新密码") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        supportingText = { 
                            if (newPassword.isNotEmpty() && (newPassword.length < 6 || newPassword.length > 120)) {
                                Text("密码长度必须在6-120之间", color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("密码长度必须在6-120之间")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 确认密码
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("确认新密码") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        supportingText = { 
                            if (confirmPassword.isNotEmpty() && (confirmPassword.length < 6 || confirmPassword.length > 120)) {
                                Text("密码长度必须在6-120之间", color = MaterialTheme.colorScheme.error)
                            } else if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
                                Text("两次输入的密码不一致", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        errorMessage = ""
                        
                        // 验证密码
                        if (newPassword != confirmPassword) {
                            errorMessage = "两次输入的密码不一致"
                            return@Button
                        }
                        
                        // 验证密码长度
                        if (newPassword.length < 6 || newPassword.length > 120) {
                            errorMessage = "密码长度必须在6-120之间"
                            return@Button
                        }
                        
                        isChangingPassword = true
                        emailViewModel.changePassword(oldPassword, newPassword)
                    },
                    enabled = !isChangingPassword && oldPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword == confirmPassword && newPassword.length >= 6 && newPassword.length <= 120
                ) {
                    if (isChangingPassword) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("确认修改")
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showChangePasswordDialog.value = false
                        oldPassword = ""
                        newPassword = ""
                        confirmPassword = ""
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
                title = { Text("个人信息") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (isLoading) {
                // 加载状态
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("加载中...")
                }
            } else {
                // 表单界面
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 成功消息
                    if (successMessage.isNotEmpty()) {
                        Snackbar(
                            modifier = Modifier.padding(bottom = 8.dp),
                            action = {},
                            content = { Text(successMessage) }
                        )
                    }
                    
                    // 错误消息
                    if (errorMessage.isNotEmpty()) {
                        Snackbar(
                            modifier = Modifier.padding(bottom = 8.dp),
                            action = {},
                            content = { Text(errorMessage) },
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    // 用户信息表单
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // 用户名（只读）
                            ProfileField(
                                label = "用户名",
                                value = username,
                                isReadOnly = true
                            )
                            
                            // 邮箱（只读）
                            ProfileField(
                                label = "邮箱",
                                value = email,
                                isReadOnly = true
                            )
                            
                            // 角色（只读）
                            ProfileField(
                                label = "角色",
                                value = role,
                                isReadOnly = true
                            )
                            
                            // 昵称（可编辑）
                            ProfileField(
                                label = "昵称",
                                value = nickname,
                                onValueChange = { nickname = it },
                                isReadOnly = false
                            )
                            
                            // 电话（可编辑）
                            ProfileField(
                                label = "电话",
                                value = phone,
                                onValueChange = { phone = it },
                                isReadOnly = false
                            )
                            
                            // 保存按钮
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp),
                                onClick = { 
                                    isSubmitting = true
                                    errorMessage = ""
                                    successMessage = ""
                                    
                                    // 调用更新用户信息方法，直接传递nickname和phone参数
                                    emailViewModel.updateUserProfile(nickname, phone)
                                },
                                enabled = !isSubmitting
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("保存修改")
                                }
                            }
                            
                            // 修改密码按钮
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                onClick = { 
                                    showChangePasswordDialog.value = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("修改密码")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit = {},
    isReadOnly: Boolean
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        if (isReadOnly) {
            TextField(
                value = value,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
        } else {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
