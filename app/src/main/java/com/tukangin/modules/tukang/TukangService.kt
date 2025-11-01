package com.tukangin.modules.tukang

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TukangService @Inject constructor() {

    fun sortByRatingDescending(tukangList: List<TukangModel>): List<TukangModel> =
        tukangList.sortedByDescending { it.rating }

    fun filterByCategory(tukangList: List<TukangModel>, category: String): List<TukangModel> =
        tukangList.filter { it.serviceCategory.equals(category, ignoreCase = true) }

    fun filterAvailable(tukangList: List<TukangModel>): List<TukangModel> =
        tukangList.filter { it.available }
}

