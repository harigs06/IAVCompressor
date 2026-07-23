package com.example.iavcompressor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iavcompressor.helper.CompressionQuality
import com.example.iavcompressor.viewmodels.CompressionViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToCompress: (CompressionQuality) -> Unit,
    sharedViewModel : CompressionViewModel
){
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier.fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(6.dp)
                .fillMaxWidth()
                .height(80.dp)
        ){



           Text(
               "IAV Compressor",
               fontSize = 22.sp,
               fontStyle = FontStyle.Italic,
               fontWeight = FontWeight.Bold
           )


        }

        ImagePicker(
            modifier = Modifier
                .fillMaxWidth(),
            onNavigation = onNavigateToCompress,
            sharedViewModel = sharedViewModel
        )

//        Text("Image Picker")

//        Body contains :
//        Button -> to select image
//        After image is selected -> AsyncImage to display it
//        Button -> Start Compressing -> then navigate to other screen


    }
}

