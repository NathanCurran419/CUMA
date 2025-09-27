// File: ui.report/VisitDetailsStep.kt
package com.example.crfcavemonitor.ui.report

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Location
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun VisitDetailsStep(
    initialData: VisitDetailsFormData,
    onDetailsChanged: (VisitDetailsFormData) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val openDatePicker = remember { mutableStateOf(false) }

    // Rationale dropdown (existing)
    var rationaleExpanded by remember { mutableStateOf(false) }
    val rationaleSuggestions = listOf("Visitation", "WNS/bats", "Gen Bio", "Cultural")

    // NEW: Organization dropdown state + options
    var orgExpanded by remember { mutableStateOf(false) }
    val organizationOptions = remember {
        listOf(
            "Cave Research Foundation",
            "Missouri Speleological Survey",
            "Springfield Plateau Grotto",
            "Kansas City Area Grotto",
            "Meramec Valley Grotto",
            "CAIRN"
        )
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    fun fetchLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val formatted = "${it.latitude}, ${it.longitude}"
                    onDetailsChanged(initialData.copy(location = formatted))
                }
            }
        }
    }

    if (openDatePicker.value) {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val newDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                onDetailsChanged(initialData.copy(monitorDate = newDate))
                openDatePicker.value = false
            },
            Calendar.getInstance().apply { time = initialData.monitorDate }.get(Calendar.YEAR),
            Calendar.getInstance().apply { time = initialData.monitorDate }.get(Calendar.MONTH),
            Calendar.getInstance().apply { time = initialData.monitorDate }.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = initialData.mssAcc,
            onValueChange = { onDetailsChanged(initialData.copy(mssAcc = it)) },
            label = { Text("MSS ACC #") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = initialData.caveName,
            onValueChange = { onDetailsChanged(initialData.copy(caveName = it)) },
            label = { Text("Cave Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = initialData.ownerUnit,
            onValueChange = { onDetailsChanged(initialData.copy(ownerUnit = it)) },
            label = { Text("Owner / Unit") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = dateFormat.format(initialData.monitorDate),
            onValueChange = {},
            label = { Text("Monitor Date") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { openDatePicker.value = true },
            enabled = false
        )

        // Rationale (existing searchable dropdown)
        ExposedDropdownMenuBox(
            expanded = rationaleExpanded,
            onExpandedChange = { rationaleExpanded = !rationaleExpanded }
        ) {
            OutlinedTextField(
                value = initialData.rationale,
                onValueChange = {
                    onDetailsChanged(initialData.copy(rationale = it))
                    rationaleExpanded = true
                },
                label = { Text("Rationale") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rationaleExpanded) }
            )
            ExposedDropdownMenu(
                expanded = rationaleExpanded,
                onDismissRequest = { rationaleExpanded = false }
            ) {
                rationaleSuggestions
                    .filter { it.contains(initialData.rationale, ignoreCase = true) && it != initialData.rationale }
                    .forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                onDetailsChanged(initialData.copy(rationale = suggestion))
                                rationaleExpanded = false
                            }
                        )
                    }
            }
        }

        OutlinedTextField(
            value = initialData.areaMonitored,
            onValueChange = { onDetailsChanged(initialData.copy(areaMonitored = it)) },
            label = { Text("Area Monitored") },
            modifier = Modifier.fillMaxWidth()
        )

        // ────────────────────────────────────────────────────────────────
        // NEW: Organization searchable dropdown / recommendation menu
        // ────────────────────────────────────────────────────────────────
        ExposedDropdownMenuBox(
            expanded = orgExpanded,
            onExpandedChange = { orgExpanded = !orgExpanded }
        ) {
            OutlinedTextField(
                value = initialData.organization,
                onValueChange = {
                    onDetailsChanged(initialData.copy(organization = it))
                    orgExpanded = true // open and filter as user types
                },
                label = { Text("Organization") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = orgExpanded) }
            )
            ExposedDropdownMenu(
                expanded = orgExpanded,
                onDismissRequest = { orgExpanded = false }
            ) {
                val filtered = organizationOptions.filter {
                    val q = initialData.organization.trim()
                    q.isBlank() || it.contains(q, ignoreCase = true)
                }
                filtered.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onDetailsChanged(initialData.copy(organization = option))
                            orgExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = initialData.monitoredBy,
            onValueChange = { onDetailsChanged(initialData.copy(monitoredBy = it)) },
            label = { Text("Monitored By") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedTextField(
                value = initialData.location,
                onValueChange = { onDetailsChanged(initialData.copy(location = it)) },
                label = { Text("Location (Lat, Long)") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (locationPermissionState.status.isGranted) {
                        fetchLocation()
                    } else {
                        locationPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier.alignByBaseline()
            ) {
                Text("Use GPS")
            }
        }
    }
}