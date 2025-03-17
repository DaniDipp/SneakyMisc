package com.danidipp.sneakymisc.elevators

import com.danidipp.sneakymisc.SneakyMisc
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.SoundCategory
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.serialization.ConfigurationSerializable

data class Elevator(
    val name: String,
): ConfigurationSerializable  {
    private val floors: MutableMap<String, ElevatorFloor> = mutableMapOf()
    var currentFloor: ElevatorFloor? = null
    var inTransit: Boolean = false
    val guis: MutableSet<ElevatorGUI> = mutableSetOf()

    fun getFloor(floor: String): ElevatorFloor? {
        return floors[floor]
    }
    fun getFloors(): Collection<ElevatorFloor> {
        return floors.values
    }

    fun createFloor(floorId: String, location: Location): ElevatorFloor {
        val elevatorFloor = ElevatorFloor.create(floorId, location, this)
        floors[floorId] = elevatorFloor
        ElevatorsModule.instance.saveConfig()
        return elevatorFloor
    }

    fun deleteFloor(elevatorFloor: String): ElevatorFloor? {
        val floor = floors[elevatorFloor] ?: return null
        return deleteFloor(floor)
    }
    fun deleteFloor(elevatorFloor: ElevatorFloor): ElevatorFloor? {
        val floor = floors.remove(elevatorFloor.id)
        floor?.cleanup()
        if (currentFloor == floor) {
            currentFloor = floors.values.firstOrNull()
        }
        ElevatorsModule.instance.saveConfig()
        return floor
    }

    fun callTo(targetFloor: ElevatorFloor) {
        if (targetFloor == currentFloor) {
            targetFloor.open()
            return
        }
        inTransit = true
        for (gui in guis) Bukkit.getScheduler().runTask(SneakyMisc.getInstance(), Runnable {
            gui.inventory.close()
        })

        var doorCloseDelay = 0L;
        if (currentFloor != null && currentFloor!!.isOpen) {
            currentFloor!!.close()
            doorCloseDelay = (ElevatorFloor.DOOR_DURATION).toLong()
        }

        Bukkit.getScheduler().runTaskLater(SneakyMisc.getInstance(), Runnable {
            for (player in currentFloor?.getPlayers() ?: emptyList()) {
                player.playSound(player, "lom:elevator", SoundCategory.MASTER, 1f, 1f)
            }

            Bukkit.getScheduler().runTaskLater(SneakyMisc.getInstance(), Runnable {
                if (currentFloor != null) {
                    movePlayers(currentFloor!!, targetFloor)
                }
                targetFloor.open()
                currentFloor = targetFloor
                inTransit = false
            }, (SONG_DURATION + 20).toLong())
        }, doorCloseDelay)
    }

    private fun movePlayers(from: ElevatorFloor, to: ElevatorFloor) {
        val yawDiffDegrees = to.location.yaw - from.location.yaw
        val yawDiffRadians = Math.toRadians(yawDiffDegrees.toDouble()) * -1

        for (player in from.getPlayers()) {
            val offset = player.location.toVector().subtract(from.location.toVector())
            val offsetRotation = offset.rotateAroundY(yawDiffRadians)
            val newLocation = to.location.clone().add(offsetRotation)
            newLocation.yaw = player.location.yaw + yawDiffDegrees
            newLocation.pitch = player.location.pitch
            player.teleport(newLocation)
        }
    }

    companion object {
        const val SONG_DURATION = 310

        fun deserialize(data: Map<String, Any>): Elevator {
            val name = data["name"] as String? ?: throw IllegalArgumentException("Failed to parse Elevator data: Missing name")
            val currentFloorId = data["currentFloor"] as String?
            val elevator = Elevator(name)

            val floorsSection = data["floors"] as MemorySection
            val floors = mutableMapOf<String, ElevatorFloor>()
            for (floorId in floorsSection.getKeys(false)) {
                val floorData = floorsSection.getConfigurationSection(floorId)?.getValues(false) ?: throw IllegalArgumentException("Failed to parse Elevator data: Missing floor data")
                val floor = ElevatorFloor.deserialize(floorData, elevator)
                floors[floorId] = floor
            }
            val currentFloor = currentFloorId?.let { floors[currentFloorId] } ?: floors.values.firstOrNull()

            elevator.floors.putAll(floors)
            elevator.currentFloor = currentFloor
            return elevator
        }
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
            "name" to name,
            "floors" to floors.mapValues { it.value.serialize() },
            "currentFloor" to (currentFloor?.id ?: "")
        )
    }

    fun formatComponent(): Component {
        return Component.text("Elevator ").append(Component.text(name, NamedTextColor.AQUA)).appendNewline()
            .append(Component.text("  ")).append(Component.join(JoinConfiguration.commas(true), floors.values.map { it.toComponent().style(if(currentFloor == it) Style.style(TextDecoration.UNDERLINED) else Style.empty()) }))
    }
}