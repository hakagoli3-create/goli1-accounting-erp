package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.ProductEntity
import com.example.data.model.WarehouseEntity
import com.example.data.repository.InventoryRepository
import com.example.util.JalaliDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    val allWarehouses: StateFlow<List<WarehouseEntity>> = repository.allWarehouses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<String>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        repository.allProducts,
        _searchQuery,
        _selectedCategory
    ) { products, query, category ->
        products.filter { p ->
            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.code.contains(query, ignoreCase = true) ||
                    p.barcode.contains(query, ignoreCase = true)

            val matchesCategory = category == null || p.category == category

            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProductCardex = MutableStateFlow<ProductEntity?>(null)
    val selectedProductCardex: StateFlow<ProductEntity?> = _selectedProductCardex.asStateFlow()

    val cardexMovements: StateFlow<List<InventoryMovementEntity>> = _selectedProductCardex
        .flatMapLatest { product ->
            if (product != null) {
                repository.getCardex(product.id)
            } else {
                repository.recentMovements
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(cat: String?) {
        _selectedCategory.value = cat
    }

    fun selectProductForCardex(product: ProductEntity?) {
        _selectedProductCardex.value = product
    }

    fun addProduct(
        code: String,
        barcode: String,
        name: String,
        category: String,
        primaryUnit: String,
        secondaryUnit: String,
        conversionRate: Double,
        initialStockSecondary: Double,
        minStock: Double,
        buyPrice: Long,
        retailPrice: Long,
        wholesalePrice: Long,
        partnerPrice: Long,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                code = code.ifBlank { "PRD-${System.currentTimeMillis() % 10000}" },
                barcode = barcode,
                name = name,
                category = category.ifBlank { "عمومی" },
                primaryUnit = primaryUnit.ifBlank { "کارتن" },
                secondaryUnit = secondaryUnit.ifBlank { "عدد" },
                conversionRate = if (conversionRate <= 0) 1.0 else conversionRate,
                currentStockInSecondary = initialStockSecondary,
                minStockThreshold = minStock,
                buyPrice = buyPrice,
                retailPrice = retailPrice,
                wholesalePrice = wholesalePrice,
                partnerPrice = partnerPrice,
                description = description
            )
            val productId = repository.insertProduct(product)
            if (initialStockSecondary > 0) {
                repository.recordMovement(
                    InventoryMovementEntity(
                        productId = productId,
                        warehouseId = 1,
                        jalaliDate = JalaliDate.now().toStandardString(),
                        documentType = "INITIAL_BALANCE",
                        referenceNumber = "INV-INIT",
                        quantitySecondary = initialStockSecondary,
                        unitPrice = buyPrice,
                        description = "موجودی اولیه هنگام تعریف کالا"
                    )
                )
            }
            onSuccess()
        }
    }

    fun addWarehouse(name: String, code: String, manager: String, phone: String, address: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.insertWarehouse(
                WarehouseEntity(
                    code = code,
                    name = name,
                    managerName = manager,
                    phone = phone,
                    address = address
                )
            )
            onSuccess()
        }
    }
}
