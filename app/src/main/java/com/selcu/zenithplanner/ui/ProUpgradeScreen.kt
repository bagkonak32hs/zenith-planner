@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.selcu.zenithplanner.ui

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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selcu.zenithplanner.R
import com.selcu.zenithplanner.ui.theme.PlannerBlack
import com.selcu.zenithplanner.ui.theme.PlannerGold
import com.selcu.zenithplanner.ui.theme.PlannerInk

@Composable
internal fun ProUpgradeScreen(viewModel: PlannerViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val isPro by viewModel.isPro.collectAsState()

    ScreenColumn(
        title = stringResource(R.string.subscription),
        subtitle = stringResource(if (isPro) R.string.pro_active else R.string.free_plan)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PlannerBlack, contentColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.Star, contentDescription = null, tint = PlannerGold, modifier = Modifier.size(48.dp))
                Text(
                    text = stringResource(R.string.pro_plan),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PlannerGold
                )
                Text(
                    text = stringResource(R.string.pro_price),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
        PlannerCard {
            SectionHeader(stringResource(R.string.pro_features_title))
            Spacer(Modifier.size(4.dp))
            ProFeatureRow(stringResource(R.string.pro_feature_unlimited_notes))
            ProFeatureRow(stringResource(R.string.pro_feature_unlimited_goals))
            ProFeatureRow(stringResource(R.string.pro_feature_unlimited_finance))
            ProFeatureRow(stringResource(R.string.pro_feature_sync))
        }
        if (isPro) {
            PlannerCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = PlannerGold)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.pro_active), fontWeight = FontWeight.SemiBold, color = PlannerGold)
                }
            }
        } else {
            Button(
                onClick = { context.findActivity()?.let(viewModel::launchSubscription) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PlannerGold, contentColor = PlannerBlack)
            ) {
                Icon(Icons.Outlined.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.upgrade_to_pro) + " — " + stringResource(R.string.pro_price),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PlannerInk, contentColor = Color.White)
        ) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
private fun ProFeatureRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = PlannerGold, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}
