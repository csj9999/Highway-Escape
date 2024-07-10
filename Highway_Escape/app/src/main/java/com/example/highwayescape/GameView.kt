package com.example.highwayescape

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast


class GameView(context: Context, private val gameTask: GameTask) : View(context) {
    private var isPaused = false  // Pause flag
    private val myPaint = Paint()
    private var speed = 1
    private var time = 0
    private var score = 0
    private var myCarPosition = 0
    private val otherCars = mutableListOf<HashMap<String, Any>>()

    // GestureDetector to handle double-tap gestures
    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                setPaused(!isPaused)  // Toggle the pause state
                return true
            }
        })

    fun setPaused(paused: Boolean) {
        isPaused = paused
        if (paused) {
            Toast.makeText(context, "Game Paused", Toast.LENGTH_SHORT).show()  // Notify when paused
        } else {
            Toast.makeText(context, "Game Resumed", Toast.LENGTH_SHORT)
                .show()  // Notify when resumed
        }
        invalidate()  // Request redraw to reflect pause/resume state
    }

    override fun onDraw(canvas: Canvas) {
        if (isPaused) return  // If paused, skip drawing

        super.onDraw(canvas)

        // Get view dimensions
        val viewWidth = measuredWidth
        val viewHeight = measuredHeight

        // Draw the player's car (Lambo)
        val lamboX = myCarPosition * (viewWidth / 3) + (viewWidth / 15) + 25
        val lamboY = viewHeight - 20

        val lamboDrawable = resources.getDrawable(R.drawable.lambo, null)
        lamboDrawable.setBounds(
            lamboX,
            lamboY - 160,  // Lambo height
            lamboX + 120 - 25,  // Lambo width
            lamboY
        )
        lamboDrawable.draw(canvas)

        // Create other cars periodically
        if (time % 700 < 10 + speed) {
            val newCar = HashMap<String, Any>()
            newCar["lane"] = (0..2).random()  // Random lane
            newCar["startTime"] = time  // Current game time
            otherCars.add(newCar)
        }

        time += 10 + speed  // Increment game time

        // Draw police cars and check for collisions
        val carsToRemove = mutableListOf<Int>()
        for (i in otherCars.indices) {
            try {
                val lane = otherCars[i]["lane"] as Int
                val carX = lane * (viewWidth / 3) + (viewWidth / 28)
                val carY = time - (otherCars[i]["startTime"] as Int)

                val policeDrawable = resources.getDrawable(R.drawable.police, null)

                policeDrawable.setBounds(
                    carX + 15,
                    carY - 200,  // Police car height
                    carX + 190 - 30,  // Police car width
                    carY
                )
                policeDrawable.draw(canvas)

                // Check for collisions with the player's car
                if (lane == myCarPosition && carY > (viewHeight - 2 - 200) && carY < (viewHeight - 2)) {
                    gameTask.closeGame(score)  // End the game on collision
                }

                // Remove off-screen cars and increase score
                if (carY > (viewHeight + 200)) {
                    carsToRemove.add(i)
                    score++
                    speed = 1 + (score / 8)  // Increase speed with score
                }
            } catch (e: Exception) {
                Log.e("GameView", "Error drawing police cars: ${e.message}")  // Log errors
            }
        }

        // Remove off-screen cars
        carsToRemove.sortDescending()  // Sort indices in descending order to avoid index errors
        for (index in carsToRemove) {
            otherCars.removeAt(index)
        }

        // Display the score and speed
        myPaint.color = Color.WHITE
        myPaint.textSize = 40f
        canvas.drawText("Score: $score", 80f, 80f, myPaint)  // Display current score
        canvas.drawText("Speed: $speed", 380f, 80f, myPaint)  // Display current speed

        // Request redraw
        invalidate()  // Request re-draw after rendering
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            gestureDetector.onTouchEvent(event)  // Handle gestures

            if (!isPaused) {  // If game is not paused, allow car movement
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val x1 = event.x
                        if (x1 < measuredWidth / 2) {  // Tap left
                            if (myCarPosition > 0) {
                                myCarPosition--
                            }
                        } else {  // Tap right
                            if (myCarPosition < 2) {
                                myCarPosition++
                            }
                        }
                        invalidate()  // Request redraw to reflect new position
                    }
                }
            }
        }
        return true
    }
}
