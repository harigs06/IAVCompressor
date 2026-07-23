package com.example.iavcompressor.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.iavcompressor.R
import com.example.iavcompressor.helper.*
import com.example.iavcompressor.viewmodels.CompressionViewModel
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.example.iavcompressor.helper.CompressionQuality

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ImagePicker(
    modifier: Modifier = Modifier,
    sharedViewModel: CompressionViewModel,
    onNavigation : (CompressionQuality) -> Unit,

    ){
    val context = LocalContext.current
    val selectedImageState = remember { mutableStateListOf<Image>() }
    var selectedQuality by remember { mutableStateOf(CompressionQuality.MEDIUM) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris: List<Uri> ->
        if(!uris.isEmpty()){
            for (uri in uris){
                val metadata = extractMetadata(context,uri)
                selectedImageState.add(
                    Image(
                        uri,
                        metadata.first,
                        metadata.second
                    )
                )
            }
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
        if (selectedImageState.isNotEmpty()) {
            // Render the image asynchronously via Coil

            val pager = rememberPagerState(
                pageCount = { selectedImageState.size }
            )

            HorizontalPager(
                state = pager,
                modifier = modifier
                    .fillMaxWidth(),

                key = {index -> selectedImageState[index].uri}
            ) { page ->

                val currentItem = selectedImageState[page]
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .wrapContentSize(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        AsyncImage(
                            model = currentItem.uri,
                            contentDescription = "Selected user image",
                            modifier = Modifier
                                .size(250.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Icon(
                            painterResource(R.drawable.cancel_24px),
                            contentDescription = "Remove Image",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(4.dp)
                                .clickable {
                                    selectedImageState.removeAt(page)
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Render the metadata fields inside a clean Card block
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.padding(8.dp).fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Name: ${currentItem.name}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Size: ${currentItem.sizeDisplay}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                }
            }






            Spacer(modifier = Modifier.height(16.dp))

            QualitySelectorRow(
                selectedQuality = selectedQuality,
                onQualitySelected = { newQuality ->
                    selectedQuality = newQuality
                },
                modifier = Modifier.fillMaxWidth()
            )


            // Note: Material3 Buttons already have background coloring built-in.
            // Using .background(Color.Blue) on a modifier can cause weird clip bugs.
            Button(
                onClick = {
                    sharedViewModel.setSelectedImages(selectedImageState)
                    onNavigation(selectedQuality)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Start Compressing", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

        } else {
            // FIX 3: Removed .size(22.dp) which was crushing your button into invisibility

            Spacer(Modifier.height(60.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Select Images", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(40.dp))

                Button(
                    onClick = {

                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Select Video", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(40.dp))

                Button(
                    onClick = {

                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Select Audio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualitySelectorRow(
    selectedQuality: CompressionQuality,
    onQualitySelected: (CompressionQuality) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Select Compression Quality",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            CompressionQuality.entries.forEachIndexed { index, quality ->
                SegmentedButton(
                    selected = selectedQuality == quality,
                    onClick = { onQualitySelected(quality) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = CompressionQuality.entries.size
                    )
                ) {
                    Text(text = quality.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}