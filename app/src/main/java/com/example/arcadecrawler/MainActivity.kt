package com.example.arcadecrawler

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.arcadecrawler.ui.theme.ArcadeCrawlerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

data class position(var x: Int, var y: Int, var direction: String, var color: Color)
data class Bullet(val id: Int, var x: Int, var y: Int, var direction: String = "")
data class mushroom_position(var x: Int, var y: Int, var lives: Int, var poison: Boolean)
data class Caterpillars(val id: Int, var size: Int, val motion: String, var position: SnapshotStateList<position>)
var mushroom_positions= SnapshotStateList<mushroom_position>()
var tintColor=SnapshotStateList<Color>()
var current_user=""
var current_pass=""
var internet=false
var done=false
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArcadeCrawlerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun fetchRandomCentipedeColor(onResult: (Color) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = ApiClient.api.getCentipedeColor()
            val parsedColor = Color(android.graphics.Color.parseColor(response.color))
            withContext(Dispatchers.Main) {
                onResult(parsedColor)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResult(Color.Green)
            }
        }
    }
}

fun fetchBackgroundImage(onResult: (Bitmap) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = ApiClient.api.getThemeImage()
            withContext(Dispatchers.Main) {
                onResult( BitmapFactory.decodeStream(response.body()?.byteStream()))
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                println("Error")
            }
        }
    }
}
fun fetchBackgroundMusic(context: Context,volume: Float, onResult: (File) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val response = ApiClient.api.getThemeAudio()
        if (response.isSuccessful) {
            val tempFile = File.createTempFile("theme_music", ".mp3", context.cacheDir)
            onResult(tempFile)

            response.body()?.byteStream()?.use { input ->
                FileOutputStream(tempFile).use { output -> input.copyTo(output) }
            }
            MusicController.start(context, tempFile, volume)
        }
    }
}
fun fetchSkinUrls(onResult: (List<List<String>>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = ApiClient.api.getSkins()
            val urls = listOf(response.mushrooms, response.guns, response.scorpions)
            withContext(Dispatchers.Main) {
                onResult(urls)
            }

        } catch (e: Exception) {

            withContext(Dispatchers.Main) {
                onResult(emptyList())
            }
        }
    }
}
fun fetchLeaderboard(onResult: (List<LeaderboardEntry>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val leaderboard = ApiClient.api.getLeaderboard()
            withContext(Dispatchers.Main) {
                onResult(leaderboard)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onResult(emptyList())
            }
        }
    }
}
fun submitScore(
    name: String,
    password: String,
    newPoints: Int,
    onResult: (UpdateScoreResponse?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {

            val leaderboard = ApiClient.api.getLeaderboard()
            val currentPlayer = leaderboard.find { it.name == name }
            val currentScore = currentPlayer?.score ?: 0
            val totalScore = currentScore + newPoints
            val request = UpdateScoreRequest(name, password, totalScore)
            val response = ApiClient.api.updateScore(request)
            println(response)
            withContext(Dispatchers.Main) {
                onResult(response)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onResult(null)
            }
        }
    }
}
@Composable
fun ScoreSubmissionDialog(
    newPoints: Int,
    onClose: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onClose() },
        title = { Text("Submit Your Score") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SCORE SUBMITTED")
                if (message != null) {
                    Text(message!!, color = Color.Red, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            if (!done) {
                Button(
                    onClick = {
                        isLoading = true

                        submitScore(current_user, current_pass, newPoints) { response ->
                            isLoading = false
                            if (response?.success == true) {
                                done = true
                                message = "Score submitted! Rank: ${response.data?.rank}"

                            } else {
                                message = response?.message ?: "Submission failed"
                            }
                        }
                    },
                    enabled = if (!done) !isLoading else false
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {

                        Text("Submit")

                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("Cancel")
            }
        }
    )
}
@Composable
fun ErrorDialog(
    str: String,
    onClose: () -> Unit
) {
    var message by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = { onClose() },
        title = { Text("ERROR") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(str)
                if (message != null) {
                    Text(message!!, color = Color.Red, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {

        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("Ok")
            }
        }
    )
}
fun fetchMushroomLayout(
    onResult: (List<Offset>) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = ApiClient.api.getMushroomLayout()
            val layout = response.layout.map { (x, y) ->
                Offset(x, y)
            }
            withContext(Dispatchers.Main) {
                onResult(layout)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onResult(emptyList())
            }
        }
    }
}
fun fetchPowerUps(onResult: (List<PowerUp>?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = ApiClient.api.getPowerUps(CentipedeDestroyedPayload())
            withContext(Dispatchers.Main) {
                onResult(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onResult(null)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val shield = remember { mutableStateOf(false) }
    var rapidfire = remember { mutableStateOf(false) }
    var fastforward = remember { mutableStateOf(false) }
    var mushbomb = remember { mutableStateOf(false) }
    var multi = remember { mutableStateOf(false) }
    var poisonremove = remember { mutableStateOf(false) }
    val score = remember { mutableStateOf(0) }
    val caterpillarCount = remember { mutableStateOf(1) }
    val screen_no= remember { mutableStateOf(0) }
    val context= LocalContext.current
    var bulletx by remember { mutableStateOf(0) }
    val gyroController = remember { GyroController(context) }

    val settings = remember { mutableStateOf(readsettings(context = context)) }
    var volume by remember { mutableStateOf(settings.value[0].toFloat()) }
    var brightness by remember { mutableStateOf(settings.value[1].toFloat()) }
    var usegyro by remember { mutableStateOf(!settings.value[2].toBoolean())  }
    var animenabled by remember { mutableStateOf(!settings.value[3].toBoolean())  }
    var spacersize by remember { mutableStateOf(0f) }
    val crabmove = remember { mutableStateOf(true) }
    val scorpionmove = remember { mutableStateOf(true) }
    val pause = remember { mutableStateOf(false) }
    val win = remember { mutableStateOf(0) }
    var size by remember { mutableStateOf(1f) }
    val scope = rememberCoroutineScope()
    var gun_height by remember { mutableStateOf(29) }
    var gun_width by remember { mutableStateOf(6) }
    var gunCooldown by remember { mutableStateOf(false) }
    var shot by remember { mutableStateOf(false) }
    val bullets= remember { mutableStateListOf<Bullet>() }
    var nextBulletId by remember { mutableStateOf(0) }
    val colHeightPx = remember { mutableStateOf(1) }
    val colWidthPx = remember { mutableStateOf(1) }
    val startanim = remember { mutableStateListOf<MutableState<Boolean>>() }
    var gun_position by remember { mutableStateOf( position(0,0,"",Color.Black))}
    var moveOffset by remember { mutableStateOf(Offset.Zero) }
    val caterpillars = remember { mutableStateListOf<Caterpillars>()}
    val skinUrls = remember { mutableStateOf<List<List<String>>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    DisposableEffect(Unit) {
        onDispose {
            gyroController.stop()
            MusicController.stop()
            startanim.clear()
            caterpillars.clear()
            bullets.clear()
            mushroom_positions.clear()
        }
    }
    if (showDialog) {
        ScoreSubmissionDialog(newPoints = score.value) {
            showDialog = false
        }
    }
    if (mushroom_positions.isEmpty()) {
        mushroompositions()
    }
    LaunchedEffect(win.value) {
        if (win.value==1){
            score.value+=30
            showDialog=true
        }
        else if (win.value==2){
            showDialog=true
        }
    }
    LaunchedEffect(shield.value) {
        if (shield.value){
            delay(30000)
            shield.value=false
        }
    }
    LaunchedEffect(fastforward.value) {
        if (fastforward.value){
            delay(15000)
            fastforward.value=false
        }
    }
    LaunchedEffect(rapidfire.value) {
        if (rapidfire.value){
            delay(15000)
            rapidfire.value=false
        }
    }
    LaunchedEffect(multi.value) {
        if (multi.value){
            delay(10000)
            multi.value=false
        }
    }
    LaunchedEffect(score.value) {
        println(score.value)
    }
    LaunchedEffect(shot) {
        if (shot){
            bulletx=gun_width

            bullets.add(Bullet(nextBulletId++, gun_width, gun_height))
            if (multi.value){
                bullets.add(Bullet(nextBulletId++, gun_width-1, gun_height))
                bullets.add(Bullet(nextBulletId++, gun_width+1, gun_height))
            }
            Soundplayer.shoot(context)
        }

        if (shot){
            repeat(16) {
                size -= 0.0625f
                if (rapidfire.value) delay(20) else delay(40)
            }
            size=1f
            shot=false
        }

    }
    var rot by remember { mutableStateOf(0f) }
    var rot2 by remember { mutableStateOf(0f) }
    val config= Bitmap.Config.ARGB_8888
    var backimg by remember { mutableStateOf(Bitmap.createBitmap(10, 10 , config )) }
    LaunchedEffect(usegyro) {
        if (usegyro) {
            repeat(15){
                rot+=2f
                delay(45)
            }
            repeat(30){
                rot-=2f
                delay(45)
            }
            repeat(15){
                rot+=2f
                delay(45)
            }
            repeat(15){
                rot2+=2f
                delay(45)
            }
            repeat(30){
                rot2-=2f
                delay(45)
            }
            repeat(15){
                rot2+=2f
                delay(45)
            }
            gyroController.start()
        } else {
            gyroController.stop()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (usegyro && !pause.value && screen_no.value==2) {
                val gyroY = gyroController.yOffset
                val gyroX = gyroController.xOffset

                if (!gunCooldown) {
                    scope.launch {
                        gunCooldown = true
                        if (gyroY > 0.2) {
                            if (gun_height == 27) gun_height = 29
                            else if (gun_height == 29) gun_height = 31
                            rot2=30f
                        } else if (gyroY < -0.2) {
                            if (gun_height == 31) gun_height = 29
                            else if (gun_height == 29) gun_height = 27
                            rot2=-30f
                        }
                        if (gyroX > 0.2) {
                            if (gun_width < 10) gun_width += 1
                            rot=30f
                        } else if (gyroX < -0.2) {
                            if (gun_width > 2) gun_width -= 1
                            rot=-30f
                        }

                        delay(100)
                        gunCooldown = false
                    }
                }
            }
            delay(16)
        }
    }
    LaunchedEffect(caterpillars.size) {
        var counter= false
        for (i in startanim) {if (i.value==false)  counter=true; break}
        if (caterpillars.isEmpty() && counter) {
            win.value = 1
            var powerups= emptyList<PowerUp>()
            fetchPowerUps { result-> if (result != null) {
                powerups=result
                for (i in powerups){
                    editpowerups(context,i.id,1)
                }
            }

            }
            pause.value = true
        }
    }
    LaunchedEffect(Unit) {
        while (true){
            internet= isInternetAvailable(context)
        if (isInternetAvailable(context)) {
            delay(500)
            fetchBackgroundImage { fetchedColor ->
                backimg = fetchedColor
            }
            gyroController.start()
            var music = File("")
            fetchBackgroundMusic(context, volume) { backmusic ->
                music = backmusic
            }
            fetchSkinUrls { urls ->
                skinUrls.value = urls
                loading = false
            }
            break
        }
        else(
                delay(100)
        )
            }
    }

    LaunchedEffect(volume) {
        MusicController.setVolume(volume)
    }
    DisposableEffect(Unit) {
        onDispose {
            gyroController.stop()
            MusicController.stop()
        }
    }

    Scaffold(
        containerColor = Color.White
        ) { paddingValues ->
        if (screen_no.value==2)Image(
            bitmap = backimg.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().onGloballyPositioned { coordinates ->
            colHeightPx.value = coordinates.size.height; colWidthPx.value = coordinates.size.width;
        }) {

            var density = LocalDensity.current
            var height = with(density) { colHeightPx.value.toDp() }
            var width = with(density) { colWidthPx.value.toDp() }
            var tileWidth = width / 16
            var tileHeight = height / (16 * height / width)
            if (screen_no.value == 0) {
                density = LocalDensity.current
                height = with(density) { colHeightPx.value.toDp() }
                width = with(density) { colWidthPx.value.toDp() }
                tileWidth = width / 16
                tileHeight = height / (16 * height / width)
                HomeScreen(caterpillars,screen_no, tileWidth, tileHeight,skinUrls.value)
            }
            else if (screen_no.value==1){
                choosescreen(caterpillars, caterpillarCount ,screen_no, tileWidth,tileHeight)
            }
            else if (screen_no.value==3){
                signinscreen(screen_no,tileWidth,tileHeight)
            }
            else if (screen_no.value==4){
                LeaderboardScreen(screen_no)
            }
            else {
                if (startanim.isEmpty()){
                for (i in caterpillars){
                    startanim.addLast(mutableStateOf(true))
                }}
                if (startanim.size!=caterpillars.size){
                    startanim.addLast(mutableStateOf(false))
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (i in 0..2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                for (j in 0..16) {
                                    if ((i + j) % 2 == 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(tileWidth, tileHeight)
                                                .background(Color.White)
                                        ) {
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(tileWidth, tileHeight)
                                                .background(Color.Black)
                                        ) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                generatemushrooms(screen_no,score,
                    colHeightPx,
                    colWidthPx,
                    tileHeight,
                    tileWidth,
                    0,
                    bullets,
                    shield, skinUrls.value[0], mushbomb, poisonremove
                )
                for (i in 0 until caterpillars.size) {
                    generatecaterpillar(screen_no,score,
                        startanim,
                        caterpillars,
                        bullets,
                        colHeightPx,
                        colWidthPx,
                        height,
                        width,
                        i,
                        pause,
                        gun_width,
                        gun_height,
                        win, fastforward
                    )
                }
                SvgImage(url=skinUrls.value[1][0], modifier=Modifier.height(tileWidth * 2.5f).graphicsLayer { rotationZ = 180f }
                    .offset(
                        y = -1 *tileHeight * gun_height, x = -1 * tileWidth * gun_width
                    ), alpha=1f)
                bullets.forEach { bullet ->
                    generatebullet(bullets, bullet, tileWidth, tileHeight, bullet.x, rapidfire,multi)
                }
                generatecrab(screen_no,tileWidth, tileHeight, crabmove, pause)
                generatescorpion(screen_no,tileWidth, tileHeight, scorpionmove, pause, shield,skinUrls.value[2])
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    Spacer(Modifier.fillMaxWidth().fillMaxHeight(0.03f))
                    Row() {

                        LaunchedEffect(Unit) {
                                repeat(20){
                                    spacersize+=0.1f/20
                                    delay(30)
                                }

                        }
                        Spacer(Modifier.fillMaxWidth(spacersize))

                            IconButton(
                                onClick = {  pause.value = !pause.value },
                                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                    .background(Color(0, 0, 0, 150)).fillMaxWidth(0.17f)
                                    .fillMaxHeight(0.077f)
                            ) {

                                Icon(
                                    painter = painterResource(R.drawable.pause),
                                    contentDescription = "Add",
                                    tint = Color.White
                                )
                            }


                    }
                    Spacer(Modifier.fillMaxWidth().fillMaxHeight(0.8f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = { if (!pause.value && !shot) shot = true },
                            modifier = Modifier.size(80.dp).clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val radius = 80.dp.toPx() / 2
                                drawCircle(
                                    color = Color(0, 0, 0, 225),
                                    radius = radius,
                                    center = center,
                                    style = Stroke(8f)
                                )

                            }
                            if (shot) {
                                Box(
                                    modifier = Modifier.fillMaxSize(size).clip(CircleShape)
                                        .background(Color(0, 0, 0, 100))
                                )
                            }
                            Icon(
                                painter = painterResource(R.drawable.bullet),
                                contentDescription = "Bullet",
                                modifier = Modifier.size(30.dp),
                                tint = Color.Black
                            )
                        }
                        Spacer(Modifier.fillMaxWidth(0.6f))
                        if (!usegyro) {
                            joystick(pause = pause,
                                size = 90.dp,
                                onMove = { offset ->
                                    moveOffset = offset
                                    gun_position = position(offset.x.toInt(), offset.y.toInt(), "", Color.Black)
                                    if (!gunCooldown) {
                                        scope.launch {
                                            gunCooldown = true
                                            if (gun_position.y > 0 && gun_position.y > 20) {
                                                if (gun_height == 27) gun_height = 29
                                                else if (gun_height == 29) gun_height = 31
                                            } else if (gun_position.y < 0 && gun_position.y < -20) {
                                                if (gun_height == 31) gun_height = 29
                                                else if (gun_height == 29) gun_height = 27
                                            }
                                            if (gun_position.x > 0 && gun_position.x > 20) {
                                                if (gun_width < 11) gun_width += 1

                                            } else if (gun_position.x < 0 && gun_position.x < -20) {
                                                if (gun_width > 1) gun_width -= 1

                                            }
                                            if (fastforward.value) delay(75) else delay(100)
                                            gunCooldown = false
                                        }
                                    }
                                }
                            )
                        } else {
                            Image(
                                painterResource(R.drawable.gyro),
                                contentDescription = "Gyro",
                                modifier = Modifier.graphicsLayer {
                                    rotationZ = rot; rotationX = rot2
                                }.fillMaxHeight(0.5f)
                            )
                        }
                    }
                }
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = AbsoluteAlignment.Right) {
                    Spacer(Modifier.fillMaxWidth().fillMaxHeight(0.03f))
                    var userpowerups = emptyList<String>().toMutableList()
                    val powerups = readpowerups(context)
                    for (i in powerups) {
                        if (i.split(",")[0] == current_user) {
                            userpowerups = i.split(",").toMutableList()
                        }
                    }
                    val tileSize = Modifier
                        .clip(CircleShape)
                        .size(tileWidth * 1.8f, tileHeight * 1.8f)

                    val iconModifier = Modifier.fillMaxSize(0.75f)
                    val countModifier = Modifier
                        .offset(tileWidth * 0.7f, tileHeight * 0f)
                        .background(Color.White, shape = CircleShape)
                        .padding(2.dp)
                    if (!pause.value) {
                        Row(
                            horizontalArrangement = Arrangement.Absolute.Right,
                            modifier = Modifier
                                .height(tileHeight * 3.8f)
                                .padding(tileWidth * 0.5f, tileHeight)
                        ) {
                            IconButton(onClick = {

                                if (userpowerups[1].toInt() > 0 && !shield.value) {

                                    if (animenabled){
                                    win.value=9;

                                    }
                                    else shield.value=true


                                    userpowerups[1] =
                                        (userpowerups[1].toInt() - 1).toString(); editpowerups(
                                        context,
                                        1,
                                        -1
                                    );
                                }
                            }, modifier = tileSize.background(Color(35, 0, 230))) {

                                Image(painterResource(R.drawable.shield), "Shield", iconModifier)
                                Text(
                                    userpowerups[1],
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    modifier = countModifier
                                )
                                if (shield.value){
                                    Box(modifier=Modifier.fillMaxSize().clip(CircleShape).background(Color(0,0,0,150)))
                                }
                            }

                            Spacer(modifier = Modifier.width(tileWidth * 0.5f))
                            IconButton(onClick = {
                                if (userpowerups[2].toInt() > 0 && !rapidfire.value) {
                                    if (animenabled){
                                        win.value=10;
                                    }
                                    else rapidfire.value=true
                                    userpowerups[2] =
                                        (userpowerups[2].toInt() - 1).toString(); editpowerups(
                                        context,
                                        2,
                                        -1
                                    );
                                }
                            }, modifier = tileSize.background(Color.Black)) {

                                Image(
                                    painterResource(R.drawable.rapidfire),
                                    "Rapid Fire",
                                    iconModifier
                                )
                                Text(
                                    userpowerups[2],
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    modifier = countModifier
                                )
                                if (rapidfire.value){
                                    Box(modifier=Modifier.fillMaxSize().clip(CircleShape).background(Color(0,0,0,150)))
                                }
                            }

                            Spacer(modifier = Modifier.width(tileWidth * 0.5f))
                            IconButton(
                                onClick = {
                                    if (userpowerups[3].toInt() > 0 && !fastforward.value) {
                                        if (animenabled){
                                            win.value=11;
                                        }
                                        else fastforward.value=true
                                        userpowerups[3] =
                                            (userpowerups[3].toInt() - 1).toString(); editpowerups(
                                            context,
                                            3,
                                            -1
                                        );
                                    }
                                },
                                modifier = tileSize.background(Color(10, 10, 36, 255))
                            ) {

                                Image(painterResource(R.drawable.ff), "Speed Boost", iconModifier)
                                Text(
                                    userpowerups[3],
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    modifier = countModifier
                                )
                                if (fastforward.value){
                                    Box(modifier=Modifier.fillMaxSize().clip(CircleShape).background(Color(0,0,0,150)))
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.Absolute.Right) {
                            IconButton(
                                onClick = {if (userpowerups[4].toInt() > 0) {
                                    if (animenabled){
                                        win.value=12;
                                    }
                                    else mushbomb.value=true
                                    userpowerups[4] =
                                        (userpowerups[4].toInt() - 1).toString(); editpowerups(
                                        context,
                                        4,
                                        -1
                                    );
                                }},
                                modifier = tileSize.background(Color(168, 217, 173))
                            ) {
                                Image(
                                    painterResource(R.drawable.mushbomb),
                                    "Mushroom Bomb",
                                    iconModifier
                                )
                                Text(
                                    userpowerups[4],
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    modifier = countModifier
                                )
                            }

                            Spacer(modifier = Modifier.width(tileWidth * 0.5f))
                            IconButton(
                                onClick = {if (userpowerups[5].toInt() > 0 && !multi.value) {
                                    if (animenabled){
                                        win.value=13;
                                    }
                                    else multi.value=true
                                    userpowerups[5] =
                                        (userpowerups[5].toInt() - 1).toString(); editpowerups(
                                        context,
                                        5,
                                        -1
                                    );
                                }},
                                modifier = tileSize.background(Color(0, 171, 186))
                            ) {
                                Image(painterResource(R.drawable.multi), "Multishot", iconModifier)
                                Text(
                                    userpowerups[5],
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    modifier = countModifier
                                )
                                if (multi.value){
                                    Box(modifier=Modifier.fillMaxSize().clip(CircleShape).background(Color(0,0,0,150)))
                                }
                            }
                            Spacer(modifier = Modifier.width(tileWidth * 0.5f))
                            IconButton(
                                onClick = {if (userpowerups[6].toInt() > 0) {
                                    if (animenabled){
                                        win.value=14;
                                    }
                                    else poisonremove.value=true
                                    userpowerups[6] =
                                        (userpowerups[6].toInt() - 1).toString(); editpowerups(
                                        context,
                                        6,
                                        -1
                                    )}},
                                modifier = tileSize.background(Color(224, 187, 228))
                            ) {
                                Image(
                                    painterResource(R.drawable.poisonremove),
                                    "Poison Remover",
                                    iconModifier
                                )
                                Text(
                                    userpowerups[6],
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    modifier = countModifier
                                )
                            }

                            Spacer(modifier = Modifier.width(tileWidth * 0.5f))
                        }
                    }
                }

                if (pause.value && win.value == 0) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(0, 0, 0, 200)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                            Spacer(Modifier.fillMaxWidth().fillMaxHeight(0.03f))
                            Row() {
                                Spacer(Modifier.fillMaxWidth(0.1f))
                                IconButton(
                                    onClick = {  pause.value = !pause.value },
                                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                        .background(Color(0, 0, 0, 150)).fillMaxWidth(0.16f)
                                        .fillMaxHeight(0.077f)
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Add",
                                        tint = Color.White, modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                        }
                        Column(
                            modifier = Modifier.fillMaxSize(0.5f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            VolumeSlider(
                                context = LocalContext.current,
                                volume = volume,
                                onVolumeChange = { volume = it },
                                tileWidth,
                                tileHeight
                            )
                            BrightnessSlider(
                                context = LocalContext.current,
                                brightness = brightness,
                                onBrightnessChange = { brightness = it },
                                tileWidth,
                                tileHeight
                            )
                            IconButton(onClick = {
                                usegyro = !usegyro; editsettings(context = context, 2, volume)
                            }, modifier = Modifier.fillMaxWidth().height(2 * tileHeight)) {
                                var color1 = listOf(Color.DarkGray, Color.LightGray)
                                var color2 = listOf(Color.Green, Color.White)
                                if (usegyro) {
                                    color1 = listOf(Color.Green, Color.White); color2 =
                                        listOf(Color.DarkGray, Color.LightGray)
                                }
                                Row(modifier = Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier.fillMaxHeight().fillMaxWidth(0.5f)
                                            .background(color1[0]),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("GYRO", color = color1[1])
                                    }
                                    Box(
                                        modifier = Modifier.fillMaxHeight().fillMaxWidth()
                                            .background(color2[0]),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("JOYSTICK", color = color2[1])
                                    }

                                }
                            }
                            Spacer(Modifier.height(tileHeight))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("Powerups Enabled: ", fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(tileHeight))
                            IconButton(onClick = {
                                animenabled = !animenabled; editsettings(context = context, 3, volume)
                            }, modifier = Modifier.width(6*tileWidth).height(2 * tileHeight).clip(RoundedCornerShape(tileHeight)).background(if (animenabled) Color.Green else Color.Red)) {
                                if (animenabled) {
                                    Row (modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End){
                                        Text("On", color = Color.Black)
                                        Spacer(modifier=Modifier.fillMaxHeight().width(tileWidth))
                                        Icon(
                                            painter = painterResource(R.drawable.circle),
                                            contentDescription = "Check",
                                            tint = Color.LightGray
                                        )
                                    }
                                }
                                else {
                                    Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(R.drawable.circle),
                                            contentDescription = "Check",
                                            tint = Color.LightGray
                                        )
                                        Spacer(modifier=Modifier.fillMaxHeight().width(tileWidth))
                                        Text("Off", color = Color.Black)
                                    }
                                }
                                    }




                        }

                    }
                }
                if (win.value==9){
                    pause.value=true
                    val videoFile = remember {
                        convert(context, R.raw.shield, "my_video.mp4")
                    }
                    VideoPlayer(
                        videoUri = Uri.fromFile(videoFile).toString()
                        ,
                        modifier = Modifier.fillMaxHeight(), token= shield, win=win, pause=pause
                    )

                }
                else if (win.value==10){
                    pause.value=true
                    val videoFile = remember {
                        convert(context, R.raw.rapidfire, "my_video.mp4")
                    }
                    VideoPlayer(
                        videoUri = Uri.fromFile(videoFile).toString()
                        ,
                        modifier = Modifier.fillMaxHeight(), token= rapidfire, win=win, pause=pause
                    )
                }
                else if (win.value==11){
                    pause.value=true
                    val videoFile = remember {
                        convert(context, R.raw.ff, "my_video.mp4")
                    }
                    VideoPlayer(
                        videoUri = Uri.fromFile(videoFile).toString()
                        ,
                        modifier = Modifier.fillMaxHeight(), token= fastforward, win=win, pause=pause
                    )
                }
                else if (win.value==12){
                    pause.value=true
                    val videoFile = remember {
                        convert(context, R.raw.mushbomb, "my_video.mp4")
                    }
                    VideoPlayer(
                        videoUri = Uri.fromFile(videoFile).toString()
                        ,
                        modifier = Modifier.fillMaxHeight(), token= mushbomb, win=win, pause=pause
                    )
                }
                else if (win.value==13){
                    pause.value=true
                    val videoFile = remember {
                        convert(context, R.raw.multi, "my_video.mp4")
                    }
                    VideoPlayer(
                        videoUri = Uri.fromFile(videoFile).toString()
                        ,
                        modifier = Modifier.fillMaxHeight(), token= multi, win=win, pause=pause
                    )
                }
                else if (win.value==14){
                    pause.value=true
                    val videoFile = remember {
                        convert(context, R.raw.poisonremover, "my_video.mp4")
                    }
                    VideoPlayer(
                        videoUri = Uri.fromFile(videoFile).toString()
                        ,
                        modifier = Modifier.fillMaxHeight(), token= poisonremove, win=win, pause=pause
                    )
                }
                else if (win.value > 0 && win.value!=9) {
                    pause.value = true
                    val text = if (win.value == 1) "WIN" else "LOSE"
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(0, 0, 0, 200)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("YOU $text", color = Color.White, fontSize = 30.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row() {
                                IconButton(
                                    onClick = {
                                        shield.value=false
                                        rapidfire.value=false
                                        multi.value=false
                                        mushbomb.value=false
                                        poisonremove.value=false
                                        fastforward.value=false
                                        startanim.clear()
                                        spacersize = 0f
                                        caterpillars.clear()
                                        bullets.clear()
                                        mushroom_positions.clear()
                                        mushroompositions()
                                        makecaterpillarlist(caterpillars, caterpillarCount.value)
                                        for (i in 0 until caterpillarCount.value) {
                                            startanim.add(mutableStateOf(true))
                                        }
                                        win.value = 0
                                        pause.value = false
                                    },

                                    modifier = Modifier.size(60.dp).clip(CircleShape)
                                        .background(Color.White)
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Restart",
                                        tint = Color.Black
                                    )
                                }

                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Color(
                            0f,
                            0f,
                            0f,
                            if (brightness != 100f) (1f - brightness) / 1.5f else 0f
                        )
                    )
                )
            }



            if (skinUrls.value.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }



        }
    }
    if (loading){
        Box(modifier=Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center){
            Column(modifier=Modifier.height(25.85f*10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val transition = rememberInfiniteTransition()
                val hueShift by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 3000, easing = LinearEasing)
                    )
                )
                val hue = (hueShift + 2 * (360f / 16)) % 360f
                val color = Color.hsv(hue, 0.9f, 1f)
                Text(
                    text = "ARCADE CRAWLER",

                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 40.sp
                )
                AnimatedCaterpillarPreview(25.85f.dp, 25.85f.dp)

                Spacer(modifier = Modifier.height(25.85f.dp * 3))
                Text(
                    text = if (internet) "Waiting to load assets" else "Please turn on the internet",
                    color = Color.Red,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(25.85f.dp ))
                snake_Loader(
                    modifier = Modifier.fillMaxSize(),
                    segmentCount = 16,
                    pathSize = 100.dp,
                    speed = 1.5f
                )

            }


        }
    }
}
@Composable
fun snake_Loader(
    modifier: Modifier = Modifier,
    segmentCount: Int = 8,
    pathSize: Dp = 100.dp,
    speed: Float = 1f
) {
    val headPainter = painterResource(id = R.drawable.snake_head)
    val bodyPainter = painterResource(id = R.drawable.snake_body)
    val density = LocalDensity.current
    val transition = rememberInfiniteTransition()
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (4000 / speed).toInt(), easing = LinearEasing)
        )
    )
    val hueShift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing)
        )
    )

    Box(
        modifier = modifier.size(pathSize * 2),
        contentAlignment = Alignment.Center
    ) {
        val center = with(density) { pathSize.toPx() }
        fun infinityPathPoint(progress: Float): Offset {
            val twoPi = 2 * Math.PI
            val theta = progress * twoPi
            val a = center / 2f
            val x = a * cos(theta).toFloat()
            val y = a * sin(2 * theta).toFloat() / 2
            return Offset(center + x, center + y)
        }
        val hue = (hueShift + 2 * (360f / segmentCount)) % 360f
        val color = Color.hsv(hue, 0.9f, 1f)
        for (i in segmentCount downTo 1) {
            val segmentProgress = (t - i * 0.03f + 1f) % 1f
            val pos = infinityPathPoint(segmentProgress)
            val alpha = 1f - (i / segmentCount.toFloat()) * 0.8f
            Icon(
                painter = bodyPainter,
                contentDescription = "Snake Body $i",
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        translationX = pos.x - center
                        translationY = pos.y - center
                        this.alpha = alpha
                    }
                , tint=color
            )
        }

        val headPos = infinityPathPoint(t)
        Icon(
            painter = headPainter,
            contentDescription = "Snake Head",
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    translationX = headPos.x - center
                    translationY = headPos.y - center
                    alpha = 1f
                }
            , tint=color
        )
        Image(
            painter = painterResource( R.drawable.snake_eyes),
            contentDescription = "Snake Head",
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    translationX = headPos.x - center
                    translationY = headPos.y - center
                    alpha = 1f
                }
        )
    }
}
fun convert(context: Context, rawResId: Int, fileName: String): File {
    val inputStream = context.resources.openRawResource(rawResId)
    val tempFile = File(context.cacheDir, fileName)

    inputStream.use { input ->
        FileOutputStream(tempFile).use { output ->
            input.copyTo(output)
        }
    }

    return tempFile
}

