package com.project.readingstats.features.home

import androidx.lifecycle.ViewModel
import com.project.readingstats.features.home.domain.usecase.SetBookTimerUseCase
import com.project.readingstats.features.home.domain.usecase.StartBookTimerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val setBookTimerUseCase: SetBookTimerUseCase,
    private val startBookTimerUseCase: StartBookTimerUseCase
): ViewModel() {

}