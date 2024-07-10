package com.example.highwayescape

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity(), GameTask {
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var startBtn: Button
    private lateinit var highestScoreBtn: Button
    private lateinit var resetHighestScoreBtn: Button  // New reset button
    private lateinit var scoreTextView: TextView
    private lateinit var gameView: GameView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("HighwayEscape", MODE_PRIVATE)

        rootLayout = findViewById(R.id.rootLayout)
        startBtn = findViewById(R.id.startBtn)
        highestScoreBtn = findViewById(R.id.highestScoreBtn)
        resetHighestScoreBtn = findViewById(R.id.resetHighestScoreBtn)  // Get the reset button
        scoreTextView = findViewById(R.id.score)

        startBtn.setOnClickListener {
            gameView = GameView(this, this)
            gameView.setBackgroundResource(R.drawable.road)  // Set the background
            rootLayout.addView(gameView)  // Add the game view to the layout
            startBtn.visibility = View.GONE
            highestScoreBtn.visibility = View.GONE
            resetHighestScoreBtn.visibility = View.GONE
            scoreTextView.visibility = View.GONE
        }

        highestScoreBtn.setOnClickListener {
            val highestScore = sharedPreferences.getInt("HighestScore", 0)
            scoreTextView.text = "Highest Score:\n$highestScore"
            scoreTextView.visibility = View.VISIBLE
            resetHighestScoreBtn.visibility = View.VISIBLE  // Show reset button when viewing highest score
        }

        resetHighestScoreBtn.setOnClickListener {
            // Reset the highest score to zero
            val editor = sharedPreferences.edit()
            editor.putInt("HighestScore", 0)  // Reset the highest score
            editor.apply()

            // Update the score display to reflect the reset
            scoreTextView.text = "Highest Score: 0"
            Toast.makeText(this, "Highest Score Reset", Toast.LENGTH_SHORT).show()  // Notify the user
        }
    }

    override fun closeGame(finalScore: Int) {
        rootLayout.removeView(gameView)  // Remove the game view on game over
        scoreTextView.text = "Score: $finalScore"
        scoreTextView.visibility = View.VISIBLE
        startBtn.visibility = View.VISIBLE
        highestScoreBtn.visibility = View.VISIBLE
        resetHighestScoreBtn.visibility = View.GONE  // Hide the reset button after the game ends

        // Check and update the highest score if needed
        val highestScore = sharedPreferences.getInt("HighestScore", 0)
        if (finalScore > highestScore) {
            val editor = sharedPreferences.edit()
            editor.putInt("HighestScore", finalScore)  // Save the new highest score
            editor.apply()
        }
    }
}
