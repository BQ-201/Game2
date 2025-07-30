package com.spaceexplorer

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener

class MainActivity : AppCompatActivity() {
    
    private lateinit var adView: AdView
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize SharedPreferences for high score
        sharedPreferences = getSharedPreferences("SpaceExplorerPrefs", MODE_PRIVATE)
        
        // Initialize AdMob
        MobileAds.initialize(this) { initializationStatus: InitializationStatus -> }
        
        // Initialize AdView
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        
        // Initialize UI
        setupUI()
        updateHighScoreDisplay()
    }
    
    private fun setupUI() {
        val playButton = findViewById<Button>(R.id.playButton)
        val highScoreButton = findViewById<Button>(R.id.highScoreButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        val exitButton = findViewById<Button>(R.id.exitButton)
        
        playButton.setOnClickListener {
            startGame()
        }
        
        highScoreButton.setOnClickListener {
            showHighScores()
        }
        
        settingsButton.setOnClickListener {
            showSettings()
        }
        
        exitButton.setOnClickListener {
            finish()
        }
    }
    
    private fun startGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }
    
    private fun showHighScores() {
        // TODO: Implement high scores dialog
        val highScore = sharedPreferences.getInt("high_score", 0)
        // For now, just show a simple message
        android.app.AlertDialog.Builder(this)
            .setTitle("Лучший результат")
            .setMessage("Ваш лучший результат: $highScore")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showSettings() {
        // TODO: Implement settings dialog
        android.app.AlertDialog.Builder(this)
            .setTitle("Настройки")
            .setMessage("Настройки будут добавлены в следующем обновлении")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun updateHighScoreDisplay() {
        val highScoreText = findViewById<TextView>(R.id.highScoreText)
        val highScore = sharedPreferences.getInt("high_score", 0)
        highScoreText.text = "Лучший результат: $highScore"
    }
    
    override fun onResume() {
        super.onResume()
        updateHighScoreDisplay()
        adView.resume()
    }
    
    override fun onPause() {
        super.onPause()
        adView.pause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        adView.destroy()
    }
}