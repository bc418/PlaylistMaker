package com.practicum.playlistmaker.data.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.dto.TrackStorageDto

class SearchHistoryStorageImpl(
    context: Context,
    private val gson: Gson
) : SearchHistoryStorage {

    private val sharedPreferences = context.getSharedPreferences(
        SEARCH_HISTORY_PREFERENCES,
        Context.MODE_PRIVATE
    )

    override fun getHistory(): ArrayList<TrackStorageDto> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null)
            ?: return arrayListOf()

        val type = object : TypeToken<ArrayList<TrackStorageDto>>() {}.type
        return gson.fromJson<ArrayList<TrackStorageDto>>(json, type) ?: arrayListOf()
    }

    override fun saveHistory(history: ArrayList<TrackStorageDto>) {
        sharedPreferences.edit()
            .putString(SEARCH_HISTORY_KEY, gson.toJson(history))
            .apply()
    }

    override fun clearHistory() {
        sharedPreferences.edit()
            .remove(SEARCH_HISTORY_KEY)
            .apply()
    }

    companion object {
        private const val SEARCH_HISTORY_PREFERENCES = "search_history_preferences"
        private const val SEARCH_HISTORY_KEY = "search_history"
    }
}