@Composable
fun VideoPlayer(
    videoUri: String,
    modifier: Modifier = Modifier,
    token: MutableState<Boolean>,
    win: MutableState<Int>,
    pause: MutableState<Boolean>
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val exoPlayer = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUri))
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    token.value = true
                    win.value = 0
                    pause.value = false
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }
    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = modifier
    )
}

@Composable
fun SvgImage(url: String, modifier: Modifier = Modifier, alpha: Float) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .decoderFactory(SvgDecoder.Factory())
            .build()
    )
    Image(painter = painter, contentDescription = null, modifier = modifier, alpha = alpha)
}
fun validpassword():  Boolean{
    if (current_pass.length>7 && current_pass.length<17 && !current_user.contains(":") && !current_pass.contains(":")){
        return true
    }
    return false
}
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun signin(context: Context, mode: Int, screen_no: MutableState<Int>):String{
    var str=""

    val users= readusers(context)
    val validpassword= validpassword()
    if (users.isEmpty() && mode==0){
        str="No users exist on this system. Sign up to create a new one"
    }
    else if (users.isEmpty() && mode==1 && validpassword){
        adduser(context)
        screen_no.value=1
    }
    else if (validpassword){
        var usermatch = false
        var passmatch=false
        for (i in users){
            val j = i.split(":")
            if (j.first()== current_user){
                usermatch=true
                if (mode==0 && j.last()== current_pass){
                    passmatch=true
                }
            }
        }
        if (mode==1){
            if (usermatch){
                str="User Exists. Use new username"
            }
            else{
                str="Done"
                screen_no.value=1
                adduser(context)

            }
        }
        else{
            if (usermatch && passmatch){
                str="Done"
                screen_no.value=1
            }
            else if (usermatch){
                str="Incorrect Password"
            }
            else{
                str="Incorrect Username"
            }
        }
    }
    else{
        str="Enter Valid Password with 8-16 characters and no invalid characters like \":\""
    }
    return str
}
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun signinscreen(screen_no: MutableState<Int>, tileWidth: Dp, tileHeight: Dp){
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("Done") }
    if (error!="Done") {
        ErrorDialog(error,{error = "Done"})
    }
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)))
    {
        var mode by remember { mutableStateOf(0) }
        AnimatedBackground(tileWidth, tileHeight)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(modifier = Modifier.clip(RoundedCornerShape(tileHeight))) {
                IconButton(
                    onClick = { mode = 0 },
                    modifier = Modifier.size(tileWidth * 5, tileHeight * 1.5f)
                        .background(if (mode == 0) Color.White else Color.Gray)
                ) {
                    Text(
                        "SIGN IN",
                        color = if (mode == 0) Color.Black else Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
                IconButton(
                    onClick = { mode = 1},
                    modifier = Modifier.size(tileWidth * 5, tileHeight * 1.5f)
                        .background(if (mode == 1) Color.White else Color.Gray)
                ) {
                    Text(
                        "SIGN UP",
                        color = if (mode == 1) Color.Black else Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") }
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Row(
                modifier = Modifier.padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {

            }

            Spacer(modifier = Modifier.height(20.dp))
            val context= LocalContext.current
            Button(
                onClick = { current_user=username; current_pass=password; error=signin(context, mode, screen_no) },
                modifier = Modifier
                    .size(tileWidth * 5, tileHeight * 2),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5722),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(tileHeight)
            ) {
                Text(if (mode==0) "SIGN IN" else "SIGN UP")
            }
        }

    }
}
@Composable
fun choosescreen(caterpillars: SnapshotStateList<Caterpillars>,caterpillarCount: MutableState<Int>, screen_no: MutableState<Int>, tileWidth: Dp,tileHeight: Dp){
        val maxCaterpillars = 4
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF121212)))
            {
                AnimatedBackground(tileWidth, tileHeight)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SELECT DIFFICULTY",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5722),
                        fontSize = 30.sp
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (caterpillarCount.value > 1) caterpillarCount.value-=1
                            },
                            modifier = Modifier.size(60.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.minus),
                                contentDescription = "Decrease",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "${caterpillarCount.value}",
                            color = Color.White,
                            fontSize = 30.sp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        IconButton(
                            onClick = {
                                if (caterpillarCount.value < maxCaterpillars) caterpillarCount.value+=1
                            },
                            modifier = Modifier.size(70.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.plus),
                                contentDescription = "Increase",
                                tint = Color.White
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(caterpillarCount.value) {
                            Image(
                                painter = painterResource(R.drawable.snake_head),
                                contentDescription = "Caterpillar",
                                modifier = Modifier
                                    .size(tileWidth * 2, tileHeight * 2)
                                    .padding(5.dp)
                                    .graphicsLayer { rotationZ = 180f }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = { makecaterpillarlist(caterpillars, caterpillarCount.value); screen_no.value=2 },
                        modifier = Modifier
                            .size(tileWidth * 5, tileHeight * 5),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722),
                            contentColor = Color.White
                        ),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Game",
                            modifier = Modifier.size(tileWidth * 5, tileHeight * 5)
                        )
                    }
                }

    }
}
@Composable
fun VolumeSlider(context: Context,
                 volume: Float,
                 onVolumeChange: (Float) -> Unit, widthDp: Dp, heightDp: Dp
) {
    Column(
        modifier = Modifier.padding((widthDp), (heightDp))
    ) {
        Text(text = "Volume: ${(volume * 100).toInt()}%", fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height((0.5*heightDp)))

        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            steps = 9,
            modifier = Modifier.fillMaxWidth()
        )
    }
    editsettings(context = context,0,volume )
}
@Composable
fun BrightnessSlider(context: Context,
                 brightness: Float,
                 onBrightnessChange: (Float) -> Unit, widthDp: Dp, heightDp: Dp
) {
    Column(
        modifier = Modifier.padding((0.5*widthDp), (heightDp))
    ) {
        Text(text = "Brightness: ${(brightness * 100).toInt()}%", fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height((0.5*heightDp)))

        Slider(
            value = brightness,
            onValueChange = onBrightnessChange,
            valueRange = 0f..1f,
            steps = 9,
            modifier = Modifier.fillMaxWidth()
        )
    }
    editsettings(context = context,1,brightness )
}
fun readsettings(context: Context): List<String> {
    return try {
        val fileInput = context.openFileInput("settings.txt")
        fileInput.bufferedReader().readLines()
    } catch (e: Exception) {
        val scoreOutput = context.openFileOutput("settings.txt", Context.MODE_PRIVATE)
        scoreOutput.write("100\n100\ntrue\ntrue\n".toByteArray())
        listOf("100", "100", "true", "true")
    }
}
fun readusers(context: Context): List<String> {
    return try {
        val fileInput = context.openFileInput("users.txt")
        fileInput.bufferedReader().readLines()
    } catch (e: Exception) {
        val scoreOutput = context.openFileOutput("users.txt", Context.MODE_PRIVATE)
        scoreOutput.write("".toByteArray())
        emptyList<String>()
    }
}
fun editsettings(context: Context, number: Int, vol: Float) {
    val fileInput = context.openFileInput("settings.txt")
    var input=fileInput.bufferedReader().readLines().toMutableList()
    if (number==2 || number==3){
        input[number]=(!input[number].toBoolean()).toString()
    }
    else{
        input[number]=vol.toString()
    }
    val scoreOutput = context.openFileOutput("settings.txt", Context.MODE_PRIVATE)
    for (i in input){
        scoreOutput.write((i+"\n").toByteArray())
    }
}
fun readpowerups(context: Context): List<String> {
    return try {
        val fileInput = context.openFileInput("powerups.txt")
        fileInput.bufferedReader().readLines()
    } catch (e: Exception) {
        val scoreOutput = context.openFileOutput("powerups.txt", Context.MODE_PRIVATE)
        scoreOutput.write("".toByteArray())
        emptyList<String>()
    }
}
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun adduser(context: Context) {
    val scoreOutput = context.openFileOutput("users.txt", Context.MODE_APPEND)
    scoreOutput.write("$current_user:$current_pass\n".toByteArray())
    val powerup = context.openFileOutput("powerups.txt", Context.MODE_APPEND)
    powerup.write("${current_user},0,0,0,0,0,0\n".toByteArray())
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun editpowerups(context: Context, powerupid:Int,number: Int) {
    var powerups= readpowerups(context).toMutableList()
    val powerup = context.openFileOutput("powerups.txt", Context.MODE_PRIVATE)
    for (i in powerups){
        val j= i.split(",").toMutableList()
        if (j[0]== current_user){
            j[powerupid]=(j[powerupid].toInt()+number).toString()
        }
        val k=j.joinToString(",")
        println("$k")
        powerup.write("$k\n".toByteArray())
    }


}
@Composable
fun generatebullet(bullets: SnapshotStateList<Bullet>, bullet: Bullet, tileWidth: Dp, tileHeight: Dp, gun_width: Int,rapidfire:MutableState<Boolean>, multi: MutableState<Boolean>) {
    var y by remember { mutableStateOf(bullet.y) }
    LaunchedEffect(bullet.id) {
        repeat(33) {
            val index = bullets.indexOfFirst { it.id == bullet.id }
            if (index != -1) {
                bullets[index] = bullets[index].copy(y = bullets[index].y - 1)
                y -= 1
            }
            if (rapidfire.value) delay(20) else delay(30)
        }
    }

    Image(
        painter = painterResource(R.drawable.bullets),
        contentDescription = "Bullet",
        modifier = Modifier
            .size(tileWidth, tileHeight)
            .offset(tileWidth * (gun_width+0.625f), tileHeight * y)
    )
}
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun makecaterpillarlist(caterpillars: SnapshotStateList<Caterpillars>, caterpillarcount: Int){
    var tintColor=Color.Black
    if (caterpillars.isEmpty()) {
        for (i in 1..caterpillarcount) {
            fetchRandomCentipedeColor { fetchedColor ->
                tintColor = fetchedColor
                caterpillars.addLast(
                    Caterpillars(
                        1,
                        9,
                        "START",
                        mutableStateListOf(
                            position(7, 0, "DOWN", tintColor),
                            position(7, -1, "DOWN", tintColor),
                            position(7, -2, "DOWN", tintColor),
                            position(7, -3, "DOWN", tintColor),
                            position(7, -4, "DOWN", tintColor),
                            position(7, -5, "DOWN", tintColor),
                            position(7, -6, "DOWN", tintColor),
                            position(7, -7, "DOWN", tintColor),
                            position(7, -8, "DOWN", tintColor)
                        )

                    )

                )
            }

        }
    }
}
@Composable
fun generatecrab(screen_no: MutableState<Int>,tileWidth: Dp,tileHeight: Dp, crabmove: MutableState<Boolean>, pause: MutableState<Boolean>){
    var x by remember { mutableStateOf(-1) }
    var y by remember { mutableStateOf(Random.nextInt(9, 28)) }

    LaunchedEffect(Unit) {
        while (true) {
            if (screen_no.value!=2){
                break
            }
            if (crabmove.value && screen_no.value==2) {
                crabmove.value = false
                val waitTime = Random.nextInt(3000, 10000)
                    if(!pause.value) {
                        y = Random.nextInt(9, 28)
                        val stepCount = Random.nextInt(13, 17)
                        val speed = 100L
                        for (i in 0 until stepCount) {
                            if (!pause.value) {
                                x += 1
                                val yShift = listOf(-1, 0, 1).random()
                                y = (y + yShift).coerceIn(9, 28)
                                delay(speed)
                            }
                        }
                    }
                if (!pause.value){
                    x = -2
                    delay(waitTime.toLong())
                }
                else{
                    delay(200)
                }



                crabmove.value = true

            }

        }
    }
    LaunchedEffect(x, y) {


        val target = mushroom_positions.find { it.x == x && it.y == y }
        if (target != null) {
            println("$x $y")
            mushroom_positions.remove(target)
        }
    }


    Image(painterResource( R.drawable.crab), contentDescription = "CRAB", modifier = Modifier.size(tileWidth*2,tileHeight*2).offset(x*tileWidth, y*tileHeight))
}
@Composable
fun generatescorpion(screen_no: MutableState<Int>, tileWidth: Dp, tileHeight: Dp, scorpionmove: MutableState<Boolean>, pause: MutableState<Boolean>, shield: MutableState<Boolean>, skinUrls: List<String>){
    var x by remember { mutableStateOf(-1) }
    var y by remember { mutableStateOf(Random.nextInt(9, 28)) }
    val listy= mutableListOf<Int>()
    for (i in mushroom_positions){
        listy.addLast(i.y)
    }
    LaunchedEffect(Unit) {
        while (true) {
            if (screen_no.value!=2){
                break
            }
            if (scorpionmove.value && screen_no.value==2) {
                scorpionmove.value = false
                val waitTime = Random.nextInt(5000, 10000)
                if (!pause.value){
                    x = -2
                    delay(waitTime.toLong())
                }
                else{
                    delay(200)
                }
                if(!pause.value) {
                    y = listy.random()
                    val stepCount = 17
                    val speed = 100L
                    for (i in 0 until stepCount) {
                        if (!pause.value) {
                            x += 1
                            delay(speed)
                        }
                    }
                    if (shield.value) shield.value=false
                }
                scorpionmove.value = true

            }

        }
    }
    LaunchedEffect(x, y) {
        val target = mushroom_positions.find { it.x == x && it.y == y }

        if (target != null && !shield.value) {
            println("$x $y")
            val index=mushroom_positions.indexOf(target)
            mushroom_positions.remove(target)
            target.poison=true
            mushroom_positions.add(index,target)

        }
    }

    SvgImage(url=skinUrls[0], modifier = Modifier.size(tileWidth*2,tileHeight*2).offset(x*tileWidth, y*tileHeight), alpha = 1f)
}
@Composable
fun joystick(pause: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    onMove: (Offset) -> Unit
) {
    var center by remember { mutableStateOf(Offset.Zero) }
    var handlePosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(size)

            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (!pause.value) {
                        center = Offset(size.toPx() / 2, size.toPx() / 2)
                        handlePosition = offset
                            }
                    },
                    onDrag = { change, dragAmount ->
                        if (!pause.value) {
                        val newOffset = handlePosition + dragAmount
                        val radius = size.toPx() / 2
                        val maxDistance = radius

                        val delta = newOffset - center
                        val distance = delta.getDistance()

                        val clampedOffset = if (distance <= maxDistance) {
                            newOffset
                        } else {
                            center + delta / distance * maxDistance
                        }

                            handlePosition = clampedOffset
                            onMove(clampedOffset - center)
                        }
                        change.consume()
                    },
                    onDragEnd = {
                        handlePosition = center
                        onMove(Offset.Zero)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.toPx() / 2
            if (center == Offset.Zero) center = Offset(radius, radius)
            if (handlePosition == Offset.Zero) handlePosition = center

            drawCircle(
                color = Color(0,0,0,100),
                radius = radius,
                center = center,
                style = Fill
            )
            drawCircle(
                color = Color(0,0,0,200),
                radius = radius / 3,
                center = handlePosition
            )
        }

    }
}
@Composable
fun LeaderboardScreen(screen_no: MutableState<Int>) {
    val listState= rememberScrollState()
    val leaderboard = remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }

    LaunchedEffect(Unit) {
        fetchLeaderboard { result ->
            leaderboard.value = result
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)
        .verticalScroll(listState).padding(10.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Row() {
            IconButton(onClick = {screen_no.value=0}) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "", tint = Color.White)
            }
            Text("LEADERBOARD", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (leaderboard.value.isEmpty()) {
            CircularProgressIndicator()
        } else {
            Column {
                for (entry in leaderboard.value) {
                    LeaderboardRow(entry)
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFFEFEFEF), shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = entry.name, modifier = Modifier.weight(1f), color= Color.Black)
        Text(text = "${entry.score}", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun generatecaterpillar(screen_no: MutableState<Int>,score:MutableState<Int>,startanim: SnapshotStateList<MutableState<Boolean>>, caterpillars:  SnapshotStateList<Caterpillars>, bullets: SnapshotStateList<Bullet>, colHeightPx: MutableState<Int>, colWidthPx: MutableState<Int>, height: Dp, width: Dp, index: Int, pause: MutableState<Boolean>, gun_width: Int, gun_height:Int, win: MutableState<Int>, fastforward:MutableState<Boolean>) {
    val isPoisoned = remember { mutableStateOf(false) }
    val targetRow = remember { mutableStateOf(27) }
    val reload = remember { mutableStateOf(false) }
    val turnPoints = remember { mutableStateListOf<position>() }
    val isMoving = remember { mutableStateOf(false) }

    LaunchedEffect(bullets.size) {
        if (!startanim[index].value) {

            while (true) {
                if (screen_no.value!=2){
                    break
                }
                delay(10)
                val currentBullets = bullets.toList()
                val currentCaterpillars = caterpillars.toList()
                currentBullets.forEachIndexed { bulletIndex, bullet ->
                    currentCaterpillars.forEachIndexed { caterpillarIndex, caterpillar ->
                        val hitSegmentIndex = caterpillar.position.indexOfFirst { segment ->
                            val bulletCenterX = bullet.x
                            val bulletCenterY = bullet.y
                            val segmentCenterX = segment.x
                            val segmentCenterY = segment.y
                            abs(bulletCenterX - segmentCenterX+0.625f) < 1f &&
                                    abs(bulletCenterY - segmentCenterY) == 0
                        }

                        if (hitSegmentIndex != -1) {
                            withContext(Dispatchers.Main) {
                                try {
                                    if (bulletIndex < bullets.size && bullets[bulletIndex] == bullet && bullet.id == bullets[bulletIndex].id) {
                                        mushroom_positions.addLast(mushroom_position(bullet.x,bullet.y,5,false))
                                        score.value+=10
                                        bullets.removeAt(bulletIndex)
                                    }

                                    val currentCaterpillar = caterpillars[caterpillarIndex]
                                    if (hitSegmentIndex == 0) {
                                        if (currentCaterpillar.size > 1) {
                                            caterpillars[caterpillarIndex] =
                                                currentCaterpillar.copy(
                                                    size = currentCaterpillar.size - 1,
                                                    position = currentCaterpillar.position
                                                        .subList(0, currentCaterpillar.size - 1)
                                                        .toMutableStateList()
                                                )
                                        } else {
                                            caterpillars.removeAt(caterpillarIndex)
                                        }
                                    } else {
                                        if (hitSegmentIndex < currentCaterpillar.position.size - 1) {
                                            val newSegments = currentCaterpillar.position
                                                .subList(hitSegmentIndex+1, currentCaterpillar.position.size)
                                                .map { it.copy() }
                                                .toMutableStateList()
                                            val newDirection = if (newSegments[0].direction == "LEFT") "RIGHT" else "LEFT"
                                            newSegments.forEach { seg ->
                                                seg.direction = newDirection
                                            }

                                            if (newSegments.isNotEmpty()) {
                                                caterpillars.add(
                                                    Caterpillars(
                                                        id = caterpillars.size + 1,
                                                        size = newSegments.size,
                                                        motion = currentCaterpillar.motion,
                                                        position = newSegments
                                                    )
                                                )

                                                if (startanim.size > caterpillarIndex) {
                                                    startanim.add(mutableStateOf(false))
                                                }
                                            }

                                            caterpillars[caterpillarIndex] =
                                                currentCaterpillar.copy(
                                                    size = hitSegmentIndex,
                                                    position = currentCaterpillar.position
                                                        .subList(0, hitSegmentIndex)
                                                        .toMutableStateList()
                                                )
                                        }
                                    }
                                } catch (e: Exception) {
                                    println("Error handling collision: ${e.message}")
                                }
                            }
                            return@forEachIndexed
                        }
                    }
                }

            }
        }
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(startanim[index].value, pause.value, Unit) {
        if ( !pause.value && screen_no.value==2) {
            while (true) {
                if (screen_no.value!=2){
                    break
                }
                isMoving.value = true

                try {
                    val updatedCaterpillar = caterpillars[index].copy(
                        position = caterpillars[index].position.map { it.copy() }
                            .toMutableStateList()
                    )

                val head = updatedCaterpillar.position[0]
                val headDirection = head.direction
                val headAdder = if (headDirection == "RIGHT") 1 else if (headDirection=="LEFT") -1 else 0
                val hitWall = (head.x < 1 && headDirection == "LEFT") ||
                        (head.x > 14 && headDirection == "RIGHT")
                val hitMushroom = mushroom_positions.any { it.x == head.x && it.y == head.y }
                val hitPoisonMushroom = mushroom_positions.any { it.x == head.x && it.y == head.y && it.poison }
                if (hitPoisonMushroom && !isPoisoned.value) {
                    isPoisoned.value = true
                    targetRow.value = 27
                }

                if (isPoisoned.value) {
                    if (head.y < targetRow.value) {

                        updatedCaterpillar.position[0] = head.copy(
                            y = head.y + 1
                        )
                        for (i in 1 until updatedCaterpillar.size) {
                            val segment = updatedCaterpillar.position[i]
                            val prevSegment = caterpillars[index].position[i - 1]
                            updatedCaterpillar.position[i] = segment.copy(
                                x = prevSegment.x,
                                y = prevSegment.y,
                                direction = prevSegment.direction
                            )
                        }
                    } else {
                        isPoisoned.value = false
                        continue
                    }
                } else {
                    if (hitWall || hitMushroom) {
                        turnPoints.add(position(head.x, head.y, headDirection, head.color))
                        updatedCaterpillar.position[0] = head.copy(
                            y = head.y + 1,
                            direction = if (headDirection == "RIGHT") "LEFT" else "RIGHT"
                        )
                    } else {
                        if (updatedCaterpillar.position[0].direction=="DOWN"){
                            if (index>0 && startanim[index-1].value){
                                delay(100)
                                continue
                            }
                            updatedCaterpillar.position[0] = head.copy(
                                y = head.y + 1, direction = "DOWN"
                            )
                            println(updatedCaterpillar)
                            if (head.y==8){
                                updatedCaterpillar.position[0] = head.copy(
                                    direction = if (index%2==0) "LEFT" else "RIGHT"
                                )
                            }
                        }
                        else {
                            updatedCaterpillar.position[0] = head.copy(
                                x = head.x + headAdder
                            )
                        }
                    }
                    for (i in 1 until updatedCaterpillar.size) {
                        val segment = updatedCaterpillar.position[i]
                        val prevSegment = caterpillars[index].position[i - 1]

                        val shouldTurn = turnPoints.any {
                            it.x == segment.x &&
                                    it.y == segment.y &&
                                    it.direction == segment.direction
                        }

                        if (shouldTurn) {
                            startanim[index].value=false
                            updatedCaterpillar.position[i] = segment.copy(
                                y = segment.y + 1,
                                direction = if (segment.direction == "RIGHT") "LEFT" else "RIGHT",
                                x = prevSegment.x
                            )

                            turnPoints.removeAll {
                                it.x == segment.x &&
                                        it.y == segment.y - 1 &&
                                        it.direction != segment.direction
                            }
                        } else {
                            updatedCaterpillar.position[i] = segment.copy(
                                x = prevSegment.x,
                                y = prevSegment.y,
                                direction = prevSegment.direction
                            )
                        }
                    }
                }
                caterpillars[index] = updatedCaterpillar
                if (fastforward.value) delay(150-index*10L) else delay(200-index*10L)
                isMoving.value = false
            }
                catch (e: Exception){
                    break
                }

        }
        }

    }
    if (caterpillars[index].motion == "START") {
        val tileWidth = width / 16
        val tileHeight = height / (16 * height / width)

        for (j in 0 until caterpillars[index].size) {
            val pos = caterpillars[index].position[j]

            val painter =
                if (j == 0) painterResource(R.drawable.snake_head) else painterResource(R.drawable.snake_body)
            val rotation = if (j == 0) {
                when (pos.direction) {
                    "DOWN" -> -90f
                    "UP" -> 90f
                    "RIGHT" -> 180f
                    else -> 0f
                }
            } else 0f
            Box() {
                Icon(
                    painter = painter,
                    contentDescription = if (j == 0) "Snake Head ${reload.value}" else "Snake Body",
                    modifier = Modifier
                        .size(tileWidth, tileHeight)
                        .offset(
                            x = pos.x * tileWidth,
                            y = pos.y * tileHeight
                        )
                        .graphicsLayer {
                            rotationZ = rotation
                        }, tint = pos.color
                )
                if (j==0) Icon(
                    painter = painterResource(R.drawable.snake_eyes),
                    contentDescription = if (j == 0) "Snake Head ${reload.value}" else "Snake Body",
                    modifier = Modifier
                        .size(tileWidth, tileHeight)
                        .offset(
                            x = pos.x * tileWidth,
                            y = pos.y * tileHeight
                        )
                        .graphicsLayer {
                            rotationZ = rotation
                        }, tint = Color.Black
                )


            }
            if (pos.y==32){

                scope.launch {
                    withContext(Dispatchers.Main) {
                        caterpillars.remove(caterpillars[index])
                    }
                }
                    break

            }
            if ((pos.y==gun_height  || pos.y==gun_height+1) && ((pos.direction=="LEFT" && pos.x==gun_width+1)||(pos.direction=="RIGHT" && pos.x==gun_width+2))){
                win.value=2

            }
        }

    }


}
@Composable
fun HelpDialog(skinUrls: List<List<String>>, onDismiss: () -> Unit) {
    var currentTab by remember { mutableStateOf(0) }
    AlertDialog(
        onDismissRequest = onDismiss,

        title = {Text("How to Play", style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(modifier = Modifier.background(Color(51, 8, 130))) {
                TabRow(modifier = Modifier.background(Color(51,8,130)),selectedTabIndex = currentTab) {
                    Tab(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        text = { Text("Basics") }
                    )
                    Tab(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        text = { Text("Power-Ups") }
                    )
                }
                when (currentTab) {
                    0 -> GameInstructions(skinUrls = skinUrls)
                    1 -> PowerUpInstructions()
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Got it!")
            }
        }
    )
}

@Composable
fun GameInstructions(skinUrls: List<List<String>>) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState()).background(Color(51,8,130))) {
        InstructionSection("Basic Controls") {
            InstructionItem(
                icon = painterResource(R.drawable.controller),
                text = "Move your ship left/right using the joystick or device tilt"
            )
            InstructionItem(
                icon = painterResource(R.drawable.bullet),
                text = "Tap the fire button to shoot bullets upward"
            )
            InstructionItem(
                icon = painterResource(R.drawable.pause),
                text = "Press pause button to access settings and power-ups"
            )
        }
        InstructionSection("Enemies") {
            InstructionItem(
                icon = painterResource(R.drawable.snake_head),
                text = "Centipedes: Shoot segments to split them. Head shots destroy segments behind"
            )
            InstructionItem(
                icon = skinUrls[1][0],
                text = "Scorpions: Move horizontally and poison mushrooms (turns them purple)"
            )
            InstructionItem(
                icon = painterResource(R.drawable.crab),
                text = "Crabs: Move in zig-zag patterns and destroy mushrooms they touch"
            )
        }
        InstructionSection("Mushrooms") {
            InstructionItem(
                icon = skinUrls[0][0],
                text = "Normal Mushrooms: Take 5 hits to destroy. Block centipede movement"
            )
            InstructionItem(
                icon = skinUrls[0][1],
                text = "Poisoned Mushrooms: Purple mushrooms slow centipedes but make them move downward"
            )
        }
    }
}
@Composable
fun InstructionSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp),
            color = Color(255,238,0)
        )
        content()
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}
@Composable
fun PowerUpInstructions() {
    val powerUps=listOf(
        PowerUp(
            id = 1,
            name = "Shield Generator",
            description = "Absorbs one hit from scorpions",
            duration = 30000,
        ),
        PowerUp(
            id = 2,
            name = "Rapid Fire",
            description = "Doubles firing speed for the round",
            duration = 15000,

        ),
        PowerUp(
            id = 3,
            name = "Speed Boost",
            description = "Increases x-axis movement speed by 50%",
            duration = 10000,
        ),
        PowerUp(
            id = 4,
            name = "Mushroom Bomb",
            description = "Removes 20% of mushrooms on screen",
            duration = 0,
        ),
        PowerUp(
            id = 5,
            name = "Multishot Blast",
            description = "Fires three bullets in a spread",
            duration = 10000,
        ),
        PowerUp(
            id = 6,
            name = "Poison Remover",
            description = "Converts poison mushrooms back to normal",
            duration = 0,
        )
    )
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
       for (powerUp in powerUps){
            PowerUpCard(powerUp)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun PowerUpCard(powerUp: PowerUp) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0f,0f,0f,alpha =0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(if (powerUp.id==1) R.drawable.shield else if (powerUp.id==2) R.drawable.rapidfire else if (powerUp.id==3) R.drawable.ff else if (powerUp.id==4) R.drawable.mushbomb else if (powerUp.id==5) R.drawable.multi  else R.drawable.poisonremove),
                contentDescription = powerUp.name,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = powerUp.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                    , color = Color(0,245,255)
                )
                Text(
                    text = powerUp.description,
                    style = MaterialTheme.typography.bodyMedium
                    , color = Color(0,245,255)
                )
            }
        }
    }
}

