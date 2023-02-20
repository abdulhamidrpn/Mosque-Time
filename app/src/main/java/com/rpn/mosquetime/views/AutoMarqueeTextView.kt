package com.rpn.mosquetime.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.TextView


class AutoMarqueeTextView : androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!,
        attrs,
        defStyle
    ) {
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        if (focused) super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }

    override fun onWindowFocusChanged(focused: Boolean) {
        if (focused) super.onWindowFocusChanged(focused)
    }

    override fun isFocused(): Boolean {
        return true
    }
}