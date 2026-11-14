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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carsona.app.model.AvatarState
import com.carsona.app.model.Tendency
import com.carsona.app.model.stageLabel
import org.json.JSONObject

// 밤하늘 배경(디자인 2)에 맞춘 밝은 글자 · 어두운 패널
private val Light = Color(0xFFF6F8FF)
private val Muted = Color(0xFFA9BDE6)
private val NightSky = Color(0xFF06143F)
private val Panel = Color(0xFF0C2160)
private val Track = Color(0xFF24407F)
private val Line = Color(0xFF1A1A1A)
private val Accent = Color(0xFFFF8A3D)
private val Leaf = Color(0xFFA8E07A)

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
    /** 꾸미기: 머리 파츠 쓰기 (null = 벗기) */
    onEquip: (String?) -> Unit = {},
    /** 시연용 "처음부터" 버튼 (시나리오 버튼 옆). null 이면 숨김 */
    onReset: (() -> Unit)? = null,
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
            Screen.HOME -> HomeScreen(ui.avatar, onPlayScenario, onEquip, onReset)
            Screen.DRIVING -> DrivingScreen(ui.avatar, line)
            Screen.REVEAL -> {
                LaunchedEffect(ui.avatar.lastReveal) { onSpeak(line) }
                RevealScreen(ui, line, onCloseReveal)
            }
        }
    }
}

// ---------- 무대: 배경 1700×1200 좌표계 (디자인 2 backgrounds.json) ----------

/** 배경 좌표(1700×1200)를 화면 dp로 바꿔 주는 무대. 배경은 가운데 기준 꽉 채우기(centerCrop). */
private class StageScope(val unit: Float, val offX: Float, val offY: Float) {
    fun x(v: Float): Dp = (offX + v * unit).dp
    fun y(v: Float): Dp = (offY + v * unit).dp
    fun len(v: Float): Dp = (v * unit).dp

    /** 배경 좌표의 사각형에 놓기 */
    fun Modifier.place(x: Float, y: Float, w: Float, h: Float): Modifier =
        this.offset(x(x), y(y)).requiredSize(len(w), len(h))
}

private const val BG_W = 1700f
private const val BG_H = 1200f

/** 아바타 1024 캔버스를 1.1배로 (287, 41)에 그리면 발이 언덕 위(y 1140), 가운데(x 850)에 선다 */
private const val AVATAR_X = 287f
private const val AVATAR_Y = 41f
private const val AVATAR_SCALE = 1.1f

/** 오른쪽 정보 패널 (토토 가로 범위 약 600~1100 바깥) */
private const val PANEL_X = 1180f
private const val PANEL_Y = 350f   // 달(1420, 210, 빛 반지름 120) 아래
private const val PANEL_W = 480f

@Composable
private fun Stage(bg: String, content: @Composable StageScope.() -> Unit) {
    val id = LocalContext.current.drawableId(bg)
    BoxWithConstraints(Modifier.fillMaxSize().background(NightSky).clipToBounds()) {
        val w = maxWidth.value
        val h = maxHeight.value
        val unit = maxOf(w / BG_W, h / BG_H)
        val scope = StageScope(unit, (w - BG_W * unit) / 2, (h - BG_H * unit) / 2)
        if (id != 0) {
            Image(
                painterResource(id), null,
                Modifier.wrapContentSize(Alignment.TopStart, unbounded = true)
                    .offset(scope.offX.dp, scope.offY.dp)
                    .requiredSize(scope.len(BG_W), scope.len(BG_H)),
                contentScale = ContentScale.FillBounds,
            )
        }
        Box(Modifier.fillMaxSize().wrapContentSize(Alignment.TopStart, unbounded = true)) {
            scope.content()
        }
    }
}

/** 토토(몸 + 머리 파츠)를 배경 위 정해진 자리에 그린다 */
@Composable
private fun StageScope.StageToto(avatar: AvatarState) {
    val canvas = 1024f * AVATAR_SCALE
    Box(Modifier.place(AVATAR_X, AVATAR_Y, canvas, canvas)) {
        TotoCanvas(avatar, len(canvas))
    }
}

