package com.example.traingym.admin

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Trainer(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    @ServerTimestamp
    val addedDate: Date? = null,
    @get:PropertyName("_suspended") @set:PropertyName("_suspended")
    var isSuspended: Boolean = false
)