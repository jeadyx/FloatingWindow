一个JetpackCompose Android扩展库，可以用来展示悬浮窗，方便好用

1. 添加依赖
```kotlin
implementation("io.github.jeadyx.compose:floatingWindow:1.0")
```

2. 响应式示例
```kotlin
var displayFloatingView by remember { mutableStateOf(true) }

if (displayFloatingView) {
    FloatingWindow(draggable = true, autoKeepSide = false) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("悬浮内容")
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
}
```

![效果简示](imgs/sample.gif)