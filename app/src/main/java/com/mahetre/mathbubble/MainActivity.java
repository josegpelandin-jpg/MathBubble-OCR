package com.mahetre.mathbubble;

import android.app.*;
import android.os.*;
import android.provider.Settings;
import android.content.*;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    static final int CAPTURE_REQ = 9001;
    MediaProjectionManager mpm;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        mpm = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(40,50,40,40);

        TextView title = new TextView(this);
        title.setText("MathBubble OCR");
        title.setTextSize(28);
        title.setTextColor(Color.BLACK);

        TextView info = new TextView(this);
        info.setText("\nLee operaciones visibles en pantalla, las resuelve y muestra la respuesta en una burbuja. AUTO puede tocar la opción coincidente.\n");
        info.setTextSize(16);
        info.setTextColor(Color.DKGRAY);

        Button overlay = new Button(this);
        overlay.setText("1. Permitir burbuja");
        overlay.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
            }
        });

        Button access = new Button(this);
        access.setText("2. Activar accesibilidad");
        access.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        Button capture = new Button(this);
        capture.setText("3. Autorizar captura e iniciar");
        capture.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this,"Concede primero el permiso de burbuja",Toast.LENGTH_LONG).show();
                return;
            }
            startActivityForResult(mpm.createScreenCaptureIntent(), CAPTURE_REQ);
        });

        TextView demo = new TextView(this);
        demo.setText("\nEjemplo objetivo: 6 × 6 → 36");
        demo.setTextSize(16);

        box.addView(title); box.addView(info); box.addView(overlay);
        box.addView(access); box.addView(capture); box.addView(demo);
        setContentView(box);
    }

    @Override protected void onActivityResult(int req, int result, Intent data) {
        super.onActivityResult(req,result,data);
        if(req==CAPTURE_REQ && result==RESULT_OK && data!=null) {
            Intent i = new Intent(this, CaptureService.class);
            i.putExtra("resultCode",result);
            i.putExtra("data",data);
            if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i);
            Toast.makeText(this,"MathBubble OCR iniciado",Toast.LENGTH_SHORT).show();
        }
    }
}
