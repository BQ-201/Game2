package com.spaceexplorer

import android.graphics.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Bonus(x: Float, y: Float, val type: BonusType) : GameObject(x, y, 40f, 40f) {
    
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val glowPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private var pulseTimer: Float = 0f
    private var collected: Boolean = false
    
    init {
        velocityY = 150f // Slower than asteroids
        velocityX = Random.nextFloat() * 50 - 25 // Slight horizontal movement
    }
    
    override fun update(deltaTime: Float) {
        x += velocityX * deltaTime
        y += velocityY * deltaTime
        pulseTimer += deltaTime * 3f // Pulse effect
    }
    
    override fun draw(canvas: Canvas, paint: Paint) {
        val centerX = x + width / 2
        val centerY = y + height / 2
        val pulse = (sin(pulseTimer) * 0.2f + 1f) // 0.8 to 1.2 scale
        
        // Draw glow effect
        val glowRadius = width * 0.8f * pulse
        val colorComponents = getColorComponents(type.color)
        val glowGradient = RadialGradient(
            centerX, centerY, glowRadius,
            intArrayOf(
                Color.argb(100, colorComponents[0], colorComponents[1], colorComponents[2]),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        glowPaint.shader = glowGradient
        canvas.drawCircle(centerX, centerY, glowRadius, glowPaint)
        
        // Draw bonus shape based on type
        paint.color = type.color
        when (type) {
            BonusType.SCORE -> drawStar(canvas, centerX, centerY, width * 0.4f * pulse, paint)
            BonusType.LIFE -> drawHeart(canvas, centerX, centerY, width * 0.4f * pulse, paint)
            BonusType.SHIELD -> drawShield(canvas, centerX, centerY, width * 0.4f * pulse, paint)
        }
    }
    
    private fun drawStar(canvas: Canvas, centerX: Float, centerY: Float, radius: Float, paint: Paint) {
        val path = Path()
        val numPoints = 5
        val outerRadius = radius
        val innerRadius = radius * 0.4f
        
        for (i in 0 until numPoints * 2) {
            val angle = i * Math.PI / numPoints
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val x = centerX + (cos(angle) * r).toFloat()
            val y = centerY + (sin(angle) * r).toFloat()
            
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }
    
    private fun drawHeart(canvas: Canvas, centerX: Float, centerY: Float, radius: Float, paint: Paint) {
        val path = Path()
        val heartWidth = radius * 1.5f
        val heartHeight = radius * 1.3f
        
        // Heart shape using Bezier curves
        path.moveTo(centerX, centerY + heartHeight * 0.3f)
        path.cubicTo(
            centerX - heartWidth * 0.5f, centerY - heartHeight * 0.5f,
            centerX - heartWidth, centerY + heartHeight * 0.1f,
            centerX, centerY + heartHeight * 0.7f
        )
        path.cubicTo(
            centerX + heartWidth, centerY + heartHeight * 0.1f,
            centerX + heartWidth * 0.5f, centerY - heartHeight * 0.5f,
            centerX, centerY + heartHeight * 0.3f
        )
        canvas.drawPath(path, paint)
    }
    
    private fun drawShield(canvas: Canvas, centerX: Float, centerY: Float, radius: Float, paint: Paint) {
        val path = Path()
        val shieldWidth = radius * 1.2f
        val shieldHeight = radius * 1.4f
        
        // Shield shape
        path.moveTo(centerX, centerY - shieldHeight * 0.5f)
        path.lineTo(centerX + shieldWidth * 0.5f, centerY - shieldHeight * 0.2f)
        path.lineTo(centerX + shieldWidth * 0.5f, centerY + shieldHeight * 0.2f)
        path.lineTo(centerX, centerY + shieldHeight * 0.5f)
        path.lineTo(centerX - shieldWidth * 0.5f, centerY + shieldHeight * 0.2f)
        path.lineTo(centerX - shieldWidth * 0.5f, centerY - shieldHeight * 0.2f)
        path.close()
        canvas.drawPath(path, paint)
    }
    
    private fun getColorComponents(color: Int): IntArray {
        return intArrayOf(
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
    }
    
    fun collect(): Int {
        if (!collected) {
            collected = true
            isActive = false
            return type.value
        }
        return 0
    }
    
    companion object {
        fun createRandomBonus(screenWidth: Int): Bonus {
            val x = Random.nextFloat() * (screenWidth - 40f)
            val y = -50f
            val type = BonusType.values().random()
            return Bonus(x, y, type)
        }
    }
}

enum class BonusType(val color: Int, val value: Int) {
    SCORE(Color.parseColor("#FDE047"), 100),    // Yellow star - 100 points
    LIFE(Color.parseColor("#EF4444"), 1),       // Red heart - 1 life
    SHIELD(Color.parseColor("#06B6D4"), 50)     // Cyan shield - 50 points + temporary shield
}