@Composable
fun InstructionItem(icon: Any, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (icon) {

            is ImageVector -> Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            is Painter -> Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            is String -> SvgImage(
                icon, modifier = Modifier.size(24.dp), 1f
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = Color(0,245,255))
    }
}
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun HomeScreen(
    caterpillars: SnapshotStateList<Caterpillars>,
    screen_no: MutableState<Int>,
    tileWidth: Dp,
    tileHeight: Dp, skinUrls: List<List<String>>
) {
    var showHelp by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)))
            {

                AnimatedBackground(tileWidth, tileHeight)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(tileHeight*7))
                    Text(
                        text = "ARCADE CRAWLER",

                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722),
                        fontSize = 40.sp
                        )
                    AnimatedCaterpillarPreview(tileWidth, tileHeight)

                    Spacer(modifier = Modifier.height(tileHeight*3))

                    Button(
                        onClick = {screen_no.value=3 },
                        modifier = Modifier
                            .size(tileWidth*5,tileHeight*5)
                            ,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722),
                            contentColor = Color.White
                        ),
                        shape = CircleShape
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "", modifier = Modifier.size(tileWidth*5,tileHeight*5))
                    }
                    Spacer(modifier = Modifier.height(tileHeight*10))
                    Row() {
                        Button(
                            onClick = { screen_no.value = 4 },
                            modifier = Modifier
                                .size(tileWidth * 3, tileHeight * 3),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(201,165,0),
                                contentColor = Color.White
                            ),
                            shape = CircleShape
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.leaderboard),
                                contentDescription = "",
                                modifier = Modifier.size(tileWidth * 5, tileHeight * 5)
                            )
                        }
                        Spacer(modifier = Modifier.width(tileWidth*7))
                        Button(
                            onClick = { showHelp = true },
                            modifier = Modifier
                                .size(tileWidth * 3, tileHeight * 3),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(201,165,0),
                                contentColor = Color.White
                            ),
                            shape = CircleShape
                        ) {
                            Text(
                                "?"
                            )
                        }
                    }

                    if (showHelp) {
                        HelpDialog(skinUrls = skinUrls, onDismiss = { showHelp = false })
                    }
                }


                }

            }


