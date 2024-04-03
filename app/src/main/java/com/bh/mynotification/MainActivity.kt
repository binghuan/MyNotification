package com.bh.mynotification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.bh.mynotification.databinding.ActivityMainBinding
import com.google.gson.Gson
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_POST_NOTIFICATIONS = 1
        private const val CHANNEL_ID = "Event Notification"
        private const val CHANNEL_NAME = "Event Notification"
        private const val TAG = "BH_MainActivity"
    }

    // Declare a binding variable
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        viewModel.loadPreferences(this)

        // Replace findViewById with view binding
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        setupUI()
        observeViewModel()
    }

    private fun startCountdownTimer(delayInSeconds: Long) {
        val timer = object : CountDownTimer(delayInSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 更新 UI 以显示剩余时间，例如更新一个 TextView。这是可选的。
                val remainingTime = millisUntilFinished / 1000
                Log.i(TAG, "Remaining time: $remainingTime")
                binding.tvNotificationCountDown.text = remainingTime.toString()
                binding.tvNotificationCountDown.isGone = false
            }

            override fun onFinish() {
                // 倒计时结束时调用
                showEventNotification(
                    this@MainActivity, "calendar_id", "event_id"
                )
                binding.tvNotificationCountDown.isGone = true
            }
        }
        timer.start()
    }

    private fun setupUI() {
        // Setup the UI components
        binding.btnShowNotification.setOnClickListener {

            val delayTime =
                binding.etDelayTime.text.toString().toIntOrNull() ?: 0
            startCountdownTimer(delayTime.toLong())

            viewModel.savePreferences(this)
        }

        binding.btnCheckMeeting.setOnClickListener {
            startActivity(Intent(this, UserMeetingEditorActivity::class.java))
        }

        binding.etTitle.doOnTextChanged { text, start, before, count ->
            viewModel.title = text.toString()
        }

        binding.etContent.doOnTextChanged { text, start, before, count ->
            viewModel.content = text.toString()
        }

        binding.etDelayTime.setText(viewModel.delayTime.toString())
        binding.etDelayTime.doOnTextChanged { text, start, before, count ->
            val delayTime = text.toString().toIntOrNull() ?: 0
            viewModel.delayTime = delayTime
        }

        // Setup listeners and adapters here
        setupSpinners()

    }

    private fun observeViewModel() {
        // Observe ViewModel LiveData and react to changes


    }

    private fun setupSpinners() {
        // Setup the spinners with the appropriate data

        binding.importanceSpinner.setSelection(
            resources.getIntArray(R.array.notification_importance_values)
                .indexOf(
                    viewModel.selectedImportance
                )
        )
        binding.importanceSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View, position: Int, id: Long
                ) {
                    val values =
                        resources.getIntArray(R.array.notification_importance_values)
                    val selectedImportance = values[position]
                    viewModel.selectedImportance = selectedImportance
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        binding.categorySpinner.setSelection(
            resources.getStringArray(R.array.notification_categories_values)
                .indexOf(
                    viewModel.selectedCategory
                )
        )
        binding.categorySpinner.setSelection(
            resources.getStringArray(R.array.notification_categories_values)
                .indexOf(
                    viewModel.selectedCategory
                )
        )
        binding.categorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View, position: Int, id: Long
                ) {
                    val values =
                        resources.getStringArray(R.array.notification_categories_values)
                    val selectedCategory = values[position]
                    viewModel.selectedCategory = selectedCategory
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Another interface callback
                }
            }

        binding.prioritySpinner.setSelection(
            resources.getIntArray(R.array.notification_priorities_values)
                .indexOf(
                    viewModel.selectedPriority
                )
        )
        binding.prioritySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View, position: Int, id: Long
                ) {
                    val values =
                        resources.getIntArray(R.array.notification_priorities_values)
                    val selectedPriority = values[position]
                    viewModel.selectedPriority = selectedPriority
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Another interface callback
                }
            }

        binding.badgeIconSpinner.setSelection(
            resources.getIntArray(R.array.notification_badge_icons_values)
                .indexOf(
                    viewModel.selectedBadgeIconType
                )
        )
        binding.badgeIconSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View, position: Int, id: Long
                ) {
                    val values =
                        resources.getIntArray(R.array.notification_badge_icons_values)
                    val selectedBadgeIconType = values[position]
                    viewModel.selectedBadgeIconType = selectedBadgeIconType
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Another interface callback
                }
            }
    }

    private fun showEventNotification(
        context: Context, cid: String, eid: String
    ) {
        val notificationId = Random.nextInt(0, 1000)

        // Create a notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val descriptionText = "Notifications for specific events"
            val importance = viewModel.selectedImportance
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, importance
            ).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val alarmSound: Uri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Intent for the "View" action button
        val viewIntent =
            Intent(context, UserMeetingEditorActivity::class.java).apply {
                putExtra(
                    UserMeetingEditorActivity.INTENT_EXTRA_CALENDAR_ID, cid
                )
                putExtra(UserMeetingEditorActivity.INTENT_EXTRA_EVENT_ID, eid)
            }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            viewIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = binding.etTitle.text.toString()
        val content = binding.etContent.text.toString()
        val category = viewModel.selectedCategory
        val priority = viewModel.selectedPriority
        val badgeIconType = viewModel.selectedBadgeIconType

        NotificationCompat.CATEGORY_EVENT
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setVibrate(longArrayOf(0, 100)).setSound(alarmSound)
            .setStyle(NotificationCompat.InboxStyle())
            .setDefaults(Notification.DEFAULT_SOUND).setCategory(category)
            .setContentTitle(title).setContentText(content)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent).setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(priority).setNumber(0).setBadgeIconType(badgeIconType)

        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission handling omitted for brevity
                return
            }
            notify(notificationId, builder.build())
        }
    }


    override fun onResume() {
        super.onResume()

        val areNotificationsEnabled =
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        binding.switchShowNotification.isChecked = areNotificationsEnabled

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel: NotificationChannel? =
                notificationManager.getNotificationChannel(CHANNEL_ID)
            if (channel != null) {
                val isChannelEnabled =
                    channel.importance != NotificationManager.IMPORTANCE_NONE
                // IMPORTANCE_NONE means the channel is blocked, thus no notifications will pop on the screen.
                binding.switchIsChannelEnabled.isChecked = isChannelEnabled
            }
        }
    }

    // Override onRequestPermissionsResult to handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            // Check if the request was granted or not
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission was granted. Show the notification again.
                showEventNotification(
                    context = applicationContext, "cid xxx", "eid xxx"
                ) // You might need to adjust this call according to your actual function and context management
            } else {
                // Permission was denied. Handle the failure to have permission.
                Toast.makeText(
                    applicationContext,
                    "Notification permission is required to show notifications.",
                    Toast.LENGTH_SHORT
                ).show()
                // Optionally, inform the user that the notification feature requires permission.
            }
        }
    }
}