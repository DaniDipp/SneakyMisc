package com.danidipp.sneakymisc

import org.bukkit.command.Command
import org.bukkit.event.Listener

interface SneakyModule {
    val commands: List<Command>
    val listeners: List<Listener>
}