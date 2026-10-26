package com.example.data.remote

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.IncomeEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreSyncRepository(private val context: Context) {

    private val auth: FirebaseAuth = Firebase.auth

    // Initialize with named database ID from res/values/firebase_applet_config.xml
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.getString(R.string.firestore_database_id)
    )

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    val currentUser get() = auth.currentUser

    private fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    // 1. Interactive Sign-In with Google via Credential Manager
    fun signInWithGoogle(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("معرف عميل جوجل غير مهيأ")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val activity = context.findActivity() ?: run {
                    onError("تعذر العثور على واجهة النشاط")
                    return@launch
                }
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                    onSuccess()
                } else {
                    onError("نوع تسجيل الدخول غير متوقع")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In cancelled: ${e.message}")
                onCancelled()
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In failed", e)
                onError(e.localizedMessage ?: "فشل تسجيل الدخول بجوجل")
            }
        }
    }

    // 2. Sign-Out Flow
    fun signOut(
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onComplete: () -> Unit
    ) {
        auth.signOut()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("Auth", "Failed to clear credential state", e)
            } finally {
                onComplete()
            }
        }
    }

    // 3. Backup all local store data (products, sales, expenses, incomes) to Firebase Firestore
    suspend fun backupLocalDataToCloud(
        products: List<ProductEntity>,
        sales: List<SaleEntity>,
        expenses: List<ExpenseEntity>,
        incomes: List<IncomeEntity>
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("يجب تسجيل الدخول أولاً"))
        _isSyncing.value = true

        return try {
            val userDoc = firestore.collection("users").document(uid)

            // Save user profile metadata
            userDoc.set(
                mapOf(
                    "email" to (auth.currentUser?.email ?: ""),
                    "displayName" to (auth.currentUser?.displayName ?: "تاجر"),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                ),
                SetOptions.merge()
            ).await()

            // Backup products
            val productsCol = userDoc.collection("products")
            products.forEach { p ->
                val pMap = mapOf(
                    "name" to p.name,
                    "category" to p.category,
                    "barcode" to p.barcode,
                    "costPrice" to p.costPrice,
                    "salePrice" to p.salePrice,
                    "stockQuantity" to p.stockQuantity,
                    "unit" to p.unit,
                    "iconKey" to p.iconKey,
                    "minStockAlert" to p.minStockAlert,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
                productsCol.document(p.id.toString()).set(pMap, SetOptions.merge()).await()
            }

            // Backup sales
            val salesCol = userDoc.collection("sales")
            sales.forEach { s ->
                val sMap = mapOf(
                    "invoiceNumber" to s.invoiceNumber,
                    "subtotalAmount" to s.subtotalAmount,
                    "discountAmount" to s.discountAmount,
                    "totalAmount" to s.totalAmount,
                    "paidAmount" to s.paidAmount,
                    "changeAmount" to s.changeAmount,
                    "customerName" to s.customerName,
                    "paymentMethod" to s.paymentMethod,
                    "timestamp" to com.google.firebase.Timestamp(s.timestamp / 1000, ((s.timestamp % 1000) * 1000000).toInt())
                )
                salesCol.document(s.saleId.toString()).set(sMap, SetOptions.merge()).await()
            }

            // Backup expenses
            val expensesCol = userDoc.collection("expenses")
            expenses.forEach { ex ->
                val exMap = mapOf(
                    "title" to ex.title,
                    "category" to ex.category,
                    "amount" to ex.amount,
                    "notes" to ex.notes,
                    "timestamp" to com.google.firebase.Timestamp(ex.timestamp / 1000, ((ex.timestamp % 1000) * 1000000).toInt())
                )
                expensesCol.document(ex.id.toString()).set(exMap, SetOptions.merge()).await()
            }

            // Backup incomes
            val incomesCol = userDoc.collection("incomes")
            incomes.forEach { inc ->
                val incMap = mapOf(
                    "source" to inc.source,
                    "category" to inc.category,
                    "amount" to inc.amount,
                    "notes" to inc.notes,
                    "timestamp" to com.google.firebase.Timestamp(inc.timestamp / 1000, ((inc.timestamp % 1000) * 1000000).toInt())
                )
                incomesCol.document(inc.id.toString()).set(incMap, SetOptions.merge()).await()
            }

            _isSyncing.value = false
            _syncMessage.value = "تمت المزامنة بنجاح مع سحابة Firebase!"
            Result.success("تم رفع ${products.size} سلعة و ${sales.size} فاتورة لسحابة فيربيز")
        } catch (e: Exception) {
            _isSyncing.value = false
            _syncMessage.value = "فشل النسخ الاحتياطي: ${e.localizedMessage}"
            Result.failure(e)
        }
    }
}
