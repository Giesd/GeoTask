package de.giesd.geotask

import android.view.View
import android.widget.TextView

fun TextView.setTextOrHide(text: String) {
    visibility = if (text.isBlank()) View.GONE else View.VISIBLE
    this.text = text
}