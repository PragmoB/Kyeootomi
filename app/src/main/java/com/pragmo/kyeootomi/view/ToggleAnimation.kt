package com.pragmo.kyeootomi.view

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

class ToggleAnimation {

    companion object {

        fun toggle(view: View, viewHeight: Int, arrow: View, expand: Boolean) {
            val velocity = view.context.resources.displayMetrics.density
            toggleArrow(arrow, expand, (viewHeight / velocity - 30).toLong())
            if (expand)
                expand(view, viewHeight, velocity)
            else
                collapse(view, velocity)
        }
        fun toggleArrow(arrow: View, expand: Boolean, duration: Long) {
            if (expand) {
                arrow.animate().setDuration(duration).rotation(180f)
            } else {
                arrow.animate().setDuration(duration).rotation(0f)
            }
        }


        fun expand(view: View, height: Int, velocity: Float) {
            view.layoutParams.height = 0
            view.visibility = View.VISIBLE

            val animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    view.layoutParams.height = (height * interpolatedTime).toInt()
                    view.requestLayout()
                }
            }

            animation.duration = (height / velocity).toLong()

            view.startAnimation(animation)
        }

        fun collapse(view: View, velocity: Float) {
            val actualHeight = view.height

            val animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    if (interpolatedTime == 1f) {
                        view.visibility = View.GONE
                    } else {
                        view.layoutParams.height = (actualHeight - (actualHeight * interpolatedTime)).toInt()
                        view.requestLayout()
                    }
                }
            }

            animation.duration = (actualHeight / velocity).toLong()
            view.startAnimation(animation)
        }
    }

}