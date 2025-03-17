package com.danidipp.sneakymisc.databasesync

import com.danidipp.sneakypocketbase.BaseRecord

data class AccountRecord(
    val name: String,
    val owner: String,
    val main: Boolean,
): BaseRecord()

data class CharacterRecord(
    val name: String,
    val account: String,
    val tags: String,
): BaseRecord()