# FrostType Keyboard

English-only iOS-inspired frosted glass keyboard for Android 12+ (including Samsung Galaxy S24).

No root needed. FrostType is an experimental offline keyboard. The OS may not permit actual live cross-window blur for keyboard windows; an opaque frosted fallback is used then.

Open the APK, enable FrostType, select it in the keyboard picker, and try typing in the test box. No INTERNET permission is requested.

The GitHub Actions build uploads the debug APK as `FrostType-English-Only-APK`.


## Liquid-glass update (v0.3)
- Cooler blue translucent panel with stronger cross-window blur attempt.
- Translucent gradient keycaps and icy edge highlights.
- Pressed key feedback: visible dip, darker keycaps, shadow reduction, and keyboard haptics.
- Android/One UI may block actual live blur behind keyboard windows; in that case a tinted translucent fallback is used.


## v0.4 experimental transparency
- Removed the double milky/opaque IME backgrounds and opaque blur-disabled fallback.
- Uses a translucent IME window theme and an alpha-only ice-blue panel.
- Draws keyboard in the candidate area rather than a resizing input view so apps that permit it can remain visible underneath.
- Uses Android cross-window blur where available. Real blur and transparency depend on One UI and the currently focused app; keyboard windows cannot force other apps to draw content behind them.
- Open FrostType Setup, then type in its test field; the gradient background is intended to make transparency visually obvious if the OS allows it.
