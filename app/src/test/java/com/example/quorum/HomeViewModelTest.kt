package com.example.quorum

import com.example.quorum.data.Apod
import com.example.quorum.data.FakePostRepository
import com.example.quorum.data.NasaApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // Use the FakeRepository instead of mocking Firebase
    private lateinit var fakePostRepository: FakePostRepository
    private val nasaApiService: NasaApiService = mockk(relaxed = true)

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePostRepository = FakePostRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init - posts are loaded successfully`() = runTest {
        // Add some initial data to the fake repository
        fakePostRepository.addPost("Title 1", "Content 1", "Topic 1")

        // Create the ViewModel
        viewModel = HomeViewModel(fakePostRepository, nasaApiService)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.posts.size)
        assertEquals("Title 1", state.posts[0].title)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `init - fetchApod success and updates state`() = runTest {
        val apod = Apod("title", "explanation", "url", "image")
        coEvery { nasaApiService.getAstronomyPictureOfTheDay(any()) } returns apod

        viewModel = HomeViewModel(fakePostRepository, nasaApiService)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(apod, state.apod)
    }

    @Test
    fun `init - fetchApod is not image and does not update state`() = runTest {
        val apod = Apod("title", "explanation", "url", "video")
        coEvery { nasaApiService.getAstronomyPictureOfTheDay(any()) } returns apod

        viewModel = HomeViewModel(fakePostRepository, nasaApiService)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.apod)
    }

    @Test
    fun `addPost - adds a new post to the list`() = runTest {
        viewModel = HomeViewModel(fakePostRepository, nasaApiService)
        advanceUntilIdle() // Wait for init to complete

        // Initial state should be empty
        assertEquals(0, viewModel.uiState.value.posts.size)

        // Add a post
        viewModel.addPost("New Post", "Some content", "New Topic")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.posts.size)
        assertEquals("New Post", state.posts[0].title)
    }

    @Test
    fun `deletePost - removes a post from the list`() = runTest {
        // Add a post to the fake repo first
        fakePostRepository.addPost("To Delete", "Content", "Topic")
        viewModel = HomeViewModel(fakePostRepository, nasaApiService)
        advanceUntilIdle() // Wait for init with the post

        val postToDelete = viewModel.uiState.value.posts[0]
        assertEquals(1, viewModel.uiState.value.posts.size)

        // Delete the post
        viewModel.deletePost(postToDelete.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.posts.isEmpty())
    }

    @Test
    fun `updatePost - updates an existing post`() = runTest {
        fakePostRepository.addPost("Original Title", "Original Content", "Original Topic")
        viewModel = HomeViewModel(fakePostRepository, nasaApiService)
        advanceUntilIdle()

        val postToUpdate = viewModel.uiState.value.posts[0]

        // Update the post
        viewModel.updatePost(postToUpdate.id, "Updated Title", "Updated Content", "Updated Topic")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.posts.size)
        assertEquals("Updated Title", state.posts[0].title)
        assertEquals("Updated Content", state.posts[0].content)
    }
}