package com.biplocker.app

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout

class PinPad @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    var onDigit: ((Char) -> Unit)? = null
    var onBackspace: (() -> Unit)? = null
    var onSubmit: (() -> Unit)? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_pin_pad, this, true)
        val digits = intArrayOf(
            R.id.key0, R.id.key1, R.id.key2, R.id.key3, R.id.key4,
            R.id.key5, R.id.key6, R.id.key7, R.id.key8, R.id.key9
        )
        digits.forEachIndexed { index, id ->
            findViewById<View>(id).setOnClickListener { onDigit?.invoke(('0' + index)) }
        }
        findViewById<View>(R.id.keyBackspace).setOnClickListener { onBackspace?.invoke() }
        findViewById<View>(R.id.keySubmit).setOnClickListener { onSubmit?.invoke() }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        setChildrenEnabled(this, enabled)
        alpha = if (enabled) 1f else 0.4f
    }

    private fun setChildrenEnabled(group: LinearLayout, enabled: Boolean) {
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i)
            child.isEnabled = enabled
            if (child is LinearLayout) setChildrenEnabled(child, enabled)
        }
    }
}

fun pinDots(length: Int, placeholder: String): String {
    if (length == 0) return placeholder
    return List(length) { "\u2022" }.joinToString(" ")
}
