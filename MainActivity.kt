package com.example.universalmusicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MusicPlayerApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerApp() {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    var tracks by remember { mutableStateOf(emptyList<MusicTrack>()) }
    var selectedTrack by remember { mutableStateOf<MusicTrack?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    var originalLyrics by remember { mutableStateOf("") }
    var translatedLyrics by remember { mutableStateOf("") }
    var showTranslation by remember { mutableStateOf(true) }

    val permission = if (Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else Manifest.permission.READ_EXTERNAL_STORAGE

    fun refreshLibrary() { tracks = MusicRepository(context).loadSongs() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) refreshLibrary() }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            refreshLibrary()
        } else permissionLauncher.launch(permission)
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text("Universal Music Player", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = { showLyrics = !showLyrics }) {
                        Text(if (showLyrics) "Songs" else "Lyrics")
                    }
                }
            )
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                if (showLyrics) {
                    LyricsPanel(
                        originalLyrics, translatedLyrics, showTranslation,
                        { showTranslation = it },
                        { originalLyrics = it },
                        { translatedLyrics = it }
                    )
                } else {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Your music", style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = {
                            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                                refreshLibrary()
                            } else permissionLauncher.launch(permission)
                        }) { Text("Rescan") }
                    }
                    if (tracks.isEmpty()) {
                        Text("No music found. Allow audio access, add music to your phone, then tap Rescan.")
                    } else {
                        LazyColumn(Modifier.weight(1f)) {
                            items(tracks, key = { it.id }) { track ->
                                ListItem(
                                    headlineContent = { Text(track.title, maxLines = 1) },
                                    supportingContent = { Text(track.artist, maxLines = 1) },
                                    modifier = Modifier.clickable {
                                        selectedTrack = track
                                        player.setMediaItem(MediaItem.fromUri(track.uri))
                                        player.prepare()
                                        player.play()
                                        isPlaying = true
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                Text(selectedTrack?.title ?: "Nothing playing", style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(selectedTrack?.artist ?: "Choose a song from your library", style = MaterialTheme.typography.bodySmall)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (tracks.isNotEmpty()) {
                            val index = tracks.indexOfFirst { it.id == selectedTrack?.id }
                            val previous = if (index > 0) index - 1 else tracks.lastIndex
                            val track = tracks[previous]
                            selectedTrack = track
                            player.setMediaItem(MediaItem.fromUri(track.uri))
                            player.prepare()
                            player.play()
                            isPlaying = true
                        }
                    }) { Text("⏮", style = MaterialTheme.typography.headlineMedium) }

                    Button(onClick = {
                        if (player.isPlaying) {
                            player.pause()
                            isPlaying = false
                        } else if (selectedTrack != null) {
                            player.play()
                            isPlaying = true
                        }
                    }) { Text(if (isPlaying) "Pause" else "Play") }

                    IconButton(onClick = {
                        if (tracks.isNotEmpty()) {
                            val index = tracks.indexOfFirst { it.id == selectedTrack?.id }
                            val next = if (index >= 0 && index < tracks.lastIndex) index + 1 else 0
                            val track = tracks[next]
                            selectedTrack = track
                            player.setMediaItem(MediaItem.fromUri(track.uri))
                            player.prepare()
                            player.play()
                            isPlaying = true
                        }
                    }) { Text("⏭", style = MaterialTheme.typography.headlineMedium) }
                }
                Text(
                    "Lyrics can be entered manually in this starter. Automatic lookup and translation are the next development step.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun LyricsPanel(
    original: String,
    translated: String,
    showTranslation: Boolean,
    onShowTranslationChange: (Boolean) -> Unit,
    onOriginalChange: (String) -> Unit,
    onTranslatedChange: (String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text("Lyrics", style = MaterialTheme.typography.headlineSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Show translation")
            Spacer(Modifier.width(8.dp))
            Switch(checked = showTranslation, onCheckedChange = onShowTranslationChange)
        }
        Text("Original lyrics", fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = original, onValueChange = onOriginalChange,
            modifier = Modifier.fillMaxWidth().weight(1f),
            placeholder = { Text("Paste lyrics in any language…") }
        )
        if (showTranslation) {
            Spacer(Modifier.height(10.dp))
            Text("Tamil / English translation", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = translated, onValueChange = onTranslatedChange,
                modifier = Modifier.fillMaxWidth().weight(1f),
                placeholder = { Text("Paste translated lyrics here…") }
            )
        }
    }
}
