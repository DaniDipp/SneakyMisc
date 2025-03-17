package com.danidipp.sneakymisc.elevators

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class ElevatorCommand(val elevators: ElevatorsModule): Command("elevator") {
    init {
        description = "Elevator command"
        usageMessage = "/elevator <list|call>"
        permission = "sneakymisc.elevator"
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            return false
        }

        when (args[0]) {
            "list" -> {
                for (elevator in elevators.getElevators())
                    sender.sendMessage(elevator.formatComponent())
            }
            "call" -> {
                if (args.size < 2) {
                    return false
                }
                val elevatorName = args[1].split("-").firstOrNull() ?: return false
                val elevator = elevators.getElevator(elevatorName) ?: run {
                    sender.sendMessage("Elevator not found")
                    return true
                }
                if (elevator.inTransit) {
                    sender.sendMessage("Elevator is already in transit")
                    return true
                }
                val floor = elevator.getFloor(args[1]) ?: run {
                    sender.sendMessage("Floor not found")
                    return true
                }

                elevator.callTo(floor)
            }
            else -> {
                sender.sendMessage("Invalid argument")
            }
        }

        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> {
        if (args.isEmpty()) return emptyList()
        if (args.size == 1) {
            return listOf("list", "call").filter { it.startsWith(args[0]) }
        }
        if (args.size == 2) {
            return when (args[0]) {
                "call" ->
                    elevators.getElevators().flatMap { it.getFloors().map { it.id } }.filter { it.startsWith(args[1]) }
                else -> emptyList()
            }
        }
        return emptyList()
    }
}