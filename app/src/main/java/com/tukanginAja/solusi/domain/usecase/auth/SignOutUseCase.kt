package com.tukanginAja.solusi.domain.usecase.auth

import com.tukanginAja.solusi.data.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.signOut()
}

