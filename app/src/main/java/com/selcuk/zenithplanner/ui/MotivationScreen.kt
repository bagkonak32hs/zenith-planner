@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.selcuk.zenithplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.selcuk.zenithplanner.R
import com.selcuk.zenithplanner.ui.theme.PlannerBlack
import com.selcuk.zenithplanner.ui.theme.PlannerGold
import com.selcuk.zenithplanner.ui.theme.PlannerInk
import com.selcuk.zenithplanner.ui.theme.PlannerMuted

@Composable
internal fun MotivationScreen(viewModel: PlannerViewModel) {
    val quotes by viewModel.quotes.collectAsState()
    var text by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    val featuredQuote = quotes.firstOrNull()

    ScreenColumn(title = stringResource(R.string.motivation), subtitle = stringResource(R.string.app_name)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PlannerBlack, contentColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.FormatQuote, contentDescription = null, tint = PlannerGold, modifier = Modifier.size(40.dp))
                Text(
                    featuredQuote?.text ?: stringResource(R.string.reflection_question),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                Text(featuredQuote?.author ?: stringResource(R.string.app_name), color = PlannerGold)
            }
        }
        PlannerCard {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                placeholder = { Text(stringResource(R.string.quote_hint)) }
            )
            Spacer(Modifier.padding(top = 8.dp))
            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.author_hint)) }
            )
            Spacer(Modifier.padding(top = 12.dp))
            Button(onClick = {
                viewModel.addQuote(text, author)
                text = ""
                author = ""
            }) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add))
            }
        }
        if (quotes.size > 1) {
            SectionHeader(stringResource(R.string.all_quotes))
            quotes.drop(1).forEach { quote ->
                PlannerCard {
                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(quote.text, fontWeight = FontWeight.SemiBold)
                            Text(quote.author, color = PlannerGold, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.deleteQuote(quote.id) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = null, tint = PlannerMuted)
                        }
                    }
                }
            }
        }
        if (featuredQuote != null) {
            Button(
                onClick = { viewModel.deleteQuote(featuredQuote.id) },
                colors = ButtonDefaults.buttonColors(containerColor = PlannerInk, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.delete_featured_quote))
            }
        }
    }
}

