// File: ui.report/PhotoStep.kt
package com.example.crfcavemonitor.ui.report

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class PhotoItem(val uri: Uri, val caption: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoStepComponent(
    photoList: List<PhotoItem>,
    onPhotoListChanged: (List<PhotoItem>) -> Unit
) {
    val context = LocalContext.current
    var selectedPhoto by remember { mutableStateOf<PhotoItem?>(null) }
    var showCaptionDialog by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var editMode by remember { mutableStateOf(false) }
    var editCaption by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                pendingUri = it
                showCaptionDialog = true
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && cameraImageUri != null) {
                pendingUri = cameraImageUri
                showCaptionDialog = true
            }
        }
    )

    // Save in app-private Pictures dir, covered by file_paths.xml
    fun createImageUri(context: Context): Uri? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: context.filesDir
        val file = File(storageDir, "IMG_$timestamp.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedPhoto != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = rememberAsyncImagePainter(selectedPhoto!!.uri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                if (editMode) {
                    OutlinedTextField(
                        value = editCaption,
                        onValueChange = { editCaption = it },
                        label = { Text("Edit Caption") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    Button(onClick = {
                        selectedPhoto?.let { current ->
                            val updatedList = photoList.map {
                                if (it.uri == current.uri) it.copy(caption = editCaption) else it
                            }
                            onPhotoListChanged(updatedList)
                            selectedPhoto = current.copy(caption = editCaption)
                            editMode = false
                        }
                    }) {
                        Text("Save Caption")
                    }
                } else {
                    Text(text = selectedPhoto!!.caption, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            editCaption = selectedPhoto!!.caption
                            editMode = true
                        }) {
                            Text("Edit Caption")
                        }
                        Button(onClick = {
                            val updatedList = photoList.filterNot { it.uri == selectedPhoto!!.uri }
                            onPhotoListChanged(updatedList)
                            selectedPhoto = null
                        }) {
                            Text("Delete Photo")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { selectedPhoto = null }) {
                        Text("Back to Gallery")
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(128.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(photoList) { photo ->
                        Image(
                            painter = rememberAsyncImagePainter(photo.uri),
                            contentDescription = null,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { selectedPhoto = photo }
                        )
                    }
                }

                if (showCaptionDialog && pendingUri != null) {
                    var captionText by remember { mutableStateOf("") }

                    AlertDialog(
                        onDismissRequest = {
                            showCaptionDialog = false
                            pendingUri = null
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                onPhotoListChanged(photoList + PhotoItem(pendingUri!!, captionText))
                                showCaptionDialog = false
                                pendingUri = null
                            }) {
                                Text("Add Photo")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showCaptionDialog = false
                                pendingUri = null
                            }) {
                                Text("Cancel")
                            }
                        },
                        title = { Text("Add a Caption") },
                        text = {
                            OutlinedTextField(
                                value = captionText,
                                onValueChange = { captionText = it },
                                label = { Text("Caption") },
                                singleLine = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {
                        cameraImageUri = createImageUri(context)
                        cameraImageUri?.let { cameraLauncher.launch(it) }
                    }) {
                        Text("Camera")
                    }
                    Button(onClick = {
                        galleryLauncher.launch("image/*")
                    }) {
                        Text("Add from Device")
                    }
                }
            }
        }
    }
}