package fsiles.name.qsrelay.feature.log

import java.io.Serializable
import java.util.*

class LogLine: Serializable {
    internal var message: String = ""
    internal var date: Date = Date()
}