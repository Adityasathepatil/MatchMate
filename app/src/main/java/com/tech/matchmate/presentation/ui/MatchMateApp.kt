package com.tech.matchmate.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tech.matchmate.domain.models.User
import com.tech.matchmate.domain.models.enums.MatchStatus
import com.tech.matchmate.presentation.viewmodels.MainViewModel
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.offset
import com.tech.matchmate.domain.models.enums.Screen
import com.tech.matchmate.presentation.viewmodels.UiState

@Composable
fun MatchMateApp() {
    val viewModel: MainViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(Screen.BROWSE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        HeaderWithNavigation(
            currentScreen = currentScreen,
            onScreenChange = { currentScreen = it },
            onRefresh = { viewModel.retryLoading() }
        )

        Spacer(modifier = Modifier.height(16.dp))


        uiState.error?.let { error ->
            ErrorCard(
                error = error,
                onDismiss = { viewModel.clearError() }
            )
        }

        when (currentScreen) {
            Screen.BROWSE -> BrowseScreen(viewModel, uiState)
            Screen.ACCEPTED -> AcceptedMatchesScreen(viewModel, uiState)
            Screen.DECLINED -> DeclinedMatchesScreen(viewModel, uiState)
        }
    }
}

@Composable
fun HeaderWithNavigation(
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit,
    onRefresh: () -> Unit
) {
    Column {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MatchMate",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TabRow(
            selectedTabIndex = Screen.values().indexOf(currentScreen),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Screen.values().forEach { screen ->
                Tab(
                    selected = currentScreen == screen,
                    onClick = { onScreenChange(screen) },
                    text = {
                        Text(
                            text = screen.title,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ErrorCard(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }
    }
}

@Composable
fun BrowseScreen(
    viewModel: MainViewModel,
    uiState: UiState
) {
    val pendingUsers by viewModel.pendingUsers.collectAsState()

    if (uiState.isLoading) {
        LoadingScreen()
    } else {
        if (pendingUsers.isEmpty()) {
            EmptyStateScreen(
                message = "No pending matches available",
                actionText = "Refresh",
                onAction = { viewModel.retryLoading() }
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(pendingUsers) { user ->
                    UserCard(
                        user = user,
                        onAccept = { viewModel.acceptUser(user.id) },
                        onDecline = { viewModel.declineUser(user.id) },
                        showActions = true
                    )
                }
            }
        }
    }
}

@Composable
fun AcceptedMatchesScreen(
    viewModel: MainViewModel,
    uiState: UiState
) {
    val acceptedUsers by viewModel.acceptedUsers.collectAsState()

    if (uiState.isLoading) {
        LoadingScreen()
    } else {
        if (acceptedUsers.isEmpty()) {
            EmptyStateScreen(
                message = "No accepted matches yet",
                actionText = "Browse Matches",
                onAction = { /* Navigate to browse screen */ }
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "You have ${acceptedUsers.size} accepted match${if (acceptedUsers.size != 1) "es" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(acceptedUsers) { user ->
                    AcceptedUserCard(
                        user = user,
                        onUndo = { viewModel.undoAction(user.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DeclinedMatchesScreen(
    viewModel: MainViewModel,
    uiState: UiState
) {
    val declinedUsers by viewModel.declinedUsers.collectAsState()

    if (uiState.isLoading) {
        LoadingScreen()
    } else {
        if (declinedUsers.isEmpty()) {
            EmptyStateScreen(
                message = "No declined matches",
                actionText = "Browse Matches",
                onAction = { }
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "You have declined ${declinedUsers.size} match${if (declinedUsers.size != 1) "es" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(declinedUsers) { user ->
                    DeclinedUserCard(
                        user = user,
                        onUndo = { viewModel.undoAction(user.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading matches...")
        }
    }
}

@Composable
fun EmptyStateScreen(
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAction) {
                Text(actionText)
            }
        }
    }
}


@Composable
fun UserCard(
    user: User,
    onAccept: (() -> Unit)? = null,
    onDecline: (() -> Unit)? = null,
    showActions: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            AsyncImage(
                model = user.imageUrl,
                contentDescription = "Profile picture of ${user.name}",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))


            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Age: ${user.age}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "City: ${user.city}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Education: ${user.education}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Profession: ${user.profession}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))


                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            user.matchScore >= 80 -> Color(0xFF4CAF50) // Green
                            user.matchScore >= 60 -> Color(0xFFFF9800) // Orange
                            else -> Color(0xFF9E9E9E) // Gray
                        }
                    )
                ) {
                    Text(
                        text = "Match: ${user.matchScore}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))


                if (showActions && user.status == MatchStatus.PENDING && onAccept != null && onDecline != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDecline,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Decline"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Decline")
                        }

                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Accept"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Accept")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AcceptedUserCard(
    user: User,
    onUndo: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF1F8E9)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Box {
                AsyncImage(
                    model = user.imageUrl,
                    contentDescription = "Profile picture of ${user.name}",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )


                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accepted",
                        tint = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))


            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text(
                            text = "MATCHED",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Age: ${user.age} • ${user.city}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = user.profession,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = user.education,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))


                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Contact details will be shared soon!",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.weight(1f))


                TextButton(
                    onClick = onUndo,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Undo"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Undo Match")
                }
            }
        }
    }
}

@Composable
fun DeclinedUserCard(
    user: User,
    onUndo: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAFAFA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box {
                AsyncImage(
                    model = user.imageUrl,
                    contentDescription = "Profile picture of ${user.name}",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .alpha(0.5f),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(
                        Color.Gray.copy(alpha = 0.3f),
                        BlendMode.Overlay
                    )
                )


                Card(
                    modifier = Modifier.align(Alignment.Center),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Declined",
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Age: ${user.age} • ${user.city}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "DECLINED",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onUndo,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reconsider"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reconsider")
                }
            }
        }
    }
}