/** 머리 꼭대기 y (배경 좌표). 말풍선 위치용. 디자인 1 validation.json alpha_bbox 기준 */
private fun headTopY(stage: Int): Float {
    val canvasTop = when (stage) { 0 -> 602f; 1 -> 495f; else -> 374f }
    return AVATAR_Y + canvasTop * AVATAR_SCALE
}

@Composable
private fun StageScope.SpeechBubble(text: String, stage: Int, wearingHead: Boolean = false) {
    if (text.isBlank()) return
    // 머리 파츠(헬멧·새싹)는 머리 위로 약 90px(캔버스) 올라오므로 그만큼 말풍선을 올린다
    val bottom = headTopY(stage) - 24f - if (wearingHead) 100f else 0f
    Box(
        Modifier.place(250f, bottom - 140f, 1200f, 140f),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            text,
            fontSize = 26.sp, color = Line, textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(3.dp, Line, RoundedCornerShape(24.dp))
                .padding(horizontal = 28.dp, vertical = 14.dp),
        )
    }
}

@Composable
private fun StageScope.NameTag(avatar: AvatarState) {
    Row(
        Modifier
            .offset(x(40f), y(36f))
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .border(3.dp, Line, RoundedCornerShape(50))
            .padding(horizontal = 26.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(avatar.name.ifBlank { "토토" }, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Line)
        Spacer(Modifier.width(12.dp))
        Text(avatar.stage.stageLabel, fontSize = 22.sp, color = Color(0xFF40507A))
    }
}

@Composable
private fun StageScope.InfoPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .offset(x(PANEL_X), y(PANEL_Y))
            .width(len(PANEL_W))
            .clip(RoundedCornerShape(28.dp))
            .background(Panel.copy(alpha = 0.88f))
            .border(2.dp, Track, RoundedCornerShape(28.dp))
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) { content() }
}

// ---------- 화면 4개 ----------

