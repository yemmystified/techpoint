package com.formatrix.techpoint.activities;

import android.content.res.TypedArray;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;

import com.formatrix.techpoint.R;

/**
 * Design and developed by formatrix.com
 *
 * ActivityBase is created to handle scrolling page event.
 * Modified from ObservableScrollView library.
 */
public abstract class ActivityBase extends AppCompatActivity {

    // Method to get Actionbar size
    protected int getActionBarSize() {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }

    // Method to get screen height
    protected int getScreenHeight() {
        return findViewById(android.R.id.content).getHeight();
    }
}