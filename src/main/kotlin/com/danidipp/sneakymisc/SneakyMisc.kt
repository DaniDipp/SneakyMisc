package com.danidipp.sneakymisc

import com.danidipp.sneakymisc.databasesync.DBSyncModule
import com.danidipp.sneakymisc.dclock.DClockModule
import com.danidipp.sneakymisc.elevators.ElevatorsModule
import com.danidipp.sneakymisc.inventorygames.InventoryGamesModule
import com.danidipp.sneakymisc.metaoverlayhelper.MetaOverlayHelper
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class SneakyMisc : JavaPlugin() {

    override fun onLoad() {
        instance = this
    }
    override fun onEnable() {
        registerModule(ElevatorsModule(logger))

        if (Bukkit.getPluginManager().isPluginEnabled("SneakyPocketbase")) {
            registerModule(DBSyncModule(logger))
            registerModule(DClockModule(logger))
        }
    }

    private fun registerModule(module: SneakyModule) {
        logger.info("Registering module ${module.javaClass.name} with ${module.commands.size} commands and ${module.listeners.size} listeners")
        Bukkit.getServer().commandMap.registerAll(IDENTIFIER, module.commands)
        for (listener in module.listeners) {
            Bukkit.getServer().pluginManager.registerEvents(listener, this)
        }
    }

    companion object {
        const val IDENTIFIER = "sneakymisc"
        const val AUTHORS = "Team Sneakymouse"
        const val VERSION = "1.0"
        private lateinit var instance: SneakyMisc

        fun getInstance(): SneakyMisc {
            return instance
        }
    }
}
