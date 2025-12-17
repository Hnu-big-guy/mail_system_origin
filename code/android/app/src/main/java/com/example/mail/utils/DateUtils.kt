package com.example.mail.utils

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * 格式化ISO日期字符串为相对时间
 * @param dateString ISO格式的日期字符串（例如：2025-12-15T11:08:47）
 * @return 格式化后的相对时间字符串
 */
fun formatTime(dateString: String): String {
    try {
        // 解析ISO日期字符串
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val instant = Instant.from(formatter.parse(dateString))
        val timestamp = instant.toEpochMilli()
        
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - timestamp
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
        
        return when {
            hours > 0 -> "${hours}小时前"
            minutes > 0 -> "${minutes}分钟前"
            else -> "刚刚"
        }
    } catch (e: Exception) {
        // 如果解析失败，直接返回原字符串
        return dateString
    }
}