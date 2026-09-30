package com.mahetre.securitybubble

import android.app.*
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var list: LinearLayout
    private lateinit var search: EditText
    private var apps = listOf<Pair<String,String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32,32,32,32)
        }
        search = EditText(this).apply { hint = "Buscar aplicación..." }
        val start = Button(this).apply {
            text = "ACTIVAR BURBUJA DE PRUEBA"
            setOnClickListener { startBubble() }
        }
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(TextView(this).apply {
            text = "🔐 SECURITY BUBBLE TESTER\nSelecciona una app instalada para identificarla visualmente. La burbuja NO modifica memoria de otras apps."
            textSize = 18f
        })
        root.addView(search)
        root.addView(start)
        root.addView(ScrollView(this).apply { addView(list) })
        setContentView(root)

        val pm = packageManager
        apps = pm.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0
        ).map { it.loadLabel(pm).toString() to it.activityInfo.packageName }
         .distinctBy { it.second }.sortedBy { it.first.lowercase() }

        search.setOnEditorActionListener { _,_,_ -> render(search.text.toString()); true }
        render("")
    }

    private fun render(q:String) {
        list.removeAllViews()
        apps.filter { it.first.contains(q,true) || it.second.contains(q,true) }.take(80).forEach { a ->
            list.addView(Button(this).apply {
                text = "${a.first}\n${a.second}"
                setOnClickListener {
                    getSharedPreferences("qa", MODE_PRIVATE).edit()
                        .putString("selected_name", a.first).putString("selected_pkg", a.second).apply()
                    Toast.makeText(this@MainActivity, "Seleccionada: ${a.first}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun startBubble() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")))
            return
        }
        startForegroundService(Intent(this, BubbleService::class.java))
        Toast.makeText(this, "Burbuja activada", Toast.LENGTH_SHORT).show()
    }
}
