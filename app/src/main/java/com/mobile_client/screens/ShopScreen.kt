package com.mobile_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mobile_client.components.PressableButton
import com.mobile_client.services.AccountService
import com.mobile_client.utils.Cosmetic
import com.mobile_client.utils.FontSize
import com.mobile_client.utils.ImageResources
import com.mobile_client.utils.ImageUtils
import com.mobile_client.utils.Screen
import com.mobile_client.utils.showDismissible
import com.mobile_client.viewModels.ShopViewModel
import com.mobile_client.viewModels.ThemeViewModel

@Composable
fun ShopScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    shopViewModel: ShopViewModel = viewModel(),
    themeViewModel: ThemeViewModel,
) {
    val assets = themeViewModel.assets
    val shopItems by shopViewModel.shopItems.collectAsState()
    val isLoading by shopViewModel.isLoading.collectAsState()
    val snackbarMessage by shopViewModel.message.collectAsState()
    val money by shopViewModel.money.collectAsState()
    val scope = rememberCoroutineScope()
    val account = AccountService.instance.accountInfo
    val avatarBitmap = remember(account?.avatar) {
        ImageUtils.base64ToBitmap(account?.avatar)
    }
    val inventory by shopViewModel.inventory.collectAsState()
    LaunchedEffect(Unit) {
        shopViewModel.loadData()
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showDismissible(scope, it)
            shopViewModel.clearMessage()
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
                    text = "Magasin de cosmétiques",
                    fontSize = FontSize.BIG_TITLE.sp,
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

                shopItems.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Le magasin est vide.",
                            fontSize = FontSize.BODY.sp,
                            color = assets.mainPageTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {

                    var itemToBuy by remember { mutableStateOf<Cosmetic?>(null) }

                    if (itemToBuy != null) {
                        BuyConfirmationDialog(
                            itemName = itemToBuy!!.name,
                            itemPrice = itemToBuy!!.price,
                            currentBalance = money,
                            themeViewModel = themeViewModel,
                            onConfirm = { shopViewModel.buyItem(itemToBuy!!) },
                            onDismiss = { itemToBuy = null }
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(shopItems) { item ->
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(assets.itemShopBackground)
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = item.name,
                                    fontSize = FontSize.BODY.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = assets.mainPageTextColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                val resId = ImageResources.cosmeticToImage[item.filePath]

                                if (resId != null) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = item.name,
                                        modifier = Modifier
                                            .padding(bottom = 10.dp)
                                            .size(100.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .padding(bottom = 10.dp)
                                            .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("?", fontSize = 28.sp, color = Color.Gray)
                                    }
                                }

                                Text(
                                    text = item.description,
                                    fontSize = FontSize.SMALL.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = assets.mainPageTextColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.height(60.dp)
                                )

                                Text(
                                    text = "${item.price} $",
                                    fontSize = FontSize.SMALL.sp,
                                    color = assets.price,
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                if (inventory.any { it.cosmeticId == item._id }) {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        colors = ButtonDefaults.buttonColors(
                                            disabledContainerColor = assets.buyButtonBackground.copy(alpha = 0.4f),
                                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Déjà dans l'inventaire", fontSize = FontSize.SMALL.sp, fontFamily = FontFamily.Default)
                                    }
                                } else {
                                    PressableButton(
                                        shadowColor = assets.buyButtonBackground,
                                        cornerRadius = 6.dp,
                                        shadowTopInset = 3.5.dp
                                    ) { interactionSource, pressModifier ->
                                        Button(
                                            onClick = {
                                                if (money < item.price) {
                                                    shopViewModel.buyItem(item)
                                                } else {
                                                    itemToBuy = item
                                                }
                                            },
                                            modifier = pressModifier,
                                            interactionSource = interactionSource,
                                            colors = ButtonDefaults.buttonColors(containerColor = assets.buyButtonBackground),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Acheter", color = Color.White, fontSize = FontSize.SMALL.sp, fontFamily = FontFamily.Default)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top-right: money + username + avatar
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 16.dp)
                .background(assets.headerRightBackground, RoundedCornerShape(20.dp))
                .border(1.dp, assets.headerRightBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.inventory_icon),
                contentDescription = "Inventory",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { navController.navigate(Screen.Inventory.route) { launchSingleTop = true } }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                modifier = Modifier
                    .clickable { navController.navigate(Screen.Account.route) { launchSingleTop = true } },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${money}$",
                    color = assets.mainPageTextColor,
                    fontSize = FontSize.BODY.sp,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = account?.username ?: "",
                    color = assets.mainPageTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = FontSize.BODY.sp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            painter = rememberAsyncImagePainter(avatarBitmap),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BuyConfirmationDialog(
    itemName: String,
    itemPrice: Int,
    currentBalance: Int,
    themeViewModel: ThemeViewModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val assets = themeViewModel.assets
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Confirmer l'achat?",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Voulez-vous acheter \"$itemName\" pour ${itemPrice}$ ?",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Solde actuel: ${currentBalance}$",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Text(
                        text = "Nouveau solde:",
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = " ${currentBalance-itemPrice}$",
                        fontSize = 14.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                PressableButton(
                    shadowColor = Color(0xFF757575),
                    cornerRadius = 22.dp,
                ) { interactionSource, pressModifier ->
                    Button(
                        onClick = onDismiss,
                        modifier = pressModifier,
                        interactionSource = interactionSource,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Non, annuler", color = Color.White, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                PressableButton(
                    shadowColor = assets.gameCreationButton,
                    cornerRadius = 22.dp,
                ) { interactionSource, pressModifier ->
                    Button(
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        modifier = pressModifier,
                        interactionSource = interactionSource,
                        colors = ButtonDefaults.buttonColors(containerColor = assets.gameCreationButton),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Oui, acheter", color = assets.textAccount, fontSize = 15.sp)
                    }
                }
            }
        }
    )
}
