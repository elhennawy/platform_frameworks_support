/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.navigation

import android.content.Context
import android.support.annotation.IdRes
import androidx.test.filters.SmallTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@RunWith(JUnit4::class)
@SmallTest
class NavDestinationTest {

    companion object {
        @IdRes
        private const val INVALID_ACTION_ID = 0
        @IdRes
        private const val ACTION_ID = 1
        @IdRes
        private const val DESTINATION_ID = 1
    }

    @Test
    fun parseClassFromNameAbsolute() {
        val context = mock(Context::class.java)
        val clazz = NavDestination.parseClassFromName(context,
                "java.lang.String", Any::class.java)
        assertNotNull(clazz)
        assertEquals(String::class.java.name, clazz.name)
    }

    @Test
    fun parseClassFromNameAbsoluteInvalid() {
        val context = mock(Context::class.java)
        try {
            NavDestination.parseClassFromName(context,
                    "definitely.not.found", Any::class.java)
            fail("Invalid type should cause an IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    @Test
    fun parseClassFromNameAbsoluteWithType() {
        val context = mock(Context::class.java)
        val clazz = NavDestination.parseClassFromName(context,
                "java.lang.String", String::class.java)
        assertNotNull(clazz)
        assertEquals(String::class.java.name, clazz.name)
    }

    @Test
    fun parseClassFromNameAbsoluteWithIncorrectType() {
        val context = mock(Context::class.java)
        try {
            NavDestination.parseClassFromName(context,
                    "java.lang.String", List::class.java)
            fail("Incorrect type should cause an IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    @Test
    fun parseClassFromNameRelative() {
        val context = mock(Context::class.java)
        `when`(context.packageName).thenReturn("java.lang")
        val clazz = NavDestination.parseClassFromName(context,
                ".String", Any::class.java)
        assertNotNull(clazz)
        assertEquals(String::class.java.name, clazz.name)
    }

    @Test
    fun parseClassFromNameRelativeInvalid() {
        val context = mock(Context::class.java)
        `when`(context.packageName).thenReturn("java.lang")
        try {
            NavDestination.parseClassFromName(context,
                    ".definitely.not.found", Any::class.java)
            fail("Invalid type should cause an IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    @Test
    fun parseClassFromNameRelativeWithType() {
        val context = mock(Context::class.java)
        `when`(context.packageName).thenReturn("java.lang")
        val clazz = NavDestination.parseClassFromName(context,
                ".String", String::class.java)
        assertNotNull(clazz)
        assertEquals(String::class.java.name, clazz.name)
    }

    @Test
    fun parseClassFromNameRelativeWithIncorrectType() {
        val context = mock(Context::class.java)
        `when`(context.packageName).thenReturn("java.lang")
        try {
            NavDestination.parseClassFromName(context,
                    ".String", List::class.java)
            fail("Incorrect type should cause an IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    @Test
    fun buildDeepLinkIds() {
        val destination = NavDestination(mock(Navigator::class.java))
        destination.id = DESTINATION_ID
        val parentId = 2
        @Suppress("UNCHECKED_CAST")
        val parent = NavGraph(mock(Navigator::class.java) as Navigator<NavGraph>).apply {
            id = parentId
        }
        destination.parent = parent
        val deepLinkIds = destination.buildDeepLinkIds()
        assertEquals(2, deepLinkIds.size)
        assertEquals(parentId, deepLinkIds[0])
        assertEquals(DESTINATION_ID, deepLinkIds[1])
    }

    @Test
    fun putActionByDestinationId() {
        val destination = NavDestination(mock(Navigator::class.java))
        destination.putAction(ACTION_ID, DESTINATION_ID)

        val action = destination.getAction(ACTION_ID)
        assertNotNull(action)
        assertEquals(DESTINATION_ID, action?.destinationId)
    }

    @Test(expected = IllegalArgumentException::class)
    fun putActionWithInvalidDestinationId() {
        val destination = NavDestination(mock(Navigator::class.java))
        destination.putAction(INVALID_ACTION_ID, DESTINATION_ID)
    }

    @Test
    fun putAction() {
        val destination = NavDestination(mock(Navigator::class.java))
        val action = NavAction(DESTINATION_ID)
        destination.putAction(ACTION_ID, action)

        assertEquals(action, destination.getAction(ACTION_ID))
    }

    @Test
    fun removeAction() {
        val destination = NavDestination(mock(Navigator::class.java))
        val action = NavAction(DESTINATION_ID)
        destination.putAction(ACTION_ID, action)

        assertEquals(action, destination.getAction(ACTION_ID))

        destination.removeAction(ACTION_ID)

        assertNull(destination.getAction(ACTION_ID))
    }
}
