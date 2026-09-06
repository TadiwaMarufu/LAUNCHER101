package com.example.core.data.repository

import com.example.core.data.store.LauncherDataStore
import com.example.core.model.HomeItem
import com.example.core.model.HomeItemType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class HomeLayoutRepository(
    private val dataStore: LauncherDataStore
) {
    val items: Flow<List<HomeItem>> = dataStore.homeItems

    suspend fun setItems(items: List<HomeItem>) {
        dataStore.setHomeItems(normalize(items))
    }

    suspend fun addItem(item: HomeItem) {
        val current = items.first()

        if (current.any { it.id == item.id }) {
            return
        }

        setItems(current + item)
    }

    suspend fun removeItem(itemId: String) {
        val current = items.first()
        setItems(current.filterNot { it.id == itemId })
    }

    suspend fun updateItem(item: HomeItem) {
        val current = items.first()

        setItems(
            current.map {
                if (it.id == item.id) item else it
            }
        )
    }

    suspend fun moveItem(
        itemId: String,
        page: Int,
        x: Int,
        y: Int
    ) {
        val current = items.first()

        setItems(
            current.map { item ->
                if (item.id == itemId) {
                    item.copy(
                        page = page.coerceAtLeast(0),
                        x = x.coerceAtLeast(0),
                        y = y.coerceAtLeast(0)
                    )
                } else {
                    item
                }
            }
        )
    }

    suspend fun clearPage(page: Int) {
        val current = items.first()
        setItems(current.filterNot { it.page == page })
    }

    suspend fun ensureDefaultCanvas() {
        val current = items.first()

        if (dataStore.homeInitialized.first()) {
            return
        }

        if (current.isEmpty()) {
            setItems(
                listOf(
                    HomeItem(
                        id = "clock-default",
                        type = HomeItemType.CLOCK,
                        page = 0,
                        x = 2,
                        y = 1,
                        width = 2,
                        height = 1
                    ),
                    HomeItem(
                        id = "nowbar-default",
                        type = HomeItemType.NOW_BAR,
                        page = 0,
                        x = 0,
                        y = 7,
                        width = 5,
                        height = 1
                    )
                )
            )
        } else {
            // Existing persisted items count as an initialized canvas.
            setItems(current)
        }

        dataStore.setHomeInitialized(true)
    }

    fun newItemId(prefix: String): String {
        return "$prefix-${UUID.randomUUID()}"
    }

    private fun normalize(
        items: List<HomeItem>
    ): List<HomeItem> {
        return items
            .distinctBy { it.id }
            .map { item ->
                item.copy(
                    page = item.page.coerceAtLeast(0),
                    x = item.x.coerceAtLeast(0),
                    y = item.y.coerceAtLeast(0),
                    width = item.width.coerceAtLeast(1),
                    height = item.height.coerceAtLeast(1)
                )
            }
    }
}
