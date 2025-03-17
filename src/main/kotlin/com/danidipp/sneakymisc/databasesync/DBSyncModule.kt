package com.danidipp.sneakymisc.databasesync

import com.danidipp.sneakymisc.SneakyModule
import com.danidipp.sneakypocketbase.SneakyPocketbase
import net.sneakycharactermanager.paper.SneakyCharacterManager
import net.sneakycharactermanager.paper.handlers.character.LoadCharacterEvent
import org.bukkit.command.Command
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class DBSyncModule(val sneakyPocketbaseAvailable: Boolean, val sneakyCharacterManagerAvailable: Boolean) : SneakyModule {
    override val commands: List<Command> = listOf()
    override val listeners: List<Listener> = getListeners()

    private fun getListeners(): List<Listener> {
        if (!sneakyPocketbaseAvailable) {
            return emptyList()
        }
        val pb = SneakyPocketbase.getInstance()

        val listeners = mutableListOf<Listener>(object : Listener {
            @EventHandler
            fun onPlayerJoin(event: PlayerJoinEvent) {
                val player = event.player
                // sync player data
            }
        })

        if (sneakyCharacterManagerAvailable) {
            listeners.add(object : Listener {
                @EventHandler
                fun onChangeCharacter(event: LoadCharacterEvent) {
//                    pb.pb().collections.update<>()
                }
            })
        }
        return listeners
    }
}