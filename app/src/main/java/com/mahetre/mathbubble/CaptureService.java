package com.mahetre.mathbubble;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.hardware.display.*;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.*;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.*;

public class CaptureService extends Service {
    WindowManager wm; TextView bubble;
    MediaProjection projection; ImageReader reader; VirtualDisplay display;
    TextRecognizer recognizer;
    volatile boolean busy=false, auto=false;
    volatile String last="?";
    volatile String pointsDouble="";
    long lastClick=0;
    final Handler uiHandler=new Handler(Looper.getMainLooper());
    int width,height,density;

    @Override public int onStartCommand(Intent intent,int flags,int id){
        createChannel();
        Notification n=new Notification.Builder(this,"mathbubble")
            .setContentTitle("MathBubble OCR activo")
            .setContentText("Reconocimiento matemático en pantalla")
            .setSmallIcon(android.R.drawable.ic_dialog_info).build();
        startForeground(11,n);

        if(projection!=null) return START_STICKY;
        int code=intent.getIntExtra("resultCode",Activity.RESULT_CANCELED);
        Intent data=intent.getParcelableExtra("data");
        if(data==null) { stopSelf(); return START_NOT_STICKY; }

        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        DisplayMetrics dm=new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        width=dm.widthPixels; height=dm.heightPixels; density=dm.densityDpi;

        MediaProjectionManager mpm=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        projection=mpm.getMediaProjection(code,data);
        recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        reader=ImageReader.newInstance(width,height,PixelFormat.RGBA_8888,2);
        display=projection.createVirtualDisplay("MathBubbleCapture",width,height,density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader.getSurface(),null,null);

        reader.setOnImageAvailableListener(r -> {
            if(busy) { Image drop=r.acquireLatestImage(); if(drop!=null) drop.close(); return; }
            Image img=r.acquireLatestImage();
            if(img==null) return;
            busy=true;
            Bitmap bmp=null;
            try { bmp=imageToBitmap(img); } finally { img.close(); }
            if(bmp==null){ busy=false; return; }
            final Bitmap use=bmp;
            recognizer.process(InputImage.fromBitmap(use,0))
                .addOnSuccessListener(this::handleText)
                .addOnCompleteListener(t -> { use.recycle(); busy=false; });
        },new Handler(Looper.getMainLooper()));

        showBubble();
        return START_STICKY;
    }

    Bitmap imageToBitmap(Image image){
        Image.Plane plane=image.getPlanes()[0];
        ByteBuffer buffer=plane.getBuffer();
        int pixelStride=plane.getPixelStride();
        int rowStride=plane.getRowStride();
        int rowPadding=rowStride-pixelStride*width;
        Bitmap full=Bitmap.createBitmap(width+rowPadding/pixelStride,height,Bitmap.Config.ARGB_8888);
        full.copyPixelsFromBuffer(buffer);
        if(full.getWidth()==width) return full;
        Bitmap cropped=Bitmap.createBitmap(full,0,0,width,height);
        full.recycle(); return cropped;
    }

    void handleText(com.google.mlkit.vision.text.Text text){
        List<Token> tokens=new ArrayList<>();
        for(Text.TextBlock b:text.getTextBlocks())
            for(Text.Line line:b.getLines())
                for(Text.Element e:line.getElements())
                    if(e.getBoundingBox()!=null) tokens.add(new Token(e.getText(),e.getBoundingBox()));

        updatePointsPreview(text);

        ExpressionHit hit=findExpressionHit(text);
        if(hit==null) { refreshBubble(); return; }
        double ans;
        try { ans=new MathParser(hit.expression).parse(); } catch(Exception ex){ return; }
        last=format(ans); refreshBubble();

        Token match=null;
        double best=Double.MAX_VALUE;
        for(Token t:tokens){
            Double v=number(t.s);
            if(v==null || Math.abs(v-ans)>1e-6) continue;
            // Evita tocar un número que forme parte de la propia operación.
            if(hit.bounds!=null && Rect.intersects(hit.bounds,t.r)) continue;
            double dx=hit.bounds==null?0:t.r.centerX()-hit.bounds.centerX();
            double dy=hit.bounds==null?0:t.r.centerY()-hit.bounds.centerY();
            double dist=dx*dx+dy*dy;
            if(dist<best){ best=dist; match=t; }
        }
        long now=System.currentTimeMillis();
        if(auto && match!=null && now-lastClick>350){
            lastClick=now;
            MathAccessibilityService.clickAt(match.r.centerX(),match.r.centerY());
        }
    }

