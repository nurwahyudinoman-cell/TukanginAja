package com.tukangin.modules.notification

import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class NotificationServiceTest {

    private val context: Context = mock {
        on { applicationContext } doReturn it
        on { resources } doReturn mock<Resources>()
        on { packageName } doReturn "com.tukanginAja.solusi"
    }

    private val notificationManager: NotificationManager = mock()

    @Before
    fun setup() {
        whenever(context.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManager)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            whenever(context.getSystemService(NotificationManager::class.java)).thenReturn(notificationManager)
        }
    }

    @Test
    fun `notification channel can be created safely`() {
        NotificationService.createChannel(context)
        assert(true)
    }
}
