package id.antasari.p8_multimedia_230104040080.ui.video

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import id.antasari.p8_multimedia_230104040080.util.FileManagerUtility
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    onBack: () -> Unit,
    videoPath: String
) {
    val context = LocalContext.current
    val activity = context as Activity
    val scrollState = rememberScrollState()

    // === WARNA BIRU (SESUAI PALET TADI) ===
    val BlueLight = Color(0xFF5DADE2)
    val BluePrimary = Color(0xFF007AFF)
    val SoftBlueSurface = Color(0xFFEAF3FF)

    var currentFile by remember { mutableStateOf(File(videoPath)) }
    var videoFiles by remember { mutableStateOf(loadVideoFiles(context)) }
    var isFullscreen by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var position by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(1L) }

    // ZOOM & PAN STATE
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Dialog state
    var showRenameDialog by remember { mutableStateOf<File?>(null) }
    var newFileName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<File?>(null) }

    // PLAYER SETUP
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.fromFile(currentFile)))
            prepare()
            play()
        }
    }

    LaunchedEffect(currentFile) {
        player.stop()
        player.setMediaItem(MediaItem.fromUri(Uri.fromFile(currentFile)))
        player.prepare()
        player.play()
        isPlaying = true
        // reset transform
        scale = 1f; offsetX = 0f; offsetY = 0f
    }

    LaunchedEffect(isPlaying) {
        while (true) {
            if (player.isPlaying) {
                position = player.currentPosition
                duration = player.duration.coerceAtLeast(1L)
            }
            delay(200)
        }
    }

    // Lifecycle Observer
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                player.pause()
                isPlaying = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.release()
        }
    }

    fun enterFullscreen() {
        isFullscreen = true
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    fun exitFullscreen() {
        isFullscreen = false
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (!isFullscreen) {
            TopAppBar(
                title = { Text("Video Player") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { enterFullscreen() }) {
                        Icon(Icons.Default.Fullscreen, contentDescription = null)
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            // VIDEO VIEW (ZOOM PAN)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isFullscreen) 300.dp else 220.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).also { view ->
                            view.player = player
                            view.useController = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (isFullscreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { exitFullscreen() }) {
                        Icon(Icons.Default.FullscreenExit, contentDescription = null)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                currentFile.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            // Play/Pause
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
                modifier = Modifier.align(Alignment.CenterHorizontally),
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

            // Slider
            Slider(
                value = position.toFloat(),
                onValueChange = {
                    position = it.toLong()
                    player.seekTo(position)
                },
                valueRange = 0f..duration.toFloat(),
                modifier = Modifier.padding(horizontal = 20.dp),
                colors = SliderDefaults.colors(
                    thumbColor = BluePrimary,
                    activeTrackColor = BluePrimary,
                    inactiveTrackColor = BlueLight.copy(alpha = 0.35f)
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatDuration(position))
                Text(formatDuration(duration))
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(14.dp))

            Text(
                "Daftar Video",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))

            videoFiles.forEach { file ->
                VideoFileCard(
                    file = file,
                    containerColor = SoftBlueSurface,
                    onPlay = { currentFile = file },
                    onEdit = {
                        showRenameDialog = file
                        newFileName = file.nameWithoutExtension
                    },
                    onDelete = { showDeleteDialog = file }
                )
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    // Rename Dialog
    if (showRenameDialog != null) {
        val file = showRenameDialog!!
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Edit Nama Video") },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    label = { Text("Nama baru (tanpa .mp4)") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    FileManagerUtility.renameFile(file, "$newFileName.mp4")
                    videoFiles = loadVideoFiles(context)
                    currentFile = File(context.filesDir, "$newFileName.mp4")
                    showRenameDialog = null
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) { Text("Batal") }
            }
        )
    }

    // Delete Dialog
    if (showDeleteDialog != null) {
        val file = showDeleteDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus Video?") },
            text = { Text(file.name) },
            confirmButton = {
                TextButton(onClick = {
                    FileManagerUtility.deleteFile(file)
                    videoFiles = loadVideoFiles(context)
                    if (file == currentFile && videoFiles.isNotEmpty()) {
                        currentFile = videoFiles.first()
                    }
                    showDeleteDialog = null
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun VideoFileCard(
    file: File,
    containerColor: Color,
    onPlay: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onPlay() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.VideoFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    file.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "${formatDuration(FileManagerUtility.getVideoDuration(file))} • ${FileManagerUtility.formatFileSize(file.length())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 46.dp)
            )
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.padding(start = 46.dp)) {
                Text(
                    "[Edit]",
                    modifier = Modifier.clickable { onEdit() },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.width(20.dp))
                Text(
                    "[Delete]",
                    modifier = Modifier.clickable { onDelete() },
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

fun loadVideoFiles(context: android.content.Context): List<File> {
    return FileManagerUtility.getAllVideoFiles(context)
}

fun formatDuration(ms: Long): String {
    val sec = (ms / 1000)
    val min = sec / 60
    val s = sec % 60
    return "%02d:%02d".format(min, s)
}
