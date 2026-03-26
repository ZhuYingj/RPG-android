package com.mobile_client.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mobile_client.utils.ImageResources
import com.mobile_client.utils.TutorialSteps
import com.mobile_client.viewModels.TutorialViewModel

@Composable
fun TutorialOverlay(tutorialViewModel: TutorialViewModel) {
    if (!tutorialViewModel.isVisible.value) return

    val currentStepIndex = tutorialViewModel.currentStep.intValue
    val step = TutorialSteps.steps.getOrNull(currentStepIndex) ?: return
    val image = ImageResources.tutorialStepToImage[currentStepIndex]

    val animatedProgress by animateFloatAsState(
        targetValue = tutorialViewModel.progressFraction,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    // Full screen blocking overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Block all clicks behind */ },
        contentAlignment = Alignment.Center
    ) {
        // Tutorial card — 90% of screen
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // === TOP: Progress bar + Title ===
            Column {
                Text(
                    text = "Étape ${currentStepIndex + 1}/${tutorialViewModel.totalSteps}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFE0E0E0),
                    strokeCap = StrokeCap.Round,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = step.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === MIDDLE: Image (left) + Description (right) ===
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left — Image
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    if (image != null) {
                        Image(
                            painter = painterResource(id = image),
                            contentDescription = step.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            "Image non disponible",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                // Right — Description
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = step.description,
                        fontSize = 18.sp,
                        color = Color(0xFF333333),
                        lineHeight = 26.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === BOTTOM: Navigation buttons ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button (commented out)
                // if (!tutorialViewModel.isFirstStep) {
                //     OutlinedButton(
                //         onClick = { tutorialViewModel.previousStep() },
                //         shape = RoundedCornerShape(8.dp)
                //     ) {
                //         Text("Précédent")
                //     }
                //     Spacer(modifier = Modifier.width(8.dp))
                // }

                OutlinedButton(
                    onClick = { tutorialViewModel.close() },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Fermer", color = Color.Gray)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { tutorialViewModel.nextStep() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text(
                        if (tutorialViewModel.isLastStep) "Terminer" else "Suivant",
                        color = Color.White
                    )
                }
            }
        }
    }
}
