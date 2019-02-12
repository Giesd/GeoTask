package de.giesd.geotask

import android.os.Bundle
import android.support.v7.widget.RecyclerView

abstract class MultiSelectionAdapter<T : RecyclerView.ViewHolder> : RecyclerView.Adapter<T>() {

    companion object {
        private const val SELECTED_POSITION_KEY = "MultiSelectionAdapter:positions"
    }

    protected val selectedItemPositions = HashSet<Int>()
    var onSelectionModeStateChanged: (Boolean) -> Unit = {}
    var selectionMode = false
        private set(value) {
            if (value != selectionMode) {
                field = value
                onSelectionModeStateChanged(value)
            }
        }

    fun saveInstanceState(outState: Bundle) {
        outState.putIntegerArrayList(SELECTED_POSITION_KEY, ArrayList(selectedItemPositions))
    }

    fun restoreInstanceState(savedInstanceState: Bundle) {
        val savedPositions = savedInstanceState.getIntegerArrayList(SELECTED_POSITION_KEY)
        savedPositions.forEach {setItemSelected(it, true)}
    }

    fun setItemSelected(position: Int, selected: Boolean) {
        if (selected) {
            selectedItemPositions.add(position)
        } else {
            selectedItemPositions.remove(position)
        }
        selectionMode = selectedItemPositions.isNotEmpty()
    }

    fun isItemSelected(position: Int) = selectedItemPositions.contains(position)

    fun toggleItemSelected(position: Int) {
        setItemSelected(position, !isItemSelected(position))
    }

    fun stopSelectionMode() {
        val previouslySelected = selectedItemPositions.toSet()
        selectedItemPositions.clear()
        selectionMode = false
        previouslySelected.forEach { notifyItemChanged(it) }
    }
}