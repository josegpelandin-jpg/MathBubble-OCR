package com.mahetre.mathbubble;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class MathAccessibilityService extends AccessibilityService {
    public static MathAccessibilityService INSTANCE;

    @Override protected void onServiceConnected() { INSTANCE=this; }
    @Override public void onAccessibilityEvent(AccessibilityEvent e) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy(){ if(INSTANCE==this) INSTANCE=null; super.onDestroy(); }

    public static void clickAt(float x,float y){
        MathAccessibilityService s=INSTANCE;
        if(s==null) return;
        Path p=new Path(); p.moveTo(x,y);
        GestureDescription.StrokeDescription stroke =
            new GestureDescription.StrokeDescription(p,0,40);
        GestureDescription g=new GestureDescription.Builder().addStroke(stroke).build();
        s.dispatchGesture(g,null,null);
    }
}
