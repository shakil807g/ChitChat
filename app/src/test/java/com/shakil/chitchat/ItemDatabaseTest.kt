package com.shakil.chitchat

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.junit.Assert.assertEquals
import org.junit.Test

class ItemDatabaseTest {


    private val inMemorySqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        Database.Schema.create(this)
    }

    private val database = Database(inMemorySqlDriver)
    private val queries = database.usersEntityQueries

    @Test
    fun smokeTest() {
        val emptyItems: List<USER> = queries.selectAll().executeAsList()
        assertEquals(emptyItems.size, 0)


        queries.insertOrReplace(
            id = "1",
            name = "shakil",
            profile_pic = "https://localhost/pineapple.png"
        )

        queries.insertOrReplace(
            id = "2",
            name = "shakil2",
            profile_pic = "https://localhost/pineapple2.png"
        )


        val users = queries.selectNameFromUsers("1").executeAsOne()
        assertEquals(users.name, "shakil")

        val items: List<USER> = queries.selectAll().executeAsList()
        assertEquals(items.size, 2)

        val pineappleItem = queries.selectById("2").executeAsOneOrNull()
        assertEquals(pineappleItem?.profile_pic, "https://localhost/pineapple2.png")
        assertEquals(pineappleItem?.name, "shakil2")
    }
}