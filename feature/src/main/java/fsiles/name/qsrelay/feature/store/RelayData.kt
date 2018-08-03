package fsiles.name.qsrelay.feature.store

import java.io.Serializable

class RelayData: Serializable{
    internal var name: String = ""
    internal var state: Boolean = false
    internal var index: Int = -1
}