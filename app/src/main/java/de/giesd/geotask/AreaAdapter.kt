package de.giesd.geotask

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.squareup.picasso.Picasso

class AreaAdapter(val showSwitches: Boolean = true) : MultiSelectionAdapter<AreaAdapter.ViewHolder>() {

    private val dataset = ArrayList<Area>()
    var onItemClick: (Area) -> Unit = {}
    var onSwitchClicked: (Area) -> Unit = {}
    var emptyView: View? = null
    var mapsApiKey: String = ""

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.area_list_item, parent,
            false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = dataset.size

    override fun getItemId(position: Int): Long = dataset[position].id.toLong()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val area = dataset[position]
        if (area.active) {
            holder.nameTextView.text = area.name + if (area.inside) " (inside)" else " (outside)"
        } else {
            holder.nameTextView.text = area.name
        }
        holder.descriptionTextView.text = area.description
        holder.itemView.isActivated = isItemSelected(position)
        holder.itemView.setOnClickListener {
            if (!selectionMode) {
                onItemClick(area)
            } else {
                toggleItemSelected(holder.adapterPosition)
                notifyItemChanged(holder.adapterPosition)
            }
        }
        holder.activationSwitch.visibility = if (showSwitches) View.VISIBLE else View.GONE
        holder.activationSwitch.setOnClickListener {
            if (!selectionMode) {
                onSwitchClicked(area)
            }
        }
        holder.itemView.setOnLongClickListener {
            if (!selectionMode) {
                setItemSelected(holder.adapterPosition, true)
                notifyItemChanged(holder.adapterPosition)
                return@setOnLongClickListener true
            }
            return@setOnLongClickListener false
        }
        holder.activationSwitch.isChecked = area.active
        loadMapPreview(holder.mapPreview, area)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.name_text)
        val descriptionTextView: TextView = itemView.findViewById(R.id.description_text)
        val activationSwitch: Switch = itemView.findViewById(R.id.activation_switch)
        val mapPreview: ImageView = itemView.findViewById(R.id.map_preview)
    }

    private fun loadMapPreview(imageView: ImageView, area: Area) {
        val url = StaticMapRequest(mapsApiKey).apply {
            latitude = area.latitude
            longitude = area.longitude
            zoom = 15
            width = 180
            height = 180
        }.toString()
        Picasso.get().load(url)/*.placeholder(R.drawable.ic_map_black_48dp)*/.into(imageView)
    }

    fun updateData(newData: List<Area>) {
        // TODO: run on background thread
        val diffResult = DiffUtil.calculateDiff(AreaDiffCallback(dataset, newData))
        dataset.clear()
        dataset.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
        updateEmptyView()
    }

    private fun updateEmptyView() {
        emptyView?.visibility = if (dataset.isEmpty()) View.VISIBLE else View.GONE
    }

    fun getSelectedItemIds(): Set<Int> =
        selectedItemPositions.map { dataset[it].id }.toSet()

    fun getSelectedItems(): List<Area> = selectedItemPositions.map { dataset[it] }
}