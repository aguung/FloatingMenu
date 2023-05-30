package com.devfutech.floatingmenu

import android.annotation.SuppressLint
import android.app.*
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.devfutech.floatingmenu.databinding.ViewFloatBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ServiceFloating : Service() {
    private val windowManager by lazy {
        getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    private val floatMenu by lazy {
        ViewFloatBinding.inflate(LayoutInflater.from(this), null, false)
    }
    private val layoutParamsRoot by lazy {
        floatMenu.root.layoutParams as WindowManager.LayoutParams
    }
    private var xInitCord: Int = 0
    private var yInitCord: Int = 0
    private var xInitMargin: Int = 0
    private var yInitMargin: Int = 0
    private var isMenuShow: Boolean = true
    private var isMove: Boolean = false
    private val messages = mutableListOf("Hello World")
    private val adapterMessage by lazy {
        ArrayAdapter(this, R.layout.item_message, messages)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return if (startId == START_STICKY) {
            if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
            generateForegroundNotification()
            addFloatingMenu()
            START_STICKY
        } else {
            START_NOT_STICKY
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFloatingControl()
        stopForeground(true)
        stopSelf()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onMessageEvent(messageEvent: MessageEvent) {
        messages.add(messageEvent.message)
        adapterMessage.notifyDataSetChanged()
    }

    private fun updateView() {
        windowManager.updateViewLayout(floatMenu.root, layoutParamsRoot)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addFloatingMenu() {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        val paramFloat = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        paramFloat.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL

        windowManager.addView(floatMenu.root, paramFloat)

        floatMenu.apply {
            root.setOnTouchListener { _, event ->
                val layoutParams = layoutParamsRoot

                val xCord = event.rawX.toInt()
                val yCord = event.rawY.toInt()
                val xCordDestination: Int
                val yCordDestination: Int

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isMove = false
                        xInitCord = xCord
                        yInitCord = yCord
                        xInitMargin = layoutParams.x
                        yInitMargin = layoutParams.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val xDiffMove: Int = xCord - xInitCord
                        val yDiffMove: Int = yCord - yInitCord
                        xCordDestination = xInitMargin + xDiffMove
                        yCordDestination = yInitMargin + yDiffMove

                        layoutParams.x = xCordDestination
                        layoutParams.y = yCordDestination
                        isMove = true
                        windowManager.updateViewLayout(floatMenu.root, layoutParams)
                    }
                }
                return@setOnTouchListener true
            }
            fabAction.setOnTouchListener { _, event ->
                val layoutParams = layoutParamsRoot

                val xCord = event.rawX.toInt()
                val yCord = event.rawY.toInt()
                val xCordDestination: Int
                val yCordDestination: Int

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isMove = false
                        xInitCord = xCord
                        yInitCord = yCord
                        xInitMargin = layoutParams.x
                        yInitMargin = layoutParams.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val xDiffMove: Int = xCord - xInitCord
                        val yDiffMove: Int = yCord - yInitCord
                        xCordDestination = xInitMargin + xDiffMove
                        yCordDestination = yInitMargin + yDiffMove

                        layoutParams.x = xCordDestination
                        layoutParams.y = yCordDestination
                        isMove = true
                        windowManager.updateViewLayout(floatMenu.root, layoutParams)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isMove) {
                            isShowMenu(isMenuShow)
                        }
                    }
                }
                return@setOnTouchListener true
            }
            ivExpandChat.setOnClickListener {
                isShowMenu(isMenuShow)
            }
            lvMessage.adapter = adapterMessage
        }
    }

    private fun isShowMenu(menuShow: Boolean) {
        val toogleIconArrow = if (menuShow) {
            ContextCompat.getDrawable(this, R.drawable.ic_arrow_down)
        } else {
            ContextCompat.getDrawable(this, R.drawable.ic_arrow_up)
        }

        floatMenu.apply {
            fabMenu1.isVisible = menuShow
            fabMenu2.isVisible = menuShow
            fabMenu3.isVisible = menuShow
            tvLabelChat.isVisible = menuShow
            ivExpandChat.apply {
                layoutParams = LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = if (menuShow) Gravity.TOP else Gravity.CENTER
                }
                setImageDrawable(toogleIconArrow)
            }
            llContainerMessage.apply {
                layoutParams = LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT
                ).apply {
                    weight = 1.0f
                    gravity = if (menuShow) Gravity.TOP else Gravity.CENTER
                }
            }
            if (!menuShow) {
                lvMessage.post {
                    lvMessage.smoothScrollToPosition(messages.size - 1)
                }
            }
        }
        this.isMenuShow = !menuShow
    }

    private fun removeFloatingControl() {
        windowManager.removeView(floatMenu.root)
    }

    private fun generateForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intentMainLanding = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, intentMainLanding, FLAG_MUTABLE)
            val iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mNotificationManager.createNotificationChannelGroup(
                    NotificationChannelGroup("chats_group", "Chats")
                )
                val notificationChannel =
                    NotificationChannel(
                        "service_channel", "Service Notifications",
                        NotificationManager.IMPORTANCE_MIN
                    )
                notificationChannel.enableLights(false)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                mNotificationManager.createNotificationChannel(notificationChannel)
            }
            val builder = NotificationCompat.Builder(this, "service_channel")

            builder.setContentTitle(
                StringBuilder(resources.getString(R.string.app_name)).append(" service is running")
                    .toString()
            )
                .setTicker(
                    StringBuilder(resources.getString(R.string.app_name)).append("service is running")
                        .toString()
                )
                .setContentText("Touch to open") //                    , swipe down for more options.
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
            if (iconNotification != null) {
                builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
            }
            val notification = builder.build()
            startForeground(101, notification)
        }

    }
}