    ExpressionHit findExpressionHit(com.google.mlkit.vision.text.Text text){
        Pattern op=Pattern.compile(".*(?:\\d|\\))\\s*(?:[+\\-×÷*/^]|²|³|%).*");
        Pattern sqrt=Pattern.compile(".*(?:√|sqrt).*\\d.*",Pattern.CASE_INSENSITIVE);
        for(Text.TextBlock b:text.getTextBlocks()){
            for(Text.Line l:b.getLines()){
                String raw=l.getText().trim();
                if(raw.length()>80) continue;
                if(op.matcher(raw).matches() || sqrt.matcher(raw).matches()){
                    String cleaned=extractMath(raw);
                    if(cleaned!=null) return new ExpressionHit(cleaned,l.getBoundingBox());
                }
            }
        }
        return null;
    }

    String extractMath(String raw){
        String s=raw.replaceAll("(?i)(resultado|result|resuelve|solve|cuanto es|cuánto es)[: ]*","").trim();
        int eq=s.indexOf('='); if(eq>=0) s=s.substring(0,eq);
        s=s.replace("?","").replace("¿","").trim();
        return s.matches(".*\\d.*") ? s : null;
    }

    void updatePointsPreview(com.google.mlkit.vision.text.Text text){
        Pattern labeled=Pattern.compile("(?i).*(?:puntos|points|pts|score)\\D{0,12}(-?\\d+(?:[.,]\\d+)?).*|.*(-?\\d+(?:[.,]\\d+)?)\\D{0,12}(?:puntos|points|pts|score).*");
        for(Text.TextBlock b:text.getTextBlocks()) for(Text.Line l:b.getLines()){
            Matcher m=labeled.matcher(l.getText());
            if(m.matches()){
                String n=m.group(1)!=null?m.group(1):m.group(2);
                try { pointsDouble="Pts×100: "+format(Double.parseDouble(n.replace(",","."))*100.0); } catch(Exception ignored){}
                return;
            }
        }
    }

    Double number(String s){
        try { return Double.parseDouble(s.trim().replace(",",".")); }
        catch(Exception e){ return null; }
    }

    String format(double d){
        if(Math.abs(d-Math.rint(d))<1e-9) return Long.toString(Math.round(d));
        return String.format(Locale.US,"%.4f",d).replaceAll("0+$","").replaceAll("\\.$","");
    }

    void showBubble(){
        bubble=new TextView(this);
        bubble.setText("?\nOFF"); bubble.setTextColor(Color.WHITE); bubble.setTextSize(14);
        bubble.setGravity(Gravity.CENTER);
        GradientDrawable bg=new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL); bg.setColor(Color.rgb(21,101,192));
        bubble.setBackground(bg);

        WindowManager.LayoutParams p=new WindowManager.LayoutParams(
            160,160,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
        p.gravity=Gravity.TOP|Gravity.START; p.x=20; p.y=300;

        final int[] sx={0},sy={0},ix={0},iy={0}; final boolean[] moved={false};
        bubble.setOnTouchListener((v,e)->{
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                sx[0]=(int)e.getRawX(); sy[0]=(int)e.getRawY();
                ix[0]=p.x; iy[0]=p.y; moved[0]=false; return true;
            }
            if(e.getAction()==MotionEvent.ACTION_MOVE){
                if(Math.abs(e.getRawX()-sx[0])>10 || Math.abs(e.getRawY()-sy[0])>10) moved[0]=true;
                p.x=ix[0]+(int)e.getRawX()-sx[0]; p.y=iy[0]+(int)e.getRawY()-sy[0];
                wm.updateViewLayout(bubble,p); return true;
            }
            if(e.getAction()==MotionEvent.ACTION_UP){
                if(!moved[0]) { auto=!auto; refreshBubble(); }
                return true;
            }
            return true;
        });
        wm.addView(bubble,p);
    }

    void refreshBubble(){
        if(bubble==null) return;
        uiHandler.post(() -> {
            String state=auto?"AUTO":"OFF";
            String extra=pointsDouble.isEmpty()?"":"\n"+pointsDouble;
            bubble.setText(last+"\n"+state+extra);
        });
    }

    void createChannel(){
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel c=new NotificationChannel("mathbubble","MathBubble OCR",
                NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(c);
        }
    }

    @Override public void onDestroy(){
        if(display!=null) display.release();
        if(reader!=null) reader.close();
        if(projection!=null) projection.stop();
        if(recognizer!=null) recognizer.close();
        if(bubble!=null && wm!=null) wm.removeView(bubble);
        super.onDestroy();
    }
    @Override public android.os.IBinder onBind(Intent i){return null;}

    static class ExpressionHit {
        String expression; Rect bounds;
        ExpressionHit(String expression,Rect bounds){this.expression=expression;this.bounds=bounds;}
    }

    static class Token {
        String s; Rect r;
        Token(String s,Rect r){this.s=s;this.r=r;}
    }
}
