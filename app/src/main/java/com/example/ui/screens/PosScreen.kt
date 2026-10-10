package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ProductEntity
import com.example.data.model.CartItem
import com.example.ui.components.CheckoutDialog
import com.example.ui.components.ProductIcon
import com.example.ui.components.ReceiptDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.MerchantViewModel

@Composable
fun PosScreen(viewModel: MerchantViewModel) {
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val receipt by viewModel.currentReceipt.collectAsStateWithLifecycle()

    var isCartExpanded by remember { mutableStateOf(false) }
    var isCheckoutOpen by remember { mutableStateOf(false) }
    var weightProductToPrompt by remember { mutableStateOf<ProductEntity?>(null) }

    val categories = listOf("الكل", "خضر", "فواكه", "مواد غذائية", "أخرى")
    val totalCartAmount = cart.sumOf { it.subtotal }
    val totalItemCount = cart.sumOf { it.quantity }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTabletWide = maxWidth > 700.dp

        if (isTabletWide) {
            // Adaptive Two-Pane Layout for Tablets/Landscape: Products Grid (60%) + Live Cart Sidebar (40%)
            Row(modifier = Modifier.fillMaxSize()) {
                // Products Grid Area
                Column(
                    modifier = Modifier
                        .weight(1.4f)
                        .fillMaxHeight()
                ) {
                    PosHeader(
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.searchQuery.value = it },
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { viewModel.selectedCategory.value = it }
                    )

                    ProductsGrid(
                        products = products,
                        cart = cart,
                        onAddToCart = { prod -> viewModel.addToCart(prod, 1.0) },
                        onQuickWeight = { prod -> weightProductToPrompt = prod },
                        onIncreaseQty = { id -> viewModel.updateCartQuantity(id, 1.0) },
                        onDecreaseQty = { id -> viewModel.updateCartQuantity(id, -1.0) }
                    )
                }

                // Dedicated Side Cart Panel with Realtime Total Auto-Calculation
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        CartHeaderRow(
                            cartCount = cart.size,
                            totalItemCount = totalItemCount,
                            onClearCart = { viewModel.clearCart() }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (cart.isEmpty()) {
                            EmptyCartView(modifier = Modifier.weight(1f))
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(cart, key = { it.product.id }) { item ->
                                    CartItemRow(
                                        item = item,
                                        onIncrease = { viewModel.updateCartQuantity(item.product.id, 1.0) },
                                        onDecrease = { viewModel.updateCartQuantity(item.product.id, -1.0) },
                                        onRemove = { viewModel.removeFromCart(item.product.id) }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Realtime Auto-Calculated Totals Summary
                        AutoCalculatedTotalCard(
                            totalCartAmount = totalCartAmount,
                            totalItemCount = totalItemCount,
                            itemTypesCount = cart.size
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { if (cart.isNotEmpty()) isCheckoutOpen = true },
                            enabled = cart.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("checkout_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إتمام البيع (${totalCartAmount.toInt()} د.ج)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // Mobile Layout: Full screen products + bottom expandable cart bar
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (cart.isNotEmpty()) 94.dp else 0.dp)
            ) {
                PosHeader(
                    searchQuery = searchQuery,
                    onSearchChange = { viewModel.searchQuery.value = it },
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = { viewModel.selectedCategory.value = it }
                )

                ProductsGrid(
                    products = products,
                    cart = cart,
                    onAddToCart = { prod -> viewModel.addToCart(prod, 1.0) },
                    onQuickWeight = { prod -> weightProductToPrompt = prod },
                    onIncreaseQty = { id -> viewModel.updateCartQuantity(id, 1.0) },
                    onDecreaseQty = { id -> viewModel.updateCartQuantity(id, -1.0) }
                )
            }

            // Mobile Persistent Bottom Cart Drawer with Auto-Calculation
            if (cart.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Expandable Drawer Toggle Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCartExpanded = !isCartExpanded }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "السلة (${cart.size} أصناف - ${"%.1f".format(totalItemCount).removeSuffix(".0")} وحدة)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (isCartExpanded) "انقر لطي السلة" else "انقر لعرض محتويات السلة",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "الإجمالي تلقائياً",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${totalCartAmount.toInt()} د.ج",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (isCartExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Expanded Cart Content
                        AnimatedVisibility(
                            visible = isCartExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("قائمة المواد المختارة:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "إفراغ السلة",
                                        fontSize = 11.sp,
                                        color = ExpenseRed,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { viewModel.clearCart() }
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((cart.size * 54).coerceAtMost(200).dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(cart, key = { it.product.id }) { item ->
                                        CartItemRow(
                                            item = item,
                                            onIncrease = { viewModel.updateCartQuantity(item.product.id, 1.0) },
                                            onDecrease = { viewModel.updateCartQuantity(item.product.id, -1.0) },
                                            onRemove = { viewModel.removeFromCart(item.product.id) }
                                        )
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Checkout Button
                        Button(
                            onClick = { isCheckoutOpen = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("checkout_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إتمام البيع (${totalCartAmount.toInt()} د.ج)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Quick Weight / Custom Quantity Dialog (for fruits, vegetables, goods)
    weightProductToPrompt?.let { product ->
        QuickWeightDialog(
            product = product,
            onDismiss = { weightProductToPrompt = null },
            onConfirmQuantity = { quantity ->
                viewModel.addToCart(product, quantity)
                weightProductToPrompt = null
            }
        )
    }

    // Checkout Dialog
    if (isCheckoutOpen) {
        CheckoutDialog(
            cartItems = cart,
            onDismiss = { isCheckoutOpen = false },
            onConfirm = { discount, paid, customerName, paymentMethod ->
                viewModel.checkout(
                    discount = discount,
                    paid = paid,
                    customerName = customerName,
                    paymentMethod = paymentMethod
                )
                isCheckoutOpen = false
            }
        )
    }

    // Receipt Dialog (shown upon successful sale)
    receipt?.let { activeReceipt ->
        ReceiptDialog(
            receipt = activeReceipt,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }
}

@Composable
private fun PosHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("ابحث عن سلعة، خضر، فواكه، أو باركود...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_product_field")
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(category) },
                    label = { Text(category, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
private fun ProductsGrid(
    products: List<ProductEntity>,
    cart: List<CartItem>,
    onAddToCart: (ProductEntity) -> Unit,
    onQuickWeight: (ProductEntity) -> Unit,
    onIncreaseQty: (Long) -> Unit,
    onDecreaseQty: (Long) -> Unit
) {
    if (products.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "لا توجد سلع مطابقة للبحث",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 145.dp),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(products, key = { it.id }) { product ->
                val cartItem = cart.find { it.product.id == product.id }
                ProductGridCardEnhanced(
                    product = product,
                    cartQuantity = cartItem?.quantity ?: 0.0,
                    onAddToCart = { onAddToCart(product) },
                    onQuickWeight = { onQuickWeight(product) },
                    onIncrease = { onIncreaseQty(product.id) },
                    onDecrease = { onDecreaseQty(product.id) }
                )
            }
        }
    }
}

@Composable
fun ProductGridCardEnhanced(
    product: ProductEntity,
    cartQuantity: Double,
    onAddToCart: () -> Unit,
    onQuickWeight: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    val isInCart = cartQuantity > 0.001
    val isWeighable = product.unit == "كغ" || product.unit == "غرام"

    val borderColor by animateColorAsState(
        targetValue = if (isInCart) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "border"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isInCart) 2.dp else 0.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onAddToCart() }
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isInCart) 4.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Icon + Stock badge + In-Cart Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                ProductIcon(
                    iconKey = product.iconKey,
                    customImageUri = product.customImageUri,
                    size = 58.dp
                )

                // Stock Badge (Top-Start)
                val stockColor = when {
                    product.stockQuantity <= 0 -> ExpenseRed
                    product.stockQuantity <= product.minStockAlert -> WarningAmber
                    else -> ProfitGreen
                }
                val stockText = when {
                    product.stockQuantity <= 0 -> "نفد"
                    else -> "${product.stockQuantity.toInt()} ${product.unit}"
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(stockColor.copy(alpha = 0.15f))
                        .border(0.5.dp, stockColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stockText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = stockColor
                    )
                }

                // In-Cart Badge (Top-End)
                if (isInCart) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "× ${"%.1f".format(cartQuantity).removeSuffix(".0")}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Name
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Price and Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${product.salePrice.toInt()} د.ج",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "لكل ${product.unit}",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick scale button for weighable items
                if (isWeighable) {
                    IconButton(
                        onClick = onQuickWeight,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = "وزن مخصص",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Inline Stepper or Quick Add Bar
            if (isInCart) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDecrease,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "إنقاص", modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = "${"%.1f".format(cartQuantity).removeSuffix(".0")} ${product.unit}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onIncrease,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "زيادة", modifier = Modifier.size(14.dp))
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onAddToCart() }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "إضافة للسلة",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AutoCalculatedTotalCard(
    totalCartAmount: Double,
    totalItemCount: Double,
    itemTypesCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("عدد الأصناف:", fontSize = 12.sp)
                Text("$itemTypesCount أصناف", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("إجمالي الكمية والوزن:", fontSize = 12.sp)
                Text("${"%.1f".format(totalItemCount).removeSuffix(".0")} وحدة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الإجمالي التلقائي:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${totalCartAmount.toInt()} د.ج",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CartHeaderRow(
    cartCount: Int,
    totalItemCount: Double,
    onClearCart: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "سلة المبيعات ($cartCount)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
        if (cartCount > 0) {
            Text(
                text = "مسح الكل",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ExpenseRed,
                modifier = Modifier.clickable { onClearCart() }
            )
        }
    }
}

@Composable
private fun EmptyCartView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.RemoveShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "السلة فارغة حالياً",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "انقر على أي سلعة لإضافتها وحساب الإجمالي تلقائياً",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun QuickWeightDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirmQuantity: (Double) -> Unit
) {
    var quantityText by remember { mutableStateOf("1.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("تحديد الكمية أو الوزن: ${product.name}")
        },
        text = {
            Column {
                Text(
                    text = "سعر البيع: ${product.salePrice.toInt()} د.ج لكل ${product.unit}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Quick Weight shortcuts for fruits/vegetables
                val presets = listOf(0.5, 1.0, 1.5, 2.0, 3.0, 5.0)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets) { preset ->
                        OutlinedButton(
                            onClick = { quantityText = preset.toString() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("$preset ${product.unit}", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("الكمية بالـ (${product.unit})") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                val qty = quantityText.toDoubleOrNull() ?: 0.0
                val calculatedSubtotal = qty * product.salePrice

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "المجموع المحسوب: ${calculatedSubtotal.toInt()} د.ج",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toDoubleOrNull() ?: 1.0
                    if (qty > 0) {
                        onConfirmQuantity(qty)
                    }
                }
            ) {
                Text("إضافة للسلة")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1.5f)
        ) {
            ProductIcon(
                iconKey = item.product.iconKey,
                customImageUri = item.product.customImageUri,
                size = 34.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = item.product.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.unitPrice.toInt()} د.ج/${item.product.unit}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Stepper: - Qty +
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "إنقاص", modifier = Modifier.size(14.dp))
            }

            Text(
                text = "${"%.1f".format(item.quantity).removeSuffix(".0")} ${item.product.unit}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "زيادة", modifier = Modifier.size(14.dp))
            }
        }

        // Subtotal & Remove
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(1.1f)
        ) {
            Text(
                text = "${item.subtotal.toInt()} د.ج",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف",
                    tint = ExpenseRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

