package com.example.bloomapp.ui.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloomapp.ui.viewmodel.Language
import com.example.bloomapp.ui.viewmodel.SettingsViewModel
import com.example.bloomapp.ui.viewmodel.SettingsViewModelFactory
import com.example.bloomapp.ui.viewmodel.ThemeMode
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel,
    navController: androidx.navigation.NavController? = null
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context)
    )
    val scope = rememberCoroutineScope()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var deleteError by remember { mutableStateOf("") }
    var passwordForDelete by remember { mutableStateOf("") }
    var isDeleting by remember { mutableStateOf(false) }


    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var isChangingPassword by remember { mutableStateOf(false) }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    // Observer les préférences
    val themeMode by viewModel.themeModeFlow.collectAsState(initial = "system")
    val language by viewModel.languageFlow.collectAsState(initial = "fr")
    val notificationsEnabled by viewModel.notificationsFlow.collectAsState(initial = true)
    val autoBackupEnabled by viewModel.autoBackupFlow.collectAsState(initial = false)

    // Synchroniser avec le ViewModel
    LaunchedEffect(themeMode) {
        viewModel.themeMode = when (themeMode) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    LaunchedEffect(notificationsEnabled) {
        viewModel.notificationsEnabled = notificationsEnabled
    }

    LaunchedEffect(autoBackupEnabled) {
        viewModel.autoBackupEnabled = autoBackupEnabled
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Section Apparence
            SettingsSection(title = "Apparence")

            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = "Thème",
                subtitle = when (viewModel.themeMode) {
                    ThemeMode.LIGHT -> "Clair"
                    ThemeMode.DARK -> "Sombre"
                    ThemeMode.SYSTEM -> "Automatique (système)"
                },
                onClick = { showThemeDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.Language,
                title = "Langue",
                subtitle = "${viewModel.selectedLanguage.flag} ${viewModel.selectedLanguage.displayName}",
                onClick = { showLanguageDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Section Notifications
            SettingsSection(title = "Notifications")

            SettingsSwitchItem(
                icon = Icons.Default.Notifications,
                title = "Activer les notifications",
                subtitle = "Recevoir des rappels d'arrosage",
                checked = viewModel.notificationsEnabled,
                onCheckedChange = { enabled ->
                    scope.launch {
                        viewModel.saveNotifications(enabled)
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Section Données
            SettingsSection(title = "Données")

            SettingsSwitchItem(
                icon = Icons.Default.Backup,
                title = "Sauvegarde automatique",
                subtitle = "Sauvegarder vos plantes dans le cloud",
                checked = viewModel.autoBackupEnabled,
                onCheckedChange = { enabled ->
                    scope.launch {
                        viewModel.saveAutoBackup(enabled)
                    }
                }
            )

            SettingsItem(
                icon = Icons.Default.CloudDownload,
                title = "Exporter les données",
                subtitle = "Télécharger vos données en JSON",
                onClick = { /* TODO: Export */ }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Section Compte
            SettingsSection(title = "Compte")

            val currentUser = FirebaseAuth.getInstance().currentUser
            SettingsItem(
                icon = Icons.Default.Person,
                title = "Compte connecté",
                subtitle = currentUser?.email ?: "Non connecté",
                onClick = { }
            )

            currentUser?.let { user ->
                val isPasswordProvider = user.providerData.any { it.providerId == "password" }

                if (isPasswordProvider) {
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Changer le mot de passe",
                        subtitle = "Modifier votre mot de passe actuel",
                        onClick = { showChangePasswordDialog = true }
                    )
                }
            }

            SettingsItem(
                icon = Icons.Default.Security,
                title = "Confidentialité",
                subtitle = "Politique de confidentialité et protection des données",
                onClick = {
                    navController?.navigate("privacy")
                }
            )

            SettingsItem(
                icon = Icons.Default.Delete,
                title = "Supprimer le compte",
                subtitle = "Cette action est irréversible",
                onClick = {
                    currentUser?.let { user ->
                        // Déterminer le provider (email/password ou Google)
                        val isPasswordProvider = user.providerData.any {
                            it.providerId == "password"
                        }

                        if (isPasswordProvider) {
                            // Demander le mot de passe pour ré-authentifier
                            showPasswordDialog = true
                        } else {
                            // Provider Google - demander confirmation directe
                            showDeleteDialog = true
                        }
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Section À propos
            SettingsSection(title = "À propos")

            SettingsItem(
                icon = Icons.Default.Info,
                title = "Version",
                subtitle = "1.0.0",
                onClick = { }
            )

            SettingsItem(
                icon = Icons.Default.Help,
                title = "Aide & Support",
                subtitle = "FAQ et contact",
                onClick = {
                    navController?.navigate("help_support")
                }
            )


            Spacer(modifier = Modifier.height(16.dp))

            // Bouton Déconnexion
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Déconnexion")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Dialog Thème
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choisir le thème") },
            text = {
                Column {
                    ThemeOption(
                        title = "Clair",
                        selected = viewModel.themeMode == ThemeMode.LIGHT,
                        onClick = {
                            scope.launch {
                                viewModel.saveThemeMode(ThemeMode.LIGHT)
                                showThemeDialog = false
                            }
                        }
                    )
                    ThemeOption(
                        title = "Sombre",
                        selected = viewModel.themeMode == ThemeMode.DARK,
                        onClick = {
                            scope.launch {
                                viewModel.saveThemeMode(ThemeMode.DARK)
                                showThemeDialog = false
                            }
                        }
                    )
                    ThemeOption(
                        title = "Automatique",
                        selected = viewModel.themeMode == ThemeMode.SYSTEM,
                        onClick = {
                            scope.launch {
                                viewModel.saveThemeMode(ThemeMode.SYSTEM)
                                showThemeDialog = false
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Dialog Langue
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Choisir la langue") },
            text = {
                Column {
                    Language.values().forEach { lang ->
                        LanguageOption(
                            flag = lang.flag,
                            title = lang.displayName,
                            selected = viewModel.selectedLanguage == lang,
                            onClick = {
                                scope.launch {
                                    viewModel.saveLanguage(lang)
                                    showLanguageDialog = false
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("⚠️ Supprimer le compte") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Êtes-vous sûr de vouloir supprimer votre compte ?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Cette action est irréversible. Toutes vos plantes et données seront définitivement supprimées.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    if (deleteError.isNotEmpty()) {
                        Text(
                            text = deleteError,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleting = true
                        currentUser?.delete()
                            ?.addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Compte supprimé avec succès",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showDeleteDialog = false
                                onLogout()
                            }
                            ?.addOnFailureListener { e ->
                                isDeleting = false
                                deleteError = when {
                                    e.message?.contains("recent login") == true ->
                                        "Veuillez vous reconnecter avant de supprimer votre compte"
                                    else -> "Erreur: ${e.message}"
                                }
                                Toast.makeText(context, deleteError, Toast.LENGTH_LONG).show()
                            }
                    },
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Supprimer définitivement", color = Color.Red)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deleteError = ""
                    },
                    enabled = !isDeleting
                ) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                passwordForDelete = ""
                deleteError = ""
            },
            title = { Text("⚠️ Confirmer la suppression") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Pour supprimer votre compte, veuillez entrer votre mot de passe.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = passwordForDelete,
                        onValueChange = {
                            passwordForDelete = it
                            deleteError = ""
                        },
                        label = { Text("Mot de passe") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = deleteError.isNotEmpty()
                    )

                    if (deleteError.isNotEmpty()) {
                        Text(
                            text = deleteError,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (passwordForDelete.isBlank()) {
                            deleteError = "Le mot de passe est requis"
                            return@TextButton
                        }

                        isDeleting = true
                        currentUser?.let { user ->
                            val email = user.email ?: ""
                            val credential = EmailAuthProvider.getCredential(email, passwordForDelete)

                            // 1. Ré-authentifier
                            user.reauthenticate(credential)
                                .addOnSuccessListener {
                                    // 2. Supprimer le compte
                                    user.delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Compte supprimé avec succès",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            showPasswordDialog = false
                                            onLogout() // Rediriger vers login
                                        }
                                        .addOnFailureListener { e ->
                                            isDeleting = false
                                            deleteError = "Erreur lors de la suppression: ${e.message}"
                                            Toast.makeText(
                                                context,
                                                deleteError,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    isDeleting = false
                                    deleteError = when {
                                        e.message?.contains("password is invalid") == true ->
                                            "Mot de passe incorrect"
                                        else -> "Erreur: ${e.message}"
                                    }
                                }
                        }
                    },
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Supprimer", color = Color.Red)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        passwordForDelete = ""
                        deleteError = ""
                    },
                    enabled = !isDeleting
                ) {
                    Text("Annuler")
                }
            }
        )
    }

    // DIALOG CHANGEMENT DE MOT DE PASSE
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showChangePasswordDialog = false
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
                passwordError = ""
                showCurrentPassword = false
                showNewPassword = false
                showConfirmPassword = false
            },
            title = { Text("🔐 Changer le mot de passe") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Entrez votre mot de passe actuel et choisissez un nouveau mot de passe.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    // Mot de passe actuel
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            passwordError = ""
                        },
                        label = { Text("Mot de passe actuel") },
                        visualTransformation = if (showCurrentPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                Icon(
                                    imageVector = if (showCurrentPassword)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = if (showCurrentPassword)
                                        "Masquer"
                                    else
                                        "Afficher"
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = passwordError.isNotEmpty()
                    )

                    // Nouveau mot de passe
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            passwordError = ""
                        },
                        label = { Text("Nouveau mot de passe") },
                        visualTransformation = if (showNewPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                Icon(
                                    imageVector = if (showNewPassword)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = if (showNewPassword)
                                        "Masquer"
                                    else
                                        "Afficher"
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = passwordError.isNotEmpty()
                    )

                    // Afficher la force du mot de passe
                    if (newPassword.isNotEmpty()) {
                        val strength = getPasswordStrength(newPassword)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(4) { index ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .background(
                                            when {
                                                index < strength.level -> strength.color
                                                else -> Color.LightGray
                                            },
                                            shape = MaterialTheme.shapes.small
                                        )
                                )
                            }
                        }
                        Text(
                            text = strength.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = strength.color
                        )
                    }

                    // Confirmer le nouveau mot de passe
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            passwordError = ""
                        },
                        label = { Text("Confirmer le nouveau mot de passe") },
                        visualTransformation = if (showConfirmPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    imageVector = if (showConfirmPassword)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = if (showConfirmPassword)
                                        "Masquer"
                                    else
                                        "Afficher"
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = passwordError.isNotEmpty()
                    )

                    // Message d'erreur
                    if (passwordError.isNotEmpty()) {
                        Text(
                            text = passwordError,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Validation
                        when {
                            currentPassword.isBlank() -> {
                                passwordError = "Le mot de passe actuel est requis"
                                return@TextButton
                            }
                            newPassword.isBlank() -> {
                                passwordError = "Le nouveau mot de passe est requis"
                                return@TextButton
                            }
                            newPassword.length < 6 -> {
                                passwordError = "Le mot de passe doit contenir au moins 6 caractères"
                                return@TextButton
                            }
                            newPassword != confirmPassword -> {
                                passwordError = "Les mots de passe ne correspondent pas"
                                return@TextButton
                            }
                            newPassword == currentPassword -> {
                                passwordError = "Le nouveau mot de passe doit être différent de l'ancien"
                                return@TextButton
                            }
                        }

                        isChangingPassword = true
                        currentUser?.let { user ->
                            val email = user.email ?: ""
                            val credential = EmailAuthProvider.getCredential(email, currentPassword)

                            // 1. Ré-authentifier
                            user.reauthenticate(credential)
                                .addOnSuccessListener {
                                    // 2. Changer le mot de passe
                                    user.updatePassword(newPassword)
                                        .addOnSuccessListener {
                                            isChangingPassword = false
                                            Toast.makeText(
                                                context,
                                                "Mot de passe modifié avec succès",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            showChangePasswordDialog = false
                                            currentPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                            passwordError = ""
                                        }
                                        .addOnFailureListener { e ->
                                            isChangingPassword = false
                                            passwordError = "Erreur lors du changement: ${e.message}"
                                            Toast.makeText(context, passwordError, Toast.LENGTH_LONG).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    isChangingPassword = false
                                    passwordError = when {
                                        e.message?.contains("password is invalid") == true ->
                                            "Mot de passe actuel incorrect"
                                        e.message?.contains("network") == true ->
                                            "Erreur réseau, vérifiez votre connexion"
                                        else -> "Erreur: ${e.message}"
                                    }
                                }
                        }
                    },
                    enabled = !isChangingPassword
                ) {
                    if (isChangingPassword) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text("Modification...")
                        }
                    } else {
                        Text("Modifier")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showChangePasswordDialog = false
                        currentPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                        passwordError = ""
                        showCurrentPassword = false
                        showNewPassword = false
                        showConfirmPassword = false
                    },
                    enabled = !isChangingPassword
                ) {
                    Text("Annuler")
                }
            }
        )
    }


}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = Color(0xFF4CAF50),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// ✅ Data class pour la force du mot de passe
data class PasswordStrength(
    val level: Int,
    val label: String,
    val color: Color
)

// ✅ Fonction pour calculer la force du mot de passe
fun getPasswordStrength(password: String): PasswordStrength {
    var score = 0

    // Longueur
    when {
        password.length >= 12 -> score += 2
        password.length >= 8 -> score += 1
    }

    // Contient des chiffres
    if (password.any { it.isDigit() }) score += 1

    // Contient des majuscules
    if (password.any { it.isUpperCase() }) score += 1

    // Contient des caractères spéciaux
    if (password.any { !it.isLetterOrDigit() }) score += 1

    return when {
        score >= 5 -> PasswordStrength(4, "Très fort", Color(0xFF4CAF50))
        score >= 4 -> PasswordStrength(3, "Fort", Color(0xFF8BC34A))
        score >= 3 -> PasswordStrength(2, "Moyen", Color(0xFFFFC107))
        score >= 2 -> PasswordStrength(1, "Faible", Color(0xFFFF9800))
        else -> PasswordStrength(1, "Très faible", Color(0xFFF44336))
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4CAF50)
            )
        )
    }
}

@Composable
fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF4CAF50)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(title)
    }
}

@Composable
fun LanguageOption(
    flag: String,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF4CAF50)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("$flag $title")
    }
}

