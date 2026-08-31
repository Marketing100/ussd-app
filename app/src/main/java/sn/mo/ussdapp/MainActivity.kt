package sn.mo.ussdapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import sn.mo.ussdapp.data.Operator
import sn.mo.ussdapp.data.Operators
import sn.mo.ussdapp.data.UssdService
import java.net.HttpURLConnection
import java.net.URL

const val CURRENT_VERSION = "2"
const val DOWNLOAD_LINK = "https://github.com/Marketing100/ussd-app/releases/latest/download/app-debug.apk"

enum class ThemeMode { LIGHT, DARK, AUTO }
enum class TextSize(val scale: Float) { SMALL(0.85f), NORMAL(1f), LARGE(1.15f) }

private fun buildTypography(scale: Float) = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = (26 * scale).sp, letterSpacing = 0.2.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = (20 * scale).sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = (17 * scale).sp),
    bodyMedium = TextStyle(fontSize = (14 * scale).sp),
    bodySmall = TextStyle(fontSize = (13 * scale).sp),
    labelSmall = TextStyle(fontSize = (12 * scale).sp)
)

private val LightGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFFEADB), Color(0xFFFFF8F1), Color(0xFFE1F6FF)),
    start = Offset(0f, 0f),
    end = Offset(1000f, 1400f)
)
private val DarkGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF14161B), Color(0xFF1B1E23)),
    start = Offset(0f, 0f),
    end = Offset(1000f, 1400f)
)

suspend fun checkForUpdate(): String? = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.github.com/repos/Marketing100/ussd-app/releases/latest")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        if (connection.responseCode != 200) return@withContext null

        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(body)
        val tag = json.optString("tag_name")
        val htmlUrl = json.optString("html_url")

        if (tag.isNotEmpty() && tag != CURRENT_VERSION) htmlUrl else null
    } catch (e: Exception) {
        null
    }
}

fun loadThemeMode(context: Context): ThemeMode {
    val prefs = context.getSharedPreferences("simeclair_settings", Context.MODE_PRIVATE)
    return ThemeMode.values()[prefs.getInt("theme_mode", ThemeMode.AUTO.ordinal)]
}

fun saveThemeMode(context: Context, mode: ThemeMode) {
    context.getSharedPreferences("simeclair_settings", Context.MODE_PRIVATE)
        .edit().putInt("theme_mode", mode.ordinal).apply()
}

fun loadTextSize(context: Context): TextSize {
    val prefs = context.getSharedPreferences("simeclair_settings", Context.MODE_PRIVATE)
    return TextSize.values()[prefs.getInt("text_size", TextSize.NORMAL.ordinal)]
}

fun saveTextSize(context: Context, size: TextSize) {
    context.getSharedPreferences("simeclair_settings", Context.MODE_PRIVATE)
        .edit().putInt("text_size", size.ordinal).apply()
}

fun shareApp(context: Context) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Telecharge SimEclair, l'appli des codes USSD : $DOWNLOAD_LINK")
    }
    context.startActivity(Intent.createChooser(sendIntent, "Partager SimEclair"))
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var themeMode by remember { mutableStateOf(loadThemeMode(this)) }
            var textSize by remember { mutableStateOf(loadTextSize(this)) }
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.AUTO -> systemDark
            }

            val colorScheme = if (isDark) {
                darkColorScheme(
                    primary = Color(0xFFFF8A33),
                    background = Color(0xFF14161B),
                    surface = Color(0xFF1E2128),
                    onSurface = Color.White,
                    onSurfaceVariant = Color(0xFFAAAAAA)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFFFF6600),
                    background = Color(0xFFFFF8F1),
                    surface = Color.White,
                    onSurface = Color(0xFF1B1E23),
                    onSurfaceVariant = Color(0xFF7A8290)
                )
            }

            MaterialTheme(colorScheme = colorScheme, typography = buildTypography(textSize.scale)) {
                AppRoot(
                    isDark = isDark,
                    themeMode = themeMode,
                    textSize = textSize,
                    onThemeModeChange = { mode ->
                        themeMode = mode
                        saveThemeMode(this, mode)
                    },
                    onTextSizeChange = { size ->
                        textSize = size
                        saveTextSize(this, size)
                    },
                    onServiceClick = { service -> openDialer(service.ussdCode) }
                )
            }
        }
    }

    private fun openDialer(ussdCode: String) {
        val encoded = Uri.encode(ussdCode)
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$encoded"))
        startActivity(intent)
    }
}