/** ① 첫 만남: 아기 토토가 태어나고 이름을 짓는다 (정차 중) */
@Composable
private fun FirstMeetScreen(line: String, onName: (String) -> Unit) {
    var name by remember { mutableStateOf("토토") }
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    // 이름을 지으면 키보드를 닫고 홈으로
    val submit = {
        keyboard?.hide()
        focus.clearFocus()
        onName(name)
    }
    Stage("bg_default") {
        StageToto(AvatarState())
        SpeechBubble(line, stage = 0)
        InfoPanel {
            Text("이름 짓기", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Light)
            Text("가족이 함께 불러 줄 이름을 지어 주세요", fontSize = 20.sp, color = Muted)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(10) },
                singleLine = true,
                textStyle = TextStyle(fontSize = 30.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Light, unfocusedTextColor = Light, cursorColor = Light,
                    focusedBorderColor = Leaf, unfocusedBorderColor = Muted,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = submit,
                colors = ButtonDefaults.buttonColors(containerColor = Leaf, contentColor = Line),
                modifier = Modifier.fillMaxWidth().height(72.dp),
            ) { Text("이름 짓기", fontSize = 26.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

/** ② 홈: 토토 + 이름표 + 성장 막대 + 성향 4개 (기어 P일 때 기본 화면) */
@Composable
private fun HomeScreen(
    avatar: AvatarState,
    onPlayScenario: (() -> Unit)?,
    onEquip: (String?) -> Unit,
    onReset: (() -> Unit)?,
) {
    Stage("bg_default") {
        StageToto(avatar)
        NameTag(avatar)
        InfoPanel {
            GrowthBar(avatar.stage, growthFraction(avatar.stage, avatar.growth))
            Spacer(Modifier.height(8.dp))
            Text("우리 차 성향", fontSize = 22.sp, color = Muted)
            TendencyBars(avatar.tendency)
            Spacer(Modifier.height(8.dp))
            BoxCount(avatar.unopenedBox)
            Spacer(Modifier.height(8.dp))
            Text("꾸미기", fontSize = 22.sp, color = Muted)
            DressUp(avatar.equipped.head, onEquip)
        }
        if (onPlayScenario != null) {
            TextButton(
                onClick = onPlayScenario,
                modifier = Modifier.offset(x(1440f), y(36f)),
            ) { Text("시나리오 재생", fontSize = 20.sp, color = Muted) }
        }
        if (onReset != null) {
            // 실수로 눌러도 저장된 토토가 지워지지 않게 한 번 더 묻는다
            var askReset by remember { mutableStateOf(false) }
            TextButton(
                onClick = { askReset = true },
                modifier = Modifier.offset(x(1270f), y(36f)),
            ) { Text("처음부터", fontSize = 20.sp, color = Muted) }
            if (askReset) {
                AlertDialog(
                    onDismissRequest = { askReset = false },
                    title = { Text("처음부터 다시 할까요?", fontSize = 26.sp) },
                    text = { Text("${avatar.name.withSubject()} 아기로 돌아가고, 지금까지의 성장은 지워져요.", fontSize = 20.sp) },
                    confirmButton = {
                        TextButton(onClick = { askReset = false; onReset() }) { Text("처음부터", fontSize = 20.sp) }
                    },
                    dismissButton = {
                        TextButton(onClick = { askReset = false }) { Text("취소", fontSize = 20.sp) }
                    },
                )
            }
        }
    }
}

/** ③ 주행 중: 멈춘 토토 + 한 줄만. 애니메이션·숫자·소리 없음. */
@Composable
private fun DrivingScreen(avatar: AvatarState, line: String) {
    Stage("bg_default") {
        StageToto(avatar)
        Box(Modifier.place(0f, 0f, BG_W, BG_H).background(NightSky.copy(alpha = 0.72f)))
        Column(
            Modifier.place(0f, 120f, BG_W, 220f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("조용히 기록하고 있어요", fontSize = 44.sp, color = Light)
            if (line.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text(line, fontSize = 24.sp, color = Muted)
            }
        }
    }
}

/** ④ 성장 공개: 오늘 +N, 말풍선 대사, 막대 차오름, 성향, 상자 개수. 단계가 오르면 새 그림. */
@Composable
private fun RevealScreen(ui: UiState, line: String, onClose: () -> Unit) {
    val avatar = ui.avatar
    val reveal = avatar.lastReveal
    val bar = remember { Animatable(growthFraction(avatar.stage, ui.growthBefore)) }
    LaunchedEffect(avatar) {
        bar.animateTo(growthFraction(avatar.stage, avatar.growth), tween(1200))
    }
    Stage("bg_reveal") {
        StageToto(avatar)
        SpeechBubble(line, avatar.stage, wearingHead = avatar.equipped.head != null)
        Column(
            Modifier.place(150f, 30f, 1000f, 190f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (reveal.stageUp) {
                val name = avatar.name.ifBlank { "토토" }
                Text("${name.withSubject()} ${avatar.stage.stageLabel}이 됐어요!", fontSize = 36.sp,
                    fontWeight = FontWeight.Bold, color = Accent)
            }
            Text("오늘 성장 +${reveal.gained}!", fontSize = 60.sp, fontWeight = FontWeight.Bold, color = Light)
        }
        InfoPanel {
            GrowthBar(avatar.stage, bar.value)
            Spacer(Modifier.height(8.dp))
            Text("우리 차 성향", fontSize = 22.sp, color = Muted)
            TendencyBars(avatar.tendency)
            Spacer(Modifier.height(8.dp))
            BoxCount(avatar.unopenedBox)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Leaf, contentColor = Line),
                modifier = Modifier.fillMaxWidth().height(72.dp),
            ) { Text("확인", fontSize = 26.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

// ---------- 패널 부품 ----------

@Composable
private fun GrowthBar(stage: Int, fraction: Float) {
    Text(if (stage >= AvatarState.MAX_STAGE) "다 자랐어요" else "${(stage + 1).stageLabel}까지",
        fontSize = 22.sp, color = Muted)
    LinearProgressIndicator(
        progress = { fraction },
        modifier = Modifier.fillMaxWidth().height(18.dp).clip(RoundedCornerShape(9.dp)),
        color = Accent,
        trackColor = Track,
    )
}

private fun growthFraction(stage: Int, growth: Int): Float =
    if (stage >= AvatarState.MAX_STAGE) 1f
    else (growth.toFloat() / AvatarState.STAGE_GOAL).coerceIn(0f, 1f)

/** 이름 뒤 주격 조사: 받침이 있으면 "이", 없으면 "가" (예: 토토가, 별이) */
private fun String.withSubject(): String {
    val last = lastOrNull() ?: return this
    val hasFinal = last in '가'..'힣' && (last.code - 0xAC00) % 28 != 0
    return this + if (hasFinal) "이" else "가"
}

@Composable
private fun TendencyBars(t: Tendency) {
    val rows = listOf("도시형" to t.city, "모험형" to t.adventure, "감성형" to t.emotional, "안정형" to t.stable)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { (label, value) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, fontSize = 20.sp, color = Light, modifier = Modifier.width(84.dp))
                LinearProgressIndicator(
                    progress = { value / 100f },
                    modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = Leaf,
                    trackColor = Track,
                )
                Text("$value%", fontSize = 20.sp, color = Muted, textAlign = TextAlign.End,
                    modifier = Modifier.width(72.dp))
            }
        }
    }
}

/** 머리 파츠 고르기 (디자인 1 파츠: helmet, leaf). 고른 것은 밝은 초록으로 표시 */
private val HEAD_PARTS = listOf(null to "없음", "helmet" to "헬멧", "leaf" to "새싹")

@Composable
private fun DressUp(current: String?, onEquip: (String?) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        HEAD_PARTS.forEach { (id, label) ->
            val selected = current == id
            Button(
                onClick = { onEquip(id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) Leaf else Track,
                    contentColor = if (selected) Line else Light,
                ),
                modifier = Modifier.weight(1f).height(56.dp),
            ) { Text(label, fontSize = 20.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }
        }
    }
}

@Composable
private fun BoxCount(count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        GiftBox(Modifier.size(48.dp))
        Spacer(Modifier.width(14.dp))
        Text(if (count > 0) "안 연 상자 ${count}개" else "아직 상자가 없어요", fontSize = 22.sp, color = Light)
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

// ---------- 토토 (디자인 1 이미지 겹치기) ----------

@SuppressLint("DiscouragedApi")
private fun Context.drawableId(name: String): Int =
    resources.getIdentifier(name, "drawable", packageName)

/**
 * 토토 1024×1024 캔버스를 [size] 크기로 그린다.
 * - 몸: avatar_stage{stage}.png (3단계 모두 발 위치 같음)
 * - 머리 파츠: part_{equipped.head}.png 를 assets/avatar/placements.json 의 단계별 사각형에 그린다.
 * - 이미지가 없으면 임시 그림.
 */
@Composable
fun TotoCanvas(avatar: AvatarState, size: Dp) {
    val context = LocalContext.current
    val body = context.drawableId("avatar_stage${avatar.stage}")
    Box(Modifier.size(size)) {
        if (body == 0) {
            PlaceholderToto(avatar.stage)
            return@Box
        }
        val placements = remember { AvatarPlacements.load(context) }
        val unit = size / 1024f
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
    val scale = when (stage) { 0 -> 0.4f; 1 -> 0.5f; else -> 0.6f }
    Canvas(Modifier.fillMaxSize()) {
        val r = size.minDimension / 2 * scale * 0.6f
        val c = Offset(center.x, size.height * 0.976f - r)
        drawCircle(Color(0xFFF2ECE2), r, c)
        drawCircle(Line, r * 0.09f, Offset(c.x - r * 0.35f, c.y - r * 0.1f))
        drawCircle(Line, r * 0.09f, Offset(c.x + r * 0.35f, c.y - r * 0.1f))
    }
}
