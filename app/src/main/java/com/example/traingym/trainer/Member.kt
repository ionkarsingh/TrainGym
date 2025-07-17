package com.example.traingym.trainer

data class Member(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val batch_start_time: String = "",
    val batch_end_time: String = "",
    val user_type: String = "normal_user",
    val is_suspended: Boolean = false
)