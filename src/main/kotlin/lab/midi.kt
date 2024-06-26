package lab

import org.openrndr.application
import org.openrndr.extra.midi.MidiTransceiver
import org.openrndr.extra.midi.listMidiDevices
import org.openrndr.extra.midi.openMidiDevice
import org.openrndr.extra.midi.openMidiDeviceOrNull
import javax.sound.midi.MidiDeviceTransmitter
import javax.sound.midi.MidiSystem

fun main() = application {

    program {
        val info = MidiSystem.getMidiDeviceInfo()
        val rec = MidiSystem.getMidiDevice(info.first { it.name.startsWith("CTRL") }).apply { open() }
        val trans = MidiSystem.getMidiDevice(info.first { it.name.startsWith("SLIDER") }).apply { open() }

        val tr = MidiTransceiver(this, rec, trans)

        tr.controlChanged.listen {
            println(it)
        }
    }
}