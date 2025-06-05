package com.example.arcadecrawler

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.arcadecrawler.ui.theme.ArcadeCrawlerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

data class position(var x: Int, var y: Int, var direction: String)
data class Bullet(val id: Int, var x: Int, var y: Int, var direction: String = "")
data class mushroom_position(var x: Int, var y: Int, var lives: Int, var poison: Boolean)
data class Caterpillars(val id: Int, var size: Int, val motion: String, var position: SnapshotStateList<position>)
var mushroom_positions= SnapshotStateList<mushroom_position>()

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

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val caterpillarCount = remember { mutableStateOf(1) }
    val screen_no= remember { mutableStateOf(0) }
    val context= LocalContext.current
    var bulletx by remember { mutableStateOf(0) }
    val gyroController = remember { GyroController(context) }
    val settings = remember { mutableStateOf(readsettings(context = context)) }
    var volume by remember { mutableStateOf(settings.value[0].toFloat()) }
    var brightness by remember { mutableStateOf(settings.value[1].toFloat()) }
    var usegyro by remember { mutableStateOf(!settings.value[2].toBoolean())  }
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
    var gun_position by remember { mutableStateOf( position(0,0,""))}
    var moveOffset by remember { mutableStateOf(Offset.Zero) }
    val caterpillars = remember { mutableStateListOf<Caterpillars>()}
    if (mushroom_positions.isEmpty()) {
        mushroompositions()
    }
    LaunchedEffect(shot) {
        if (shot){
            bulletx=gun_width

            bullets.add(Bullet(nextBulletId++, gun_width, gun_height))
            Soundplayer.shoot(context)
        }

        if (shot){
            repeat(16) {
                size -= 0.0625f
                delay(40)
            }
            size=1f
            shot=false
        }

    }
    var rot by remember { mutableStateOf(0f) }
    var rot2 by remember { mutableStateOf(0f) }
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
            if (usegyro && !pause.value) {
                val gyroY = gyroController.yOffset
                val gyroX = gyroController.xOffset

                if (!gunCooldown) {
                    scope.launch {
                        gunCooldown = true

                        // Vertical movement
                        if (gyroY > 0.2) {
                            if (gun_height == 27) gun_height = 29
                            else if (gun_height == 29) gun_height = 31
                            rot2=30f
                        } else if (gyroY < -0.2) {
                            if (gun_height == 31) gun_height = 29
                            else if (gun_height == 29) gun_height = 27
                            rot2=-30f
                        }

                        // Horizontal movement
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
           // Soundplayer.win(context)
            pause.value = true
        }
    }
    LaunchedEffect(Unit) {
        gyroController.start()
        MusicController.start(context)
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
        Box(modifier = Modifier.fillMaxSize().onGloballyPositioned { coordinates ->
            colHeightPx.value = coordinates.size.height; colWidthPx.value = coordinates.size.width;  // height in pixels
        }) {

            val density = LocalDensity.current
            val height = with(density) { colHeightPx.value.toDp() }
            val width = with(density) { colWidthPx.value.toDp() }
            val tileWidth = width / 16
            val tileHeight = height / (16 * height / width)
            if (screen_no.value == 0) {
                HomeScreen(caterpillars,screen_no, tileWidth, tileHeight)
            }
            else if (screen_no.value==1){
                choosescreen(caterpillars, caterpillarCount ,screen_no, tileWidth,tileHeight)
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
                generatemushrooms(
                    colHeightPx,
                    colWidthPx,
                    tileHeight,
                    tileWidth,
                    0,
                    bullets,
                    crabmove
                )
                for (i in 0 until caterpillars.size) {
                    generatecaterpillar(
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
                        win
                    )
                }
                caterpillarstart(
                    startanim,
                    caterpillars,
                    bullets,
                    colHeightPx,
                    colWidthPx,
                    height,
                    width,
                    0,
                    pause,
                    gun_width,
                    gun_height,
                    win
                )



                Image(
                    painter = painterResource(R.drawable.gun),
                    contentDescription = "GUN $gun_position",
                    modifier = Modifier.height(tileWidth * 2.5f).graphicsLayer { rotationZ = 90f }
                        .offset(
                            x = tileHeight * gun_height, y = -1 * tileWidth * gun_width
                        )
                )
                // Replace your current bullet rendering with:
                bullets.forEach { bullet ->
                    generatebullet(bullets, bullet, tileWidth, tileHeight, bulletx)
                }
                generatecrab(tileWidth, tileHeight, crabmove, pause)
                generatescorpion(tileWidth, tileHeight, scorpionmove, pause)
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    Spacer(Modifier.fillMaxWidth().fillMaxHeight(0.03f))
                    Row() {

                        LaunchedEffect(startanim.first().value) {
                            if(!startanim.first().value){
                                repeat(20){
                                    spacersize+=0.1f/20
                                    delay(30)
                                }
                            }
                        }
                        Spacer(Modifier.fillMaxWidth(spacersize))
                        if (!startanim.first().value) {
                            IconButton(
                                onClick = { if (!startanim.last().value) pause.value = !pause.value },
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
                                    gun_position = position(offset.x.toInt(), offset.y.toInt(), "")
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
                                                if (gun_width < 10) gun_width += 1

                                            } else if (gun_position.x < 0 && gun_position.x < -20) {
                                                if (gun_width > 2) gun_width -= 1

                                            }
                                            delay(100) // actual cooldown
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



                if (pause.value && win.value == 0 && !startanim.last().value) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(0, 0, 0, 200)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                            Spacer(Modifier.fillMaxWidth().fillMaxHeight(0.03f))
                            Row() {
                                Spacer(Modifier.fillMaxWidth(0.1f))
                                IconButton(
                                    onClick = { if (!startanim.last().value) pause.value = !pause.value },
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


                        }

                    }
                }
                if (win.value > 0) {
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
                            IconButton(
                                onClick = {
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
                                    imageVector = Icons.Default.Refresh, // Add a restart icon
                                    contentDescription = "Restart",
                                    tint = Color.Black
                                )
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

        }
    }
}
@Composable
fun choosescreen(caterpillars: SnapshotStateList<Caterpillars>,caterpillarCount: MutableState<Int>, screen_no: MutableState<Int>, tileWidth: Dp,tileHeight: Dp){
        val maxCaterpillars = 4 // Maximum number allowed
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF121212)))
            {
                // Reuse your animated background
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

                    // Caterpillar count display
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

                    // Visual caterpillar preview
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

                    // Start Button
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
        scoreOutput.write("100\n100\ntrue\n".toByteArray())
        listOf("100", "100", "true")
    }
}
fun editsettings(context: Context, number: Int, vol: Float) {
    val fileInput = context.openFileInput("settings.txt")
    var input=fileInput.bufferedReader().readLines().toMutableList()
    if (number==2){
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
@Composable
fun generatebullet(bullets: SnapshotStateList<Bullet>, bullet: Bullet, tileWidth: Dp, tileHeight: Dp, gun_width: Int) {
    var y by remember { mutableStateOf(bullet.y) }

    LaunchedEffect(bullet.id) {
        repeat(33) {
            val index = bullets.indexOfFirst { it.id == bullet.id }
            if (index != -1) {
                bullets[index] = bullets[index].copy(y = bullets[index].y - 1)
                y -= 1
            }
            delay(30)
        }

        //bullets.removeIf { it.id == bullet.id }
    }

    Image(
        painter = painterResource(R.drawable.bullets),
        contentDescription = "Bullet",
        modifier = Modifier
            .size(tileWidth, tileHeight)
            .offset(tileWidth * (gun_width+1.25f), tileHeight * y)
    )
}
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun makecaterpillarlist(caterpillars: SnapshotStateList<Caterpillars>, caterpillarcount: Int){
    if (caterpillars.isEmpty()) {
        for (i in 1..caterpillarcount) {
            caterpillars.addLast(
                Caterpillars(
                    1,
                    9,
                    "START",
                    mutableStateListOf(
                        position(7, 0, "DOWN"),
                        position(7, -1, "DOWN"),
                        position(7, -2, "DOWN"),
                        position(7, -3, "DOWN"),
                        position(7, -4, "DOWN"),
                        position(7, -5, "DOWN"),
                        position(7, -6, "DOWN"),
                        position(7, -7, "DOWN"),
                        position(7, -8, "DOWN")
                    )
                )
            )
        }
    }
}
@Composable
fun generatecrab(tileWidth: Dp,tileHeight: Dp, crabmove: MutableState<Boolean>, pause: MutableState<Boolean>){
    var x by remember { mutableStateOf(-1) }
    var y by remember { mutableStateOf(Random.nextInt(9, 28)) }

    LaunchedEffect(Unit) {
        while (true) {
            if (crabmove.value) {
                crabmove.value = false
                val waitTime = Random.nextInt(3000, 10000)
                    if(!pause.value) {
                        y = Random.nextInt(9, 28)
                        val stepCount = Random.nextInt(13, 17) // how far to go horizontally

                        val speed = 100L //Random.nextLong(100L, 300L)
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
fun generatescorpion(tileWidth: Dp,tileHeight: Dp, scorpionmove: MutableState<Boolean>, pause: MutableState<Boolean>){
    var x by remember { mutableStateOf(-1) }
    var y by remember { mutableStateOf(Random.nextInt(9, 28)) }
    val listy= mutableListOf<Int>()
    for (i in mushroom_positions){
        listy.addLast(i.y)
    }
    LaunchedEffect(Unit) {
        while (true) {
            if (scorpionmove.value) {
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
                    val stepCount = 17 // how far to go horizontally

                    val speed = 100L //Random.nextLong(100L, 300L)
                    for (i in 0 until stepCount) {
                        if (!pause.value) {
                            x += 1
                            delay(speed)
                        }
                    }
                }
                scorpionmove.value = true

            }
        }
    }
    LaunchedEffect(x, y) {
        val target = mushroom_positions.find { it.x == x && it.y == y }
        if (target != null) {
            println("$x $y")
            val index=mushroom_positions.indexOf(target)
            mushroom_positions.remove(target)
            target.poison=true
            mushroom_positions.add(index,target)
        }
    }


    Image(painterResource( R.drawable.scorpion), contentDescription = "SCORPION", modifier = Modifier.size(tileWidth*2,tileHeight*2).offset(x*tileWidth, y*tileHeight))
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

            // Outer circle
            drawCircle(
                color = Color(0,0,0,100),
                radius = radius,
                center = center,
                style = Fill
            )

            // Inner handle circle
            drawCircle(
                color = Color(0,0,0,200),
                radius = radius / 3,
                center = handlePosition
            )
        }

    }
}

@Composable
fun caterpillarstart(
    startanim: SnapshotStateList<MutableState<Boolean>>,
    caterpillars: SnapshotStateList<Caterpillars>,
    bullets: SnapshotStateList<Bullet>,
    colHeightPx: MutableState<Int>,
    colWidthPx: MutableState<Int>,
    height: Dp,
    width: Dp,
    indec: Int,
    pause: MutableState<Boolean>,
    gun_width: Int,
    gun_height: Int,
    win: MutableState<Int>
) {
    val reload = remember { mutableStateOf(false) }
    var index by remember { mutableStateOf(indec) }
    if (caterpillars.size!=0 && caterpillars[0].position[0].y==0 && index==startanim.size-1){
        index=0
    }
    try {
        val x = startanim[index].value
    }
    catch (e:Exception) {
        index -= 1
    }
    LaunchedEffect(startanim[index].value) {
            println(startanim[index].value)
            if (!startanim[index].value) {
                if (index < startanim.size - 1) {
                    index += 1
                }
            }

        }



        if (index < caterpillars.size && index < startanim.size && startanim[index].value) {

            val caterpillar = caterpillars[index]
            LaunchedEffect(startanim[index].value, pause.value) {
                if (startanim[index].value && !pause.value) {
                    repeat(7) {
                        for (i in 0 until caterpillar.size) {
                            caterpillar.position[i] =
                                caterpillar.position[i].copy(y = caterpillar.position[i].y + 1)
                        }
                        reload.value = !reload.value
                        delay(200)
                    }

                    // Diagonal movement
                    var counter = 0
                    repeat(9) {
                        for (i in counter + 1 until caterpillar.size) {
                            caterpillar.position[i] =
                                caterpillar.position[i].copy(y = caterpillar.position[i].y + 1)
                        }
                        for (j in 0 until counter + 1) {
                            caterpillar.position[j] = caterpillar.position[j].copy(
                                x = if (index%2==0) caterpillar.position[j].x + 1 else caterpillar.position[j].x - 1,
                                direction = if (index%2==0) "RIGHT" else "LEFT"
                            )
                        }
                        counter += 1
                        reload.value = !reload.value
                        delay(200)
                    }

                    // Mark animation as complete
                    startanim[index].value = false
                }
            }

            // Render the caterpillar during start animation
            val tileWidth = width / 16
            val tileHeight = height / (16 * height / width)

            for (j in 0 until caterpillar.size) {
                val pos = caterpillar.position[j]
                val painter = if (j == 0) painterResource(R.drawable.snake_head)
                else painterResource(R.drawable.snake_body)
                val rotation = when {
                    j == 0 -> when (pos.direction) {
                        "DOWN" -> -90f
                        "UP" -> 90f
                        "RIGHT" -> 180f
                        else -> 0f
                    }

                    else -> 0f
                }

                Image(
                    painter = painter,
                    contentDescription = if (j == 0) "Snake Head" else "Snake Body",
                    modifier = Modifier
                        .size(tileWidth, tileHeight)
                        .offset(x = pos.x * tileWidth, y = pos.y * tileHeight)
                        .graphicsLayer { rotationZ = rotation }
                )
            }
        }

}
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun generatecaterpillar(startanim: SnapshotStateList<MutableState<Boolean>>, caterpillars:  SnapshotStateList<Caterpillars>, bullets: SnapshotStateList<Bullet>, colHeightPx: MutableState<Int>, colWidthPx: MutableState<Int>, height: Dp, width: Dp, index: Int, pause: MutableState<Boolean>, gun_width: Int, gun_height:Int, win: MutableState<Int>) {
    val isPoisoned = remember { mutableStateOf(false) }
    val targetRow = remember { mutableStateOf(27) }
    val downing = remember { mutableStateOf(false) }
    val downing2 = remember { mutableStateOf(false) }
    val moving = remember { mutableStateOf(false) }
    val reload = remember { mutableStateOf(false) }
    val hitindex = remember { mutableStateOf(-1) }

    val turnPoints = remember { mutableStateListOf<position>() }
    val isMoving = remember { mutableStateOf(false) }
    LaunchedEffect(bullets.size) {
        if (!startanim[index].value) {
            while (true) {
                delay(20)
                val currentBullets = bullets.toList()
                val currentCaterpillars = caterpillars.toList()
                currentBullets.forEachIndexed { bulletIndex, bullet ->
                    currentCaterpillars.forEachIndexed { caterpillarIndex, caterpillar ->
                        val hitSegmentIndex = caterpillar.position.indexOfFirst { segment ->
                            val bulletCenterX = bullet.x
                            val bulletCenterY = bullet.y
                            val segmentCenterX = segment.x
                            val segmentCenterY = segment.y
                            abs(bulletCenterX - segmentCenterX+1.25f) < 1f &&
                                    abs(bulletCenterY - segmentCenterY) == 0
                        }

                        if (hitSegmentIndex != -1) {
                            withContext(Dispatchers.Main) {
                                try {

                                    if (bulletIndex < bullets.size && bullets[bulletIndex] == bullet && bullet.id == bullets[bulletIndex].id) {
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

                                                // Add the new caterpillar to startanim if needed
                                                if (startanim.size > caterpillarIndex) {
                                                    startanim.add(mutableStateOf(false))
                                                }
                                            }

                                            // Trim original caterpillar
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
    LaunchedEffect(startanim[index].value, pause.value) {
        if (!startanim[index].value && !pause.value) {
            while (true) {
                isMoving.value = true

                try {
                    val updatedCaterpillar = caterpillars[index].copy(
                        position = caterpillars[index].position.map { it.copy() }
                            .toMutableStateList()
                    )

                val head = updatedCaterpillar.position[0]
                val headDirection = head.direction
                val headAdder = if (headDirection == "RIGHT") 1 else -1
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
                        // Move down
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
                        turnPoints.add(position(head.x, head.y, headDirection))
                        updatedCaterpillar.position[0] = head.copy(
                            y = head.y + 1,
                            direction = if (headDirection == "RIGHT") "LEFT" else "RIGHT"
                        )
                    } else {
                        updatedCaterpillar.position[0] = head.copy(
                            x = head.x + headAdder
                        )
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
                delay(200-index*10L)
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
                    else -> 0f // RIGHT or default
                }
            } else 0f

            Image(
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
                    }
            )
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
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun HomeScreen(caterpillars: SnapshotStateList<Caterpillars>,
               screen_no: MutableState<Int>,
               tileWidth: Dp,
               tileHeight: Dp
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)))
            {
                // Animated background
                AnimatedBackground(tileWidth, tileHeight)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ARCADE CRAWLER",

                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722),
                        fontSize = 40.sp
                        )
                    AnimatedCaterpillarPreview(tileWidth, tileHeight)

                    Spacer(modifier = Modifier.height(40.dp))

                    // Start Button
                    Button(
                        onClick = {screen_no.value=1 },
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


                }

            }
}

@Composable
fun AnimatedBackground(tileWidth: Dp, tileHeight: Dp) {
    // Convert Dp to Px once at the start
    val tileWidthPx = with(LocalDensity.current) { tileWidth.toPx() }
    val tileHeightPx = with(LocalDensity.current) { tileHeight.toPx() }

    // Only proceed if we have valid dimensions
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
        // Grid pattern background
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Use the pre-calculated pixel values
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

        // Moving enemies in background
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
fun generatemushrooms(
    colHeightPx: MutableState<Int>,
    colWidthPx: MutableState<Int>,
    tileHeight: Dp,
    tileWidth: Dp,
    index: Int,
    bullets: SnapshotStateList<Bullet>,
    crabmove: MutableState<Boolean>
) {
    // ✅ Use shared reactive mushroom_positions
    LaunchedEffect(bullets.size) {
        while (true) {
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

                                // ✅ Modify directly on shared list
                                mushroom_positions[mushroomIndex] =
                                    mushroom.copy(lives = mushroom.lives - 1)

                                if (mushroom_positions[mushroomIndex].lives <= 0) {
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
        }
    }

    // ✅ Display using shared state
    mushroom_positions.forEachIndexed { _, mushroom ->
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
                .offset(x = mushroom.x * tileWidth, y = mushroom.y * tileHeight)
        ) {
            Image(
                painter = painterResource(id = if (mushroom.poison)R.drawable.poisonmushroom else R.drawable.mushroom),
                contentDescription = "Mushroom",
                modifier = Modifier.fillMaxSize(),
                alpha = tint
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
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun mushroompositions() {
    val number = Random.nextInt(9, 13)
    val usedPositions = mutableSetOf<Pair<Int, Int>>()
    mushroom_positions.clear()
    for (i in 1..number) {
        val x = Random.nextInt(2, 14)
        val yMin = 5 + i
        val yMax = (8 + 3 * i).coerceAtMost(27)
        val y = Random.nextInt(yMin, yMax + 1)
        val pos = x to y
        val isTooClose = usedPositions.any { (ux, uy) ->
            kotlin.math.abs(ux - x) <= 2 && kotlin.math.abs(uy - y) <= 2
        }
        if (!isTooClose) {
            usedPositions.add(pos)
            mushroom_positions.addLast(mushroom_position(x, (y/2).toInt()*2, 5,false))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ArcadeCrawlerTheme {
        Greeting("Android")
    }
}