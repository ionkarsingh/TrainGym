package com.example.traingym.trainer

data class Exercise(
    val exercise_id: String = "",
    val category_id: String = "",
    val name: String = "",
    val sets: String = "",
    val reps: String = "",
    val rest_time: String = "",
    val target_muscle: String = "",
    val instructions: String = "",
    val image_urls: List<String> = emptyList()
)