package com.example.frosttype;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;
import android.widget.TextView;

/** English QWERTY offline experimental glass keyboard. */
public final class FrostKeyboardService extends InputMethodService {
    private static final int DARK_TEXT = 0xFF353841;
    private static final int MILK_KEY = 0xEEFFFFFF;
    private static final int SPECIAL_KEY = 0xD2B2B7C1;
    private static final int BLUE_KEY = 0xFF2975DC;
    private static final int GLASS = 0xBAE5E5EB;
    private static final int FALLBACK = 0xFFE3E2E8;
    private boolean shift = false;
    private boolean symbols = false;
    private boolean blurAvailable = false;
    private LinearLayout root;

    private int dp(float px) {
        return (int)(px * getResources().getDisplayMetrics().density + .5f);
    }

    private GradientDrawable rounded(int color, int radiusDp, int borderColor) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        d.setStroke(dp(1), borderColor);
        return d;
    }

    private void updateGlass() {
        if (Build.VERSION.SDK_INT >= 31) {
            WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
            blurAvailable = wm != null && wm.isCrossWindowBlurEnabled();
        } else blurAvailable = false;
        Window win = getWindow() == null ? null : getWindow().getWindow();
        if (win != null) {
            win.setBackgroundDrawable(rounded(blurAvailable ? GLASS : FALLBACK, 28, 0x55FFFFFF));
            if (Build.VERSION.SDK_INT >= 31) {
                win.setBackgroundBlurRadius(blurAvailable ? dp(72) : 0);
            }
        }
        if (root != null) {
            root.setBackground(rounded(blurAvailable ? GLASS : FALLBACK, 28, 0x66FFFFFF));
        }
    }

    @Override public void onWindowShown() {
        super.onWindowShown();
        updateGlass();
    }

    @Override public View onCreateInputView() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(5), dp(17), dp(5), dp(10));
        root.setClipToPadding(false);
        View clearHeader = new View(this);
        root.addView(clearHeader, new LinearLayout.LayoutParams(-1, dp(33)));
        rebuildRows();
        updateGlass();
        return root;
    }

    @Override public void onStartInput(EditorInfo info, boolean restarting) {
        super.onStartInput(info, restarting);
        shift = false;
        symbols = false;
        if (root != null) rebuildRows();
    }

    private void rebuildRows() {
        if (root == null) return;
        while (root.getChildCount() > 1) root.removeViewAt(1);
        if (symbols) {
            addRow(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"}, false);
            addRow(new String[]{"@", "#", "$", "%", "&", "-", "+", "(", ")", "/"}, false);
            addRow(new String[]{"ABC", "*", "\"", "'", ":", ";", "!", "?", "⌫"}, true);
        } else {
            addRow(new String[]{"q", "w", "e", "r", "t", "y", "u", "i", "o", "p"}, false);
            addRow(new String[]{"a", "s", "d", "f", "g", "h", "j", "k", "l"}, false);
            addRow(new String[]{"⇧", "z", "x", "c", "v", "b", "n", "m", "⌫"}, true);
        }
        addBottomRow();
    }

    private void addRow(String[] labels, boolean hasSpecials) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, dp(52));
        rowParams.bottomMargin = dp(7);
        row.setLayoutParams(rowParams);
        for (String label : labels) {
            boolean special = hasSpecials && (label.equals("⇧") || label.equals("⌫") || label.equals("ABC"));
            addKey(row, label, special ? 1.52f : 1f,
                    special ? SPECIAL_KEY : MILK_KEY, DARK_TEXT, false);
        }
        root.addView(row);
    }

    private void addBottomRow() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(54));
        p.topMargin = dp(1);
        p.bottomMargin = dp(5);
        row.setLayoutParams(p);
        addKey(row, symbols ? "ABC" : "123", 1.45f, SPECIAL_KEY, DARK_TEXT, false);
        addKey(row, "space", 4.2f, MILK_KEY, DARK_TEXT, false);
        addKey(row, "➜", 1.45f, BLUE_KEY, Color.WHITE, true);
        root.addView(row);
    }

    private void addKey(LinearLayout row, String label, float weight, int color, int textColor, boolean enterKey) {
        TextView key = new TextView(this);
        String rendered = (shift && label.length() == 1 && Character.isLetter(label.charAt(0)))
                ? label.toUpperCase(java.util.Locale.ROOT) : label;
        key.setText(rendered);
        key.setTextSize(label.equals("space") ? 12 : enterKey ? 31 : label.equals("⇧") || label.equals("⌫") ? 25 : 23);
        key.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        key.setTextColor(textColor);
        key.setGravity(Gravity.CENTER);
        key.setBackground(rounded(color, 9, 0x33FFFFFF));
        key.setElevation(dp(1.2f));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -1, weight);
        p.leftMargin = dp(2);
        p.rightMargin = dp(2);
        row.addView(key, p);
        key.setOnClickListener(v -> press(label));
    }

    private void press(String key) {
        InputConnection ic = getCurrentInputConnection();
        switch (key) {
            case "⇧":
                shift = !shift;
                rebuildRows();
                return;
            case "ABC":
                symbols = false;
                rebuildRows();
                return;
            case "123":
                symbols = true;
                shift = false;
                rebuildRows();
                return;
            case "⌫":
                if (ic != null) {
                    CharSequence before = ic.getTextBeforeCursor(2, 0);
                    int n = before != null && before.length() == 2 &&
                            Character.isSurrogatePair(before.charAt(0), before.charAt(1)) ? 2 : 1;
                    ic.deleteSurroundingText(n, 0);
                }
                return;
            case "space":
                if (ic != null) ic.commitText(" ", 1);
                return;
            case "➜":
                if (ic == null) return;
                EditorInfo ei = getCurrentInputEditorInfo();
                int action = ei == null ? EditorInfo.IME_ACTION_NONE :
                        (ei.imeOptions & EditorInfo.IME_MASK_ACTION);
                if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED)
                    ic.performEditorAction(action);
                else ic.commitText("\n", 1);
                return;
            default:
                if (ic != null) ic.commitText(shift ? key.toUpperCase(java.util.Locale.ROOT) : key, 1);
                if (shift) {
                    shift = false;
                    rebuildRows();
                }
        }
    }
}
