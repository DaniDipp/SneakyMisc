package com.danidipp.sneakymisc

import org.bukkit.Location

enum class Direction {
    SOUTH,
    SOUTHWEST,
    WEST,
    NORTHWEST,
    NORTH,
    NORTHEAST,
    EAST,
    SOUTHEAST;


    companion object {
        private val directions = arrayOf(SOUTH, WEST, NORTH, EAST)
        private val doubleDirection = arrayOf(SOUTH, SOUTHWEST, WEST, NORTHWEST, NORTH, NORTHEAST, EAST, SOUTHEAST)

        fun getDirection(location: Location): Direction {
            return getDirection(location.yaw)
        }

        fun getDoubleDirection(location: Location): Direction {
            return getDoubleDirection(location.yaw)
        }

        fun getDirection(yaw: Float): Direction {
            var yaw = yaw
            while (yaw < 0) yaw += 360f
            return directions[((yaw + 45f).toInt() % 360) / 90]
        }

        fun getDoubleDirection(yaw: Float): Direction {
            var yaw = yaw
            while (yaw < 0) yaw += 360f
            return doubleDirection[((yaw + 22.5f).toInt() % 360) / 45]
        }

    }
}