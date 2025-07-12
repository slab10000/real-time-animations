package com.animations.real_time_animations

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.animations.real_time_animations.ui.theme.RealtimeanimationsTheme
import kotlinx.coroutines.android.awaitFrame
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RealtimeanimationsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val screenDimensions = Offset(resources.displayMetrics.widthPixels.toFloat(), resources.displayMetrics.heightPixels.toFloat())

                    val groupOfDotsDrawing = remember { mutableStateOf(GroupOfDotsAndLines(screenDimensions = screenDimensions, numberOfDots = 100)) }

                    LaunchedEffect(Unit) {
                        while (true) {
                            awaitFrame()
                            groupOfDotsDrawing.value = groupOfDotsDrawing.value.next()
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize()){

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
//                                .background(color = Color(0xffa3b18a)),
                                .background(color = Color.Black),
                        ) {
                            groupOfDotsDrawing.value.lines.forEach { line ->
                                drawLine(
                                    color = Color.White,
                                    start = line.first,
                                    end = line.second,
                                    strokeWidth = 10 - (9 * (line.first - line.second).getDistance() / threshHoldDistance),
                                    alpha = 1 - (1 * (line.first - line.second).getDistance() / threshHoldDistance)
                                )
                            }

                            groupOfDotsDrawing.value.dots.forEach {
                                drawCircle(
                                    color = Color.White,
                                    radius = 10f,
                                    center = it.position,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Class for 1 Dot **/
data class Dot(
    var position: Offset = Offset(x = 100f, y = 100f),
    var velocity: Offset = Offset(x = 1f, y = 1f)
){

    fun next(screenDimensions: Offset): Dot{
        var positionOnX: Float
        var positionOnY: Float
        var velocityOnX: Float
        var velocityOnY: Float

        positionOnX = position.x + velocity.x * dampingCoefficient
        velocityOnX = velocity.x

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

        positionOnY = position.y + velocity.y * dampingCoefficient
        velocityOnY = velocity.y

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
        return Dot(position = Offset(positionOnX, positionOnY), velocity = Offset(velocityOnX,velocityOnY))
    }

     infix fun distanceTo(anotherDot: Dot): Float{
        return (position - anotherDot.position).getDistance()
    }
}

data class GroupOfDotsAndLines(
    val screenDimensions: Offset,
    val numberOfDots: Int = 0,
    var dots: Array<Dot> = Array(numberOfDots){ Dot(
        position = Offset(Random.nextInt(0, screenDimensions.x.toInt()).toFloat(), Random.nextInt(0,screenDimensions.y.toInt()).toFloat()),
        velocity = Offset(getDotVelocity(), getDotVelocity()),
    )
    },
    var lines:MutableList<Pair<Offset, Offset>> = arrayListOf()
){
    fun next(): GroupOfDotsAndLines{
        return GroupOfDotsAndLines(
            screenDimensions = screenDimensions,
            numberOfDots = numberOfDots,
            dots = dots.map { it.next(screenDimensions) }.toTypedArray(),
            lines = nextArrayOfLines()
        )
    }

    private fun nextArrayOfLines(): MutableList<Pair<Offset, Offset>>{
        val newLines: MutableList<Pair<Offset, Offset>> = arrayListOf()
        dots.forEachIndexed{ index, dot ->
            for (i in (index + 1) until dots.size){
                val followingDot = dots[i]
                if(dot distanceTo followingDot < threshHoldDistance){
                    newLines.add(Pair(dot.position, followingDot.position))
                }
            }
        }
        return newLines
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupOfDotsAndLines

        if (numberOfDots != other.numberOfDots) return false
        if (!dots.contentEquals(other.dots)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = numberOfDots
        result = 31 * result + dots.contentHashCode()
        return result
    }

    companion object{
        private fun getDotVelocity() = when (Random.nextInt(0,2)){
            0 -> -1 * Random.nextFloat()
            else -> 1 * Random.nextFloat()
        }
    }

}

const val threshHoldDistance = 200f
const val dampingCoefficient = 0.5f
