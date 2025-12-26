package id.antasari.p8_multimedia_230104040080.ui

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AudioRecorder : Screen("audio_recorder")

    // Route dengan argumen path file
    object AudioPlayer : Screen("audio_player/{audioPath}") {
        fun passPath(encodedPath: String): String = "audio_player/$encodedPath"
    }

    object VideoPlayer : Screen("video_player/{videoPath}") {
        fun passPath(encodedPath: String): String = "video_player/$encodedPath"
    }

    object CameraGallery : Screen("camera_gallery")
}
