package io.github.jeadyx.floatingWindowSample

import android.app.Instrumentation
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.github.jeadyx.compose.floatingwindow.FloatingWindow
import io.github.jeadyx.compose.floatingwindow.FloatingWindowSample
import io.github.jeadyx.floatingWindowSample.ui.theme.AutoClickTheme
import java.lang.Thread.sleep
import kotlin.concurrent.thread


class MainActivity : ComponentActivity(), LifecycleOwner {
    private var displayFloatingView by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoClickTheme {
                Scaffold {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(it), contentAlignment = Alignment.Center) {
                        if (displayFloatingView) {
                            Text("已进入悬浮窗模式， 你可以前往其他页面判断悬浮窗是否正常显示")
                            FloatingWindowSample()
                            FloatingWindow(draggable = true, autoKeepSide = false) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(Modifier.clip(CircleShape)) {
                                        Image(
                                            painterResource(R.drawable.ic_launcher_background),
                                            null
                                        )
                                        Image(
                                            painterResource(R.drawable.ic_launcher_foreground),
                                            null
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            displayFloatingView = false
                                        },
                                        colors = ButtonDefaults.buttonColors()
                                            .copy(containerColor = Color(0xffa00000))
                                    ) {
                                        Text("Close")
                                    }
                                }
                            }
                        } else {
                            Menu()
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Menu() {
        val TAG = "MainActivity Menu"
        val context = LocalContext.current
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            var hasOverlayPermission by remember{ mutableStateOf(Settings.canDrawOverlays(context)) }
            Button(onClick = {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = android.net.Uri.parse("package:${context.packageName}")
                startActivity(intent)
            }) {
                Text(if (hasOverlayPermission) "取消悬浮窗权限" else "申请悬浮窗权限")
            }
            Button(onClick = {
                displayFloatingView = !displayFloatingView
            }, enabled = hasOverlayPermission) {
                Text(if (displayFloatingView) "关闭悬浮窗" else "显示悬浮窗")
            }
            LaunchedEffect(key1 = true) {
                lifecycle.addObserver(object : DefaultLifecycleObserver {
                    override fun onResume(owner: LifecycleOwner) {
                        super.onResume(owner)
                        hasOverlayPermission = Settings.canDrawOverlays(context)
                    }
                })
            }
        }
    }
}