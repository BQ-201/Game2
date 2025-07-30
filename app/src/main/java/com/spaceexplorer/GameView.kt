package com.spaceexplorer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.random.Random

class GameView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    
    private var gameThread: GameThread? = null
    private lateinit var spaceship: Spaceship
    private val asteroids = mutableListOf<Asteroid>()
    private val bonuses = mutableListOf<Bonus>()
    private val stars = mutableListOf<Star>()
    private val explosions = mutableListOf<Explosion>()
    
    // Game state
    private var gameState = GameState.PLAYING
    private var score = 0
    private var lives = 3
    private var asteroidSpawnTimer = 0f
    private var bonusSpawnTimer = 0f
    private var difficultyTimer = 0f
    private var gameSpeed = 1f
    
    // Input handling
    private var touchX = 0f
    private var isTouching = false
    
    // Paint objects
    private val paint = Paint().apply { isAntiAlias = true }
    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 48f
        typeface = Typeface.DEFAULT_BOLD
    }
    
    // Background
    private lateinit var backgroundGradient: LinearGradient
    
    var gameListener: GameListener? = null
    
    init {
        holder.addCallback(this)
        initializeGame()
    }
    
    private fun initializeGame() {
        // Initialize spaceship
        spaceship = Spaceship(0f, 0f) // Position will be set in surfaceChanged
        
        // Generate background stars
        generateStars()
    }
    
    private fun generateStars() {
        stars.clear()
        for (i in 0 until 100) {
            val x = Random.nextFloat() * width
            val y = Random.nextFloat() * height
            val size = Random.nextFloat() * 3 + 1
            val speed = Random.nextFloat() * 50 + 25
            stars.add(Star(x, y, size, speed))
        }
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        gameThread = GameThread(holder)
        gameThread?.isRunning = true
        gameThread?.start()
    }
    
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Position spaceship at bottom center
        spaceship.x = (width - spaceship.width) / 2
        spaceship.y = height - spaceship.height - 100f
        
        // Create background gradient
        backgroundGradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(
                Color.parseColor("#0D1421"),
                Color.parseColor("#1E3A8A"),
                Color.parseColor("#0D1421")
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        
        // Regenerate stars for new screen size
        generateStars()
    }
    
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        gameThread?.isRunning = false
        while (retry) {
            try {
                gameThread?.join()
                retry = false
            } catch (e: InterruptedException) {
                // Will try again
            }
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isTouching = true
                touchX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                touchX = event.x
            }
            MotionEvent.ACTION_UP -> {
                isTouching = false
                spaceship.stopMoving()
            }
        }
        return true
    }
    
    private fun update(deltaTime: Float) {
        if (gameState != GameState.PLAYING) return
        
        // Handle input
        if (isTouching) {
            val spaceshipCenter = spaceship.x + spaceship.width / 2
            if (touchX < spaceshipCenter - 20) {
                spaceship.moveLeft(deltaTime, width)
            } else if (touchX > spaceshipCenter + 20) {
                spaceship.moveRight(deltaTime, width)
            }
        }
        
        // Update game objects
        spaceship.update(deltaTime)
        
        // Update stars (scrolling background)
        stars.forEach { star ->
            star.update(deltaTime)
            if (star.y > height) {
                star.y = -10f
                star.x = Random.nextFloat() * width
            }
        }
        
        // Update asteroids
        asteroids.removeAll { asteroid ->
            asteroid.update(deltaTime)
            if (asteroid.isOffScreen(width, height)) {
                true
            } else {
                // Check collision with spaceship
                if (asteroid.intersects(spaceship)) {
                    createExplosion(asteroid.x, asteroid.y)
                    lives--
                    gameListener?.onLifeLost(lives)
                    if (lives <= 0) {
                        gameState = GameState.GAME_OVER
                        gameListener?.onGameOver(score)
                    }
                    true
                } else {
                    false
                }
            }
        }
        
        // Update bonuses
        bonuses.removeAll { bonus ->
            bonus.update(deltaTime)
            if (bonus.isOffScreen(width, height)) {
                true
            } else {
                // Check collision with spaceship
                if (bonus.intersects(spaceship)) {
                    val points = bonus.collect()
                    if (bonus.type == BonusType.LIFE) {
                        lives = (lives + 1).coerceAtMost(3)
                        gameListener?.onLifeGained(lives)
                    } else {
                        score += points
                        gameListener?.onScoreChanged(score)
                    }
                    createExplosion(bonus.x, bonus.y, ExplosionType.BONUS)
                    true
                } else {
                    false
                }
            }
        }
        
        // Update explosions
        explosions.removeAll { explosion ->
            explosion.update(deltaTime)
            !explosion.isActive
        }
        
        // Spawn asteroids
        asteroidSpawnTimer += deltaTime
        val asteroidSpawnRate = (2f - difficultyTimer * 0.1f).coerceAtLeast(0.5f)
        if (asteroidSpawnTimer > asteroidSpawnRate) {
            asteroids.add(Asteroid.createRandomAsteroid(width))
            asteroidSpawnTimer = 0f
        }
        
        // Spawn bonuses
        bonusSpawnTimer += deltaTime
        if (bonusSpawnTimer > 8f) { // Every 8 seconds
            bonuses.add(Bonus.createRandomBonus(width))
            bonusSpawnTimer = 0f
        }
        
        // Increase difficulty over time
        difficultyTimer += deltaTime
        gameSpeed = 1f + difficultyTimer * 0.05f
        
        // Update score based on survival time
        score += (10 * deltaTime * gameSpeed).toInt()
        gameListener?.onScoreChanged(score)
    }
    
    private fun createExplosion(x: Float, y: Float, type: ExplosionType = ExplosionType.ASTEROID) {
        explosions.add(Explosion(x, y, type))
    }
    
    private fun drawGame(canvas: Canvas) {
        // Clear canvas with background
        paint.shader = backgroundGradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null
        
        // Draw stars
        paint.color = Color.WHITE
        stars.forEach { star ->
            paint.alpha = (star.alpha * 255).toInt()
            canvas.drawCircle(star.x, star.y, star.size, paint)
        }
        paint.alpha = 255
        
        // Draw game objects
        spaceship.draw(canvas, paint)
        
        asteroids.forEach { asteroid ->
            asteroid.draw(canvas, paint)
        }
        
        bonuses.forEach { bonus ->
            bonus.draw(canvas, paint)
        }
        
        explosions.forEach { explosion ->
            explosion.draw(canvas, paint)
        }
        
        // Draw game over screen
        if (gameState == GameState.GAME_OVER) {
            paint.color = Color.argb(150, 0, 0, 0)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            
            textPaint.textSize = 64f
            textPaint.color = Color.RED
            canvas.drawText("GAME OVER", width / 2f, height / 2f - 100f, textPaint)
            
            textPaint.textSize = 36f
            textPaint.color = Color.WHITE
            canvas.drawText("Score: $score", width / 2f, height / 2f, textPaint)
        }
    }
    
    fun pauseGame() {
        gameState = GameState.PAUSED
    }
    
    fun resumeGame() {
        gameState = GameState.PLAYING
    }
    
    fun restartGame() {
        gameState = GameState.PLAYING
        score = 0
        lives = 3
        asteroidSpawnTimer = 0f
        bonusSpawnTimer = 0f
        difficultyTimer = 0f
        gameSpeed = 1f
        
        asteroids.clear()
        bonuses.clear()
        explosions.clear()
        
        spaceship.x = (width - spaceship.width) / 2
        spaceship.y = height - spaceship.height - 100f
        
        gameListener?.onScoreChanged(score)
        gameListener?.onLifeChanged(lives)
    }
    
    private inner class GameThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        var isRunning = false
        private var lastTime = System.currentTimeMillis()
        
        override fun run() {
            while (isRunning) {
                val canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    try {
                        val currentTime = System.currentTimeMillis()
                        val deltaTime = (currentTime - lastTime) / 1000f
                        lastTime = currentTime
                        
                        update(deltaTime)
                        drawGame(canvas)
                    } finally {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    }
                }
            }
        }
    }
    
    interface GameListener {
        fun onScoreChanged(score: Int)
        fun onLifeChanged(lives: Int)
        fun onLifeLost(lives: Int)
        fun onLifeGained(lives: Int)
        fun onGameOver(finalScore: Int)
    }
}

