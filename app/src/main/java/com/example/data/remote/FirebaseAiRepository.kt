package com.example.data.remote

import android.util.Log
import com.example.data.local.entity.CashTransactionEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "FirebaseAiRepository"

class FirebaseAiRepository {

    // Lazy initialization of Gemini 3.8 Flash model via Firebase AI Logic
    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    /**
     * Streams AI financial and inventory analysis for the merchant
     */
    fun analyzeStorePerformanceStream(
        products: List<ProductEntity>,
        sales: List<SaleEntity>,
        transactions: List<CashTransactionEntity>
    ): Flow<String> = flow {
        val totalRevenue = sales.sumOf { it.totalAmount }
        val totalExpenses = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val netProfit = totalRevenue - totalExpenses
        val lowStockProducts = products.filter { it.stockQuantity <= it.minStockAlert }
        val topProducts = products.take(5).joinToString(", ") { "${it.name} (${it.stockQuantity} ${it.unit})" }

        val prompt = """
            أنت خبير ومستشار تجاري واقتصادي ذكي مخصص للتجار ومحلات التجزئة ونقاط البيع (POS) في الجزائر.
            قم بتحليل البيانات المالية والمخزونية التالية وقدم تقريراً وتوصيات عملية مختصرة ومباشرة باللغة العربية:
            
            - إجمالي المبيعات المحققة: $totalRevenue د.ج
            - إجمالي المصاريف التشغيلية: $totalExpenses د.ج
            - صافي الربح التقديري: $netProfit د.ج
            - إجمالي عدد السلع في المخزن: ${products.size}
            - إجمالي عدد فواتير البيع: ${sales.size}
            - السلع التي شارفت على النفاد (${lowStockProducts.size} سلعة): ${lowStockProducts.joinToString(", ") { it.name }}
            - عينة من السلع المتوفرة: $topProducts
            
            المطلوب:
            1. تقييم سريع للوضع المالي والتدفق النقدي (Cash Flow).
            2. توصيات فورية لإعادة تزويد المخزون وتفادي نفاد السلع الأكثر طلباً.
            3. نصيحة ذكية لتحسين هوامش الربح وزيادة متوسط قيمة سلة الشراء (Basket Size).
            
            قدم الإجابة بتنسيق جذاب ومباشر مع نقاط ورموز تعبيرية ملائمة.
        """.trimIndent()

        try {
            withTimeout(60_000L) {
                model.generateContentStream(prompt).collect { chunk ->
                    val text = chunk.text
                    if (!text.isNullOrEmpty()) {
                        emit(text)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error in analyzeStorePerformanceStream", e)
            throw e
        }
    }

    /**
     * Generates a catchy social media / WhatsApp marketing promo for selected products
     */
    fun generateMarketingOfferStream(
        productName: String,
        price: Double,
        discountPercent: Int,
        category: String
    ): Flow<String> = flow {
        val prompt = """
            أنت خبير تسويق محترف لمحلات السوبرماركت والتجزئة.
            قم بصياغة رسالة تسويقية وترويجية جذابة ومميزة جداً للنشر على فيسبوك وتطبيق واتساب للزبائن:
            
            - اسم المنتج: $productName
            - التصنيف: $category
            - السعر الأصلي: $price د.ج
            - نسبة التخفيض أو العرض الخاص: $discountPercent%
            
            المطلوب:
            - عنوان مشوق يخطف الأنظار مع عبارة ترحيبية بالزبائن.
            - تسليط الضوء على الجودة العالية والطزاجة أو الميزة التنافسية.
            - دعوة صريحة للشراء (Call to action) مع التذكير بنفاد الكمية.
            - استخدام لهجة جزائرية لطيفة ومهنية مع إيموجي مناسبة.
        """.trimIndent()

        try {
            withTimeout(60_000L) {
                model.generateContentStream(prompt).collect { chunk ->
                    val text = chunk.text
                    if (!text.isNullOrEmpty()) {
                        emit(text)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateMarketingOfferStream", e)
            throw e
        }
    }

    /**
     * Custom merchant interactive question answering
     */
    fun askGeminiStream(
        question: String,
        storeContext: String
    ): Flow<String> = flow {
        val prompt = """
            أنت المساعد الذكي لمحل 'التاجر الذكي - Smart POS' في الجزائر.
            سياق المحل الحالي:
            $storeContext
            
            سؤال التاجر أو الموظف:
            $question
            
            أجب بدقة ومهنية باللغة العربية مع مراعاة بيئة التجارة ونقاط البيع اليومية.
        """.trimIndent()

        try {
            withTimeout(60_000L) {
                model.generateContentStream(prompt).collect { chunk ->
                    val text = chunk.text
                    if (!text.isNullOrEmpty()) {
                        emit(text)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error in askGeminiStream", e)
            throw e
        }
    }
}
