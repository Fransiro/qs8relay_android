package fsiles.name.qsrelay.feature.log

import java.io.Serializable

class LogStore: Serializable {
    internal var logLines: MutableList<LogLine> = ArrayList()
}