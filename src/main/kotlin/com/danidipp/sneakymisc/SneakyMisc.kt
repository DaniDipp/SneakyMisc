package com.danidipp.sneakymisc

import org.bukkit.plugin.java.JavaPlugin

class SneakyMisc : JavaPlugin() {

    override fun onLoad() {
        instance = this
    }
    override fun onEnable() {
        saveDefaultConfig()
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
