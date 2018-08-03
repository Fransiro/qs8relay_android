package fsiles.name.qsrelay.feature.utils

import android.bluetooth.BluetoothAdapter
import android.util.Log
import java.io.IOException
import java.math.BigInteger
import java.util.*

class RelaysUtils{
    companion object {
        // This UUID is standard UUID for relays do not change it
        private var mMyUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        fun sendCommands(mBluetoothAdapter: BluetoothAdapter, mAddress: String,
                         commands: ArrayList<ByteArray>): ArrayList<Boolean> {
            var lastIndex = 0
            try {
                mBluetoothAdapter.cancelDiscovery()
                val device = mBluetoothAdapter.getRemoteDevice(mAddress)
                val mBluetoothSocket =
                        device.createRfcommSocketToServiceRecord(mMyUUID)
                mBluetoothSocket.connect()
                if (mBluetoothSocket != null && mBluetoothSocket.isConnected) {
                    for (command in commands) {
                        mBluetoothSocket.outputStream.write(command)
                        mBluetoothSocket.outputStream.write("\r".toByteArray())
                        lastIndex++
                    }
                    mBluetoothSocket.outputStream.flush()
                    mBluetoothSocket.close()
                }else{

                }
            } catch (e: IOException) {
                Log.e("ConnectionException", "Can not connect to device", e)
            }
            return getToReturnArray(lastIndex, commands.size)
        }

        fun getCommand(positionOnArray: Int, setOn: Boolean): ByteArray {
            var state: Byte = 0
            if (setOn) {
                state = 1
            }
            var toReturn = BigInteger("FD022001015D", 16).toByteArray()
            toReturn = toReturn.sliceArray(1 until toReturn.size)
            toReturn[3] = (positionOnArray + 1).toByte()
            toReturn[4] = state
            return toReturn
        }

        private fun getToReturnArray(lastIndex: Int, size: Int): ArrayList<Boolean> {
            val toReturn = ArrayList<Boolean>()
            var index = 0
            while (index < size) {
                toReturn.add(index, index < lastIndex)
                index++
            }
            return toReturn
        }
    }
}