enum class GameState {
    PLAYING,
    PAUSED,
    GAME_OVER
}

// Helper classes
private class Star(var x: Float, var y: Float, val size: Float, private val speed: Float) {
    var alpha: Float = Random.nextFloat()
    
    fun update(deltaTime: Float) {
        y += speed * deltaTime
        alpha = (alpha + deltaTime * 2f) % 1f
    }
}

private class Explosion(var x: Float, var y: Float, private val type: ExplosionType) {
    var isActive = true
    private var timer = 0f
    private val duration = 0.5f
    private val particles = mutableListOf<ExplosionParticle>()
    
    init {
        val numParticles = when (type) {
            ExplosionType.ASTEROID -> 15
            ExplosionType.BONUS -> 8
        }
        
        val color = when (type) {
            ExplosionType.ASTEROID -> Color.parseColor("#FFA500")
            ExplosionType.BONUS -> Color.YELLOW
        }
        
        repeat(numParticles) {
            particles.add(ExplosionParticle(x, y, color))
        }
    }
    
    fun update(deltaTime: Float) {
        timer += deltaTime
        if (timer > duration) {
            isActive = false
            return
        }
        
        particles.forEach { it.update(deltaTime) }
    }
    
    fun draw(canvas: Canvas, paint: Paint) {
        particles.forEach { it.draw(canvas, paint) }
    }
}

private class ExplosionParticle(x: Float, y: Float, private val color: Int) {
    private var x = x
    private var y = y
    private val velocityX = (Random.nextFloat() - 0.5f) * 400f
    private val velocityY = (Random.nextFloat() - 0.5f) * 400f
    private var alpha = 255f
    private val size = Random.nextFloat() * 6 + 2
    
    fun update(deltaTime: Float) {
        x += velocityX * deltaTime
        y += velocityY * deltaTime
        alpha -= 500f * deltaTime
    }
    
    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.argb(
            alpha.toInt().coerceIn(0, 255),
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
        canvas.drawCircle(x, y, size, paint)
    }
}

enum class ExplosionType {
    ASTEROID,
    BONUS
}