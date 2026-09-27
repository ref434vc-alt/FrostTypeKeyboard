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

## v0.5 Kyant AndroidLiquidGlass
Actual Kyant Backdrop 2.0.1 is used for refraction, blur, and vibrancy on the keyboard's glass keys. The keys are back in the real input view (not candidates), with the English QWERTY layout and haptic press feedback. Kyant's Backdrop captures pixels within FrostType's own Compose view; it **cannot access the other app's pixels**. FrostType still requests OS cross-window blur, which One UI may disable for keyboard windows. Compile SDK is 37; Gradle 8.13 and Kotlin 2.4.10 are required.
Source: https://github.com/Kyant0/AndroidLiquidGlass (Apache 2.0).
