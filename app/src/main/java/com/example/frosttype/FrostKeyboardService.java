package com.example.frosttype;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
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
    private static final int SPECIAL_TEXT = 0xFF31445D;
    private static final int PANEL_STROKE = 0x7AFFFFFF;
    private static final int KEY_STROKE = 0x99FFFFFF;
    private boolean shift = false;
    private boolean symbols = false;
    private boolean blurAvailable = false;
    private LinearLayout root;

    private int dp(float px) {
        return (int)(px * getResources().getDisplayMetrics().density + .5f);
    }

    // Gradients, edge highlights, and transparency mimic liquid glass.
    // Android only blurs the app behind the keyboard if it permits cross-window blur here.
    private GradientDrawable panelDrawable(boolean blur) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            blur ? new int[]{0x67F7FBFF, 0x48B5D9FF, 0x76E0F0FF}
                 : new int[]{0xEBD9E8FD, 0xD5AFCFEB, 0xE9DCEFFF});
        d.setCornerRadius(dp(28));
        d.setStroke(dp(1), PANEL_STROKE);
        return d;
    }

    private GradientDrawable keyDrawable(int topColor, int bottomColor, int border) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, new int[]{topColor, bottomColor});
        d.setCornerRadius(dp(14));
        d.setStroke(dp(1), border);
        return d;
    }

    private void updateGlass() {
        if (Build.VERSION.SDK_INT >= 31) {
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            blurAvailable = wm != null && wm.isCrossWindowBlurEnabled();
        } else blurAvailable = false;
        Window win = getWindow() == null ? null : getWindow().getWindow();
        if (win != null) {
            win.setBackgroundDrawable(panelDrawable(blurAvailable));
            if (Build.VERSION.SDK_INT >= 31) {
                win.setBackgroundBlurRadius(blurAvailable ? dp(110) : 0);
            }
        }
        if (root != null) {
            // Keep this layer translucent rather than covering the blur with opaque white.
            root.setBackground(panelDrawable(blurAvailable));
        }
    }

    @Override public void onWindowShown() {
        super.onWindowShown();
        updateGlass();
    }

    @Override public View onCreateInputView() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(6), dp(18), dp(6), dp(11));
        root.setClipToPadding(false);
        View clearHeader = new View(this);
        root.addView(clearHeader, new LinearLayout.LayoutParams(-1, dp(28)));
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
            addKey(row, label, special ? 1.52f : 1f, special, false);
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
        addKey(row, symbols ? "ABC" : "123", 1.45f, true, false);
        addKey(row, "space", 4.2f, false, false);
        addKey(row, "➜", 1.45f, false, true);
        root.addView(row);
    }

    // Pressed keycaps dip, darken, contract, and generate native keyboard haptics.
    private void addKey(LinearLayout row, String label, float weight, boolean special, boolean enterKey) {
        final TextView key = new TextView(this);
        String rendered = (shift && label.length() == 1 && Character.isLetter(label.charAt(0)))
                ? label.toUpperCase(java.util.Locale.ROOT) : label;
        key.setText(rendered);
        key.setTextSize(label.equals("space") ? 12 : enterKey ? 31 : label.equals("⇧") || label.equals("⌫") ? 25 : 23);
        key.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        key.setTextColor(enterKey ? Color.WHITE : special ? SPECIAL_TEXT : DARK_TEXT);
        key.setGravity(Gravity.CENTER);

        final GradientDrawable normalBackground;
        final GradientDrawable pressedBackground;
        if (enterKey) {
            normalBackground = keyDrawable(0xFF72B6FF, 0xED2B78E7, 0xA0C4E7FF);
            pressedBackground = keyDrawable(0xFF387BD9, 0xFF1550AD, 0xC0E5F5FF);
        } else if (special) {
            normalBackground = keyDrawable(0xD2DBEAFE, 0x91B9D3EF, 0x88F3FBFF);
            pressedBackground = keyDrawable(0xE0B3CDEB, 0xB67C9EC8, 0xAFFBFFFF);
        } else {
            normalBackground = keyDrawable(0xD9FFFFFF, 0x8DD4E7FA, KEY_STROKE);
            pressedBackground = keyDrawable(0xE6ACD3FA, 0xB879B1EA, 0xBBFFFFFF);
        }
        key.setBackground(normalBackground);
        key.setElevation(dp(2.6f));
        key.setHapticFeedbackEnabled(true);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -1, weight);
        p.leftMargin = dp(2);
        p.rightMargin = dp(2);
        row.addView(key, p);
        key.setOnClickListener(v -> press(label));
        key.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    key.animate().cancel();
                    key.setBackground(pressedBackground);
                    key.setScaleX(.94f);
                    key.setScaleY(.94f);
                    key.setTranslationY(dp(2.5f));
                    key.setElevation(dp(.4f));
                    key.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    return true;
                case MotionEvent.ACTION_UP:
                    key.setBackground(normalBackground);
                    key.animate().scaleX(1f).scaleY(1f).translationY(0).setDuration(95).start();
                    key.setElevation(dp(2.6f));
                    if (event.getX() >= 0 && event.getX() < key.getWidth()
                            && event.getY() >= 0 && event.getY() < key.getHeight()) {
                        v.performClick();
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    key.setBackground(normalBackground);
                    key.animate().scaleX(1f).scaleY(1f).translationY(0).setDuration(95).start();
                    key.setElevation(dp(2.6f));
                    return true;
                default:
                    return true;
            }
        });
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
