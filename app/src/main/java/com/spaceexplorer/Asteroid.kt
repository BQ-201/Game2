package com.spaceexplorer

import android.graphics.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Asteroid(x: Float, y: Float) : GameObject(x, y, 0f, 0f) {
    
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val strokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    
    private val size: Float = Random.nextFloat() * 40 + 30 // 30-70 pixels
    private val rotationSpeed: Float = Random.nextFloat() * 180 - 90 // -90 to 90 degrees per second
    private var rotation: Float = 0f
    private val vertices: List<PointF>
    private val color: Int
    
    init {
        width = size
        height = size
        velocityY = Random.nextFloat() * 200 + 100 // 100-300 pixels per second
        velocityX = Random.nextFloat() * 100 - 50 // -50 to 50 pixels per second
        
        // Generate random asteroid shape
        vertices = generateAsteroidVertices()
        
        // Random asteroid color (shades of gray and brown)
        val colors = listOf(
            Color.parseColor("#6B7280"), // Gray
            Color.parseColor("#78716C"), // Warm gray
            Color.parseColor("#A8A29E"), // Light gray
            Color.parseColor("#57534E")  // Dark gray
        )
        color = colors.random()
    }
    
    private fun generateAsteroidVertices(): List<PointF> {
        val numVertices = Random.nextInt(6, 10)
        val vertices = mutableListOf<PointF>()
        
        for (i in 0 until numVertices) {
            val angle = (i * 360f / numVertices) * Math.PI / 180
            val radius = size / 2 * (0.7f + Random.nextFloat() * 0.3f)
            val px = (cos(angle) * radius).toFloat()
            val py = (sin(angle) * radius).toFloat()
            vertices.add(PointF(px, py))
        }
        
        return vertices
    }
    
    override fun update(deltaTime: Float) {
        x += velocityX * deltaTime
        y += velocityY * deltaTime
        rotation += rotationSpeed * deltaTime
    }
    
    override fun draw(canvas: Canvas, paint: Paint) {
        canvas.save()
        canvas.translate(x + width / 2, y + height / 2)
        canvas.rotate(rotation)
        
        // Draw asteroid body
        val path = Path()
        if (vertices.isNotEmpty()) {
            path.moveTo(vertices[0].x, vertices[0].y)
            for (i in 1 until vertices.size) {
                path.lineTo(vertices[i].x, vertices[i].y)
            }
            path.close()
            
            paint.color = color
            canvas.drawPath(path, paint)
            
            // Draw outline
            strokePaint.color = Color.WHITE
            strokePaint.alpha = 100
            canvas.drawPath(path, strokePaint)
        }
        
        canvas.restore()
    }
    
    companion object {
        fun createRandomAsteroid(screenWidth: Int): Asteroid {
            val x = Random.nextFloat() * screenWidth
            val y = -100f // Start above screen
            return Asteroid(x, y)
        }
    }
}