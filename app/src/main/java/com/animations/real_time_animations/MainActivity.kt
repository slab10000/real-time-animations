package com.animations.real_time_animations

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import com.animations.real_time_animations.ui.theme.RealtimeanimationsTheme
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import java.lang.Thread.sleep

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RealtimeanimationsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val dotDrawing = remember { mutableStateOf(Dot(position = Offset(100f,100f))) }
                    val screenDimensions = Offset(resources.displayMetrics.widthPixels.toFloat(), resources.displayMetrics.heightPixels.toFloat()).also { println("estas son las dimensiones $it") }
                    var positionOnX: Float
                    var positionOnY: Float
                    var velocityOnX: Float
                    var velocityOnY: Float

                    LaunchedEffect(Unit) {
                        while (true) {
                            awaitFrame()

                            positionOnX = dotDrawing.value.position.x + dotDrawing.value.velocity.x
                            velocityOnX = dotDrawing.value.velocity.x

                            /** Check X overflow **/
                            when{
                                positionOnX > screenDimensions.x -> {
                                    positionOnX -= positionOnX - screenDimensions.x
                                    velocityOnX = -velocityOnX
                                }

                                positionOnX < 0 -> {
                                    positionOnX = -positionOnX
                                    velocityOnX = -velocityOnX
                                }
                            }

                            positionOnY = dotDrawing.value.position.y + dotDrawing.value.velocity.y
                            velocityOnY = dotDrawing.value.velocity.y

                            /** Check Y overflow **/
                            when {
                                positionOnY > screenDimensions.y -> {
                                    positionOnY -= positionOnY - screenDimensions.y
                                    velocityOnY = -velocityOnY
                                }
                                positionOnY < 0 -> {
                                    positionOnY = -positionOnY
                                    velocityOnY = -velocityOnY
                                }
                            }

                            dotDrawing.value = dotDrawing.value.copy(position = Offset(positionOnX, positionOnY), velocity = Offset(velocityOnX, velocityOnY))
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize()){

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Color(0xffa3b18a)),
                        ) {
                            drawCircle(
                                color = Color.Black,
                                radius = 10f,
                                center = dotDrawing.value.position,
                            )
                        }
                    }
                }
            }
        }
    }
}

data class Dot(
    var position: Offset = Offset(x = 100f, y = 100f),
    var velocity: Offset = Offset(x = 10f, y = 10f)
)
