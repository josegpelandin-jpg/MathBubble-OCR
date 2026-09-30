package com.mahetre.securitybubble

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.*

class BubbleService : Service() {
    private lateinit var wm: WindowManager
    private var bubble: View? = null
    private var panel: View? = null
    private var testScore = 16

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel("qa","QA Bubble",NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        startForeground(7, Notification.Builder(this,"qa")
            .setContentTitle("Security Bubble Tester")
            .setContentText("Burbuja QA activa")
            .setSmallIcon(android.R.drawable.ic_menu_info_details).build())
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        showBubble()
    }

    private fun params(w:Int,h:Int) = WindowManager.LayoutParams(
        w,h, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.START; x = 20; y = 250 }

    private fun showBubble() {
        val b = Button(this).apply {
            text = "🔧"
            textSize = 20f
            setOnClickListener { if(panel==null) showPanel() else hidePanel() }
        }
        bubble = b
        wm.addView(b, params(120,120))
    }

    private fun showPanel() {
        val prefs = getSharedPreferences("qa", MODE_PRIVATE)
        val name = prefs.getString("selected_name","Sin selección")
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24,24,24,24)
            setBackgroundColor(0xEE202020.toInt())
        }
        val title = TextView(this).apply { text = "QA: $name"; setTextColor(0xFFFFFFFF.toInt()); textSize=18f }
        val score = TextView(this).apply { setTextColor(0xFFFFFFFF.toInt()); textSize=24f }
        fun refresh(){ score.text = "Score de prueba: $testScore" }
        refresh()
        box.addView(title); box.addView(score)

        listOf("-1" to -1, "+1" to 1, "+100" to 100).forEach { (label,delta) ->
            box.addView(Button(this).apply {
                text = label
                setOnClickListener { testScore += delta; refresh() }
            })
        }
        val input = EditText(this).apply { hint="Valor de prueba"; setTextColor(0xFFFFFFFF.toInt()) }
        box.addView(input)
        box.addView(Button(this).apply {
            text="FIJAR VALOR"
            setOnClickListener { input.text.toString().toIntOrNull()?.let { testScore=it; refresh() } }
        })
        box.addView(TextView(this).apply {
            text="Modo seguro: este valor pertenece al tester y no escribe en la memoria/almacenamiento de la aplicación seleccionada."
            setTextColor(0xFFFFFFFF.toInt())
        })
        panel = box
        wm.addView(box, params(650, WindowManager.LayoutParams.WRAP_CONTENT).apply { x=150; y=250 })
    }

    private fun hidePanel(){ panel?.let { wm.removeView(it) }; panel=null }
    override fun onDestroy(){ hidePanel(); bubble?.let { wm.removeView(it) }; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
