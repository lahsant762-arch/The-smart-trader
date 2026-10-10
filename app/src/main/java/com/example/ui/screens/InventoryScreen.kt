package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ProductEntity
import com.example.ui.components.AddProductDialog
import com.example.ui.components.ProductIcon
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.MerchantViewModel

@Composable
fun InventoryScreen(viewModel: MerchantViewModel) {
    val allProducts by viewModel.products.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("الكل") }
    var isAddDialogOpen by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }
    var stockAdjustProduct by remember { mutableStateOf<ProductEntity?>(null) }

    // Summary KPIs
    val totalProductsCount = allProducts.size
    val totalStockCostValue = allProducts.sumOf { it.costPrice * it.stockQuantity }
    val lowStockCount = allProducts.count { it.stockQuantity <= it.minStockAlert }

    // Filtering
    val filteredProducts = allProducts.filter { product ->
        val matchesCategory = when (selectedFilter) {
            "الكل" -> true
            "نقص المخزون" -> product.stockQuantity <= product.minStockAlert
            else -> product.category == selectedFilter
        }
        val matchesSearch = searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.barcode.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Stats Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "قيمة رأس المال بالمخزون",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${totalStockCostValue.toInt()} د.ج",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (lowStockCount > 0) WarningAmber.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "إجمالي الأصناف / نواقص",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$totalProductsCount صنف",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (lowStockCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "($lowStockCount ناقص)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar & Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث في المخزون والسلع...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filterOptions = listOf("الكل", "نقص المخزون", "خضر", "فواكه", "مواد غذائية", "أخرى")
                    items(filterOptions) { opt ->
                        FilterChip(
                            selected = selectedFilter == opt,
                            onClick = { selectedFilter = opt },
                            label = { Text(opt, fontSize = 12.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Products List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد سلع مسجلة تطابق التصفية",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        InventoryProductCard(
                            product = product,
                            onEdit = { editingProduct = product },
                            onDelete = { productToDelete = product },
                            onAdjustStock = { stockAdjustProduct = product }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Product
        FloatingActionButton(
            onClick = { isAddDialogOpen = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_product_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة سلعة")
        }
    }

    // Add / Edit Dialog
    if (isAddDialogOpen || editingProduct != null) {
        AddProductDialog(
            initialProduct = editingProduct,
            onDismiss = {
                isAddDialogOpen = false
                editingProduct = null
            },
            onSave = { product ->
                viewModel.saveProduct(product)
                isAddDialogOpen = false
                editingProduct = null
            }
        )
    }

    // Delete Confirmation Dialog
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("تأكيد حذف السلعة") },
            text = { Text("هل أنت متأكد من حذف \"${prod.name}\" من قائمة السلع وقاعدة البيانات؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(prod)
                        productToDelete = null
                    }
                ) {
                    Text("نعم، حذف")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { productToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Quick Stock Adjust Dialog
    stockAdjustProduct?.let { prod ->
        var adjustAmountText by remember { mutableStateOf("") }
        var isAdding by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { stockAdjustProduct = null },
            title = { Text("تحديث مخزون: ${prod.name}") },
            text = {
                Column {
                    Text("الكمية الحالية: ${prod.stockQuantity} ${prod.unit}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = isAdding,
                            onClick = { isAdding = true },
                            label = { Text("إضافة كمية واردة (+)") }
                        )
                        FilterChip(
                            selected = !isAdding,
                            onClick = { isAdding = false },
                            label = { Text("خصم كمية تالفة (-)") }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = adjustAmountText,
                        onValueChange = { adjustAmountText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("الكمية بالـ (${prod.unit})") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = adjustAmountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            val delta = if (isAdding) amount else -amount
                            viewModel.adjustStock(prod.id, delta)
                        }
                        stockAdjustProduct = null
                    }
                ) {
                    Text("تأكيد")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { stockAdjustProduct = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun InventoryProductCard(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdjustStock: () -> Unit
) {
    val profitPerUnit = product.salePrice - product.costPrice
    val profitPercent = if (product.costPrice > 0) (profitPerUnit / product.costPrice) * 100 else 0.0

    val isLowStock = product.stockQuantity <= product.minStockAlert

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductIcon(
                iconKey = product.iconKey,
                customImageUri = product.customImageUri,
                size = 56.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    // Stock Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isLowStock) ExpenseRed.copy(alpha = 0.15f) else ProfitGreen.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${product.stockQuantity} ${product.unit}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) ExpenseRed else ProfitGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Price and Cost info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "سعر البيع: ${product.salePrice.toInt()} د.ج",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "سعر الشراء: ${product.costPrice.toInt()} د.ج",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "هامش الربح: +${profitPerUnit.toInt()} د.ج",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProfitGreen
                        )
                        Text(
                            text = "(+${profitPercent.toInt()}%)",
                            fontSize = 10.sp,
                            color = ProfitGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onAdjustStock,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text("تعديل المخزون", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(18.dp))
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
