package com.bh.mynotification

import android.content.AsyncQueryHandler
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import java.lang.ref.WeakReference

class NotificationBadge(context: Context) {
    private val contextRef = WeakReference(context)
    private val context: Context?
        get() = contextRef.get()

    var componentName: ComponentName? = null
        private set

    init {
        componentName =
            context.packageManager?.getLaunchIntentForPackage(context.packageName)?.component
    }

    private var badger: Badger? = null
    private var initied = false

    companion object {
        private val BADGERS = listOf(
            AdwHomeBadger::class.java,
            ApexHomeBadger::class.java,
            NewHtcHomeBadger::class.java,
            NovaHomeBadger::class.java,
            SonyHomeBadger::class.java,
            XiaomiHomeBadger::class.java,
            AsusHomeBadger::class.java,
            HuaweiHomeBadger::class.java,
            OPPOHomeBadger::class.java,
            SamsungHomeBadger::class.java,
            ZukHomeBadger::class.java,
            VivoHomeBadger::class.java
        )

        private fun close(cursor: Cursor?) {
            if (cursor?.isClosed == false) {
                cursor.close()
            }
        }
    }

    fun applyCount(badgeCount: Int): Boolean {
        return try {
            if (badger == null && !initied) {
                initBadger()
                initied = true
            }
            badger?.executeBadge(badgeCount) ?: false
        } catch (e: Throwable) {
            Log.e("NotificationBadge", "applyCount error: $e")
            false
        }
    }

    private fun initBadger(): Boolean {
        val context = context ?: return false
        val launchIntent =
            context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?: return false

        componentName = launchIntent.component

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }

        val resolveInfo = context.packageManager.resolveActivity(
            intent, PackageManager.MATCH_DEFAULT_ONLY
        )
        if (resolveInfo != null) {
            val currentHomePackage = resolveInfo.activityInfo.packageName
            badger = findBadgerForPackage(currentHomePackage)
            if (badger != null) {
                return true
            }
        }

        val resolveInfos = context.packageManager.queryIntentActivities(
            intent, PackageManager.MATCH_DEFAULT_ONLY
        )
        resolveInfos.forEach { info ->
            val currentHomePackage = info.activityInfo.packageName
            badger = findBadgerForPackage(currentHomePackage)
            if (badger != null) {
                return true
            }
        }

        badger = when {
            Build.MANUFACTURER.equals(
                "Xiaomi", ignoreCase = true
            ) -> XiaomiHomeBadger()

            Build.MANUFACTURER.equals(
                "ZUK", ignoreCase = true
            ) -> ZukHomeBadger()

            Build.MANUFACTURER.equals(
                "OPPO", ignoreCase = true
            ) -> OPPOHomeBadger()

            Build.MANUFACTURER.equals(
                "VIVO", ignoreCase = true
            ) -> VivoHomeBadger()

            else -> DefaultBadger()
        }

