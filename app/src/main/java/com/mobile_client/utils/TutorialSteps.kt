package com.mobile_client.utils

data class TutorialStep (
    val step: Int,
    val title: String,
    val description: String
)

object TutorialSteps {
    val steps = listOf(
        TutorialStep(0, "Bienvenue sur Les Carrottes", "Ceci est un tutoriel"),
        TutorialStep(1, "Le clavardage global/de partie", "chat global extensible et retractable"),
        TutorialStep(2, "Interface d'amis", "amis extensible et retractable"),
        //TODO add more steps
    )
}
