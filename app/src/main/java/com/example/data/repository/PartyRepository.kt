package com.example.data.repository

import com.example.data.dao.PartyDao
import com.example.data.model.PartyEntity
import kotlinx.coroutines.flow.Flow

class PartyRepository(private val dao: PartyDao) {

    val allParties: Flow<List<PartyEntity>> = dao.getAllParties()
    val allCustomers: Flow<List<PartyEntity>> = dao.getAllCustomers()
    val allSuppliers: Flow<List<PartyEntity>> = dao.getAllSuppliers()
    val overCreditLimitCount: Flow<Int> = dao.getOverCreditLimitCount()

    fun searchParties(query: String): Flow<List<PartyEntity>> = dao.searchParties(query)

    suspend fun getPartyById(id: Long): PartyEntity? = dao.getPartyById(id)

    suspend fun insertParty(party: PartyEntity): Long = dao.insertParty(party)

    suspend fun updateParty(party: PartyEntity) = dao.updateParty(party)

    suspend fun deleteParty(party: PartyEntity) = dao.deleteParty(party)

    suspend fun adjustBalance(partyId: Long, delta: Long) =
        dao.updatePartyBalance(partyId, delta)
}
