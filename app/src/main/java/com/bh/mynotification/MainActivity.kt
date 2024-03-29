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
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bh.mynotification.databinding.ActivityMainBinding
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    // Declare a binding variable
    private lateinit var binding: ActivityMainBinding

    private lateinit var importanceAdapter: ArrayAdapter<CharSequence>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.btnShowNotification.setOnClickListener {
            showEventNotification(this, "calendar_id", "event_id")
        }

        binding.btnShowNotificationPostDelay.setOnClickListener {
            Handler(Looper.getMainLooper()).postDelayed({
                showEventNotification(this, "calendar_id", "event_id")
            }, 5000)
        }

        binding.btnCheckMeeting.setOnClickListener {
            startActivity(Intent(this, UserMeetingEditorActivity::class.java))
        }

        importanceAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.notification_importance,
            android.R.layout.simple_spinner_item
        )
        importanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.importanceSpinner.adapter = importanceAdapter;

        binding.categorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View, position: Int, id: Long
                ) {
                    val selectedCategory =
                        parent.getItemAtPosition(position).toString()
                    // Handle the selected category
                    //Toast.makeText(this@MainActivity, "Selected: $selectedCategory", Toast.LENGTH_SHORT).show()
                    this@MainActivity.selectedCategory = selectedCategory
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Another interface callback
                }
            }

        val prioritySpinner: Spinner =
            findViewById(R.id.priority_spinner) // Assuming you have defined this Spinner in your XML
        val priorityAdapter: ArrayAdapter<CharSequence> =
            ArrayAdapter.createFromResource(
                this,
                R.array.notification_priorities,
                android.R.layout.simple_spinner_item
            )
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = priorityAdapter
    }

    private fun getPriority(): Int {
        return when (binding.prioritySpinner.selectedItemPosition) {
            0 -> NotificationCompat.PRIORITY_DEFAULT
            1 -> NotificationCompat.PRIORITY_LOW
            2 -> NotificationCompat.PRIORITY_MIN
            3 -> NotificationCompat.PRIORITY_HIGH
            4 -> NotificationCompat.PRIORITY_MAX
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    private var selectedCategory: String = ""

    private fun getImportance(): Int {
        return when (binding.importanceSpinner.selectedItemPosition) {
            0 -> NotificationManager.IMPORTANCE_NONE
            1 -> NotificationManager.IMPORTANCE_MIN
            2 -> NotificationManager.IMPORTANCE_LOW
            3 -> NotificationManager.IMPORTANCE_DEFAULT
            4 -> NotificationManager.IMPORTANCE_HIGH
            5 -> NotificationManager.IMPORTANCE_MAX
            else -> NotificationManager.IMPORTANCE_DEFAULT
        }
    }

    private fun getBadgeIconType(): Int {
        return when (binding.badgeIconSpinner.selectedItemPosition) {
            0 -> NotificationCompat.BADGE_ICON_NONE
            1 -> NotificationCompat.BADGE_ICON_SMALL
            2 -> NotificationCompat.BADGE_ICON_LARGE
            else -> NotificationCompat.BADGE_ICON_SMALL
        }
    }

    private fun showEventNotification(
        context: Context, cid: String, eid: String
    ) {
        val channelId = "event_notification_channel"
        val notificationId = Random.nextInt(0, 1000)

        // Create a notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Event Notification"
            val descriptionText = "Notifications for specific events"
            val importance = getImportance()
            val channel =
                NotificationChannel(channelId, name, importance).apply {
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
        val category = selectedCategory
        val priority = getPriority()
        val badgeIconType = getBadgeIconType()

        val builder = NotificationCompat.Builder(context, channelId)
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

    // Ensure you define REQUEST_CODE_POST_NOTIFICATIONS as a constant
    companion object {
        private const val REQUEST_CODE_POST_NOTIFICATIONS = 1
    }
}