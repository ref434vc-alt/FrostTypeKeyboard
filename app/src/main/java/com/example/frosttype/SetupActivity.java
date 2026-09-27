package com.example.frosttype;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Simple offline setup screen for enabling the keyboard. */
public final class SetupActivity extends Activity {
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTextColor(Color.rgb(27,38,54));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xEBFFFFFF);
        bg.setCornerRadius(dp(17));
        b.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(58));
        p.setMargins(0, dp(9), 0, 0);
        b.setLayoutParams(p);
        return b;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(32), dp(24), dp(24));
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF72C2F4, 0xFF9075CD, 0xFFF5B9D3}));
        TextView title = new TextView(this);
        title.setText("FrostType");
        title.setTextColor(0xFF24374E);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextSize(36);
        root.addView(title);
        TextView body = new TextView(this);
        body.setText("English-only frosted-glass keyboard. Works offline. Live background blur depends on your Android version and device. Enable the keyboard, select it, then type below.");
        body.setTextSize(16);
        body.setTextColor(0xFF49586A);
        body.setPadding(0, dp(14), 0, dp(10));
        root.addView(body);
        Button enable = button("1   Enable FrostType in keyboard settings");
        enable.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        root.addView(enable);
        Button select = button("2   Switch to FrostType");
        select.setOnClickListener(v -> {
            InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (manager != null) manager.showInputMethodPicker();
        });
        root.addView(select);
        EditText test = new EditText(this);
        test.setTextSize(18);
        test.setSingleLine(false);
        test.setGravity(Gravity.TOP);
        test.setHint("3   Test the glass keyboard here…");
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(120));
        p.topMargin = dp(20);
        root.addView(test, p);
        setContentView(root);
    }
}
