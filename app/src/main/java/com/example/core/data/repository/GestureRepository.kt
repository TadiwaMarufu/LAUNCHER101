package com.example.core.data.repository

import com.example.core.data.store.LauncherDataStore
import com.example.core.model.GestureAction
import kotlinx.coroutines.flow.Flow

class GestureRepository(
    private val dataStore: LauncherDataStore
) {

    val swipeUpAction: Flow<GestureAction> =
        dataStore.swipeUpAction

    val swipeDownAction: Flow<GestureAction> =
        dataStore.swipeDownAction

    val doubleTapAction: Flow<GestureAction> =
        dataStore.doubleTapAction

    suspend fun setSwipeUpAction(action: GestureAction) =
        dataStore.setSwipeUpAction(action)

    suspend fun setSwipeDownAction(action: GestureAction) =
        dataStore.setSwipeDownAction(action)

    suspend fun setDoubleTapAction(action: GestureAction) =
        dataStore.setDoubleTapAction(action)
}
