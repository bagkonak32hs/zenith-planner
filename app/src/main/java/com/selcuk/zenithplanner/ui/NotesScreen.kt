@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.selcuk.zenithplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selcuk.zenithplanner.R
import com.selcuk.zenithplanner.data.NoteEntity
import com.selcuk.zenithplanner.subscription.FREE_NOTES_LIMIT
import com.selcuk.zenithplanner.ui.theme.PlannerInk
import com.selcuk.zenithplanner.ui.theme.PlannerMuted

@Composable
internal fun NotesScreen(viewModel: PlannerViewModel, onUpgrade: () -> Unit) {
    val notes by viewModel.notes.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val atLimit = !isPro && notes.size >= FREE_NOTES_LIMIT
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    ScreenColumn(title = stringResource(R.string.notes), subtitle = stringResource(R.string.add_note)) {
        if (atLimit) {
            ProLimitBanner(
                message = stringResource(R.string.pro_limit_notes, FREE_NOTES_LIMIT),
                onUpgrade = onUpgrade
            )
        } else {
            PlannerCard {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.note_title)) }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text(stringResource(R.string.note_body)) }
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    viewModel.addNote(title, body)
                    title = ""
                    body = ""
                }) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.add_note))
                }
            }
        }
        if (notes.isEmpty()) {
            Text(stringResource(R.string.no_notes), color = PlannerMuted)
        } else {
            notes.forEach { note ->
                NoteCard(
                    note = note,
                    onUpdate = { t, b -> viewModel.updateNote(note.id, t, b) },
                    onDelete = { viewModel.deleteNote(note.id) }
                )
            }
        }
    }
}

@Composable
private fun NoteCard(note: NoteEntity, onUpdate: (String, String) -> Unit, onDelete: () -> Unit) {
    var editing by remember(note.id) { mutableStateOf(false) }
    var editTitle by remember(note.id) { mutableStateOf(note.title) }
    var editBody by remember(note.id) { mutableStateOf(note.body) }

    PlannerCard {
        if (editing) {
            OutlinedTextField(
                value = editTitle,
                onValueChange = { editTitle = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.note_title)) }
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = editBody,
                onValueChange = { editBody = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                placeholder = { Text(stringResource(R.string.note_body)) }
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    onUpdate(editTitle, editBody)
                    editing = false
                }) { Text(stringResource(R.string.save)) }
                Button(
                    onClick = { editing = false; editTitle = note.title; editBody = note.body },
                    colors = ButtonDefaults.buttonColors(containerColor = PlannerInk)
                ) { Text(stringResource(R.string.back)) }
            }
        } else {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(note.title, fontWeight = FontWeight.SemiBold)
                    if (note.body.isNotBlank()) Text(note.body, color = PlannerMuted)
                }
                IconButton(onClick = { editing = true; editTitle = note.title; editBody = note.body }) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = PlannerMuted)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = PlannerMuted)
                }
            }
        }
    }
}

