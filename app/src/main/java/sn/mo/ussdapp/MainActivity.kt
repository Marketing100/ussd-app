package sn.mo.ussdapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import sn.mo.ussdapp.data.Operator
import sn.mo.ussdapp.data.Operators
import sn.mo.ussdapp.data.SimSlot
import sn.mo.ussdapp.data.UssdDialer
import sn.mo.ussdapp.data.UssdService

class MainActivity : ComponentActivity() {

    private lateinit var dialer: UssdDialer
    private var permissionsGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialer = UssdDialer(this)

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            permissionsGranted = result.values.all { it }
        }

        setContent {
            MaterialTheme {
                AppRoot(
                    dialer = dialer,
                    permissionsGranted = permissionsGranted,
                    onRequestPermissions = { permissionLauncher.launch(UssdDialer.REQUIRED_PERMISSIONS) }
                )
            }
        }
    }
}

@Composable
fun AppRoot(
    dialer: UssdDialer,
    permissionsGranted: Boolean,
    onRequestPermissions: () -> Unit
) {
    var selectedOperator by remember { mutableStateOf<Operator?>(null) }
    var pendingService by remember { mutableStateOf<UssdService?>(null) }

    if (!permissionsGranted) {
        PermissionScreen(onRequestPermissions)
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedOperator == null) {
            HomeScreen(onOperatorClick = { selectedOperator = it })
        } else {
            ServiceListScreen(
                operator = selectedOperator!!,
                onBack = { selectedOperator = null },
                onServiceClick = { pendingService = it }
            )
        }

        pendingService?.let { service ->
            val sims = remember { dialer.listActiveSims() }
            SimPickerSheet(
                service = service,
                sims = sims,
                onDismiss = { pendingService = null },
                onSimChosen = { sim ->
                    dialer.dial(service.ussdCode, sim)
                    pendingService = null
                }
            )
        }
    }
}

@Composable
fun PermissionScreen(onRequestPermissions: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Autorisation necessaire",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "L'appli a besoin d'acceder aux appels et aux cartes SIM pour lancer les services directement sur la bonne SIM.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRequestPermissions) {
            Text("Autoriser")
        }
    }
}

@Composable
fun HomeScreen(onOperatorClick: (Operator) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Mes services SIM", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "Choisis un operateur pour lancer un service",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(Operators.ALL.chunked(2)) { rowOperators ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowOperators.forEach { operator ->
                        OperatorCard(operator, modifier = Modifier.weight(1f), onClick = { onOperatorClick(operator) })
                    }
                    if (rowOperators.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun OperatorCard(operator: Operator, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val accent = Color(operator.accentColor)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF3F4F6))
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(accent))
        Column(modifier = Modifier.padding(16.dp)) {
            Text(operator.label, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                "${operator.services.size} services",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ServiceListScreen(operator: Operator, onBack: () -> Unit, onServiceClick: (UssdService) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(
            "‹ Operateurs",
            modifier = Modifier.clickable(onClick = onBack),
            color = Color.Gray
        )
        Spacer(Modifier.height(12.dp))
        Text(operator.label, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(operator.services) { service ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .clickable { onServiceClick(service) }
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(service.name)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimPickerSheet(
    service: UssdService,
    sims: List<SimSlot>,
    onDismiss: () -> Unit,
    onSimChosen: (SimSlot) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Lancer", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(service.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Text("Quelle carte SIM ?", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(Modifier.height(8.dp))

            if (sims.isEmpty()) {
                Text(
                    "Aucune SIM detectee. Verifie que les permissions sont accordees et qu'au moins une SIM est active.",
                    color = Color.Gray
                )
            }

            sims.forEach { sim ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .clickable { onSimChosen(sim) }
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Text(sim.displayName)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
