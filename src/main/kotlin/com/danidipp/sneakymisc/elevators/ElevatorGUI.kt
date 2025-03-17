package com.danidipp.sneakymisc.elevators

import com.danidipp.sneakymisc.SneakyMisc
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class ElevatorGUI(private val elevator: Elevator) : InventoryHolder {
    private val inventory = Bukkit.createInventory(this, 9*6, Component.text("Elevator ${elevator.name}"))
    private var editingOnes = true
    private var locked = false
    init {
        val floor = elevator.currentFloor
        inventory.setItem(8, getNumber(3046)) // Background

        if (inventory.holder !is ElevatorGUI) throw IllegalArgumentException("Inventory is not an ElevatorGUI")
        val floorId = floor?.id?.split("-")?.get(1)
        if (floorId == null) {
            inventory.setItem(4, null)
            inventory.setItem(5, null)
        } else {
            if (floorId == "B") {
                inventory.setItem(4, null)
                inventory.setItem(5, getNumber(5996))
                editingOnes = false
            } else if (floorId == "G") {
                inventory.setItem(4, null)
                inventory.setItem(5, getNumber(5997))
                editingOnes = false
            } else {
                val floorNumber = floorId.toInt()
                val tens = if (floorNumber < 10) 10 else floorNumber / 10
                val units = floorNumber % 10

                inventory.setItem(4, getNumber(6000 + tens * 10))
                inventory.setItem(5, getNumber(6000 + units))
            }
        }
    }

    override fun getInventory(): Inventory {
        return inventory
    }

    companion object {
        fun getNumber(modelData: Int): ItemStack {
            val item = ItemStack(Material.JIGSAW)
            val meta = item.itemMeta
//            meta.isHideTooltip = true
            meta.setCustomModelData(modelData)
            item.itemMeta = meta
            return item
        }

        val listener = object : Listener{
            @EventHandler
            fun onInventoryOpen(event: InventoryOpenEvent) {
                if (event.inventory.holder !is ElevatorGUI) return
                val gui = event.inventory.holder as ElevatorGUI
                if (gui.elevator.inTransit) {
                    event.isCancelled = true
                    return
                }
                gui.elevator.guis.add(gui)
            }

            @EventHandler
            fun onInventoryClose(event: InventoryCloseEvent) {
                if (event.inventory.holder !is ElevatorGUI) return
                val gui = event.inventory.holder as ElevatorGUI
                gui.elevator.guis.remove(gui)
            }

            @EventHandler
            fun onInventoryDrag(event: InventoryDragEvent) {
                if (event.inventory.holder !is ElevatorGUI) return
                event.isCancelled = true
            }

            @EventHandler
            fun onInventoryClick(event: InventoryClickEvent) {
                if (event.inventory.holder !is ElevatorGUI) return
                event.isCancelled = true
                if (event.clickedInventory != event.inventory) return
                val gui = event.inventory.holder as ElevatorGUI
                if (gui.locked || gui.elevator.inTransit) return
                (event.whoClicked as Player).playSound(event.whoClicked, "lom:ui.button.click", SoundCategory.MASTER, 1f, 1f)

                when (event.slot) {
                    12 -> gui.updateDigit(1)
                    13 -> gui.updateDigit(2)
                    14 -> gui.updateDigit(3)
                    21 -> gui.updateDigit(4)
                    22 -> gui.updateDigit(5)
                    23 -> gui.updateDigit(6)
                    30 -> gui.updateDigit(7)
                    31 -> gui.updateDigit(8)
                    32 -> gui.updateDigit(9)
                    39 -> gui.updateDigit(-1)
                    40 -> gui.updateDigit(0)
                    41 -> {
                        val id = gui.getSelection()
                        val floor = gui.elevator.getFloor("${gui.elevator.name}-$id")
                        if (floor == null) {
                            gui.locked = true
                            gui.inventory.setItem(4, getNumber(5998))
                            gui.inventory.setItem(5, getNumber(5998))
                            (event.whoClicked as Player).playSound(event.whoClicked, "lom:fail_wrong", SoundCategory.MASTER, 1f, 1f)
                            Bukkit.getScheduler().runTaskLater(SneakyMisc.getInstance(), Runnable {
                                gui.inventory.setItem(4, null)
                                gui.inventory.setItem(5, null)
                                Bukkit.getScheduler().runTaskLater(SneakyMisc.getInstance(), Runnable {
                                    gui.inventory.setItem(4, getNumber(5998))
                                    gui.inventory.setItem(5, getNumber(5998))
                                    Bukkit.getScheduler().runTaskLater(SneakyMisc.getInstance(), Runnable {
                                        gui.inventory.setItem(4, null)
                                        gui.inventory.setItem(5, null)
                                        gui.locked = false
                                        gui.editingOnes = true
                                    }, 5)
                                }, 5)
                            }, 5)
                        } else {
                            gui.locked = true
                            gui.inventory.setItem(4, null)
                            gui.inventory.setItem(5, getNumber(5999))
                            (event.whoClicked as Player).playSound(event.whoClicked, "lom:cute.get", SoundCategory.MASTER, .5f, 1f)
                            Bukkit.getScheduler().runTaskLater(SneakyMisc.getInstance(), Runnable {
                                gui.locked = false
                                gui.editingOnes = true
                                gui.elevator.callTo(floor)
                                gui.inventory.close()
                            }, 10)
                        }
                    }
                }
            }
        }
    }

    private fun getSelection(): String {
        var number = 0

        when (val tens = inventory.getItem(4)?.itemMeta?.customModelData) {
            null, 6100 -> {}
            else -> if (tens in 6000..6098) number += tens - 6000
        }
        when (val units = inventory.getItem(5)?.itemMeta?.customModelData) {
            null -> {}
            5996 -> return "B"
            5997 -> return "G"
            else -> if (units in 6000..6098) number += units - 6000
        }

        return number.toString()
    }

    private fun updateDigit(number: Int) {
        if (locked) return
        if (number == -1) {
            inventory.setItem(4, null)
            inventory.setItem(5, getNumber(5996))
            editingOnes = true
            return
        }

        var digit = number * if (editingOnes) 1 else 10
        if (!editingOnes && digit == 0) digit = 100
        val item = getNumber(6000 + digit)
        if (editingOnes) {
            inventory.setItem(5, item)
        } else {
            inventory.setItem(4, item)
        }

        editingOnes = !editingOnes
    }
}