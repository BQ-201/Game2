package com.spaceexplorer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

abstract class GameObject(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float
) {
    var velocityX: Float = 0f
    var velocityY: Float = 0f
    var isActive: Boolean = true
    
    val bounds: RectF
        get() = RectF(x, y, x + width, y + height)
    
    abstract fun update(deltaTime: Float)
    abstract fun draw(canvas: Canvas, paint: Paint)
    
    fun intersects(other: GameObject): Boolean {
        return bounds.intersect(other.bounds)
    }
    
    fun isOffScreen(screenWidth: Int, screenHeight: Int): Boolean {
        return x + width < 0 || x > screenWidth || y + height < 0 || y > screenHeight
    }
}