package com.spaceexplorer

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds

class GameActivity : AppCompatActivity(), GameView.GameListener {
    
    private lateinit var gameView: GameView
    private lateinit var scoreText: TextView
    private lateinit var pauseButton: ImageButton
    private lateinit var pauseOverlay: View
    private lateinit var gameOverOverlay: View
    private lateinit var finalScoreText: TextView
    private lateinit var newHighScoreText: TextView
    private lateinit var lifeViews: Array<ImageView>
    
    private lateinit var sharedPreferences: SharedPreferences
    private var interstitialAd: InterstitialAd? = null
    private var gameOverCount = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SpaceExplorerPrefs", MODE_PRIVATE)
        
        // Initialize AdMob
        MobileAds.initialize(this) {}
        loadInterstitialAd()
        
        // Initialize UI
        initializeUI()
        
        // Set up game view
        gameView = findViewById(R.id.gameView)
        gameView.gameListener = this
    }
    
    private fun initializeUI() {
        scoreText = findViewById(R.id.scoreText)
        pauseButton = findViewById(R.id.pauseButton)
        pauseOverlay = findViewById(R.id.pauseOverlay)
        gameOverOverlay = findViewById(R.id.gameOverOverlay)
        finalScoreText = findViewById(R.id.finalScoreText)
        newHighScoreText = findViewById(R.id.newHighScoreText)
        
        // Life indicators
        lifeViews = arrayOf(
            findViewById(R.id.life1),
            findViewById(R.id.life2),
            findViewById(R.id.life3)
        )
        
        // Pause button
        pauseButton.setOnClickListener {
            pauseGame()
        }
        
        // Pause overlay buttons
        findViewById<Button>(R.id.resumeButton).setOnClickListener {
            resumeGame()
        }
        
        findViewById<Button>(R.id.mainMenuButton).setOnClickListener {
            returnToMainMenu()
        }
        
        // Game over overlay buttons
        findViewById<Button>(R.id.restartButton).setOnClickListener {
            restartGame()
        }
        
        findViewById<Button>(R.id.gameOverMainMenuButton).setOnClickListener {
            returnToMainMenu()
        }
    }
    
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, getString(R.string.interstitial_ad_unit_id), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
            }
            
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                interstitialAd = null
            }
        })
    }
    
    private fun pauseGame() {
        gameView.pauseGame()
        pauseOverlay.visibility = View.VISIBLE
    }
    
    private fun resumeGame() {
        gameView.resumeGame()
        pauseOverlay.visibility = View.GONE
    }
    
    private fun restartGame() {
        gameView.restartGame()
        gameOverOverlay.visibility = View.GONE
        newHighScoreText.visibility = View.GONE
    }
    
    private fun returnToMainMenu() {
        finish()
    }
    
    private fun showInterstitialAd() {
        interstitialAd?.let { ad ->
            ad.show(this)
            loadInterstitialAd() // Load next ad
        }
    }
    
    override fun onScoreChanged(score: Int) {
        runOnUiThread {
            scoreText.text = "Счет: $score"
        }
    }
    
    override fun onLifeChanged(lives: Int) {
        runOnUiThread {
            updateLifeDisplay(lives)
        }
    }
    
    override fun onLifeLost(lives: Int) {
        runOnUiThread {
            updateLifeDisplay(lives)
            // Add vibration effect
            try {
                val vibrator = getSystemService(VIBRATOR_SERVICE) as android.os.Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(200)
                }
            } catch (e: Exception) {
                // Vibration not available
            }
        }
    }
    
    override fun onLifeGained(lives: Int) {
        runOnUiThread {
            updateLifeDisplay(lives)
        }
    }
    
    override fun onGameOver(finalScore: Int) {
        runOnUiThread {
            finalScoreText.text = "Счет: $finalScore"
            
            // Check for high score
            val currentHighScore = sharedPreferences.getInt("high_score", 0)
            if (finalScore > currentHighScore) {
                sharedPreferences.edit().putInt("high_score", finalScore).apply()
                newHighScoreText.visibility = View.VISIBLE
            }
            
            gameOverOverlay.visibility = View.VISIBLE
            
            // Show interstitial ad every 3rd game over
            gameOverCount++
            if (gameOverCount % 3 == 0) {
                showInterstitialAd()
            }
        }
    }
    
    private fun updateLifeDisplay(lives: Int) {
        for (i in lifeViews.indices) {
            lifeViews[i].alpha = if (i < lives) 1.0f else 0.3f
        }
    }
    
    override fun onBackPressed() {
        if (gameOverOverlay.visibility == View.VISIBLE) {
            returnToMainMenu()
        } else if (pauseOverlay.visibility == View.VISIBLE) {
            resumeGame()
        } else {
            pauseGame()
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (pauseOverlay.visibility == View.GONE && gameOverOverlay.visibility == View.GONE) {
            pauseGame()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        interstitialAd = null
    }
}