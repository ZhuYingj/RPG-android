package com.mobile_client.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mobile_client.components.Header
import com.mobile_client.services.AccountService
import com.mobile_client.viewModels.AccountViewModel

@Composable
fun AccountScreen(
    navController: NavController,
    accountViewModel: AccountViewModel = viewModel()
) {
    val account by accountViewModel.account.collectAsState()

    var name by remember(account) { mutableStateOf(account?.username ?: "") }
    var email by remember(account) { mutableStateOf(account?.email ?: "") }
    println("QEWQWEQ")
    println(AccountService.instance.accountInfo)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Header(navController, "Compte")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Avatar circulaire
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = account?.username?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Badge caméra
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Changer la photo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Input Nom
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Nom") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Input Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Boutons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        name = account?.username ?: ""
                        email = account?.email ?: ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Réinitialiser")
                }

                Button(
                    onClick = { accountViewModel.updateAccount(name = name, email = email) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Enregistrer")
                }
            }
        }
    }
}
