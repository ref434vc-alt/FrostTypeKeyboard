package com.example.frosttype

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.drawRect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

// The backdrop library captures OUR keyboard's sampled texture, not another app's pixels.
// OS-level blur behind the whole keyboard is configured by FrostKeyboardService.
object FrostGlassViewFactory {
    @JvmStatic
    fun create(service: FrostKeyboardService): View {
        val view = ComposeView(service)
        val owner = InputComposeOwner()
        ViewTreeLifecycleOwner.set(view, owner)
        ViewTreeViewModelStoreOwner.set(view, owner)
        ViewTreeSavedStateRegistryOwner.set(view, owner)
        view.setContent { FrostGlassKeys(service) }
        return view
    }
}

private class InputComposeOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val registry = LifecycleRegistry(this)
    private val saved = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = registry
    override val viewModelStore = ViewModelStore()
    override val savedStateRegistry: SavedStateRegistry get() = saved.savedStateRegistry
    init {
        saved.performAttach()
        saved.performRestore(null)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }
}

private val row1 = listOf("q","w","e","r","t","y","u","i","o","p")
private val row2 = listOf("a","s","d","f","g","h","j","k","l")
private val row3 = listOf("⇧","z","x","c","v","b","n","m","⌫")
private val symbol1 = listOf("1","2","3","4","5","6","7","8","9","0")
private val symbol2 = listOf("@","#","$","%","&","-","+","(",")","/")
private val symbol3 = listOf("ABC","*","\"","'",":",";","!","?","⌫")

@Composable
private fun FrostGlassKeys(service: FrostKeyboardService) {
    var uppercase by remember { mutableStateOf(false) }
    var symbols by remember { mutableStateOf(false) }
    val backdrop = rememberLayerBackdrop()
    val keyPress: (String) -> Unit = { label ->
        service.pressKey(label)
        when (label) {
            "⇧" -> uppercase = !uppercase
            "123" -> { symbols = true; uppercase = false }
            "ABC" -> { symbols = false; uppercase = false }
            else -> if (uppercase && label.length == 1) uppercase = false
        }
    }
    Box(Modifier.fillMaxWidth().height(277.dp)) {
        // A softly varied internal scene to refract under each key. This remains
        // translucent so window-level Android blur can show through if supported.
        Box(
            Modifier.fillMaxSize()
                .layerBackdrop(backdrop)
                .background(Brush.verticalGradient(listOf(
                    Color(0x22F7FCFF), Color(0x2962B4F5), Color(0x2864D6F9)
                )))
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    Brush.radialGradient(listOf(Color(0x5677C4FF), Color.Transparent),
                        center = Offset(size.width * .16f, size.height * .24f),
                        radius = size.width * .83f),
                    center = Offset(size.width * .16f, size.height * .24f),
                    radius = size.width * .83f
                )
                drawCircle(
                    Brush.radialGradient(listOf(Color(0x3DE5F0FF), Color.Transparent),
                        center = Offset(size.width * .86f, size.height * .93f),
                        radius = size.width * .72f),
                    center = Offset(size.width * .86f, size.height * .93f),
                    radius = size.width * .72f
                )
            }
        }
        Column(
            Modifier.fillMaxSize().padding(start = 5.dp, end = 5.dp, top = 12.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            val rows = if (symbols) listOf(symbol1,symbol2,symbol3)
                       else listOf(row1,row2,row3)
            rows.forEach { row ->
                Row(Modifier.fillMaxWidth().height(53.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    row.forEach { label ->
                        val special = label == "⇧" || label == "⌫" || label == "ABC"
                        GlassKey(label, if (special) 1.48f else 1f,
                            if (uppercase && label.length == 1 && label[0].isLetter())
                                label.uppercase() else label,
                            special, false, backdrop, keyPress)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().height(56.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                GlassKey(if(symbols) "ABC" else "123",1.4f,if(symbols) "ABC" else "123",true,false,backdrop,keyPress)
                GlassKey("space",4.1f,"space",false,false,backdrop,keyPress)
                GlassKey("➜",1.45f,"➜",false,true,backdrop,keyPress)
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.GlassKey(
    label: String,
    widthWeight: Float,
    displayed: String,
    special: Boolean,
    enter: Boolean,
    backdrop: Backdrop,
    onTap: (String) -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressedScale by animateFloatAsState(if (pressed) .94f else 1f, label = "pressScale")
    val view = LocalView.current
    val tint = if (enter) Color(0x99569FFB)
               else if (special) Color(0x597DA9DA)
               else Color(0x48FFFFFF)
    Box(
        Modifier.weight(widthWeight).fillMaxWidth().height(if (enter) 54.dp else 51.dp)
            .graphicsLayer { scaleX = pressedScale; scaleY = pressedScale; translationY = if (pressed) 3.dp.toPx() else 0f }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(12.dp) },
                effects = {
                    vibrancy()
                    blur(16.dp.toPx())
                    lens(9.dp.toPx(), 17.dp.toPx())
                },
                shadow = null,
                onDrawSurface = { drawRect(if(pressed) tint.copy(alpha=.85f) else tint) }
            )
            .clickable(interactionSource=interaction, indication=null) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onTap(label)
            },
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            displayed,
            style = TextStyle(
                color = if (enter) Color.White else Color(0xFF24334D),
                fontSize = if (label == "space") 12.sp else if (label == "➜") 29.sp else 22.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )
    }
}
