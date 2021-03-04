package com.jamal2367.styx.utils

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewDatabase
import com.jamal2367.styx.database.history.HistoryRepository
import com.jamal2367.styx.utils.Utils.trimCache
import io.reactivex.Scheduler
import java.io.File

/**
 * Copyright 8/4/2015 Anthony Restaino
 */
object WebUtils {
    fun clearCookies() {
        val c = CookieManager.getInstance()
        c.removeAllCookies(null)
    }

    fun clearWebStorage() {
        WebStorage.getInstance().deleteAllData()
    }

    fun clearHistory(
            context: Context,
            historyRepository: HistoryRepository,
            databaseScheduler: Scheduler
    ) {
        historyRepository.deleteHistory()
                .subscribeOn(databaseScheduler)
                .subscribe()
        val webViewDatabase = WebViewDatabase.getInstance(context)
        webViewDatabase.clearFormData()
        webViewDatabase.clearHttpAuthUsernamePassword()
        trimCache(context)
    }

    fun clearCache(view: WebView?, context: Context) {
        if (view == null) return
        view.clearCache(true)
        deleteCache(context)
    }

    private fun deleteCache(context: Context) {
        try {
            val dir = context.cacheDir
            deleteDir(dir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }
}