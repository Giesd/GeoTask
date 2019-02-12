package de.giesd.geotask

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractFragmentPluginActivity
import kotlinx.android.synthetic.main.content_area_overview.*

class TaskerConfigActivity : AbstractFragmentPluginActivity() {

    private val areaAdapter = AreaAdapter(false)
    private lateinit var viewModel: TaskerConfigViewModel
    private var selectedArea: Area? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasker_config)
        //setSupportActionBar()

        viewModel = ViewModelProviders.of(this).get(TaskerConfigViewModel::class.java)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        areaAdapter.onItemClick = {
            selectedArea = it
            finish()
        }
        areaAdapter.emptyView = areaEmptyView
        areaAdapter.mapsApiKey = resources.getString(R.string.google_maps_api_key)
        areaRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@TaskerConfigActivity)
            adapter = areaAdapter
        }
        viewModel.areas.observe(this, Observer { areas -> run {
            if (areas != null) {
                areaAdapter.updateData(areas)
            }
        } })
    }

    override fun onPostCreateWithPreviousResult(previousBundle: Bundle, previousBlurb: String) {
        // TODO
    }

    override fun getResultBundle(): Bundle? =
        createTaskerConfigBundle(selectedArea)

    override fun isBundleValid(bundle: Bundle): Boolean =
        isTaskerBundleValid(bundle)

    override fun getResultBlurb(bundle: Bundle): String =
        selectedArea?.name ?: ""
}
