package com.mahetre.mathbubble

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40,40,40,40) }
        box.addView(TextView(this).apply { text = "MathBubble OCR — versión corregida\n\n1) Concede permiso para mostrar sobre otras apps.\n2) Activa MathBubble OCR en Accesibilidad.\n3) Abre el juego.\n\nEsta compilación corrige la estructura Gradle y deja la base de OCR/automatización preparada." })
        box.addView(Button(this).apply { text = "Permitir burbuja"; setOnClickListener { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) } })
        box.addView(Button(this).apply { text = "Abrir Accesibilidad"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } })
        setContentView(box)
    }
}