@Composable
fun AnimatedBackground(tileWidth: Dp, tileHeight: Dp) {
    val tileWidthPx = with(LocalDensity.current) { tileWidth.toPx() }
    val tileHeightPx = with(LocalDensity.current) { tileHeight.toPx() }
    if (tileWidthPx.toInt() <= 0 || tileHeightPx.toInt() <= 0) return

    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = tileWidthPx * 16,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (x in 0..size.width.toInt() step tileWidthPx.toInt()) {
                drawLine(
                    color = Color(0x1AFFFFFF),
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), size.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..size.height.toInt() step tileWidthPx.toInt()) {
                drawLine(
                    color = Color(0x1AFFFFFF),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(size.width, y.toFloat()),
                    strokeWidth = 1f
                )
            }
        }

        Image(
            painter = painterResource(R.drawable.crab),
            contentDescription = null,
            modifier = Modifier
                .size(tileWidth * 2, tileHeight * 2)
                .offset(x = offset.dp, y = tileHeight * 10)
                .alpha(0.3f)
        )

        Image(
            painter = painterResource(R.drawable.scorpion),
            contentDescription = null,
            modifier = Modifier
                .size(tileWidth * 2, tileHeight * 2)
                .offset(x = (offset.dp * 0.7f), y = tileHeight * 15)
                .alpha(0.3f)
        )
    }
}

