package com.carsona.app.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import org.json.JSONObject
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carsona.app.model.AvatarState
import com.carsona.app.model.Tendency
import com.carsona.app.model.stageLabel

private val Ink = Color(0xFF1B1D22)
private val Soft = Color(0xFF6B7080)
private val Paper = Color(0xFFF7F6F2)
private val Track = Color(0xFFE6E3DA)
private val Night = Color(0xFF14161B)
private val Accent = Color(0xFFFF8A3D)

@Composable
fun CarsonaApp(
    ui: UiState,
    dialogues: Dialogues,
    onName: (String) -> Unit,
    onCloseReveal: () -> Unit,
    /** 테스트용 "시나리오 재생" 버튼. 시나리오 파일이 없으면 null (버튼 숨김) */
    onPlayScenario: (() -> Unit)? = null,
    /** 정차 후 성장 공개 화면에서 대사를 소리로 읽기 (주행 중에는 호출하지 않음) */
    onSpeak: (String) -> Unit = {},
) {
    AnimatedContent(
        targetState = ui.screen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen",
    ) { screen ->
        // 대사는 화면이 바뀔 때 한 번만 고른다 (다시 그릴 때마다 바뀌지 않게)
        val lineKey = when (screen) {
            Screen.FIRST_MEET -> "birth"
            Screen.DRIVING -> "driving"
            Screen.REVEAL -> ui.avatar.lastReveal.lineKey
            Screen.HOME -> ""
        }
        val line = remember(screen, ui.avatar.lastReveal) { dialogues.pick(lineKey) }
        when (screen) {
            Screen.FIRST_MEET -> FirstMeetScreen(line, onName)
            Screen.HOME -> HomeScreen(ui.avatar, onPlayScenario)
            Screen.DRIVING -> DrivingScreen(ui.avatar, line)
            Screen.REVEAL -> {
                LaunchedEffect(ui.avatar.lastReveal) { onSpeak(line) }
                RevealScreen(ui, line, onCloseReveal)
            }
        }
    }
}

/** ① 첫 만남: 아기 토토가 태어나고 이름을 짓는다 (정차 중) */
@Composable
private fun FirstMeetScreen(line: String, onName: (String) -> Unit) {
    var name by remember { mutableStateOf("토토") }
    Backdrop("bg_default", Paper) {
        Row(Modifier.fillMaxSize().padding(64.dp), verticalAlignment = Alignment.CenterVertically) {
            Toto(AvatarState(), 420.dp)
            Spacer(Modifier.width(72.dp))
            Column {
                Text(line, fontSize = 40.sp, color = Ink, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(40.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(10) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 36.sp),
                    modifier = Modifier.width(520.dp),
                )
                Spacer(Modifier.height(32.dp))
                Button(onClick = { onName(name) }, modifier = Modifier.height(80.dp).width(260.dp)) {
                    Text("이름 짓기", fontSize = 28.sp)
                }
            }
        }
    }
}

/** ② 홈: 토토 + 이름 + 성장 막대 + 성향 4개 (기어 P일 때 기본 화면) */
@Composable
private fun HomeScreen(avatar: AvatarState, onPlayScenario: (() -> Unit)?) {
    Backdrop("bg_default", Paper) {
        if (onPlayScenario != null) {
            TextButton(
                onClick = onPlayScenario,
                modifier = Modifier.align(Alignment.TopEnd).padding(24.dp),
            ) { Text("시나리오 재생", fontSize = 20.sp, color = Soft) }
        }
        Row(Modifier.fillMaxSize().padding(64.dp), verticalAlignment = Alignment.CenterVertically) {
            Toto(avatar, 460.dp)
            Spacer(Modifier.width(72.dp))
            Column(Modifier.weight(1f)) {
                NameAndStage(avatar)
                Spacer(Modifier.height(40.dp))
                GrowthBar(avatar.stage, growthFraction(avatar.stage, avatar.growth))
                Spacer(Modifier.height(48.dp))
                TendencyBars(avatar.tendency)
                if (avatar.unopenedBox > 0) {
                    Spacer(Modifier.height(40.dp))
                    BoxCount(avatar.unopenedBox)
                }
            }
        }
    }
}

