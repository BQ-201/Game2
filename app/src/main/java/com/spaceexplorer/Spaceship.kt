package com.spaceexplorer

import android.graphics.*

class Spaceship(x: Float, y: Float) : GameObject(x, y, 80f, 100f) {
    
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val trailPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private var trailParticles = mutableListOf<TrailParticle>()
    private var trailTimer = 0f
    
    fun moveLeft(deltaTime: Float, screenWidth: Int) {
        velocityX = -500f
        x += velocityX * deltaTime
        if (x < 0) x = 0f
    }
    
    fun moveRight(deltaTime: Float, screenWidth: Int) {
        velocityX = 500f
        x += velocityX * deltaTime
        if (x + width > screenWidth) x = screenWidth - width
    }
    
    fun stopMoving() {
        velocityX = 0f
    }
    
    override fun update(deltaTime: Float) {
        // Update trail particles
        trailTimer += deltaTime
        if (trailTimer > 0.05f) {
            addTrailParticle()
            trailTimer = 0f
        }
        
        // Update existing trail particles
        trailParticles.removeAll { particle ->
            particle.update(deltaTime)
            !particle.isActive
        }
    }
    
    private fun addTrailParticle() {
        val particleX = x + width / 2 + (Math.random() * 20 - 10).toFloat()
        val particleY = y + height
        trailParticles.add(TrailParticle(particleX, particleY))
    }
    
    override fun draw(canvas: Canvas, paint: Paint) {
        // Draw trail particles first
        for (particle in trailParticles) {
            particle.draw(canvas, trailPaint)
        }
        
        // Draw spaceship body
        paint.color = Color.parseColor("#7C3AED") // Purple
        val bodyPath = Path().apply {
            moveTo(x + width / 2, y)
            lineTo(x + width * 0.8f, y + height * 0.7f)
            lineTo(x + width * 0.6f, y + height)
            lineTo(x + width * 0.4f, y + height)
            lineTo(x + width * 0.2f, y + height * 0.7f)
            close()
        }
        canvas.drawPath(bodyPath, paint)
        
        // Draw cockpit
        paint.color = Color.parseColor("#06B6D4") // Cyan
        canvas.drawCircle(x + width / 2, y + height * 0.3f, width * 0.15f, paint)
        
        // Draw engines
        paint.color = Color.parseColor("#EF4444") // Red
        canvas.drawRect(x + width * 0.25f, y + height * 0.8f, x + width * 0.4f, y + height, paint)
        canvas.drawRect(x + width * 0.6f, y + height * 0.8f, x + width * 0.75f, y + height, paint)
    }
    
    private class TrailParticle(var x: Float, var y: Float) {
        var alpha: Float = 255f
        var isActive: Boolean = true
        private val size: Float = (Math.random() * 6 + 2).toFloat()
        
        fun update(deltaTime: Float) {
            y += 200f * deltaTime
            alpha -= 400f * deltaTime
            if (alpha <= 0) {
                isActive = false
            }
        }
        
        fun draw(canvas: Canvas, paint: Paint) {
            paint.color = Color.argb(
                alpha.toInt().coerceIn(0, 255),
                255, 165, 0 // Orange color
            )
            canvas.drawCircle(x, y, size, paint)
        }
    }
}