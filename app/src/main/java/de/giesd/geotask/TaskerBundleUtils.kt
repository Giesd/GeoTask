package de.giesd.geotask

import android.content.Intent
import android.os.Bundle
import com.twofortyfouram.assertion.BundleAssertions

const val EXTRA_AREA_ID = "GeoTask:AreaId"

val taskerRequestQueryIntent: Intent by lazy {
    Intent(com.twofortyfouram.locale.api.Intent.ACTION_REQUEST_QUERY)
        .putExtra(com.twofortyfouram.locale.api.Intent.EXTRA_STRING_ACTIVITY_CLASS_NAME,
            TaskerConfigActivity::class.java.name)
}

fun isTaskerBundleValid(bundle: Bundle): Boolean {
    try {
        BundleAssertions.assertHasInt(bundle, EXTRA_AREA_ID)
        BundleAssertions.assertKeyCount(bundle, 1)
    } catch (e: AssertionError) {
        return false
    }
    return true
}

fun createTaskerConfigBundle(area: Area?): Bundle {
    val bundle = Bundle()
    bundle.putInt(EXTRA_AREA_ID, area?.id ?: 0)
    return bundle
}

fun getAreaIdFromBundle(bundle: Bundle): Int =
    bundle.getInt(EXTRA_AREA_ID, 0)
