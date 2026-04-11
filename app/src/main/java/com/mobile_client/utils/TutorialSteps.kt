package com.mobile_client.utils

data class TutorialStep (
    val step: Int,
    val title: String,
    val description: String
)

object TutorialSteps {
    val steps = listOf(
        TutorialStep(0, "Page d'accueil", "On y retrouve la boutique, l’inventaire et les informations du compte en haut de l’écran."),
        TutorialStep(1, "Page d'accueil", "Le clavardage global peut être étendu ou réduit."),
        TutorialStep(2, "Page d'accueil", "En partie, un clavardage distinct du clavardage global est disponible. Attention à la profanité!"),
        TutorialStep(3, "Page d'accueil", "Il est possible de rechercher, ajouter des amis ou bloquer un utilisateur."),
        TutorialStep(4, "Boutique", "On peut acheter des cosmétiques qui seront ajoutés à l’inventaire."),
        TutorialStep(5, "Inventaire", "On peut équiper des cosmétiques et les trier selon leur type."),
        TutorialStep(6, "Compte", "On retrouve les informations du compte. Il est possible de reprendre ou recommencer le tutoriel."),
        TutorialStep(7, "Joindre une partie", "On peut entrer un code, scanner un code QR ou choisir une partie dans la liste."),
        TutorialStep(8, "Création d'une partie", "Choisir une carte parmi celles disponibles."),
        TutorialStep(9, "Création d'une partie", "En mode élimination rapide, une défaite élimine immédiatement au lieu de trois normalement."),
        TutorialStep(10, "Salle d'attente", "Le propriétaire peut ajouter des joueurs virtuels et démarrer la partie."),
        TutorialStep(11, "Règles du jeu", "En mode Classique, le premier à 3 victoires gagne (1 en élimination rapide). En mode CTF, l’équipe qui capture le drapeau et revient gagne."),
        TutorialStep(12, "Actions de jeu", "Une action permet d’ouvrir une porte ou d’attaquer un joueur. En combat, on peut attaquer ou tenter d'évader deux fois."),
        TutorialStep(13, "Statistiques de fin", "Il est possible de trier les joueurs selon différentes statistiques."),
        TutorialStep(14, "Classement", "Le classement est mis à jour en temps réel après chaque partie et comporte plusieurs catégories.")
    )
}
