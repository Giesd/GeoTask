package de.giesd.geotask

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_area_overview.*
import kotlinx.android.synthetic.main.content_area_overview.*

class AreaOverviewActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_FINE_LOCATION = 11
    }

    private val areaAdapter = AreaAdapter()
    private lateinit var viewModel: AreaOverviewViewModel
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_area_overview)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(AreaOverviewViewModel::class.java)

        setupFab()
        setupRecyclerView()
        setupActionMode()

        viewModel.lastDeleted.observe(this, Observer { showDeletedSnackbar(it?.size) })
    }

    override fun onStart() {
        super.onStart()
        if (!checkPermission()) {
            requestPermission()
        } else {
            // TODO: perform pending task
        }
    }

    private fun setupFab() {
        fab.setOnClickListener {
            startActivity(Intent(this@AreaOverviewActivity,
                EditAreaActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        areaAdapter.apply {
            onItemClick = { area ->
                val intent = Intent(this@AreaOverviewActivity,
                    EditAreaActivity::class.java)
                intent.putExtra(EditAreaActivity.EXTRA_PARAM_ID, area.id)
                startActivity(intent)
            }
            onSwitchClicked = { area ->
                if (checkPermission()) {
                    viewModel.toggleAreaActive(this@AreaOverviewActivity, area)
                }
            }
            emptyView = areaEmptyView
            mapsApiKey = resources.getString(R.string.google_maps_api_key)
        }
        areaRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@AreaOverviewActivity)
            adapter = areaAdapter
        }
        viewModel.areas.observe(this, Observer { areas -> run {
            if (areas != null) {
                areaAdapter.updateData(areas)
            }
        } })
    }

    private fun setupActionMode() {
        val callback = object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                if (item?.itemId == R.id.delete) {
                    val selectedItems = areaAdapter.getSelectedItems()
                    viewModel.delete(this@AreaOverviewActivity, selectedItems)
                    mode?.finish()
                    return true
                }
                return false
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                mode?.menuInflater?.inflate(R.menu.area_actionmode, menu)
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = false

            override fun onDestroyActionMode(p0: ActionMode?) {
                areaAdapter.stopSelectionMode()
            }

        }

        areaAdapter.onSelectionModeStateChanged = { state ->
            if (state) {
                actionMode = startSupportActionMode(callback)
            } else {
                actionMode?.finish()
            }
        }
    }

    private fun showDeletedSnackbar(deletedItemsCount: Int?) {
        if (deletedItemsCount != null && deletedItemsCount > 0) {
            val message = deletedItemsCount.toString() + " " + getString(R.string.deleted)
            val snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
            snackbar.setAction(R.string.undo) { viewModel.undelete() }
            snackbar.show()
        }
    }

    private fun checkPermission(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_FINE_LOCATION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO: perform pending task
                } else {
                    // TODO: Toast or Snackbar
                }
            }
        }
    }
}
