package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ProductEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.AiUiState
import com.example.ui.viewmodel.MerchantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAdvisorScreen(viewModel: MerchantViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val products by viewModel.products.collectAsStateWithLifecycle()
    val storeAnalysisState by viewModel.storeAnalysisState.collectAsStateWithLifecycle()
    val marketingState by viewModel.marketingState.collectAsStateWithLifecycle()
    val chatState by viewModel.chatState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab row: 1. تحليل المتجر | 2. عروض ترويجية | 3. اسأل المستشار | 4. استيراد RTDB
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("تحليل المتجر", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("عروض تسويقية", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("اسأل Gemini", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("استيراد بيانات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (selectedTab) {
            0 -> StoreAnalysisView(
                analysisState = storeAnalysisState,
                onRunAnalysis = { viewModel.runStoreAnalysis() }
            )
            1 -> MarketingGeneratorView(
                products = products,
                marketingState = marketingState,
                onGenerate = { prod, price, disc, cat ->
                    viewModel.generateMarketingOffer(prod, price, disc, cat)
                }
            )
            2 -> AiChatAssistantView(
                chatState = chatState,
                onAsk = { q -> viewModel.askMerchantAssistant(q) },
                onReset = { viewModel.resetAiChat() }
            )
            3 -> RtdbImportView(
                onImportDefault = {
                    viewModel.importRtdbExportData { count ->
                        Toast.makeText(context, "تم استيراد $count سلعة من بيانات RTDB بنجاح!", Toast.LENGTH_LONG).show()
                    }
                },
                onImportJson = { json ->
                    viewModel.importProductsFromJson(json) { count ->
                        Toast.makeText(context, "تم استيراد $count سلعة بنجاح!", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun StoreAnalysisView(
    analysisState: AiUiState,
    onRunAnalysis: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "المستشار التجاري الذكي",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "مدعوم بنموذج Gemini 3.8 Flash عبر Firebase AI Logic",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "يقوم الذكاء الاصطناعي بفحص مبيعاتك وأسعارك والتدفق النقدي الفعلي المخزن محلياً، ثم يقدم توصيات عملية لزيادة الأرباح وتجنب ركود السلع.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onRunAnalysis,
                        enabled = analysisState !is AiUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_ai_analysis_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (analysisState is AiUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري التحليل واستدعاء Gemini...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("بدء التحليل المالي والمخزوني الآن", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            AiResponseCard(
                title = "تقرير التوصيات والأداء التجاري",
                state = analysisState,
                onCopy = { text ->
                    copyToClipboard(context, "تقرير_التاجر_الذكي", text)
                },
                onShare = { text ->
                    shareText(context, "تقرير أداء المحل التجاري", text)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketingGeneratorView(
    products: List<ProductEntity>,
    marketingState: AiUiState,
    onGenerate: (productName: String, price: Double, discountPercent: Int, category: String) -> Unit
) {
    val context = LocalContext.current
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(products.firstOrNull()) }
    var discountPercent by remember { mutableIntStateOf(10) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مولد العروض الترويجية والمنشورات",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اختر سلعة من مخزنك وسيقوم Gemini بصياغة منشور دعائي جذاب باللغة العربية لنشره على فيسبوك وواتساب لجلب الزبائن.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Product Selector
                    Text("اختر السلعة المعروضة:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))

                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedProduct?.name ?: "اختر سلعة...",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            products.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text("${p.name} - ${p.salePrice} د.ج (${p.category})") },
                                    onClick = {
                                        selectedProduct = p
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Discount Selector Chips
                    Text("نسبة التخفيض المقترحة:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10, 15, 20, 25).forEach { disc ->
                            val isSelected = discountPercent == disc
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { discountPercent = disc }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$disc%",
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            selectedProduct?.let { p ->
                                onGenerate(p.name, p.salePrice, discountPercent, p.category)
                            }
                        },
                        enabled = selectedProduct != null && marketingState !is AiUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_marketing_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (marketingState is AiUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري صياغة المنشور التسويقي...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("توليد المنشور الإعلاني", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            AiResponseCard(
                title = "نص المنشور الإعلاني الجاهز للنشر",
                state = marketingState,
                onCopy = { text ->
                    copyToClipboard(context, "منشور_ترويجي", text)
                },
                onShare = { text ->
                    shareText(context, "عرض ترويجي خاص", text)
                }
            )
        }
    }
}

@Composable
private fun AiChatAssistantView(
    chatState: AiUiState,
    onAsk: (String) -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current
    var questionInput by remember { mutableStateOf("") }

    val quickQuestions = listOf(
        "كيف أتعامل مع ركود بيع الطماطم والخضار لتفادي التلف؟",
        "ما هي أنسب طريقة لتسعير الفواكه بهامش ربح 20%؟",
        "كيف أنظم جرد المحل نهاية كل أسبوع بشكل دقيق؟",
        "طريقة ذكية لزيادة متوسط قيمة سلة مشتريات الزبون الواحد."
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استفسار تجاري فوري (Ask Gemini)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "اطرح أي سؤال حول التجارة، إدارة المخازن، الضرائب، أو تنشيط المبيعات.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("أسئلة سريعة شائعة:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(quickQuestions) { qq ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        questionInput = qq
                                        onAsk(qq)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(qq, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = questionInput,
                        onValueChange = { questionInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_question_input"),
                        placeholder = { Text("اكتب سؤالك التجاري هنا...", fontSize = 13.sp) },
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (questionInput.isNotBlank()) {
                                        onAsk(questionInput)
                                    }
                                },
                                enabled = questionInput.isNotBlank() && chatState !is AiUiState.Loading
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "إرسال")
                            }
                        }
                    )
                }
            }
        }

        item {
            AiResponseCard(
                title = "إجابة المستشار التجاري",
                state = chatState,
                onCopy = { text -> copyToClipboard(context, "إجابة_المستشار", text) },
                onShare = { text -> shareText(context, "استشارة تجارية", text) }
            )
        }
    }
}

@Composable
private fun RtdbImportView(
    onImportDefault: () -> Unit,
    onImportJson: (String) -> Unit
) {
    var rawJson by remember {
        mutableStateOf(
            """
            {
              "products": {
                "item_1": { "name": "تفاح أحمر", "price": 250, "stock": 100, "category": "فواكه", "SKU": "AL-TF-610" },
                "item_2": { "name": "موز طازج", "price": 150, "stock": 120, "category": "فواكه", "SKU": "AL-MZ-120" },
                "item_3": { "name": "طماطم محلية", "price": 200, "stock": 2000, "category": "خضر", "SKU": "AL-TM-287" }
              }
            }
            """.trimIndent()
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "استيراد قاعدة بيانات RTDB المرفقة",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يتضمن ملف (islmport-default-rtdb-export.json) قائمة واسعة من السلع كالطماطم، البطاطا، البصل، الفواكه (تفاح، موز، رمان، كمثرى)، والملابس بالأسعار الجزائرية والصور.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onImportDefault,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_rtdb_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استيراد بيانات islmport-default-rtdb بنقرة واحدة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("استيراد مخصص من نص JSON:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("ألصق أي كائن JSON يحتوي على منتجات:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = rawJson,
                        onValueChange = { rawJson = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onImportJson(rawJson) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("استيراد المنتجات من الـ JSON")
                    }
                }
            }
        }
    }
}

@Composable
private fun AiResponseCard(
    title: String,
    state: AiUiState,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                if (state is AiUiState.Success || state is AiUiState.Streaming) {
                    val textToExport = when (state) {
                        is AiUiState.Success -> state.text
                        is AiUiState.Streaming -> state.text
                        else -> ""
                    }
                    Row {
                        IconButton(onClick = { onCopy(textToExport) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { onShare(textToExport) }) {
                            Icon(Icons.Default.Share, contentDescription = "مشاركة", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            when (state) {
                is AiUiState.Idle -> {
                    Text(
                        text = "النتائج ستظهر هنا فور تشغيل التحليل أو الاستفسار.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is AiUiState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("جاري المعالجة بواسطة Gemini 3.8 Flash...", fontSize = 13.sp)
                    }
                }
                is AiUiState.Streaming -> {
                    Text(
                        text = state.text,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                is AiUiState.Success -> {
                    Text(
                        text = state.text,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                is AiUiState.Error -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.message,
                            fontSize = 12.sp,
                            color = ExpenseRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "تم نسخ النص إلى الحافظة بنجاح", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, title: String, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TITLE, title)
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, title))
}
