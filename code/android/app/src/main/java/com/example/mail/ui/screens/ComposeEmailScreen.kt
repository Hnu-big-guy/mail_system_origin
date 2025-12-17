package com.example.mail.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mail.viewmodel.EmailViewModel
import com.example.mail.data.model.EmailRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeEmailScreen(navController: NavHostController, draftEmailId: Long?) {
    val emailViewModel: EmailViewModel = viewModel()
    val sendEmailState by emailViewModel.sendEmailState.collectAsState()
    val saveDraftState by emailViewModel.saveDraftState.collectAsState()
    val currentDraft by emailViewModel.currentDraft.collectAsState()
    
    var to by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var attachments by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // 根据draftEmailId加载草稿
    LaunchedEffect(draftEmailId) {
        if (draftEmailId != null) {
            emailViewModel.loadDraftById(draftEmailId)
        } else {
            emailViewModel.loadLatestDraft()
        }
    }
    
    // 监听草稿加载结果，填充表单
    LaunchedEffect(currentDraft) {
        currentDraft?.onSuccess { draft ->
            to = draft.to.orEmpty()
            subject = draft.subject.orEmpty()
            content = draft.content.orEmpty()
        }
    }

    // 监听发送邮件结果
    LaunchedEffect(sendEmailState) {
        if (sendEmailState?.isSuccess == true) {
            // 发送成功，返回邮件列表
            navController.popBackStack()
        } else if (sendEmailState?.isFailure == true) {
            isLoading = false
        }
    }
    
    // 监听保存草稿结果
    LaunchedEffect(saveDraftState) {
        if (saveDraftState?.isSuccess == true) {
            isLoading = false
            // 可以添加一个Toast提示用户保存成功
            // Toast.makeText(context, "草稿保存成功", Toast.LENGTH_SHORT).show()
        } else if (saveDraftState?.isFailure == true) {
            isLoading = false
            // 可以添加一个Toast提示用户保存失败
            // Toast.makeText(context, "草稿保存失败: ${saveDraftState.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("写邮件") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                IconButton(onClick = { 
                    // 附件功能：目前暂未实现
                    // 未来可以添加文件选择器来选择附件
                    // Toast.makeText(context, "附件功能暂未实现", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.InsertDriveFile, contentDescription = "添加附件")
                }
                IconButton(
                    onClick = {
                        isLoading = true
                        val emailRequest = EmailRequest(
                            subject = subject,
                            from = "user@example.com",  // 这里可以根据实际情况获取当前用户邮箱
                            to = to,
                            content = content,
                            attachments = if (attachments.isNotEmpty()) attachments else null
                        )
                        emailViewModel.saveDraft(emailRequest)
                    },
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Save, contentDescription = "保存")
                }
                IconButton(
                            onClick = {
                                isLoading = true
                                val emailRequest = EmailRequest(
                                    subject = subject,
                                    from = "user@example.com",  // 这里可以根据实际情况获取当前用户邮箱
                                    to = to,
                                    content = content,
                                    attachments = if (attachments.isNotEmpty()) attachments else null,
                                    draftId = draftEmailId
                                )
                                emailViewModel.sendEmail(emailRequest)
                            },
                            enabled = to.isNotEmpty() && content.isNotEmpty() && !isLoading
                        ) {
                    Icon(Icons.Default.Send, contentDescription = "发送")
                }
            }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = to,
                onValueChange = { to = it },
                label = { Text("收件人") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
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
                minLines = 10,
                maxLines = Int.MAX_VALUE
            )

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            // 显示发送失败信息
            if (sendEmailState?.isFailure == true) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "发送失败：${sendEmailState?.exceptionOrNull()?.message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}