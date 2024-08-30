package io.github.jeadyx.floatingWindowSample

import android.app.Instrumentation
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import java.lang.Thread.sleep
import kotlin.concurrent.thread

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AutoClickSample() {
    val context = LocalContext.current
    var collecting by remember { mutableStateOf(false) }
    var pointer by remember {
        mutableStateOf(
            Offset(
                context.resources.displayMetrics.widthPixels / 2f,
                context.resources.displayMetrics.heightPixels / 2f
            )
        )
    }
    var lastPointer by remember { mutableStateOf(pointer) }
    val instrumentation = remember { Instrumentation() }
    Column{
        Button(onClick = {
            thread {
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_UP)
            }
        }) {
            Text("Up")
        }
        Button(onClick = {
            thread {
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_DOWN)
            }
        }) {
            Text("Down")
        }
        Button(onClick = {
            thread {
                sleep(1000)
                instrumentation.sendPointerSync(
                    MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_DOWN,
                        pointer.x,
                        pointer.y,
                        0
                    )
                )
                sleep(1000)
                instrumentation.sendPointerSync(
                    MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP,
                        pointer.x,
                        pointer.y,
                        0
                    )
                )
//                instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 600f, 600f, 0))
//                instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 600f, 600f, 0))
            }
        }) {
            Text("Click")
        }
        Button(onClick = {
            collecting = !collecting
            pointer = lastPointer
        }) {
            Text(if (collecting) "停止采集" else "采集")
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInteropFilter {
                if (collecting) {
                    lastPointer = pointer
                    pointer = Offset(it.x, it.y)
                }
                return@pointerInteropFilter false
            }
    )
}