package com.example.mail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mail.data.model.*
import com.example.mail.data.repository.EmailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmailViewModel : ViewModel() {
    // 单例实例
    companion object {
        @Volatile
        private var INSTANCE: EmailViewModel? = null

        fun getInstance(): EmailViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EmailViewModel().also { INSTANCE = it }
            }
        }
    }
    private val repository = EmailRepository()

    // 登录状态
    private val _loginState = MutableStateFlow<Result<LoginResponse>?>(null)
    val loginState: StateFlow<Result<LoginResponse>?> = _loginState.asStateFlow()
    
    // 注册状态
    private val _registerState = MutableStateFlow<Result<LoginResponse>?>(null)
    val registerState: StateFlow<Result<LoginResponse>?> = _registerState.asStateFlow()

    // 登出状态
    private val _logoutState = MutableStateFlow<Result<BaseResponse<Unit>>?>(null)
    val logoutState: StateFlow<Result<BaseResponse<Unit>>?> = _logoutState.asStateFlow()
    
    // 重置登出状态
    fun resetLogoutState() {
        _logoutState.value = null
    }

    // 用户信息状态
    private val _userProfileState = MutableStateFlow<Result<UserProfile>?>(null)
    val userProfileState: StateFlow<Result<UserProfile>?> = _userProfileState.asStateFlow()

    // 邮件列表状态
    private val _emailListState = MutableStateFlow<Result<EmailListResponse>?>(null)
    val emailListState: StateFlow<Result<EmailListResponse>?> = _emailListState.asStateFlow()

    // 已发送邮件列表状态
    private val _sentMailsState = MutableStateFlow<Result<EmailListResponse>?>(null)
    val sentMailsState: StateFlow<Result<EmailListResponse>?> = _sentMailsState.asStateFlow()

    // 草稿箱邮件列表状态
    private val _draftMailsState = MutableStateFlow<Result<EmailListResponse>?>(null)
    val draftMailsState: StateFlow<Result<EmailListResponse>?> = _draftMailsState.asStateFlow()

    // 发送邮件状态
    private val _sendEmailState = MutableStateFlow<Result<BaseResponse<Unit>>?>(null)
    val sendEmailState: StateFlow<Result<BaseResponse<Unit>>?> = _sendEmailState.asStateFlow()

    // 邮件详情状态
    private val _emailDetailState = MutableStateFlow<Result<EmailDetail>?>(null)
    val emailDetailState: StateFlow<Result<EmailDetail>?> = _emailDetailState.asStateFlow()

    // 标记邮件为已读状态
    private val _markAsReadState = MutableStateFlow<Result<EmailDetail>?>(null)
    val markAsReadState: StateFlow<Result<EmailDetail>?> = _markAsReadState.asStateFlow()

    // 移动邮件状态
    private val _moveToFolderState = MutableStateFlow<Result<BaseResponse<Unit>>?>(null)
    val moveToFolderState: StateFlow<Result<BaseResponse<Unit>>?> = _moveToFolderState.asStateFlow()
    
    // 管理员功能：用户列表状态
    private val _userListState = MutableStateFlow<Result<List<User>>?>(null)
    val userListState: StateFlow<Result<List<User>>?> = _userListState.asStateFlow()
    
    // 管理员功能：删除用户状态
    private val _deleteUserState = MutableStateFlow<Result<BaseResponse<Unit>>?>(null)
    val deleteUserState: StateFlow<Result<BaseResponse<Unit>>?> = _deleteUserState.asStateFlow()
    
    // 管理员功能：群发邮件状态
    private val _broadcastEmailState = MutableStateFlow<Result<BaseResponse<Unit>>?>(null)
    val broadcastEmailState: StateFlow<Result<BaseResponse<Unit>>?> = _broadcastEmailState.asStateFlow()
    
    // 修改密码状态
    private val _changePasswordState = MutableStateFlow<Result<BaseResponse<Unit>>?>(null)
    val changePasswordState: StateFlow<Result<BaseResponse<Unit>>?> = _changePasswordState.asStateFlow()
    
    // 重置密码状态
    private val _resetPasswordState = MutableStateFlow<Result<BaseResponse<Unit>>?>(null)
    val resetPasswordState: StateFlow<Result<BaseResponse<Unit>>?> = _resetPasswordState.asStateFlow()
    
    // 更新个人信息状态
    private val _updateUserProfileState = MutableStateFlow<Result<BaseResponse<Unit>>?>(null)
    val updateUserProfileState: StateFlow<Result<BaseResponse<Unit>>?> = _updateUserProfileState.asStateFlow()
    
    // 保存草稿状态
    private val _saveDraftState = MutableStateFlow<Result<BaseResponse<Unit>>?>(null)
    val saveDraftState: StateFlow<Result<BaseResponse<Unit>>?> = _saveDraftState.asStateFlow()
    
    // 当前草稿状态
    private val _currentDraft = MutableStateFlow<Result<EmailDetail>?>(null)
    val currentDraft: StateFlow<Result<EmailDetail>?> = _currentDraft.asStateFlow()

    // 登录
    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val loginRequest = LoginRequest(username, password)
                val response = repository.login(loginRequest)
                if (response.isSuccessful) {
                    println("登录响应成功: ${response.body()}")
                    response.body()?.let {
                        _loginState.value = Result.success(it)
                        // 保存JWT token
                        com.example.mail.data.network.ApiClient.setJwtToken(it.token)
                        println("Token保存成功: ${it.token}")
                    } ?: run {
                        println("登录响应body为空")
                        _loginState.value = Result.failure(Exception("登录失败: 服务器响应为空"))
                    }
                } else {
                    println("登录响应失败: ${response.message()}, 状态码: ${response.code()}")
                    // 尝试解析错误响应体
                    try {
                        val errorResponse = response.errorBody()?.string()
                        println("错误响应体: $errorResponse")
                        // 解析BaseResponse格式的错误信息
                        errorResponse?.let {
                            val gson = com.google.gson.Gson()
                            val baseResponse = gson.fromJson(it, BaseResponse::class.java)
                            _loginState.value = Result.failure(Exception("登录失败: ${baseResponse.message}"))
                        } ?: run {
                            _loginState.value = Result.failure(Exception("登录失败: ${response.message()}"))
                        }
                    } catch (e: Exception) {
                        println("解析错误响应失败: ${e.message}")
                        _loginState.value = Result.failure(Exception("登录失败: ${response.message()}"))
                    }
                }
            } catch (e: Exception) {
                println("登录异常: ${e.message}")
                e.printStackTrace()
                _loginState.value = Result.failure(e)
            }
        }
    }
    
    // 注册
    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val registerRequest = RegisterRequest(username, email, password)
                val response = repository.register(registerRequest)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _registerState.value = Result.success(it)
                        // 保存JWT token
                        com.example.mail.data.network.ApiClient.setJwtToken(it.token)
                    }
                } else {
                    _registerState.value = Result.failure(Exception("注册失败: ${response.message()}"))
                }
            } catch (e: Exception) {
                _registerState.value = Result.failure(e)
            }
        }
    }

    // 获取邮件列表
    fun getInbox(page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            try {
                val response = repository.getInbox(page, size)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _emailListState.value = Result.success(it)
                    }
                } else {
                    _emailListState.value = Result.failure(Exception("Failed to get emails"))
                }
            } catch (e: Exception) {
                _emailListState.value = Result.failure(e)
            }
        }
    }

    // 发送邮件
    fun sendEmail(emailRequest: EmailRequest) {
        viewModelScope.launch {
            try {
                val response = repository.sendEmail(emailRequest)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _sendEmailState.value = Result.success(it)
                    }
                } else {
                    _sendEmailState.value = Result.failure(Exception("Failed to send email"))
                }
            } catch (e: Exception) {
                _sendEmailState.value = Result.failure(e)
            }
        }
    }

    // 获取邮件详情
    fun getEmailDetail(id: Long) {
        viewModelScope.launch {
            try {
                val response = repository.getEmailDetail(id)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _emailDetailState.value = Result.success(it)
                    }
                } else {
                    _emailDetailState.value = Result.failure(Exception("Failed to get email detail"))
                }
            } catch (e: Exception) {
                _emailDetailState.value = Result.failure(e)
            }
        }
    }

    // 更新邮件（暂时注释，因为后端没有提供该接口）
    /*
    fun updateEmail(id: Long, emailRequest: EmailRequest) {
        viewModelScope.launch {
            try {
                // 后端没有提供updateEmail接口，暂时不实现
            } catch (e: Exception) {
                // 处理异常
            }
        }
    }
    */

    // 删除邮件
    fun deleteEmail(id: Long) {
        viewModelScope.launch {
            try {
                val response = repository.deleteEmail(id)
                if (response.isSuccessful) {
                    // 更新邮件列表
                    getInbox()
                } else {
                    // 处理错误
                }
            } catch (e: Exception) {
                // 处理异常
            }
        }
    }
    
    // 登出
    fun logout() {
        viewModelScope.launch {
            try {
                val response = repository.logout()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _logoutState.value = Result.success(it)
                    }
                } else {
                    _logoutState.value = Result.failure(Exception("登出失败: ${response.message()}"))
                }
            } catch (e: Exception) {
                _logoutState.value = Result.failure(e)
            } finally {
                // 清除JWT token
                com.example.mail.data.network.ApiClient.clearJwtToken()
                // 重置登录状态和相关状态
                _loginState.value = null
                _userProfileState.value = null
                _emailListState.value = null
                _sentMailsState.value = null
                _draftMailsState.value = null
            }
        }
    }
    
    // 获取用户信息
    fun getUserProfile() {
        viewModelScope.launch {
            try {
                val response = repository.getUserProfile()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _userProfileState.value = Result.success(it)
                    }
                } else {
                    _userProfileState.value = Result.failure(Exception("获取用户信息失败: ${response.message()}"))
                }
            } catch (e: Exception) {
                _userProfileState.value = Result.failure(e)
            }
        }
    }
    
    // 获取已发送邮件列表
    fun getSentMails(page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            try {
                val response = repository.getSentMails(page, size)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _sentMailsState.value = Result.success(it)
                    }
                } else {
                    _sentMailsState.value = Result.failure(Exception("Failed to get sent emails"))
                }
            } catch (e: Exception) {
                _sentMailsState.value = Result.failure(e)
            }
        }
    }
    
    // 获取草稿箱邮件列表
    fun getDraftMails(page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            try {
                val response = repository.getDraftMails(page, size)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _draftMailsState.value = Result.success(it)
                    }
                } else {
                    _draftMailsState.value = Result.failure(Exception("Failed to get draft emails"))
                }
            } catch (e: Exception) {
                _draftMailsState.value = Result.failure(e)
            }
        }
    }
    
    // 标记邮件为已读
    fun markAsRead(id: Long) {
        viewModelScope.launch {
            try {
                val response = repository.markAsRead(id)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _markAsReadState.value = Result.success(it)
                        // 更新邮件详情
                        _emailDetailState.value = Result.success(it)
                        // 更新邮件列表
                        getInbox()
                    }
                } else {
                    _markAsReadState.value = Result.failure(Exception("标记为已读失败: ${response.message()}"))
                }
            } catch (e: Exception) {
                _markAsReadState.value = Result.failure(e)
            }
        }
    }
    
    // 移动邮件到指定文件夹
    fun moveToFolder(id: Long, folder: String) {
        viewModelScope.launch {
            try {
                val response = repository.moveToFolder(id, folder)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _moveToFolderState.value = Result.success(it)
                        // 更新邮件列表
                        getInbox()
                    }
                } else {
                    _moveToFolderState.value = Result.failure(Exception("移动邮件失败: ${response.message()}"))
                }
            } catch (e: Exception) {
                _moveToFolderState.value = Result.failure(e)
            }
        }
    }
    
    // 管理员功能：获取所有用户列表
    fun getAllUsers(page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            try {
                val response = repository.getAllUsers(page, size)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _userListState.value = Result.success(it.content)
                    }
                } else {
                    _userListState.value = Result.failure(Exception("获取用户列表失败: ${response.message()}"))
                }
            } catch (e: Exception) {
                _userListState.value = Result.failure(e)
            }
        }
    }
    
    // 管理员功能：删除用户
    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.deleteUser(userId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _deleteUserState.value = Result.success(it)
                        // 删除成功后重新获取用户列表
                        getAllUsers()
                    }
                } else {
                    _deleteUserState.value = Result.failure(Exception("删除用户失败: ${response.message()}"))
                }
            } catch (e: Exception) {
                _deleteUserState.value = Result.failure(e)
            }
        }
    }
    
    // 管理员功能：群发邮件
    fun broadcastEmail(emailRequest: EmailRequest) {
        viewModelScope.launch {
            try {
                val response = repository.broadcastEmail(emailRequest)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _broadcastEmailState.value = Result.success(it)
                    }
                } else {
                    _broadcastEmailState.value = Result.failure(Exception("群发邮件失败: ${response.message()}"))
                }
            } catch (e: Exception) {
                _broadcastEmailState.value = Result.failure(e)
            }
        }
    }
    
    // 修改自己的密码
    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val request = ChangePasswordRequest(oldPassword, newPassword)
                val response = repository.changePassword(request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _changePasswordState.value = Result.success(it)
                    }
                } else {
                    _changePasswordState.value = Result.failure(Exception("修改密码失败: ${response.message()}"))
                }
            } catch (e: Exception) {
                _changePasswordState.value = Result.failure(e)
            }
        }
    }
    
    // 管理员功能：重置用户密码
    fun resetPassword(userId: Long, newPassword: String) {
        viewModelScope.launch {
            try {
                val request = ResetPasswordRequest(newPassword)
                val response = repository.resetPassword(userId, request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _resetPasswordState.value = Result.success(it)
                        // 重置成功后重新获取用户列表
                        getAllUsers()
                    }
                } else {
                    _resetPasswordState.value = Result.failure(Exception("重置密码失败: ${response.message()}"))
                }
            } catch (e: Exception) {
                _resetPasswordState.value = Result.failure(e)
            }
        }
    }
    
    // 更新个人信息
    fun updateUserProfile(nickname: String, phone: String) {
        viewModelScope.launch {
            try {
                val updateRequest = UpdateProfileRequest(nickname, phone)
                val response = repository.updateUserProfile(updateRequest)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _updateUserProfileState.value = Result.success(it)
                        // 更新本地用户信息
                        _userProfileState.value?.onSuccess { currentProfile ->
                            val updatedProfile = currentProfile.copy(
                                nickname = nickname,
                                phone = phone
                            )
                            _userProfileState.value = Result.success(updatedProfile)
                        }
                    }
                } else {
                    _updateUserProfileState.value = Result.failure(Exception("更新个人信息失败: ${response.message()}"))
                }
            } catch (e: Exception) {
                _updateUserProfileState.value = Result.failure(e)
            }
        }
    }
    
    // 保存草稿（无附件）
    fun saveDraft(emailRequest: EmailRequest) {
        viewModelScope.launch {
            try {
                val response = repository.saveDraft(emailRequest)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _saveDraftState.value = Result.success(it)
                    }
                } else {
                    _saveDraftState.value = Result.failure(Exception("保存草稿失败: ${response.message()}"))
                }
            } catch (e: Exception) {
                _saveDraftState.value = Result.failure(e)
            }
        }
    }
    
    // 保存草稿（有附件）
    fun saveDraftWithAttachments(emailRequest: EmailRequest, attachments: List<okhttp3.MultipartBody.Part>? = null) {
        viewModelScope.launch {
            try {
                val response = repository.saveDraftWithAttachments(emailRequest, attachments)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _saveDraftState.value = Result.success(it)
                    }
                } else {
                    _saveDraftState.value = Result.failure(Exception("保存草稿失败: ${response.message()}"))
                }
            } catch (e: Exception) {
                _saveDraftState.value = Result.failure(e)
            }
        }
    }
    
    // 加载最新的草稿
    fun loadLatestDraft() {
        viewModelScope.launch {
            try {
                // 获取草稿列表
                val draftListResponse = repository.getDraftMails(0, 10) // 获取前10个草稿
                if (draftListResponse.isSuccessful) {
                    val draftList = draftListResponse.body()?.content
                    if (!draftList.isNullOrEmpty()) {
                        // 找到最新的草稿（按发送时间降序排列）
                        val latestDraft = draftList.maxByOrNull { it.sentAt ?: "" } ?: draftList[0]
                        // 获取草稿详情
                        val draftDetailResponse = repository.getEmailDetail(latestDraft.id)
                        if (draftDetailResponse.isSuccessful) {
                            draftDetailResponse.body()?.let {
                                _currentDraft.value = Result.success(it)
                            }
                        } else {
                            _currentDraft.value = Result.failure(Exception("获取草稿详情失败: ${draftDetailResponse.message()}"))
                        }
                    }
                } else {
                    _currentDraft.value = Result.failure(Exception("获取草稿列表失败: ${draftListResponse.message()}"))
                }
            } catch (e: Exception) {
                _currentDraft.value = Result.failure(e)
            }
        }
    }
    
    // 根据ID加载草稿
    fun loadDraftById(draftId: Long) {
        viewModelScope.launch {
            try {
                val draftDetailResponse = repository.getEmailDetail(draftId)
                if (draftDetailResponse.isSuccessful) {
                    draftDetailResponse.body()?.let {
                        _currentDraft.value = Result.success(it)
                    }
                } else {
                    _currentDraft.value = Result.failure(Exception("获取草稿详情失败: ${draftDetailResponse.message()}"))
                }
            } catch (e: Exception) {
                _currentDraft.value = Result.failure(e)
            }
        }
    }
}