@Composable
fun AnimatedCaterpillarPreview(tileWidth: Dp, tileHeight: Dp) {
    val segments = remember { mutableStateListOf<Offset>() }
    val infiniteTransition = rememberInfiniteTransition()
    val headX by infiniteTransition.animateFloat(
        initialValue = -tileWidth.value * 3,
        targetValue = tileWidth.value * 6,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(headX) {
        segments.clear()
        for (i in 0..5) {
            segments.add(Offset(headX - i * tileWidth.value * 0.8f, 0f))
        }
    }

    Box(
        modifier = Modifier
            .width(tileWidth * 8)
            .height(tileHeight * 2),
        contentAlignment = Alignment.Center
    ) {
        segments.forEachIndexed { index, offset ->
            Image(
                painter = if (index == 0) painterResource(R.drawable.snake_head)
                else painterResource(R.drawable.snake_body),
                contentDescription = null,
                modifier = Modifier
                    .size(tileWidth, tileHeight)
                    .offset(x = offset.x.dp, y = 0.dp)
                    .graphicsLayer {
                        rotationZ =  180f
                    }
            )
        }
    }
}
@Composable
fun generatemushrooms(screen_no: MutableState<Int>,
    score: MutableState<Int>,
                      colHeightPx: MutableState<Int>,
                      colWidthPx: MutableState<Int>,
                      tileHeight: Dp,
                      tileWidth: Dp,
                      index: Int,
                      bullets: SnapshotStateList<Bullet>,
                      shield: MutableState<Boolean>, skinUrls: List<String>, mushbomb:MutableState<Boolean>, poisonremove: MutableState<Boolean>
) {
    val context= LocalContext.current
    var size = remember {mutableStateOf(0f)}
    LaunchedEffect(mushbomb.value) {
        if (mushbomb.value) {
            val mushpos = mushroom_positions
            var index = (mushpos.size * 0.2f).toInt()
            if (index==0 && mushpos.size>0) {index=1 }
            Soundplayer.explode(context)
            repeat(20){
                size.value+=0.1f
                delay(20)
            }
            for (i in 0..index - 1) {

                mushroom_positions.removeAt(i)
            }
            mushbomb.value=false
        }
    }
    LaunchedEffect(poisonremove.value) {
        if (poisonremove.value){
        for (i in mushroom_positions){
            if (i.poison){
                i.poison=false
            }
        }
        poisonremove.value=false
        }
    }
    LaunchedEffect(bullets.size) {
        while (true) {
            if (screen_no.value!=2){
                break
            }
            delay(50)
            val currentBullets = bullets.toList()
            val currentMushrooms = mushroom_positions.toList()
            currentBullets.forEachIndexed { bulletIndex, bullet ->
                currentMushrooms.forEachIndexed { mushroomIndex, mushroom ->
                    if (bullet.x == mushroom.x - 1 && bullet.y == mushroom.y) {
                        withContext(Dispatchers.Main) {
                            try {
                                if (bulletIndex < bullets.size && bullets[bulletIndex] == bullet && bullet.id == bullets[bulletIndex].id) {
                                    bullets.removeAt(bulletIndex)
                                }
                                mushroom_positions[mushroomIndex] =
                                    mushroom.copy(lives = mushroom.lives - 1)

                                if (mushroom_positions[mushroomIndex].lives <= 0) {
                                    score.value+=15
                                    mushroom_positions.removeAt(mushroomIndex)
                                }

                                println("Mushroom hit! Remaining lives: ${mushroom_positions.getOrNull(mushroomIndex)?.lives ?: 0}")
                            } catch (e: Exception) {
                                println("Error handling mushroom collision: ${e.message}")
                            }
                        }
                        return@forEachIndexed
                    }
                }
            }
            if (screen_no.value==0){
                break
            }
        }
    }
    mushroom_positions.forEachIndexed { mushroomIndex, mushroom ->
        val tint = when (mushroom.lives) {
            5 -> 1f
            4 -> 0.8f
            3 -> 0.6f
            2 -> 0.4f
            1 -> 0.3f
            else -> 0f
        }

        Box(
            modifier = Modifier
                .size(tileWidth, tileHeight)
                .offset(x = mushroom.x * tileWidth, y = mushroom.y * tileHeight), contentAlignment = Alignment.Center
        ) {
            SvgImage(
                url = if (mushroom.poison)skinUrls[1] else skinUrls[0],
                modifier = Modifier.fillMaxSize(), alpha=tint
            )
            if (mushroom.lives < 5) {
                Text(
                    text = mushroom.lives.toString(),
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 4.dp)
                )
            }


        }
        if (shield.value){
            Icon(painter= painterResource(R.drawable.circle), contentDescription = "", modifier = Modifier
                .size(tileWidth*1.3f, tileHeight*1.3f).offset(x = (mushroom.x-0.1f) * tileWidth, y = (mushroom.y-0.1f) * tileHeight)
                ,tint=Color(106,222,234,150))
        }
        if (mushbomb.value && mushroomIndex< mushroom_positions.size*0.2f){
            Image(painter= painterResource(R.drawable.explode), contentDescription = "", modifier = Modifier
                .size(tileWidth*size.value, tileHeight*size.value).offset(x = (mushroom.x-0.1f) * tileWidth, y = (mushroom.y-0.1f) * tileHeight)
                )
        }

    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun mushroompositions() {
    var newpositions= mutableListOf<Offset>()
    newpositions.clear()
    fetchMushroomLayout{result ->
        newpositions=result.toMutableList()
        mushroom_positions.clear()
        for (i in 0 until newpositions.size){
            newpositions[i] = Offset(2+(newpositions[i].x*12).roundToInt().toFloat(), ((newpositions[i].y-0.7)*63.4+8).roundToInt().toFloat())
            mushroom_positions.addLast(mushroom_position(newpositions[i].x.toInt(), newpositions[i].y.toInt(),5,false))
        }
        println(newpositions)
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ArcadeCrawlerTheme {
        Greeting("Android")
    }
}