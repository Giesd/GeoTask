package de.giesd.geotask

class StaticMapRequest(private val apiKey: String) {

    companion object {
        private const val URL = "https://maps.googleapis.com/maps/api/staticmap"
    }

    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var zoom = 1
    var width = 200
    var height = 200

    override fun toString(): String =
        URL + "?center=" + latitude + "," + longitude +
                "&zoom=" + zoom +
                "&size=" + width + "x" + height +
                "&key=" + apiKey
}