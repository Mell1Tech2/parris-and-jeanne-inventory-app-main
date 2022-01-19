package com.example.inventory.shapedrawable

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.example.inventory.R

class DrawBoxView(
    context: Context,
    attribute: AttributeSet
) : View(context, attribute) {

    private val paint = Paint()
    private var mRect = RectF()

    //private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)

    init {
        Log.d("Log", "start")
        //setWillNotDraw(false)
    }


    override fun onDraw(canvas: Canvas) {
        //drawable.draw(canvas)
        Log.d("Log", "Drawing")
        val cornerRadius = 10f
        paint.style = Paint.Style.STROKE
        paint.color = Color.RED
        paint.strokeWidth = 20f

        Log.d("Log", "Drawn")
        canvas.drawRoundRect(mRect, cornerRadius, cornerRadius, paint)
    }
    fun setRect(rect: RectF) {
        Log.d("Log", "rect: $rect")
        mRect = rect
        Log.d("Log", "rect: $mRect")


        Log.d("Log", "force")
        requestLayout()
        Log.d("Log", "request")

        postInvalidate()
        Log.d("Log", "invalidate")
    }
}