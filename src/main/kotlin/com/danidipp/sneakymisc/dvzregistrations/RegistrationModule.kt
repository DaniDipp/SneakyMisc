package com.danidipp.sneakymisc.dvzregistrations

import com.danidipp.sneakymisc.SneakyModule
import org.bukkit.command.Command
import org.bukkit.event.Listener
import java.util.UUID

class RegistrationModule: SneakyModule {
    override val commands = listOf<Command>()
    override val listeners = listOf<Listener>()

    fun register(uuid: UUID) {
        
    }
}