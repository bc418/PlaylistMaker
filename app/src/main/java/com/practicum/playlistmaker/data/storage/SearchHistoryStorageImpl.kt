package com.practicum.playlistmaker.data.storage

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.dto.TrackStorageDto

class SearchHistoryStorageImpl(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : SearchHistoryStorage {

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
        private const val SEARCH_HISTORY_KEY = "search_history"
    }
}
