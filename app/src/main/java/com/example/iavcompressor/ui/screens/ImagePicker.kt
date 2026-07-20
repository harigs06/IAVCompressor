package com.example.iavcompressor.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.iavcompressor.R
import com.example.iavcompressor.helper.*

@Composable
fun ImagePicker(
    modifier: Modifier = Modifier, // Accept the modifier passed from parent
    onNavigation : (Uri) -> Unit
){
    val context = LocalContext.current
    var selectedImageState by remember { mutableStateOf<Image?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if(uri != null){
            val metadata = extractMetadata(context, uri)
            selectedImageState = Image(
                uri,
                metadata.first,
                metadata.second,
            )
        }
    }

    // FIX 1: Use the incoming 'modifier' argument instead of forcing fillMaxSize()
    Column(
        modifier = modifier
            .fillMaxWidth() // Fills width, but dynamically scales height to its contents
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        // FIX 2: Corrected the flat if-else structure
        if (selectedImageState != null) {
            // Render the image asynchronously via Coil
            Box(

            ) {
                AsyncImage(
                    model = selectedImageState!!.uri,
                    contentDescription = "Selected user image",
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Icon(
                    painterResource(R.drawable.cancel_24px),
                    contentDescription = "",
                    modifier = Modifier.align(Alignment.TopEnd)
//                        .size(22.dp)
                        .clickable{
                            selectedImageState = null
                        }
                )

            }


            Spacer(modifier = Modifier.height(16.dp))

            // Render the metadata fields inside a clean Card block
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Name: ${selectedImageState!!.name}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Size: ${selectedImageState!!.sizeDisplay}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note: Material3 Buttons already have background coloring built-in.
            // Using .background(Color.Blue) on a modifier can cause weird clip bugs.
            Button(
                onClick = {
                    selectedImageState?.uri?.let { onNavigation(it) }
                          selectedImageState = null},
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Start Compressing", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

        } else {
            // FIX 3: Removed .size(22.dp) which was crushing your button into invisibility

            Spacer(Modifier.height(160.dp))
            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Select Image", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}