package sn.mo.ussdapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.delay
import sn.mo.ussdapp.data.Operator
import sn.mo.ussdapp.data.Operators
import sn.mo.ussdapp.data.UssdService

private val AppTypography = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp, letterSpacing = 0.2.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 13.sp),
    labelSmall = TextStyle(fontSize = 12.sp)
)

private val BackgroundGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFFEADB), Color(0xFFFFF8F1), Color(0xFFE1F6FF)),
    start = Offset(0f, 0f),
    end = Offset(1000f, 1400f)
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(typography = AppTypography) {
                AppRoot(onServiceClick = { service -> openDialer(service.ussdCode) })
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
fun AppRoot(onServiceClick: (UssdService) -> Unit) {
    var selectedOperator by remember { mutableStateOf<Operator?>(null) }
    val context = LocalContext.current
    var backPressedOnce by remember { mutableStateOf(false) }

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
            .background(BackgroundGradient)
            .statusBarsPadding()
    ) {
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
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppLogo()
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Mes services SIM", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Choisis un operateur",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
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
            .background(Color.White)
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
            Text(operator.label, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(2.dp))
            Text(
                "${operator.services.size} services",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ServiceListScreen(operator: Operator, onBack: () -> Unit, onServiceClick: (UssdService) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
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
        Text(operator.label, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(operator.services) { service ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .clickable { onServiceClick(service) }
                        .padding(vertical = 20.dp, horizontal = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(service.name, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
