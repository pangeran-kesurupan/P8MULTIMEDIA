package id.antasari.p8_multimedia_230104040080.ui.player

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import id.antasari.p8_multimedia_230104040080.ui.recorder.formatDuration
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    onBack: () -> Unit,
    audioPath: String
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var currentFile by remember { mutableStateOf(File(audioPath)) }

    // === WARNA BIRU (SESUAI PALET TADI) ===
    val BlueLight = Color(0xFF5DADE2)
    val BluePrimary = Color(0xFF007AFF)

    // ExoPlayer Setup
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.fromFile(currentFile)))
            prepare()
            play()
        }
    }

    var isPlaying by remember { mutableStateOf(true) }
    var position by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(1L) }

    // Update slider & time
    LaunchedEffect(isPlaying) {
        while (true) {
            if (player.isPlaying) {
                position = player.currentPosition
                duration = player.duration.coerceAtLeast(1L)
            }
            delay(200)
        }
    }

    // Clean up
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    // When file changes
    LaunchedEffect(currentFile) {
        player.stop()
        player.setMediaItem(MediaItem.fromUri(Uri.fromFile(currentFile)))
        player.prepare()
        player.play()
        isPlaying = true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Audio Player") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))
            Text(
                currentFile.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            FloatingActionButton(
                onClick = {
                    if (isPlaying) {
                        player.pause()
                        isPlaying = false
                    } else {
                        player.play()
                        isPlaying = true
                    }
                },
                containerColor = BluePrimary,
                contentColor = Color.White
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Slider(
                value = position.toFloat(),
                onValueChange = {
                    position = it.toLong()
                    player.seekTo(position)
                },
                valueRange = 0f..duration.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = BluePrimary,
                    activeTrackColor = BluePrimary,
                    inactiveTrackColor = BlueLight.copy(alpha = 0.35f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatDuration(position))
                Text(formatDuration(duration))
            }
        }
    }
}
