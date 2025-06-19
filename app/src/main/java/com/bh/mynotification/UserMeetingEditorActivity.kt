package com.bh.mynotification

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bh.mynotification.databinding.ActivityUserMeetingEditorBinding

class UserMeetingEditorActivity : AppCompatActivity() {

    companion object {
        const val INTENT_EXTRA_ACTION_BUTTON_TEXT = "INTENT_EXTRA_ACTION_BUTTON_TEXT"
        const val INTENT_EXTRA_ENTRY_POINT = "INTENT_EXTRA_ENTRY_POINT"
        const val INTENT_EXTRA_EVENT_ID = "INTENT_EXTRA_MEETING_ID"
        const val INTENT_EXTRA_CALENDAR_ID = "INTENT_EXTRA_CALENDAR_ID"
        const val INTENT_EXTRA_CALENDAR_TYPE = "INTENT_EXTRA_CALENDAR_TYPE"
        const val INTENT_EXTRA_GROUP_ID = "INTENT_EXTRA_GROUP_ID"
        const val INTENT_EXTRA_PRIVATE_CHAT_MEMBER_ID =
            "INTENT_EXTRA_PRIVATE_CHAT_MEMBER_ID"
    }

    private lateinit var binding: ActivityUserMeetingEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserMeetingEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eid = intent.getStringExtra(INTENT_EXTRA_EVENT_ID)
        val calendarId = intent.getStringExtra(INTENT_EXTRA_CALENDAR_ID)
        binding.valueEid.text = eid
        binding.valueCid.text = calendarId

        val actionText = intent.getStringExtra(INTENT_EXTRA_ACTION_BUTTON_TEXT)
        binding.valueAction.text = actionText
    }
}

