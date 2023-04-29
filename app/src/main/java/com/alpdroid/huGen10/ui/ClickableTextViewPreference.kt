package com.alpdroid.huGen10.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder

class ClickableTextViewPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {
    private var onClickListener: View.OnClickListener? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val textView = holder.itemView.findViewById<TextView>(android.R.id.title)
        textView.setOnClickListener(onClickListener)
    }

    fun setOnTextViewClickListener(listener: View.OnClickListener?) {
        onClickListener = listener
    }
}