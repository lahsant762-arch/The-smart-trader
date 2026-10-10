package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen

@Composable
fun AddTransactionDialog(
    initialType: String = "EXPENSE",
    onDismiss: () -> Unit,
    onSave: (type: String, category: String, amount: Double, notes: String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(if (initialType == "EXPENSE") "نقل وتوصيل" else "إيداع رأس مال") }
    var notes by remember { mutableStateOf("") }

    val expenseCategories = listOf("نقل وتوصيل", "فواتير كهرباء/ماء", "إيجار المحل", "أجور عمال", "تغليف وأكياس", "تلف سلع", "أخرى")
    val incomeCategories = listOf("إيداع رأس مال", "مبيعات إضافية", "استرداد ديون", "مداخيل أخرى")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (type == "EXPENSE") "تسجيل مصروف جديد" else "تسجيل مدخول جديد",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (type == "EXPENSE") ExpenseRed else ProfitGreen
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Type Toggle (Expense / Income)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = {
                            type = "EXPENSE"
                            category = "نقل وتوصيل"
                        },
                        label = { Text("مصروف (خارج)", fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ExpenseRed.copy(alpha = 0.2f),
                            selectedLabelColor = ExpenseRed
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = type == "INCOME",
                        onClick = {
                            type = "INCOME"
                            category = "إيداع رأس مال"
                        },
                        label = { Text("مدخول (داخل)", fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ProfitGreen.copy(alpha = 0.2f),
                            selectedLabelColor = ProfitGreen
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("المبلغ بالدينار (د.ج) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category chips
                Text(text = "التصنيف:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                val currentCats = if (type == "EXPENSE") expenseCategories else incomeCategories

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    currentCats.chunked(3).forEach { rowCats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowCats.forEach { catItem ->
                                FilterChip(
                                    selected = category == catItem,
                                    onClick = { category = catItem },
                                    label = { Text(catItem, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات وتفاصيل إضافية") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                onSave(type, category, amount, notes.trim())
                            }
                        },
                        enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "EXPENSE") ExpenseRed else ProfitGreen
                        )
                    ) {
                        Text("تسجيل")
                    }
                }
            }
        }
    }
}
