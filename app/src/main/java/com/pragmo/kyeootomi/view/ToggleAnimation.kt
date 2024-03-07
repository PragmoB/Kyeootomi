package com.pragmo.kyeootomi.view

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation

class ToggleAnimation {

    companion object {

        fun toggleArrow(view: View, isExpanded: Boolean): Boolean {
            if (isExpanded) {
                view.animate().setDuration(200).rotation(180f)
                return true
            } else {
                view.animate().setDuration(200).rotation(0f)
                return false
            }
        }


        fun expand(view: View) {
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED )
            val actualHeight = view.measuredHeight

            view.layoutParams.height = 0
            view.visibility = View.VISIBLE

            val animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    view.layoutParams.height = (actualHeight * interpolatedTime).toInt()
                    view.requestLayout()
                }
            }

            animation.duration = (actualHeight / view.context.resources.displayMetrics.density).toLong()

            view.startAnimation(animation)
        }

        fun collapse(view: View) {
            val actualHeight = view.measuredHeight

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