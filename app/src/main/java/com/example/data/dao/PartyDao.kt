package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PartyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {

    @Query("SELECT * FROM parties ORDER BY id DESC")
    fun getAllParties(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE partyType IN ('CUSTOMER', 'BOTH') ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE partyType IN ('SUPPLIER', 'BOTH') ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE id = :id LIMIT 1")
    suspend fun getPartyById(id: Long): PartyEntity?

    @Query("SELECT * FROM parties WHERE name LIKE '%' || :query || '%' OR mobile LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%'")
    fun searchParties(query: String): Flow<List<PartyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: PartyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParties(parties: List<PartyEntity>)

    @Update
    suspend fun updateParty(party: PartyEntity)

    @Delete
    suspend fun deleteParty(party: PartyEntity)

    @Query("UPDATE parties SET currentBalance = currentBalance + :delta WHERE id = :partyId")
    suspend fun updatePartyBalance(partyId: Long, delta: Long)

    @Query("SELECT COUNT(*) FROM parties WHERE creditLimit > 0 AND currentBalance > creditLimit")
    fun getOverCreditLimitCount(): Flow<Int>
}
