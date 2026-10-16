package com.example.data.repository

import com.example.data.dao.InventoryDao
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.ProductEntity
import com.example.data.model.WarehouseEntity
import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val dao: InventoryDao) {

    val allWarehouses: Flow<List<WarehouseEntity>> = dao.getAllWarehouses()
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()
    val allCategories: Flow<List<String>> = dao.getAllCategories()
    val lowStockProducts: Flow<List<ProductEntity>> = dao.getLowStockProducts()
    val lowStockCount: Flow<Int> = dao.getLowStockCount()
    val recentMovements: Flow<List<InventoryMovementEntity>> = dao.getRecentMovements()

    fun searchProducts(query: String): Flow<List<ProductEntity>> = dao.searchProducts(query)

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> =
        dao.getProductsByCategory(category)

    suspend fun getProductByBarcode(barcode: String): ProductEntity? =
        dao.getProductByBarcode(barcode)

    suspend fun getProductById(id: Long): ProductEntity? =
        dao.getProductById(id)

    suspend fun insertProduct(product: ProductEntity): Long =
        dao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) =
        dao.updateProduct(product)

    suspend fun deleteProduct(product: ProductEntity) =
        dao.deleteProduct(product)

    suspend fun insertWarehouse(warehouse: WarehouseEntity): Long =
        dao.insertWarehouse(warehouse)

    fun getCardex(productId: Long): Flow<List<InventoryMovementEntity>> =
        dao.getCardexForProduct(productId)

    suspend fun recordMovement(movement: InventoryMovementEntity) {
        dao.insertMovement(movement)
        dao.adjustStock(movement.productId, movement.quantitySecondary)
    }
}
