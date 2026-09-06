package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.engine.ProfileEngine
import com.example.core.model.AppCategory
import com.example.core.model.LauncherProfile
import com.example.core.search.SearchEngine
import com.example.core.search.SearchResultItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Purple Launcher", appName)
    }

    @Test
    fun `verify all five profiles are registered`() {
        val profiles = LauncherProfile.values()
        assertEquals(5, profiles.size)
        assertTrue(profiles.contains(LauncherProfile.FLUID))
        assertTrue(profiles.contains(LauncherProfile.PREMIUM))
        assertTrue(profiles.contains(LauncherProfile.CALM))
        assertTrue(profiles.contains(LauncherProfile.FOCUS))
        assertTrue(profiles.contains(LauncherProfile.EXPRESSIVE))
    }

    @Test
    fun `verify profile engine provides valid configurations`() {
        for (profile in LauncherProfile.values()) {
            val config = ProfileEngine.getConfig(profile)
            assertNotNull(config)
            assertEquals(profile, config.profile)
            assertTrue(config.gridColumns > 0)
        }
    }

    @Test
    fun `verify search engine command evaluation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val searchEngine = SearchEngine(context)

        // Math calculation command
        val mathResults = searchEngine.executeSearch("calc 40 + 2")
        assertTrue(mathResults.any { it is SearchResultItem.CommandResult && it.title.contains("42") })

        // Personality command
        val focusResults = searchEngine.executeSearch("focus")
        assertTrue(focusResults.any { it is SearchResultItem.CommandResult && it.title.contains("Focus") })
    }

    @Test
    fun `verify all app categories are defined`() {
        val categories = AppCategory.values()
        assertTrue(categories.contains(AppCategory.ALL))
        assertTrue(categories.contains(AppCategory.COMMUNICATION))
        assertTrue(categories.contains(AppCategory.SOCIAL))
        assertTrue(categories.contains(AppCategory.MEDIA))
        assertTrue(categories.contains(AppCategory.WORK))
        assertTrue(categories.contains(AppCategory.GAMES))
        assertTrue(categories.contains(AppCategory.TOOLS))
    }
}
