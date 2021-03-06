package com.example.inventory.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Database access object to access the Inventory database
 */
@Dao
interface ItemDao {

    // Get all items
    @Query("SELECT * from item ORDER BY name ASC")
    fun getItems(): Flow<List<Item>>

    @Query("SELECT * from item ORDER BY name ASC")
    fun getItemsJson(): Flow<Array<Item>>

    // Get a single item
    @Query("SELECT * from item WHERE id = :id")
    fun getItem(id: Int): Flow<Item>

    // Specify the conflict strategy as IGNORE, when the user tries to add an
    // existing Item into the database Room ignores the conflict.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item)

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    @Query("DELETE from item")
    fun deleteTable()
}