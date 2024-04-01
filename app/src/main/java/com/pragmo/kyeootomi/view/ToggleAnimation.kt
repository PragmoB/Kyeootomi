package com.pragmo.kyeootomi.view

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

class ToggleAnimation {

    companion object {

        fun toggle(view: View, viewHeight: Int, arrow: View, expand: Boolean) {
            val velocity = view.context.resources.displayMetrics.density
            if (expand) {
                toggleArrow(arrow, true, ((viewHeight - view.height) / velocity - 30).toLong())
                expand(view, viewHeight, velocity)
            }
            else {
                toggleArrow(arrow, false, (view.height / velocity - 30).toLong())
                collapse(view, velocity)
            }
        }
        fun toggleArrow(arrow: View, expand: Boolean, duration: Long) {
            val animator = if (duration > 0)
                arrow.animate().setDuration(duration)
            else
                arrow.animate().setDuration(0)

            if (expand)
                animator.rotation(-180f)
            else
                animator.rotation(0f)
        }


        fun expand(view: View, height: Int, velocity: Float) {
            view.visibility = View.VISIBLE
            val actualHeight = view.height

            val animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    view.layoutParams.height = (actualHeight + ((height - actualHeight) * interpolatedTime)).toInt()
                    view.requestLayout()
                }
            }

            animation.duration = ((height - actualHeight) / velocity).toLong()
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