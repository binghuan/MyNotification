package com.bh.mynotification

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var delayTime: Int = 0
    var content: String = ""
    var title: String = ""
    var selectedBadgeIconType: Int = NotificationCompat.BADGE_ICON_NONE
    var selectedPriority: Int = NotificationCompat.PRIORITY_DEFAULT
    var selectedCategory: String = NotificationCompat.CATEGORY_EVENT
    var selectedImportance: Int = NotificationManager.IMPORTANCE_DEFAULT

    fun savePreferences(context: Context) {
        val sharedPref = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("Content", content)
            putString("Title", title)
            putInt("SelectedPriority", selectedPriority)
            putInt("SelectedImportance", selectedImportance)
            putString("SelectedCategory", selectedCategory)
            putInt("DelayTime", delayTime)
            apply()
        }
    }

    fun loadPreferences(context: Context) {
        val sharedPref = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE) ?: return
        selectedImportance = sharedPref.getInt("SelectedImportance", 0)
        selectedCategory = sharedPref.getString("SelectedCategory", "") ?: ""
        selectedPriority = sharedPref.getInt("SelectedPriority", 0)
        title = sharedPref.getString("Title", "") ?: ""
        content = sharedPref.getString("Content", "") ?: ""
        delayTime = sharedPref.getInt("DelayTime", 0)
        selectedBadgeIconType =
            sharedPref.getInt("SelectedBadgeIconType", NotificationCompat.BADGE_ICON_NONE)
    }
}
