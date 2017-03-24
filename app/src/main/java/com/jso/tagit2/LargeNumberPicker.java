package com.jso.tagit2;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by jshelley on 24/03/2017.
 */

public class LargeNumberPicker extends NumberPicker {
    public LargeNumberPicker(Context context) {
        super(context);
    }

    public LargeNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LargeNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
    }

    private void updateView(View view) {
        if(view instanceof EditText){
            ((EditText) view).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        }
    }

}
