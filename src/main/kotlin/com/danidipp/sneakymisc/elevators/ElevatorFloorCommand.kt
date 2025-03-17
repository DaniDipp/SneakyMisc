package com.danidipp.sneakymisc.elevators

import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ElevatorFloorCommand(val elevators: ElevatorsModule) : Command("elevatorfloor") {
    init {
        description = "ElevatorFloor door command"
        usageMessage = "/elevatorfloor <create|delete|open|close>"
        permission = "sneakymisc.elevatorfloor"
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
        if (args.isEmpty()) return false

        when (args[0]) {
            "create" -> {
                if (sender !is Player) {
                    sender.sendMessage("Only players can create elevators and floors")
                    return true
                }
                if (args.size < 2)  return false
                if (args[1].count{it == '-'} != 1) {
                    sender.sendMessage("Format: <name>-<floor>")
                    return true
                }
                val elevatorName = args[1].split('-').first()
                val elevator = elevators.getElevator(elevatorName) ?: elevators.createElevator(elevatorName)

                val existingFloor = elevator.getFloor(args[1])
                if (existingFloor != null) {
                    sender.sendMessage(Component.text("ElevatorFloor ").append(existingFloor.toComponent()).append(Component.text(" already exists")))
                    return true
                }

                val location = sender.location.toBlockLocation().setDirection(sender.facing.direction)
                val elevatorFloor = elevator.createFloor(args[1], location)
                sender.sendMessage(Component.text("ElevatorFloor ").append(elevatorFloor.toComponent()).append(Component.text(" created")))
            }
            "delete" -> {
                if (args.size < 2) return false
                if (args[1].count{it == '-'} != 1) {
                    sender.sendMessage("Format: <name>-<floor>")
                    return true
                }
                val elevatorName = args[1].split('-').first()
                val elevator = elevators.getElevator(elevatorName) ?: run {
                    sender.sendMessage("Elevator $elevatorName not found")
                    return true
                }
                val floor = elevator.deleteFloor(args[1])
                if (floor == null) {
                    sender.sendMessage("ElevatorFloor ${args[1]} not found")
                    return true
                }
                sender.sendMessage(Component.text("ElevatorFloor ").append(floor.toComponent()).append(Component.text(" deleted")))
            }
            "open" -> {
                if (args.size < 2) return false
                if (args[1].count{it == '-'} != 1) {
                    sender.sendMessage("Format: <name>-<floor>")
                    return true
                }
                val elevator = elevators.getElevator(args[1].split("-").first()) ?: run {
                    sender.sendMessage("Elevator not found")
                    return true
                }
                if (elevator.inTransit) {
                    sender.sendMessage("Elevator is in transit")
                    return true
                }
                val elevatorFloor = elevators.getFloor(args[1]) ?: run {
                    sender.sendMessage("ElevatorFloor not found")
                    return true
                }
                elevatorFloor.open()
            }
            "close" -> {
                if (args.size < 2) return false
                if (args[1].count{it == '-'} != 1) {
                    sender.sendMessage("Format: <name>-<floor>")
                    return true
                }
                val elevatorFloor = elevators.getFloor(args[1]) ?: run {
                    sender.sendMessage("ElevatorFloor not found")
                    return true
                }
                elevatorFloor.close()
            }
            else -> return false
        }
        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> {
        if (args.isEmpty()) return emptyList()
        if (args.size == 1) {
            return listOf("create", "delete", "open", "close").filter { it.startsWith(args[0]) }
        }
        if (args.size == 2) {
            return when (args[0]) {
                "delete", "open", "close" ->
                    elevators.getElevators().flatMap { it.getFloors().map { it.id } }.filter { it.startsWith(args[1]) }
                else -> emptyList()
            }
        }
        return emptyList()
    }
}