/** ③ 주행 중: 멈춘 토토 + 한 줄만. 애니메이션·숫자·소리 없음. */
@Composable
private fun DrivingScreen(avatar: AvatarState, line: String) {
    Box(Modifier.fillMaxSize().background(Night), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Toto(avatar, 280.dp)
            Spacer(Modifier.width(56.dp))
            Column {
                Text("조용히 기록하고 있어요", fontSize = 44.sp, color = Color(0xFFDADCE2))
                if (line.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(line, fontSize = 26.sp, color = Soft)
                }
            }
        }
    }
}

/** ④ 성장 공개: 오늘 +N, 막대 차오름, 성향, 상자 개수, 대사. 단계가 오르면 새 그림. */
@Composable
private fun RevealScreen(ui: UiState, line: String, onClose: () -> Unit) {
    val avatar = ui.avatar
    val reveal = avatar.lastReveal
    val bar = remember { Animatable(growthFraction(avatar.stage, ui.growthBefore)) }
    LaunchedEffect(avatar) {
        bar.animateTo(growthFraction(avatar.stage, avatar.growth), tween(1200))
    }
    Backdrop("bg_reveal", Paper) {
        Row(Modifier.fillMaxSize().padding(64.dp), verticalAlignment = Alignment.CenterVertically) {
            Toto(avatar, 460.dp)
            Spacer(Modifier.width(72.dp))
            Column(Modifier.weight(1f)) {
                if (reveal.stageUp) {
                    Text("${avatar.name}가 ${avatar.stage.stageLabel}이 됐어요!", fontSize = 44.sp,
                        fontWeight = FontWeight.Bold, color = Accent)
                    Spacer(Modifier.height(8.dp))
                }
                Text("오늘 성장 +${reveal.gained}!", fontSize = 60.sp, fontWeight = FontWeight.Bold, color = Ink)
                if (line.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("“$line”", fontSize = 28.sp, color = Ink)
                }
                Spacer(Modifier.height(32.dp))
                GrowthBar(avatar.stage, bar.value)
                Spacer(Modifier.height(32.dp))
                TendencyBars(avatar.tendency)
                Spacer(Modifier.height(32.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BoxCount(avatar.unopenedBox)
                    Spacer(Modifier.weight(1f))
                    Button(onClick = onClose, modifier = Modifier.height(72.dp).width(220.dp)) {
                        Text("확인", fontSize = 28.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun NameAndStage(avatar: AvatarState) {
    Text(avatar.name, fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Ink)
    Text(avatar.stage.stageLabel, fontSize = 32.sp, color = Soft)
}

@Composable
private fun GrowthBar(stage: Int, fraction: Float) {
    Text(if (stage >= AvatarState.MAX_STAGE) "다 자랐어요" else "${(stage + 1).stageLabel}까지",
        fontSize = 24.sp, color = Soft)
    Spacer(Modifier.height(10.dp))
    LinearProgressIndicator(
        progress = { fraction },
        modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(10.dp)),
        color = Accent,
        trackColor = Track,
    )
}

private fun growthFraction(stage: Int, growth: Int): Float =
    if (stage >= AvatarState.MAX_STAGE) 1f
    else (growth.toFloat() / AvatarState.STAGE_GOAL).coerceIn(0f, 1f)

@Composable
private fun TendencyBars(t: Tendency) {
    val rows = listOf("도시형" to t.city, "모험형" to t.adventure, "감성형" to t.emotional, "안정형" to t.stable)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { (label, value) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, fontSize = 22.sp, color = Ink, modifier = Modifier.width(110.dp))
                LinearProgressIndicator(
                    progress = { value / 100f },
                    modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(6.dp)),
                    color = Ink,
                    trackColor = Track,
                )
                Text("$value%", fontSize = 22.sp, color = Soft,
                    modifier = Modifier.width(90.dp).padding(start = 16.dp))
            }
        }
    }
}

@Composable
private fun BoxCount(count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        GiftBox(Modifier.size(64.dp))
        Spacer(Modifier.width(16.dp))
        Text("안 연 상자 ${count}개", fontSize = 28.sp, color = Ink)
    }
}

// ---------- 이미지 겹치기 (디자인 파일이 있으면 그 이미지, 없으면 임시 그림) ----------

@SuppressLint("DiscouragedApi")
private fun Context.drawableId(name: String): Int =
    resources.getIdentifier(name, "drawable", packageName)

/** 배경: res/drawable/bg_default.png, bg_reveal.png 가 있으면 깔고, 없으면 단색 */
@Composable
private fun Backdrop(name: String, fallback: Color, content: @Composable BoxScope.() -> Unit) {
    val id = LocalContext.current.drawableId(name)
    Box(Modifier.fillMaxSize().background(fallback)) {
        if (id != 0) {
            Image(painterResource(id), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        content()
    }
}

/**
 * 토토 그리기 (디자인 1 item_placement_guide 기준)
 * - 몸: avatar_stage{stage}.png, 1024×1024 캔버스. 3단계 모두 발 위치가 같다.
 * - 머리 파츠: part_{equipped.head}.png (알파 영역만 잘라낸 이미지)를
 *   assets/avatar/placements.json 의 단계별 사각형(1024 기준 x, y, width, height)에 그린다.
 * - 캔버스 여백이 커서 토토가 작아 보이므로, 모든 단계·파츠를 포함하는 [VIEW] 영역만 확대해 보여준다.
 */
@Composable
fun Toto(avatar: AvatarState, size: Dp) {
    val context = LocalContext.current
    val body = context.drawableId("avatar_stage${avatar.stage}")
    if (body == 0) {
        Box(Modifier.size(size)) { PlaceholderToto(avatar.stage) }
        return
    }
    val placements = remember { AvatarPlacements.load(context) }
    val unit = size / VIEW_SIZE  // 1024 캔버스의 1px 이 화면에서 차지하는 크기
    Box(Modifier.size(size).clipToBounds()) {
        Box(
            Modifier
                .wrapContentSize(Alignment.TopStart, unbounded = true)
                .offset(-unit * VIEW_X, -unit * VIEW_Y)
                .requiredSize(unit * 1024f)
        ) {
            Image(painterResource(body), "토토 ${avatar.stage.stageLabel}", Modifier.fillMaxSize())
            val head = avatar.equipped.head
            val rect = head?.let { placements["stage${avatar.stage}"]?.get(it) }
            val part = head?.let { context.drawableId("part_$it") } ?: 0
            if (rect != null && part != 0) {
                Image(
                    painterResource(part), null,
                    Modifier.offset(unit * rect.x, unit * rect.y).requiredSize(unit * rect.w, unit * rect.h),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}

/** 1024 캔버스에서 보여줄 영역: 성인+헬멧(y 278)부터 발(y 1000)까지, 가운데 정렬 */
private const val VIEW_X = 137f
private const val VIEW_Y = 262f
private const val VIEW_SIZE = 750f

private data class PartRect(val x: Float, val y: Float, val w: Float, val h: Float)

private object AvatarPlacements {
    fun load(context: Context): Map<String, Map<String, PartRect>> = runCatching {
        val obj = JSONObject(context.assets.open("avatar/placements.json").bufferedReader().use { it.readText() })
        obj.keys().asSequence().associateWith { stage ->
            val items = obj.getJSONObject(stage)
            items.keys().asSequence().associateWith { item ->
                val r = items.getJSONObject(item)
                PartRect(
                    r.getDouble("x").toFloat(), r.getDouble("y").toFloat(),
                    r.getDouble("width").toFloat(), r.getDouble("height").toFloat(),
                )
            }
        }
    }.getOrDefault(emptyMap())
}

@Composable
private fun PlaceholderToto(stage: Int) {
    val scale = when (stage) { 0 -> 0.62f; 1 -> 0.8f; else -> 0.95f }
    Canvas(Modifier.fillMaxSize()) {
        val r = size.minDimension / 2 * scale
        val c = Offset(center.x, center.y + (size.minDimension / 2 - r) * 0.6f)
        drawCircle(Color(0xFFFFD9A8), r, c)
        drawCircle(Ink, r * 0.09f, Offset(c.x - r * 0.35f, c.y - r * 0.1f))
        drawCircle(Ink, r * 0.09f, Offset(c.x + r * 0.35f, c.y - r * 0.1f))
        drawCircle(Color(0xFFFF9E9E), r * 0.1f, Offset(c.x - r * 0.55f, c.y + r * 0.2f))
        drawCircle(Color(0xFFFF9E9E), r * 0.1f, Offset(c.x + r * 0.55f, c.y + r * 0.2f))
    }
}

@Composable
private fun GiftBox(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        drawRect(Accent, Offset(w * 0.08f, w * 0.38f), Size(w * 0.84f, w * 0.6f))
        drawRect(Color(0xFFFFB37A), Offset(0f, w * 0.22f), Size(w, w * 0.2f))
        drawRect(Color(0xFFFFE2C7), Offset(w * 0.44f, w * 0.22f), Size(w * 0.12f, w * 0.76f))
    }
}