@Composable
fun AppRoot(
    isDark: Boolean,
    themeMode: ThemeMode,
    textSize: TextSize,
    onThemeModeChange: (ThemeMode) -> Unit,
    onTextSizeChange: (TextSize) -> Unit,
    onServiceClick: (UssdService) -> Unit
) {
    var selectedOperator by remember { mutableStateOf<Operator?>(null) }
    var showOptions by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var backPressedOnce by remember { mutableStateOf(false) }
    var updateUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        updateUrl = checkForUpdate()
    }

    BackHandler(enabled = selectedOperator != null) {
        selectedOperator = null
    }

    BackHandler(enabled = selectedOperator == null) {
        if (backPressedOnce) {
            (context as? Activity)?.finish()
        } else {
            backPressedOnce = true
            Toast.makeText(context, "Appuie de nouveau pour quitter", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(backPressedOnce) {
        if (backPressedOnce) {
            delay(2000)
            backPressedOnce = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkGradient else LightGradient)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RoundIconButton(onClick = { shareApp(context) }) {
                    ShareIcon(tint = MaterialTheme.colorScheme.onSurface)
                }
                RoundIconButton(onClick = { showOptions = true }) {
                    Text("\u2699\uFE0F", fontSize = 18.sp)
                }
            }

            updateUrl?.let { url ->
                UpdateBanner(
                    onUpdateClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                )
            }

            if (selectedOperator == null) {
                HomeScreen(onOperatorClick = { selectedOperator = it })
            } else {
                ServiceListScreen(
                    operator = selectedOperator!!,
                    onBack = { selectedOperator = null },
                    onServiceClick = onServiceClick
                )
            }
        }

        if (showOptions) {
            OptionsDialog(
                themeMode = themeMode,
                textSize = textSize,
                onThemeModeChange = onThemeModeChange,
                onTextSizeChange = onTextSizeChange,
                onDismiss = { showOptions = false }
            )
        }
    }
}

@Composable
fun RoundIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun ShareIcon(tint: Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val u = size.width / 24f
        val strokeW = 1.6f * u
        val a = Offset(6f * u, 12f * u)
        val b = Offset(18f * u, 6f * u)
        val c = Offset(18f * u, 18f * u)
        drawLine(tint, a, b, strokeWidth = strokeW)
        drawLine(tint, a, c, strokeWidth = strokeW)
        val r = 2.4f * u
        drawCircle(tint, r, a)
        drawCircle(tint, r, b)
        drawCircle(tint, r, c)
    }
}

@Composable
fun OptionsDialog(
    themeMode: ThemeMode,
    textSize: TextSize,
    onThemeModeChange: (ThemeMode) -> Unit,
    onTextSizeChange: (TextSize) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        },
        title = { Text("Options") },
        text = {
            Column {
                Text("Theme", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OptionChip("Clair", themeMode == ThemeMode.LIGHT) { onThemeModeChange(ThemeMode.LIGHT) }
                    OptionChip("Sombre", themeMode == ThemeMode.DARK) { onThemeModeChange(ThemeMode.DARK) }
                    OptionChip("Auto", themeMode == ThemeMode.AUTO) { onThemeModeChange(ThemeMode.AUTO) }
                }
                Spacer(Modifier.height(20.dp))
                Text("Taille du texte", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OptionChip("Petit", textSize == TextSize.SMALL) { onTextSizeChange(TextSize.SMALL) }
                    OptionChip("Normal", textSize == TextSize.NORMAL) { onTextSizeChange(TextSize.NORMAL) }
                    OptionChip("Grand", textSize == TextSize.LARGE) { onTextSizeChange(TextSize.LARGE) }
                }
            }
        }
    )
}

@Composable
fun OptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, color = fg, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun UpdateBanner(onUpdateClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1B1E23))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            "Nouvelle version disponible",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onUpdateClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6600))
        ) {
            Text("Mettre a jour")
        }
    }
}

@Composable
fun AppLogo(size: androidx.compose.ui.unit.Dp = 56.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFFFF6600), Color(0xFF1DC8F2)))),
        contentAlignment = Alignment.Center
    ) {
        Text("\uD83D\uDCF2", fontSize = (size.value * 0.5).sp)
    }
}

@Composable
fun HomeScreen(onOperatorClick: (Operator) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppLogo()
            Spacer(Modifier.width(14.dp))
            Column {
                Text("SimEclair", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Choisis un operateur",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(Operators.ALL) { operator ->
                OperatorCard(operator, onClick = { onOperatorClick(operator) })
            }
        }
    }
}

@Composable
fun OperatorCard(operator: Operator, onClick: () -> Unit) {
    val accent = Color(operator.accentColor)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.6f)))),
            contentAlignment = Alignment.Center
        ) {
            Text("\uD83D\uDCF6", fontSize = 24.sp)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(operator.label, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(
                "${operator.services.size} services",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ServiceListScreen(operator: Operator, onBack: () -> Unit, onServiceClick: (UssdService) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onBack)
                .padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Text(
                "‹",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Operateurs",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(operator.label, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(operator.services) { service ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onServiceClick(service) }
                        .padding(vertical = 20.dp, horizontal = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(service.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
