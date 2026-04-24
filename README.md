# PolyRPG — Client Léger (Android)

Application mobile Android du projet **PolyRPG** développée dans le cadre du cours **LOG3900** (Hiver 2026) à Polytechnique Montréal.

Ce client léger est l'une des trois composantes du système PolyRPG (client lourd desktop + client léger mobile + serveur). Il permet aux joueurs de se connecter, rejoindre/créer des parties, jouer en mode Classique ou CTF sur une grille, clavarder, gérer leurs amis, acheter des cosmétiques, et suivre leur progression.

---

## ✨ Fonctionnalités

### 🔐 Comptes & authentification
- Création de compte avec pseudonyme unique, courriel, mot de passe et avatar
- Authentification sécurisée par token JWT
- Modification des paramètres du compte (pseudonyme, courriel, avatar)
- Statistiques d'utilisation (parties Classique/CTF jouées, gagnées, temps moyen, défis complétés)
- Historique détaillé des connexions/déconnexions avec horodatage
- Historique des parties jouées (date, résultat, abandon)
- Suppression de compte avec nettoyage complet des données associées

### 🖼️ Avatars
- 16 avatars prédéfinis (thèmes légumes) dont 4 premium
- Capture photo via l'appareil photo de la tablette
- Synchronisation de l'avatar entre appareils

### 🎲 Modes de jeu
- ⚔️ **Mode Classique** — premier à 3 victoires en combat
- 🚩 **Mode CTF** — capturer le drapeau et le ramener à son point de départ
- ⚡ **Option Élimination Rapide** — une défaite = élimination immédiate
- 🤖 **Joueurs virtuels** (profils agressif/défensif) pour combler les places vides
- 🔄 **Drop-in/Drop-out** — rejoindre ou quitter une partie en cours
- 🔒 **Verrouillage du lobby** et exclusion de joueurs par l'organisateur
- 🐛 **Mode débogage** activable par 3 secouements horizontaux (téléportation sans coût)

### 💬 Clavardage
- Canal général accessible partout dans l'application
- Canal dédié à la partie (lobby + en jeu)
- Communication temps réel via WebSocket

### 👥 Système d'amis
- Recherche de profils utilisateurs en temps réel
- Envoi et réception de demandes d'amitié
- Acceptation/refus des demandes
- Parties "amis seulement" accessibles uniquement à la liste d'amis
- 🚫 Blocage d'utilisateurs avec effets temps réel (masquage des messages, refus d'entrée en partie)

### 💰 Monnaie virtuelle & boutique
- 🪙 Monnaie virtuelle gagnée à chaque partie (victoire + consolation)
- 🎫 Frais d'entrée pour les parties compétitives avec redistribution des gains
- 🏆 **Défis de partie** aléatoires parmi 5 types (eau, portes, combats, items, évasions)
- 🛒 Boutique de cosmétiques (personnages exclusifs, éléments visuels)
- 🎨 Inventaire avec équipement/déséquipement des cosmétiques

### 📱 Fonctionnalités mobile
- 📸 **Scan de QR code** pour rejoindre rapidement une partie et ajouter un utilisateur
- **Génération de QR code** pour partager le lobby ou le profil
- Détection de secouements via capteurs (accéléromètre)
- Interface optimisée pour tablette en mode paysage

### 🎨 Personnalisation
- 🥕 Thème **Les Carottes**
- 🍆 Thème **Les Aubergines**

### 🏅 Classement & progression
- 🥇 Leaderboard **Argent**
- ⏱️ Leaderboard **Temps de jeu**
- 🏆 Leaderboard **Parties gagnées**
- ⚔️ Leaderboard **Combats gagnés**

### 🎓 Tutoriel
- Tutoriel d'introduction avec textes et images
- Progression sauvegardée côté serveur (reprise possible)
- Relançable depuis les paramètres du compte

---

## 🛠️ Stack technique

| Composant | Technologie |
|---|---|
| Langage | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Min SDK / Target SDK | 34 / 36 |
| Orientation | Paysage (forcée) |
| Communication HTTP | Ktor Client |
| Communication temps réel | Socket.IO Client |
| Images | Coil |
| Scan QR | Google Play Services Code Scanner + ZXing |
| Sérialisation | Gson |

---

## 🚀 Installation et compilation

### Prérequis
- Android Studio (Koala ou plus récent recommandé)
- JDK 11
- Un appareil/émulateur Android 14+ (API 34)

### Étapes
1. Cloner le dépôt et ouvrir le dossier `mobile-export/` dans Android Studio.
2. Laisser Gradle synchroniser les dépendances.
3. Configurer l'URL du serveur dans `app/src/main/java/com/mobile_client/environment/environment.kt` :
   L'URL par défaut pointe vers une instance EC2 hébergée sur AWS.
4. Compiler et lancer :
   - Depuis Android Studio : `Run > Run 'app'`
   - En ligne de commande :
     ```bash
     ./gradlew assembleDebug      # APK de développement
     ```
5. L'APK généré se trouve dans `app/build/outputs/apk/`.

### Permissions requises
- `INTERNET` — communication avec le serveur
- `ACCESS_NETWORK_STATE` — détection de connectivité
- `CAMERA` — capture d'avatar et scan de codes QR

---

## 📂 Structure du projet

```
mobile-export/app/src/main/java/com/mobile_client/
├── screens/          # Écrans Compose (13 écrans : Login, Home, Game, etc.)
├── components/       # Composants UI réutilisables (Chat, GameBoard, FriendPanel, etc.)
├── services/         # Couche réseau (HTTP + WebSocket + services métier)
├── viewModels/       # ViewModels (gestion d'état)
├── utils/            # Modèles de données, constantes, helpers
└── environment/      # Configuration de l'URL serveur
```

Flot de navigation principal :
`Login → SignUp → Home → {JoinGame | GameCreation | Shop | Inventory | Account | LeaderBoard} → CharacterCreation → WaitingPage → Game → EndGame`
