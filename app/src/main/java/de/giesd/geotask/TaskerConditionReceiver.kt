package de.giesd.geotask

import android.content.Context
import android.os.Bundle
import com.twofortyfouram.locale.api.Intent.*
import com.twofortyfouram.locale.sdk.client.receiver.AbstractPluginConditionReceiver

class TaskerConditionReceiver : AbstractPluginConditionReceiver() {

    override fun isAsync(): Boolean = true

    override fun getPluginConditionResult(context: Context, bundle: Bundle): Int {
        val id = getAreaIdFromBundle(bundle)
        if (id != 0) {
            val areaDao = AreaDatabase.getInstance().areaDao()
            val area = areaDao.getById(id)
            if (area != null && area.active) {
                return if (area.inside) {
                    RESULT_CONDITION_SATISFIED
                } else {
                    RESULT_CONDITION_UNSATISFIED
                }
            }
        }
        return RESULT_CONDITION_UNKNOWN
    }

    override fun isBundleValid(bundle: Bundle): Boolean =
        isTaskerBundleValid(bundle)

}