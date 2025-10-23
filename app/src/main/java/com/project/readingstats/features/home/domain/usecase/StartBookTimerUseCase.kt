package com.project.readingstats.features.home.domain.usecase

import com.project.readingstats.features.home.domain.repository.HomeRepository
import javax.inject.Inject

class StartBookTimerUseCase @Inject constructor(
    private val homeRepository: HomeRepository
){
    operator fun invoke(): Long = System.currentTimeMillis()
}