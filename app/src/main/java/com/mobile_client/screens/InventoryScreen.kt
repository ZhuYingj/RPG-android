package com.mobile_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mobile_client.utils.ImageResources
import com.mobile_client.utils.Screen
import com.mobile_client.viewModels.InventoryViewModel
import com.mobile_client.viewModels.ThemeViewModel
import kotlinx.coroutines.launch

@Composable
fun InventoryScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    inventoryViewModel: InventoryViewModel = viewModel(),
    themeViewModel: ThemeViewModel,
) {
    val assets = themeViewModel.assets
    val inventory by inventoryViewModel.inventory.collectAsState()
    val equipped by inventoryViewModel.equipped.collectAsState()
    val isLoading by inventoryViewModel.isLoading.collectAsState()
    val snackbarMessage by inventoryViewModel.message.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        inventoryViewModel.loadData()
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            inventoryViewModel.clearMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(assets.backgroundInventory),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Inventaire",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = assets.mainPageTextColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                inventory.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Votre inventaire est vide.",
                            fontSize = 19.sp,
                            color = assets.mainPageTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(inventory) { item ->
                            val cosmetic = inventoryViewModel.getCosmetic(item.cosmeticId)
                            val isEquipped = equipped.any { it._id == item.cosmeticId }

                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFB4D8A6))
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = cosmetic?.name ?: "???",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                val resId = cosmetic?.filePath?.let { ImageResources.cosmeticToImage[it] }

                                if (resId != null) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = cosmetic.name,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .padding(bottom = 10.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                Text(
                                    text = cosmetic?.description ?: "",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.height(50.dp)
                                )

                                Text(
                                    text = "Quantité possédée: ${item.quantity}",
                                    fontSize = 13.sp,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                if (cosmetic?.type == 0) {
                                    Text(
                                        text = "Disponible pendant la partie",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Button(
                                            onClick = {
                                                if (cosmetic?.type != 0 && cosmetic?.type != 3)
                                                    inventoryViewModel.equipItem(item)
                                                else if (cosmetic?.type == 3) {
                                                    navController.navigate(Screen.Account.route) {
                                                        popUpTo(0) { inclusive = true }
                                                    }
                                                }
                                            },
                                            enabled = !isEquipped,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF4CAF50),
                                                disabledContainerColor = Color(0xFF759D77)
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (cosmetic?.type == 0) {
                                                    "Disponible pendant la partie"
                                                } else if (cosmetic?.type == 3) {
                                                    "Compte"
                                                } else {
                                                    if (isEquipped) "Équipé" else "Équipper"
                                                },
                                                color = Color.White,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
