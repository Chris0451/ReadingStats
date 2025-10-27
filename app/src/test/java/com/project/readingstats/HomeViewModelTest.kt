@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.project.readingstats

import com.google.common.truth.Truth.assertThat
import com.project.readingstats.features.home.HomeViewModel
import com.project.readingstats.features.home.domain.model.UiHomeBook
import com.project.readingstats.features.home.domain.repository.HomeRepository
import com.project.readingstats.features.home.domain.usecase.SetBookTimerUseCase
import com.project.readingstats.features.home.domain.usecase.StartBookTimerUseCase
import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.model.UserBook
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class HomeViewModelTest {

    private val repo: HomeRepository = mockk(relaxed = true)
    private val startTimer: StartBookTimerUseCase = mockk()
    private val setTimer: SetBookTimerUseCase = mockk(relaxed = true)

    private fun book(
        id: String = "b1",
        title: String = "Title",
        pageCount: Int? = 200,
        pageInReading: Int? = 10,
        totalReadSeconds: Long = 0
    ) = UiHomeBook(
        id = id,
        title = title,
        authors = emptyList(),
        thumbnail = null,
        description = null,
        pageCount = pageCount,
        pageInReading = pageInReading,
        totalReadSeconds = totalReadSeconds,
        isbn13 = null,
        isbn10 = null
    )

    @Test
    fun `onStart avvia la sessione e segna isRunning=true`() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        try {
            val b1 = book("b1")
            val booksFlow = MutableStateFlow(listOf(b1))
            every { repo.observeReadingBooks() } returns booksFlow
            every { startTimer() } returns 123456789L
            coJustRun { setTimer(any(), any(), any()) }

            val vm = HomeViewModel(repo, startTimer, setTimer)
            val collect = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.uiState.collect()
            }

            vm.onStart(b1)
            runCurrent()

            val item = vm.uiState.value.items.single { it.book.id == "b1" }
            assertThat(item.isRunning).isTrue()
            assertThat(item.sessionStartMillis).isEqualTo(123456789L)
            verify(exactly = 1) { startTimer() }

            vm.onStop(b1)
            runCurrent()
            collect.cancel()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `onStart su secondo libro non parte se uno e' gia' in corso`() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        try {
            val b1 = book("b1")
            val b2 = book("b2")
            val booksFlow = MutableStateFlow(listOf(b1, b2))
            every { repo.observeReadingBooks() } returns booksFlow
            every { startTimer() } returns 1000L
            coJustRun { setTimer(any(), any(), any()) }

            val vm = HomeViewModel(repo, startTimer, setTimer)
            val collect = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.uiState.collect()
            }

            vm.onStart(b1)
            vm.onStart(b2) // ignorato
            runCurrent()

            val st = vm.uiState.value
            assertThat(st.items.single { it.book.id == "b1" }.isRunning).isTrue()
            assertThat(st.items.single { it.book.id == "b2" }.isRunning).isFalse()
            verify(exactly = 1) { startTimer() }

            vm.onStop(b1)
            runCurrent()
            collect.cancel()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `onStop ferma la sessione, apre dialog e chiama setTimer`() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        try {
            val b1 = book(id = "b1", pageInReading = 12)
            val booksFlow = MutableStateFlow(listOf(b1))
            every { repo.observeReadingBooks() } returns booksFlow
            every { startTimer() } returns 777L
            coJustRun { setTimer(any(), any(), any()) }

            val vm = HomeViewModel(repo, startTimer, setTimer)
            val collect = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.uiState.collect()
            }

            vm.onStart(b1)
            runCurrent()
            vm.onStop(b1)
            runCurrent()

            val dialog = vm.uiState.value.pagesDialog
            assertThat(dialog).isNotNull()
            assertThat(dialog!!.book.id).isEqualTo("b1")
            assertThat(dialog.currentRead).isEqualTo(12)

            coVerify { setTimer("b1", 777L, any()) }

            collect.cancel()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `confirmPages aggiorna le pagine senza setStatus quando sotto il totale`() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        try {
            val b1 = book(id = "b1", pageCount = 200)
            val booksFlow = MutableStateFlow(listOf(b1))
            every { repo.observeReadingBooks() } returns booksFlow
            every { startTimer() } returns 1L
            coEvery { repo.updatePagesRead("b1", 50) } returns Unit

            val vm = HomeViewModel(repo, startTimer, setTimer)
            val collect = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.uiState.collect()
            }

            vm.onStart(b1); runCurrent()
            vm.onStop(b1);  runCurrent()

            vm.confirmPages(50)
            runCurrent()

            coVerify(exactly = 1) { repo.updatePagesRead("b1", 50) }
            coVerify(exactly = 0) { repo.setStatus(any(), any(), any()) }

            collect.cancel()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `confirmPages al totale setta stato READ e chiude la dialog`() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        try {
            val b1 = book(id = "b1", pageCount = 120)
            val booksFlow = MutableStateFlow(listOf(b1))
            every { repo.observeReadingBooks() } returns booksFlow
            every { startTimer() } returns 2L
            coEvery { repo.updatePagesRead("b1", 120) } returns Unit
            coEvery { repo.setStatus(eq("b1"), eq(ReadingStatus.READ), any()) } returns Unit

            val vm = HomeViewModel(repo, startTimer, setTimer)
            val collect = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.uiState.collect()
            }

            vm.onStart(b1); runCurrent()
            vm.onStop(b1);  runCurrent()

            vm.confirmPages(120)
            runCurrent()

            coVerify { repo.updatePagesRead("b1", 120) }
            coVerify {
                repo.setStatus(
                    "b1",
                    ReadingStatus.READ,
                    withArg<UserBook> {
                        assertThat(it.id).isEqualTo("b1")
                        assertThat(it.status).isEqualTo(ReadingStatus.READ)
                        assertThat(it.pageInReading).isEqualTo(120)
                    }
                )
            }
            assertThat(vm.uiState.value.pagesDialog).isNull()

            collect.cancel()
        } finally {
            Dispatchers.resetMain()
        }
    }
}