        return true
    }

    private fun findBadgerForPackage(packageName: String): Badger? {
        return BADGERS.firstNotNullOfOrNull { badgerClass ->
            try {
                val badger =
                    badgerClass.getDeclaredConstructor(NotificationBadge::class.java)
                        .newInstance(this)
                if (badger.getSupportLaunchers()
                        .contains(packageName)
                ) badger else null
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun canResolveBroadcast(intent: Intent): Boolean {
        val packageManager = context?.packageManager ?: return false
        val receivers = packageManager.queryBroadcastReceivers(intent, 0)
        return receivers.isNotEmpty()
    }

    interface Badger {
        fun executeBadge(badgeCount: Int): Boolean
        fun getSupportLaunchers(): List<String>
    }

    inner class AdwHomeBadger : Badger {
        val INTENT_UPDATE_COUNTER = "org.adw.launcher.counter.SEND"
        val PACKAGENAME = "PNAME"
        val CLASSNAME = "CNAME"
        val COUNT = "COUNT"

        override fun executeBadge(badgeCount: Int): Boolean {
            val intent = Intent(INTENT_UPDATE_COUNTER).apply {
                putExtra(PACKAGENAME, componentName?.packageName)
                putExtra(CLASSNAME, componentName?.className)
                putExtra(COUNT, badgeCount)
            }
            return canResolveBroadcast(intent)
        }

        override fun getSupportLaunchers() = listOf(
            "org.adw.launcher", "org.adwfreak.launcher"
        )
    }

    inner class ApexHomeBadger : Badger {
        val INTENT_UPDATE_COUNTER = "com.anddoes.launcher.COUNTER_CHANGED"
        val PACKAGENAME = "package"
        val COUNT = "count"
        val CLASS = "class"

        override fun executeBadge(badgeCount: Int): Boolean {
            val intent = Intent(INTENT_UPDATE_COUNTER).apply {
                putExtra(PACKAGENAME, componentName?.packageName)
                putExtra(COUNT, badgeCount)
                putExtra(CLASS, componentName?.className)
            }
            return canResolveBroadcast(intent)
        }

        override fun getSupportLaunchers() = listOf("com.anddoes.launcher")
    }

    inner class AsusHomeBadger : Badger {
        val INTENT_ACTION = "android.intent.action.BADGE_COUNT_UPDATE"
        val INTENT_EXTRA_BADGE_COUNT = "badge_count"
        val INTENT_EXTRA_PACKAGENAME = "badge_count_package_name"
        val INTENT_EXTRA_ACTIVITY_NAME = "badge_count_class_name"

        override fun executeBadge(badgeCount: Int): Boolean {
            val intent = Intent(INTENT_ACTION).apply {
                putExtra(INTENT_EXTRA_BADGE_COUNT, badgeCount)
                putExtra(INTENT_EXTRA_PACKAGENAME, componentName?.packageName)
                putExtra(INTENT_EXTRA_ACTIVITY_NAME, componentName?.className)
                putExtra("badge_vip_count", 0)
            }
            return canResolveBroadcast(intent)
        }

        override fun getSupportLaunchers() = listOf("com.asus.launcher")
    }

    inner class DefaultBadger : Badger {
        val INTENT_ACTION = "android.intent.action.BADGE_COUNT_UPDATE"
        val INTENT_EXTRA_BADGE_COUNT = "badge_count"
        val INTENT_EXTRA_PACKAGENAME = "badge_count_package_name"
        val INTENT_EXTRA_ACTIVITY_NAME = "badge_count_class_name"

        override fun executeBadge(badgeCount: Int): Boolean {
            val intent = Intent(INTENT_ACTION).apply {
                putExtra(INTENT_EXTRA_BADGE_COUNT, badgeCount)
                putExtra(INTENT_EXTRA_PACKAGENAME, componentName?.packageName)
                putExtra(INTENT_EXTRA_ACTIVITY_NAME, componentName?.className)
            }
            try {
                context?.sendBroadcast(intent)
                return true
            } catch (e: Exception) {
                Log.e("DefaultBadger", "executeBadge error: $e")
                return false
            }
        }

        override fun getSupportLaunchers() = listOf(
            "fr.neamar.kiss",
            "com.quaap.launchtime",
            "com.quaap.launchtime_official"
        )
    }

    inner class HuaweiHomeBadger : Badger {
        override fun executeBadge(badgeCount: Int): Boolean {
            val localBundle = Bundle().apply {
                putString("package", context?.packageName)
                putString("class", componentName?.className)
                putInt("badgenumber", badgeCount)
            }
            try {
                context?.contentResolver?.call(
                    "content://com.huawei.android.launcher.settings/badge/".toUri(),
                    "change_badge",
                    null,
                    localBundle
                )
                return true
            } catch (e: Exception) {
                Log.e("HuaweiHomeBadger", "executeBadge error: $e")
                return false
            }
        }

        override fun getSupportLaunchers() =
            listOf("com.huawei.android.launcher")
    }

    inner class NewHtcHomeBadger : Badger {
        val INTENT_UPDATE_SHORTCUT = "com.htc.launcher.action.UPDATE_SHORTCUT"
        val INTENT_SET_NOTIFICATION = "com.htc.launcher.action.SET_NOTIFICATION"
        val PACKAGENAME = "packagename"
        val COUNT = "count"
        val EXTRA_COMPONENT = "com.htc.launcher.extra.COMPONENT"
        val EXTRA_COUNT = "com.htc.launcher.extra.COUNT"

        override fun executeBadge(badgeCount: Int): Boolean {
            val intent1 = Intent(INTENT_SET_NOTIFICATION).apply {
                putExtra(EXTRA_COMPONENT, componentName?.flattenToShortString())
                putExtra(EXTRA_COUNT, badgeCount)
            }

            val intent = Intent(INTENT_UPDATE_SHORTCUT).apply {
                putExtra(PACKAGENAME, componentName?.packageName)
                putExtra(COUNT, badgeCount)
            }

            return canResolveBroadcast(intent1) || canResolveBroadcast(intent)
        }

        override fun getSupportLaunchers() = listOf("com.htc.launcher")
    }

    inner class NovaHomeBadger : Badger {
        val CONTENT_URI = "content://com.teslacoilsw.notifier/unread_count"
        val COUNT = "count"
        val TAG = "tag"

        override fun executeBadge(badgeCount: Int): Boolean {
            val contentValues = ContentValues().apply {
                put(
                    TAG,
                    "${componentName?.packageName}/${componentName?.className}"
                )
                put(COUNT, badgeCount)
            }
            return context?.contentResolver?.insert(CONTENT_URI.toUri(), contentValues) != null
        }

        override fun getSupportLaunchers() = listOf("com.teslacoilsw.launcher")
    }

    inner class OPPOHomeBadger : Badger {
        val PROVIDER_CONTENT_URI = "content://com.android.badge/badge"
        val INTENT_ACTION = "com.oppo.unsettledevent"
        val INTENT_EXTRA_PACKAGENAME = "pakeageName"
        val INTENT_EXTRA_BADGE_COUNT = "number"
        val INTENT_EXTRA_BADGE_UPGRADENUMBER = "upgradeNumber"
        val INTENT_EXTRA_BADGEUPGRADE_COUNT = "app_badge_count"

        private var mCurrentTotalCount = -1

        override fun executeBadge(badgeCount: Int): Boolean {
            if (mCurrentTotalCount == badgeCount) {
                return true
            }
            mCurrentTotalCount = badgeCount
            return executeBadgeByContentProvider(badgeCount)
        }

        override fun getSupportLaunchers() = listOf("com.oppo.launcher")

        private fun executeBadgeByContentProvider(badgeCount: Int): Boolean {
            try {
                val extras = Bundle().apply {
                    putInt(INTENT_EXTRA_BADGEUPGRADE_COUNT, badgeCount)
                }
                return context?.contentResolver?.call(
                    PROVIDER_CONTENT_URI.toUri(),
                    "setAppBadgeCount",
                    null,
                    extras
                ) != null
            } catch (ignored: Throwable) {
                return false
            }
        }
    }

    inner class SamsungHomeBadger : Badger {
        val CONTENT_URI = "content://com.sec.badge/apps?notify=true"
        val CONTENT_PROJECTION = arrayOf("_id", "class")

        private var defaultBadger: DefaultBadger? = null

        override fun executeBadge(badgeCount: Int): Boolean {
            val context = this@NotificationBadge.context ?: run {
                Log.w("SamsungHomeBadger", "executeBadge: context is null")
                return false
            }
            val component = this@NotificationBadge.componentName ?: run {
                Log.w(
                    "SamsungHomeBadger", "executeBadge: componentName is null"
                )
                return false
            }

            Log.i(
                "SamsungHomeBadger",
                "executeBadge: badgeCount=$badgeCount, component=${component.flattenToShortString()}"
            )

            var success = false
            try {
                if (defaultBadger == null) {
                    defaultBadger = DefaultBadger()
                }
                success = defaultBadger?.executeBadge(badgeCount) == true
            } catch (ignore: Exception) {
                Log.w(
                    "SamsungHomeBadger",
                    "executeBadge: defaultBadger failed: $ignore"
                )
            }

            val mUri = CONTENT_URI.toUri()
            val contentResolver = context.contentResolver
            var cursor: Cursor? = null
            try {
                cursor = contentResolver.query(
                    mUri,
                    CONTENT_PROJECTION,
                    "package=?",
                    arrayOf(component.packageName),
                    null
                )

                if (cursor == null) {
                    Log.w(
                        "SamsungHomeBadger",
                        "executeBadge: cursor is null, badge provider not available"
                    )
                    return success
                }

                cursor.use {
                    val entryActivityName = component.className
                    var entryActivityExist = false

                    while (it.moveToNext()) {
                        val id = it.getInt(0)
                        val clazz =
                            it.getStringOrNull(it.getColumnIndex("class"))
                                ?: continue
                        val values =
                            getContentValues(component, badgeCount, false)
                        val updateResult = contentResolver.update(
                            mUri, values, "_id=?", arrayOf(id.toString())
                        )
                        if (clazz == entryActivityName) {
                            entryActivityExist = true
                            success = updateResult > 0
                        }
                    }

                    if (!entryActivityExist) {
                        val insertValues =
                            getContentValues(component, badgeCount, true)
                        val insertResult = contentResolver.insert(mUri, insertValues)
                        success = insertResult != null
                    }
                }
                Log.i(
                    "SamsungHomeBadger",
                    "executeBadge: success=$success, badgeCount=$badgeCount"
                )
                return success
            } catch (e: Exception) {
                Log.e("SamsungHomeBadger", "executeBadge: error: $e")
                return false
            } finally {
                close(cursor)
            }
        }

        private fun getContentValues(
            componentName: ComponentName, badgeCount: Int, isInsert: Boolean
        ): ContentValues {
            return ContentValues().apply {
                if (isInsert) {
                    put("package", componentName.packageName)
                    put("class", componentName.className)
                }
                put("badgecount", badgeCount)
            }
        }

        override fun getSupportLaunchers() = listOf(
            "com.sec.android.app.launcher", "com.sec.android.app.twlauncher"
        )
    }

    inner class SonyHomeBadger : Badger {
        val INTENT_ACTION = "com.sonyericsson.home.action.UPDATE_BADGE"
        val INTENT_EXTRA_PACKAGE_NAME =
            "com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME"
        val INTENT_EXTRA_ACTIVITY_NAME =
            "com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME"
        val INTENT_EXTRA_MESSAGE =
            "com.sonyericsson.home.intent.extra.badge.MESSAGE"
        val INTENT_EXTRA_SHOW_MESSAGE =
            "com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE"

        val PROVIDER_CONTENT_URI =
            "content://com.sonymobile.home.resourceprovider/badge"
        val PROVIDER_COLUMNS_BADGE_COUNT = "badge_count"
        val PROVIDER_COLUMNS_PACKAGE_NAME = "package_name"
        val PROVIDER_COLUMNS_ACTIVITY_NAME = "activity_name"
        val SONY_HOME_PROVIDER_NAME = "com.sonymobile.home.resourceprovider"

        private val BADGE_CONTENT_URI = PROVIDER_CONTENT_URI.toUri()
        private var mQueryHandler: AsyncQueryHandler? = null

        override fun executeBadge(badgeCount: Int): Boolean {
            if (sonyBadgeContentProviderExists()) {
                return executeBadgeByContentProvider(badgeCount)
            } else {
                return executeBadgeByBroadcast(badgeCount)
            }
        }

        override fun getSupportLaunchers() = listOf(
            "com.sonyericsson.home", "com.sonymobile.home"
        )

        private fun executeBadgeByBroadcast(badgeCount: Int): Boolean {
            val intent = Intent(INTENT_ACTION).apply {
                putExtra(INTENT_EXTRA_PACKAGE_NAME, componentName?.packageName)
                putExtra(INTENT_EXTRA_ACTIVITY_NAME, componentName?.className)
                putExtra(INTENT_EXTRA_MESSAGE, badgeCount.toString())
                putExtra(INTENT_EXTRA_SHOW_MESSAGE, badgeCount > 0)
            }
            return canResolveBroadcast(intent)
        }

        private fun executeBadgeByContentProvider(badgeCount: Int): Boolean {
            if (badgeCount < 0) {
                return false
            }

            if (mQueryHandler == null) {
                context?.contentResolver?.let { resolver ->
                    mQueryHandler = object : AsyncQueryHandler(resolver) {
                        override fun handleMessage(msg: Message) {
                            try {
                                super.handleMessage(msg)
                            } catch (ignore: Throwable) {
                            }
                        }
                    }
                }
            }

            componentName?.let {
                return insertBadgeAsync(badgeCount, it.packageName, it.className)
            }
            return false
        }

        private fun insertBadgeAsync(
            badgeCount: Int, packageName: String, activityName: String
        ): Boolean {
            val contentValues = ContentValues().apply {
                put(PROVIDER_COLUMNS_BADGE_COUNT, badgeCount)
                put(PROVIDER_COLUMNS_PACKAGE_NAME, packageName)
                put(PROVIDER_COLUMNS_ACTIVITY_NAME, activityName)
            }
            return mQueryHandler?.startInsert(
                0, null, BADGE_CONTENT_URI, contentValues
            ) != null
        }

        private fun sonyBadgeContentProviderExists(): Boolean {
            val info = context?.packageManager?.resolveContentProvider(
                SONY_HOME_PROVIDER_NAME, 0
            )
            return info != null
        }
    }

    inner class XiaomiHomeBadger : Badger {
        val INTENT_ACTION = "android.intent.action.APPLICATION_MESSAGE_UPDATE"
        val EXTRA_UPDATE_APP_COMPONENT_NAME =
            "android.intent.extra.update_application_component_name"
        val EXTRA_UPDATE_APP_MSG_TEXT =
            "android.intent.extra.update_application_message_text"

        override fun executeBadge(badgeCount: Int): Boolean {
            try {
                val miuiNotificationClass =
                    Class.forName("android.app.MiuiNotification")
                val miuiNotification = miuiNotificationClass.newInstance()
                val field =
                    miuiNotification.javaClass.getDeclaredField("messageCount")
                field.isAccessible = true
                field.set(
                    miuiNotification,
                    if (badgeCount == 0) "" else badgeCount.toString()
                )
                return true
            } catch (e: Throwable) {
                val localIntent = Intent(INTENT_ACTION).apply {
                    putExtra(
                        EXTRA_UPDATE_APP_COMPONENT_NAME,
                        "${componentName?.packageName}/${componentName?.className}"
                    )
                    putExtra(
                        EXTRA_UPDATE_APP_MSG_TEXT,
                        if (badgeCount == 0) "" else badgeCount.toString()
                    )
                }
                return canResolveBroadcast(localIntent)
            }
        }

        override fun getSupportLaunchers() = listOf(
            "com.miui.miuilite",
            "com.miui.home",
            "com.miui.miuihome",
            "com.miui.miuihome2",
            "com.miui.mihome",
            "com.miui.mihome2"
        )
    }

    inner class ZukHomeBadger : Badger {
        private val CONTENT_URI = "content://com.android.badge/badge".toUri()

        override fun executeBadge(badgeCount: Int): Boolean {
            val extra = Bundle().apply {
                putInt("app_badge_count", badgeCount)
            }
            try {
                return context?.contentResolver?.call(
                    CONTENT_URI, "setAppBadgeCount", null, extra
                ) != null
            } catch (e: Exception) {
                Log.e( "ZukHomeBadger", "executeBadge error: $e")
                return false
            }
        }

        override fun getSupportLaunchers() = listOf("com.zui.launcher")
    }

    inner class VivoHomeBadger : Badger {
        override fun executeBadge(badgeCount: Int): Boolean {
            val intent =
                Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM").apply {
                    `package` = "com.vivo.launcher"
                    putExtra("packageName", context?.packageName)
                    putExtra("className", componentName?.className)
                    putExtra("notificationNum", badgeCount)
                }
            return canResolveBroadcast(intent)
        }

        override fun getSupportLaunchers() = listOf("com.vivo.launcher")
    }
} 