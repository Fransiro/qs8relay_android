package fsiles.name.qsrelay.feature.store

import java.io.Serializable

class DeviceData: Serializable {
    internal var name: String? = null
    internal var relays: ArrayList<RelayData> = ArrayList()
}