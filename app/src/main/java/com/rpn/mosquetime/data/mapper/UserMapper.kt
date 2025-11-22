package com.rpn.mosquetime.data.mapper

import com.rpn.mosquetime.domain.model.User

// This is a placeholder DTO. You'll need to define your actual DTOs.
data class UserDto(val id: String, val name: String, val email: String)

fun UserDto.toDomain(): User {
    return User(id = id, name = name, email = email)
}

fun User.toDto(): UserDto {
    return UserDto(id = id, name = name, email = email)
}