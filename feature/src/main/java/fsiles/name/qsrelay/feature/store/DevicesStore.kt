package fsiles.name.qsrelay.feature.store

import java.io.Serializable

class DevicesStore: Serializable{
    internal var devices: MutableMap<String, DeviceData> = HashMap()
}