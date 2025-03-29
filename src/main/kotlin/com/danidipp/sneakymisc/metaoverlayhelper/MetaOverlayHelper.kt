package com.danidipp.sneakymisc.metaoverlayhelper

import com.danidipp.sneakymisc.SneakyModule
import net.sneakycharactermanager.paper.handlers.character.Character
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.filter.AbstractFilter
import org.apache.logging.log4j.message.Message
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.Listener
import java.util.UUID

class MetaOverlayHelper(logger: java.util.logging.Logger): SneakyModule {
    init {
        try {
            val log4j = LogManager.getRootLogger() as org.apache.logging.log4j.core.Logger
            log4j.addFilter(object: AbstractFilter() {
                fun validateMessage(message: String?): Filter.Result {
                    if (message == null) return Filter.Result.NEUTRAL
                    if (message.contains("/metaoverlayhelper")) return Filter.Result.DENY
                    return Filter.Result.NEUTRAL
                }
                override fun filter(event: LogEvent?): Filter.Result {
                    return validateMessage(event?.message?.formattedMessage)
                }
                override fun filter(logger: Logger?, level: Level?, marker: Marker?, msg: Message?, t: Throwable?): Filter.Result {
                    return validateMessage(msg?.formattedMessage)
                }
                override fun filter(logger: Logger?, level: Level?, marker: Marker?, msg: String?, vararg params: Any? ): Filter.Result {
                    return validateMessage(msg)
                }
                override fun filter(logger: Logger?, level: Level?, marker: Marker?, msg: Any?, t: Throwable?): Filter.Result {
                    return validateMessage(msg?.toString())
                }
            })
        } catch (e: Exception) {
            logger.warning("Failed to get log4j logger")
        }
    }
    override val commands: List<Command> = listOf(object: Command("metaoverlayhelper") {
        val RESPONSE_PREFIX = "[MetaOverlayHelper] "
        init {
            description = "Meta Overlay Helper command"
            usageMessage = "/metaoverlayhelper charid <uuid>"
            permission = "sneakymisc.metaoverlayhelper"
        }
        override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
            if (!Bukkit.getPluginManager().isPluginEnabled("SneakyCharacterManager")) {
                sender.sendMessage(RESPONSE_PREFIX + "SneakyCharacterManager plugin is not enabled")
                return true
            }
            if(args.size < 2) return false
            when (args[0]) {
                "charid" -> {
                    val player = Bukkit.getPlayer(UUID.fromString(args[1])) ?: run {
                        sender.sendMessage(RESPONSE_PREFIX + "Player not found")
                        return true
                    }
                    val character = Character.get(player) ?: run {
                        sender.sendMessage(RESPONSE_PREFIX + "Character not found")
                        return true
                    }
                    sender.sendMessage(RESPONSE_PREFIX + character.characterUUID)
                    return true
                }
                else -> return false
            }
        }
    })
    override val listeners: List<Listener> = listOf()
}