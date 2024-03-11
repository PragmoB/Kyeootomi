package com.pragmo.kyeootomi.view

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

class ToggleAnimation {

    companion object {

        fun toggleArrow(view: View, isExpanded: Boolean): Boolean {
            return if (isExpanded) {
                view.animate().setDuration(200).rotation(180f)
                true
            } else {
                view.animate().setDuration(200).rotation(0f)
                false
            }
        }


        fun expand(view: View, height: Int) {
            view.layoutParams.height = 0
            view.visibility = View.VISIBLE

            val animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    view.layoutParams.height = (height * interpolatedTime).toInt()
                    view.requestLayout()
                }
            }

            animation.duration = (height / view.context.resources.displayMetrics.density).toLong()

            view.startAnimation(animation)
        }

        fun collapse(view: View) {
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

            animation.duration = (actualHeight / view.context.resources.displayMetrics.density).toLong()
            view.startAnimation(animation)
        }
    }

}