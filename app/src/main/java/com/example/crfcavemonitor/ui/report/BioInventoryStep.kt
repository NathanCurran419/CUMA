package com.example.crfcavemonitor.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.crfcavemonitor.data.SpeciesCount
import androidx.compose.ui.Alignment

@Composable
fun BioInventoryStep(
    speciesList: List<SpeciesCount>,
    onSpeciesListChanged: (List<SpeciesCount>) -> Unit
) {
    val tempSpeciesList = speciesList.toMutableList()

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        itemsIndexed(tempSpeciesList) { index, species ->
            SpeciesRow(
                species = species,
                onUpdate = { updated ->
                    tempSpeciesList[index] = updated
                    onSpeciesListChanged(tempSpeciesList)
                }
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val newList = tempSpeciesList.toMutableList()
                newList.add(
                    SpeciesCount(
                        reportId = 0,
                        speciesName = "",
                        count = 0,
                        tentative = false,
                        notes = "" // ensure new rows always have notes
                    )
                )
                onSpeciesListChanged(newList)
            }) {
                Text("Add Blank Row")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeciesRow(
    species: SpeciesCount,
    onUpdate: (SpeciesCount) -> Unit
) {
    var name by remember { mutableStateOf(species.speciesName) }
    var count by remember { mutableStateOf(species.count) }
    var expanded by remember { mutableStateOf(false) }
    var showNoteField by remember { mutableStateOf(species.notes.isNotBlank()) }
    var noteText by remember { mutableStateOf(species.notes) } // start with persisted notes
    var noteDropdownExpanded by remember { mutableStateOf(false) }

    val isEntered = name.isNotBlank()

    val suggestions = if (name.isBlank()) {
        allSpecies
    } else {
        allSpecies.filter {
            it.name.contains(name, ignoreCase = true)
        }
    }

    val noteSuggestions = listOf(
        "In water",
        "Tentative",
        "White nose syndrome positive",
        "By signs"
    )

    val filteredNotes = if (noteText.isBlank()) noteSuggestions else noteSuggestions.filter {
        it.contains(noteText, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (species.speciesName.isBlank()) "New" else "#${species.id}",
                modifier = Modifier.width(50.dp)
            )

            Column(Modifier.weight(1f)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            expanded = true
                            onUpdate(species.copy(speciesName = it, notes = noteText))
                        },
                        label = { Text("Species Name") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        readOnly = false
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        suggestions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name) },
                                onClick = {
                                    name = option.name
                                    expanded = false
                                    onUpdate(
                                        species.copy(
                                            speciesName = option.name,
                                            id = option.id.toLong(),
                                            notes = noteText
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            if (isEntered) {
                Row(modifier = Modifier.wrapContentWidth()) {
                    IconButton(onClick = {
                        if (count > 0) {
                            count--
                            onUpdate(species.copy(count = count, notes = noteText))
                        }
                    }) {
                        Text("-")
                    }
                    OutlinedTextField(
                        value = count.toString(),
                        onValueChange = {
                            val safeCount = it.toIntOrNull() ?: count
                            count = safeCount
                            onUpdate(species.copy(count = count, notes = noteText))
                        },
                        modifier = Modifier
                            .width(60.dp)
                            .align(Alignment.CenterVertically),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )
                    IconButton(onClick = {
                        count++
                        onUpdate(species.copy(count = count, notes = noteText))
                    }) {
                        Text("+")
                    }
                }
            }
        }

        if (isEntered) {
            TextButton(onClick = { showNoteField = !showNoteField }) {
                Text(if (showNoteField) "Hide Note" else "Add or Show Note")
            }
        }

        if (showNoteField) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = noteDropdownExpanded,
                    onExpandedChange = { noteDropdownExpanded = !noteDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = {
                            noteText = it
                            noteDropdownExpanded = filteredNotes.isNotEmpty()
                            onUpdate(species.copy(notes = noteText))
                        },
                        label = { Text("Observation Notes") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        singleLine = false
                    )

                    ExposedDropdownMenu(
                        expanded = noteDropdownExpanded,
                        onDismissRequest = { noteDropdownExpanded = false }
                    ) {
                        filteredNotes.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    noteText = suggestion
                                    noteDropdownExpanded = false
                                    onUpdate(species.copy(notes = noteText)) // persist notes
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}