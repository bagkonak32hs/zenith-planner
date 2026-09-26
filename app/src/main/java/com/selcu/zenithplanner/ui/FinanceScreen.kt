@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.selcu.zenithplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selcu.zenithplanner.R
import com.selcu.zenithplanner.data.FinanceEntryEntity
import com.selcu.zenithplanner.subscription.FREE_FINANCE_LIMIT
import com.selcu.zenithplanner.ui.theme.PlannerBlack
import com.selcu.zenithplanner.ui.theme.PlannerMuted

@Composable
internal fun FinanceScreen(viewModel: PlannerViewModel, onUpgrade: () -> Unit) {
    val entries by viewModel.financeEntries.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val atLimit = !isPro && entries.size >= FREE_FINANCE_LIMIT
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("income") }
    val income = entries.filter { it.type == "income" }.sumOf { it.amount }
    val expense = entries.filter { it.type == "expense" }.sumOf { it.amount }

    ScreenColumn(title = stringResource(R.string.finance), subtitle = stringResource(R.string.balance) + ": " + money(income - expense)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FinanceMetric(stringResource(R.string.income), income, Color(0xFF617462))
            FinanceMetric(stringResource(R.string.expense), expense, Color(0xFF8E4B4B))
            FinanceMetric(stringResource(R.string.balance), income - expense, PlannerBlack)
        }
        if (atLimit) {
            ProLimitBanner(
                message = stringResource(R.string.pro_limit_finance, FREE_FINANCE_LIMIT),
                onUpgrade = onUpgrade
            )
        } else {
            PlannerCard {
                SectionHeader(stringResource(R.string.add_transaction))
                OutlinedTextField(value = title, onValueChange = { title = it }, placeholder = { Text(stringResource(R.string.title)) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.padding(top = 8.dp))
                OutlinedTextField(value = category, onValueChange = { category = it }, placeholder = { Text(stringResource(R.string.category)) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.padding(top = 8.dp))
                OutlinedTextField(value = amountText, onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } }, placeholder = { Text(stringResource(R.string.amount)) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.padding(top = 10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == "income", onClick = { type = "income" }, label = { Text(stringResource(R.string.income)) })
                    FilterChip(selected = type == "expense", onClick = { type = "expense" }, label = { Text(stringResource(R.string.expense)) })
                }
                Spacer(Modifier.padding(top = 12.dp))
                Button(onClick = {
                    viewModel.addFinanceEntry(title, category, amountText.toDoubleOrNull() ?: 0.0, type)
                    title = ""
                    category = ""
                    amountText = ""
                }) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.add))
                }
            }
        }
        if (entries.isEmpty()) {
            Text(stringResource(R.string.no_finance), color = PlannerMuted, modifier = Modifier.padding(vertical = 16.dp))
        } else {
            entries.forEach { entry ->
                FinanceEntryRow(entry, onDelete = { viewModel.deleteFinanceEntry(entry.id) })
            }
        }
    }
}

@Composable
private fun FinanceMetric(label: String, amount: Double, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color, contentColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.width(170.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, color = Color(0xFFE8DBC0))
            Text(money(amount), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FinanceEntryRow(entry: FinanceEntryEntity, onDelete: () -> Unit) {
    PlannerCard {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(entry.title, fontWeight = FontWeight.SemiBold)
                Text("${entry.category} • ${entry.date}", color = PlannerMuted, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = (if (entry.type == "income") "+" else "-") + money(entry.amount),
                color = if (entry.type == "income") Color(0xFF617462) else Color(0xFF8E4B4B),
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = PlannerMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}
