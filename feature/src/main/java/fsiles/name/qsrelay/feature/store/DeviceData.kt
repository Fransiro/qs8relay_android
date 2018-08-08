package fsiles.name.qsrelay.feature.store

import java.io.Serializable

class DeviceData: Serializable {
    internal var name: String? = null
    internal var relays: ArrayList<RelayData> = ArrayList()
    internal var maxTries: Int = 1
    internal var msTimeBetweenEachTry: Long = 1000
}