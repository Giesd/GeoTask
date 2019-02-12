package de.giesd.geotask

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.SeekBar
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.android.synthetic.main.activity_area_overview.*
import kotlinx.android.synthetic.main.content_edit_area.*

class EditAreaActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PARAM_ID = "editArea:id"
        private const val PERMISSION_REQUEST_FINE_LOCATION = 11
    }

    private lateinit var viewModel: EditAreaViewModel
    private lateinit var map: GoogleMap
    private lateinit var circle: Circle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_area)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        fab.setOnClickListener { saveArea() }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync { onMapReady(it) }
    }

    private fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMap()
        setupCircle()
        setupSeekBar()
        setupNameInput()
        subscribeToViewModel()
        adjustZoom()
    }

    private fun setupMap() {
        if (checkPermission()) {
            map.isMyLocationEnabled = true
            map.setOnMapClickListener { viewModel.setCenter(it) }
            map.uiSettings.isMapToolbarEnabled = false
        }
    }

    private fun setupCircle() {
        val circleOptions = CircleOptions()
            .center(LatLng(0.0, 0.0))
            .radius(Area.DEFAULT_RADIUS.toDouble())
            .strokeColor(ContextCompat.getColor(this, R.color.circleStroke))
            .fillColor(ContextCompat.getColor(this, R.color.circleFill))
        circle = map.addCircle(circleOptions)
    }

    private fun setupSeekBar() {
        seekBar.progress = circle.radius.toInt()
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setRadius(progress.toFloat())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupNameInput() {
        nameInput.addTextChangedListener(object : TextWatcher {
            var text = ""

            override fun afterTextChanged(s: Editable?) {
                val newText = s.toString()
                if (text != newText) {
                    text = newText
                    viewModel.setName(newText)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun subscribeToViewModel() {
        val id = intent.getIntExtra(EXTRA_PARAM_ID, 0)
        val factory = EditAreaViewModel.Factory(application, id)
        viewModel = ViewModelProviders.of(this, factory).get(EditAreaViewModel::class.java)
        viewModel.area.observe(this, Observer { refreshUi(it) })
    }

    private fun refreshUi(area: Area?) {
        if (area != null) {
            circle.radius = area.radius.toDouble()
            seekBar.progress = area.radius.toInt()
            if (area.name != nameInput.text.toString()) {
                nameInput.setText(area.name)
            }
            circle.center = LatLng(area.latitude, area.longitude)
        }
    }

    private fun saveArea() {
        viewModel.saveArea()
        if (checkPermission()) {
            viewModel.monitorGeofence(this)
        }
        finish()
    }

    private fun adjustZoom() {
        if (!viewModel.isCenterSet) {
            zoomToCurrentLocation()
        } else {
            val observer = object : Observer<Area> {
                override fun onChanged(area: Area?) {
                    if (area != null) {
                        val bounds = zoomBounds(area.latitude, area.longitude, area.radius)
                        val camUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100)
                        map.animateCamera(camUpdate)
                    }
                    viewModel.area.removeObserver(this)
                }
            }
            viewModel.area.observe(this, observer)
        }
    }

    private fun zoomBounds(latitude: Double, longitude: Double, radius: Float): LatLngBounds {
        val metersToDegFactor = 9E-6
        val difference = 2 * radius * metersToDegFactor
        return LatLngBounds.Builder()
            .include(LatLng(latitude + difference, longitude))
            .include(LatLng(latitude - difference, longitude))
            .build()
    }

    private fun zoomToCurrentLocation() {
        if (checkPermission()) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val position = LatLng(location.latitude, location.longitude)
                val camUpdate = CameraUpdateFactory.newLatLngZoom(position, 15f)
                map.animateCamera(camUpdate)
                viewModel.setCenter(position)
            }
        }
    }

    private fun checkPermission(): Boolean {
        val fineLocation = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
        return if (fineLocation == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            requestPermission()
            false
        }
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
                    adjustZoom()
                } else {
                    // TODO: show Toast or Snackbar
                }
            }
        }
    }

}
