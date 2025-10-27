package com.project.readingstats

import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import com.project.readingstats.features.bookdetail.ui.components.BookDetailScreen
import com.project.readingstats.features.catalog.domain.model.Book
import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.usecase.*
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class BookDetailScreenInstrumentedTest {

    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose =
        createAndroidComposeRule<HiltTestActivity>()

    // nav usato in entrambi i test
    private lateinit var nav: TestNavHostController

    // ---- BindValue mock use-case (come già li hai) ----
    @BindValue @JvmField val observeBookStatusUseCase: ObserveBookStatusUseCase = mockk()
    @BindValue @JvmField val setBookStatusUseCase: SetBookStatusUseCase = mockk(relaxed = true)
    @BindValue @JvmField val removeBookFromShelfUseCase: RemoveBookFromShelfUseCase = mockk(relaxed = true)
    @BindValue @JvmField val setPageCountUseCase: SetPageCountUseCase = mockk(relaxed = true)
    @BindValue @JvmField val observeUserBookUseCase: ObserveUserBookUseCase = mockk()
    @BindValue @JvmField val upsertStatusBookUseCase: UpsertStatusBookUseCase = mockk(relaxed = true)

    private val volumeId = "vol-123"

    @Before
    fun injectHilt() {
        hiltRule.inject()
        // NIENTE setContent qui.
        // Qui puoi mettere eventuali stub “di default” validi per TUTTI i test,
        // ma per chiarezza li impostiamo dentro ogni test.
    }

    /** Helper: monta il NavHost con BookDetailScreen e naviga a /detail/$volumeId */
    private fun launchDetail(pageCount: Int?) {
        compose.setContent {
            val context = LocalContext.current
            nav = TestNavHostController(context).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(navController = nav, startDestination = "start") {
                composable("start") { Text("start") }
                composable("detail/{volumeId}") {
                    BookDetailScreen(
                        book = testBook(volumeId, pageCount = pageCount),
                        onBack = {}
                    )
                }
            }
        }
        // fai la navigate fuori dal setContent e aspetta che Compose sia idle
        compose.runOnIdle { nav.navigate("detail/$volumeId") }
        compose.waitForIdle()
    }

    @Test
    fun click_same_status_removes_from_shelf() {
        // Stato corrente TO_READ → clic su “Da leggere” = rimozione
        every { observeBookStatusUseCase(volumeId) } returns flowOf(ReadingStatus.TO_READ)
        every { observeUserBookUseCase(volumeId) } returns flowOf(null)

        launchDetail(pageCount = 300)

        compose.onNodeWithContentDescription("Da leggere").performClick()

        coVerify(exactly = 1) { removeBookFromShelfUseCase(volumeId) }
        coVerify(exactly = 0) { setBookStatusUseCase(any(), any(), any()) }
    }

    @Test
    fun click_read_with_known_total_calls_upsertStatus() {
        // Nessuno stato → clic su “Letto” con totale noto = upsert con pagesRead = total
        every { observeBookStatusUseCase(volumeId) } returns flowOf(null)
        every { observeUserBookUseCase(volumeId) } returns flowOf(null)

        launchDetail(pageCount = 250)

        compose.onNodeWithContentDescription("Letto").performClick()

        coVerify {
            upsertStatusBookUseCase(
                userBook = any(),
                payload = any(),
                status = ReadingStatus.READ,
                pageCount = null,   // totale era già noto dal book
                pageInReading = 250 // pagesRead = totale
            )
        }
    }

    // helper per creare un Book compatibile col tuo modello
    private fun testBook(id: String, pageCount: Int?): Book = Book(
        id = id,
        title = "Sample title",
        authors = listOf("Author"),
        publishedDate = null,
        pageCount = pageCount,
        description = "Desc",
        thumbnail = null,
        categories = emptyList(),
        isbn13 = null,
        isbn10 = null
    )
}

