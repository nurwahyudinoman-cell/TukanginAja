package com.tukanginAja.solusi.domain.usecase.auth

import com.tukanginAja.solusi.data.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) = 
        authRepository.login(email, password)
}

