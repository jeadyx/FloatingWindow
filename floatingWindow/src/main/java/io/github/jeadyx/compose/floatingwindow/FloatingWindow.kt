package io.github.jeadyx.compose.floatingwindow

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.view.size
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlin.math.abs

@Composable
fun FloatingWindow(view: View, layoutParams: LayoutParams) {
    val context = LocalContext.current
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    DisposableEffect(Unit) {
        windowManager.addView(view, layoutParams)
        onDispose {
            windowManager.removeView(view)
        }
    }
}

@Composable
fun FloatingWindow(layoutParams: LayoutParams, draggable: Boolean=true, autoKeepSide: Boolean=true, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val lifeCycleOwner = LocalLifecycleOwner.current
    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
    DisposableEffect(Unit) {
        val view = ComposeView(context)
        view.setContent {
            Box(Modifier.composed {
                if(draggable){
                    return@composed dragLayout(view, layoutParams, autoKeepSide)
                }
                this
            }){ content() }
        }
        view.setViewTreeLifecycleOwner(lifeCycleOwner)
        view.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
        windowManager.addView(view, layoutParams)
        onDispose {
            windowManager.removeView(view)
        }
    }
}


@Composable
fun FloatingWindow(draggable: Boolean=true, autoKeepSide: Boolean=true, content: @Composable () -> Unit) {
    val defaultLayoutParams = LayoutParams(
        LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT,
        LayoutParams.TYPE_APPLICATION_OVERLAY,
        LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.CENTER
    }
    FloatingWindow(defaultLayoutParams, draggable=draggable, autoKeepSide=autoKeepSide){
        content()
    }
}

/**
 * 一个可以拖动并自动靠边的小窗
 * 你可以点击按钮，让数字加1
 */
@Composable
fun FloatingWindowSample() {
    val defaultLayoutParams = LayoutParams(
        LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT,
        LayoutParams.TYPE_APPLICATION_OVERLAY,
        LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.CENTER
    }
    FloatingWindow(defaultLayoutParams){
        FloatingContentSample()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.dragLayout(view: ComposeView, layoutParams: LayoutParams, autoKeepSide: Boolean=true): Modifier = this.composed {
    val context = LocalContext.current
    val windowManager = remember{
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    val containerSize = remember{
        IntSize(windowManager.currentWindowMetrics.bounds.width()/2, windowManager.currentWindowMetrics.bounds.height()/2)
    }
    var viewSize = IntSize(view.width, view.height)
    var maxPos = IntOffset(containerSize.width - viewSize.width/2, containerSize.height - viewSize.height/2)
    var minPos = IntOffset(-containerSize.width + viewSize.width/2,-containerSize.height + viewSize.height/2)
    val listener = ViewTreeObserver.OnGlobalLayoutListener {
        if(viewSize != IntSize(view.width, view.height)){
            viewSize = IntSize(view.width, view.height)
            maxPos = IntOffset(containerSize.width - viewSize.width/2, containerSize.height - viewSize.height/2)
            minPos = IntOffset(-containerSize.width + viewSize.width/2,-containerSize.height + viewSize.height/2)
        }
    }
    DisposableEffect(Unit) {
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { 
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
    val drag2dState = rememberDraggable2DState {
        val tarPos = IntOffset(
            (layoutParams.x + it.x.toInt()).coerceIn(minPos.x, maxPos.x),
            (layoutParams.y + it.y.toInt()).coerceIn(minPos.y, maxPos.y)
        )
        layoutParams.x = tarPos.x
        layoutParams.y = tarPos.y
        windowManager.updateViewLayout(view, layoutParams)
    }
    draggable2D(drag2dState,
        onDragStopped = {
            if(autoKeepSide) {
                val shouldPlaceVertical =
                    abs(layoutParams.x).toFloat() / maxPos.x < abs(layoutParams.y).toFloat() / maxPos.y
                var tarPos = if (shouldPlaceVertical) {
                    IntOffset(
                        layoutParams.x,
                        if (layoutParams.y > 0) maxPos.y else minPos.y
                    )
                } else {
                    IntOffset(
                        if (layoutParams.x > 0) maxPos.x else minPos.x,
                        layoutParams.y
                    )
                }
                tarPos = IntOffset(
                    tarPos.x.coerceIn(minPos.x, maxPos.x),
                    tarPos.y.coerceIn(minPos.y, maxPos.y)
                )
                val animation = ValueAnimator.ofObject(
                    IntOffsetEvaluator(),
                    IntOffset(layoutParams.x, layoutParams.y),
                    IntOffset(tarPos.x, tarPos.y)
                )
                animation.duration = 500
                animation.addUpdateListener {
                    val offset = it.animatedValue as IntOffset
                    layoutParams.x = offset.x
                    layoutParams.y = offset.y
                    windowManager.updateViewLayout(view, layoutParams)
                }
                animation.start()
            }
    })
}

@Composable
private fun FloatingContentSample() {
    Column(
        Modifier
            .wrapContentSize()
            .background(Color(0xc0ffffff), RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var count by remember{ mutableIntStateOf(0) }
        Text("Count $count")
        Button({
            count++
        }){
            Text(text = "Click")
        }
    }
}

private class IntOffsetEvaluator: TypeEvaluator<IntOffset> {
    override fun evaluate(fraction: Float, startValue: IntOffset, endValue: IntOffset): IntOffset {
        // 贝塞尔曲线式的计算
        return IntOffset(
            (startValue.x + (endValue.x - startValue.x) * fraction * fraction).toInt(),
            (startValue.y + (endValue.y - startValue.y) * fraction * fraction).toInt()
        )
//        return IntOffset(
//            (startValue.x + (endValue.x - startValue.x) * fraction).toInt(),
//            (startValue.y + (endValue.y - startValue.y) * fraction).toInt()
//        )
    }
}

//private class FadeInFastOutEvaluator: TypeEvaluator<IntOffset> {
//    override fun evaluate(fraction: Float, startValue: IntOffset, endValue: IntOffset): IntOffset {
//        return IntOffset(
//            (startValue.x + )
//        )
//    }
//}