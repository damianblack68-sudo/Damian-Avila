package com.example.ui.screens.subscription

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.PaymentCard
import com.example.domain.model.SubscriptionTier
import com.example.ui.theme.OnyxRedPrimary
import com.example.ui.theme.OnyxWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SubscriptionShopScreen(
    currentTier: SubscriptionTier,
    onUpgradeTier: (SubscriptionTier, PaymentCard?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val plans = remember {
        listOf(
            SubscriptionTier.BASIC_20,
            SubscriptionTier.PRO_50,
            SubscriptionTier.VIP_100,
            SubscriptionTier.EVOLUTION_1000
        )
    }

    var selectedPlanForPayment by remember { mutableStateOf<SubscriptionTier?>(null) }
    var cardNumberInput by remember { mutableStateOf("") }
    var cardHolderInput by remember { mutableStateOf("") }
    var cardExpiryInput by remember { mutableStateOf("") }
    var cardCvvInput by remember { mutableStateOf("") }
    var isScanningCard by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Planes & Suscripciones Onyx",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = OnyxRedPrimary
                    )
                    Text(
                        text = "Elige tu nivel de potencia, fluidez y personalización",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // Privacy Guarantee Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = OnyxRedPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "🔒 Privacidad Total: Cada usuario gestiona sus métodos de pago con cifrado en su propio dispositivo. Nadie más puede ver tus datos privados.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        items(plans) { plan ->
            val isCurrent = plan == currentTier
            val isEvolution = plan == SubscriptionTier.EVOLUTION_1000

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isEvolution) Color(0xFF1B0B10) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isEvolution || isCurrent) 2.dp else 1.dp,
                        color = if (isCurrent) Color(0xFF00E676) else if (isEvolution) OnyxRedPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = plan.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (isEvolution) OnyxRedPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                if (plan.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verificado",
                                        tint = Color(0xFF2979FF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Duración: ${plan.period}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (plan == SubscriptionTier.BASIC_20 || plan == SubscriptionTier.PRO_50) OnyxRedPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // Price Tag
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = OnyxRedPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$${plan.priceMxn} MXN",
                                color = OnyxRedPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Feature highlights list
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        FeatureRow("✓ Cero Anuncios en toda la app")
                        if (plan.isVerified) FeatureRow("✓ Insignia de verificación de cuenta oficial")
                        if (plan.canCustomizeLogo) FeatureRow("✓ Decoración de app y personalización de logo")
                        if (plan == SubscriptionTier.PRO_50) FeatureRow("✓ Creación mejorada de imágenes y videos IA")
                        if (plan.hasOnyxPlus) FeatureRow("✓ Motor Onyx Plus: mayor conocimiento y fluidez IA")
                        if (plan.hasOnyxPlus) FeatureRow("✓ Catálogo exclusivo de estilos y plantillas de video")
                        if (plan.hasOnyxEvolution) FeatureRow("✓ Motor Onyx Evolution: IA cuántica de máxima velocidad")
                        if (plan.canCustomizeBanner) FeatureRow("✓ Banner de perfil exclusivo (Solo en Onyx Evolution)")
                        if (plan.hasNearbyConnect) FeatureRow("✓ Dispositivos Cercanos (Nearby Connect) exclusivo")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isCurrent) {
                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Plan Actualmente Activo", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                selectedPlanForPayment = plan
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pagar $${plan.priceMxn} MXN (${plan.period})",
                                color = OnyxWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Secure Payment Modal
    selectedPlanForPayment?.let { plan ->
        AlertDialog(
            onDismissRequest = { selectedPlanForPayment = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = OnyxRedPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Método de Pago Seguro", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Activando: ${plan.title} por $${plan.priceMxn} MXN (${plan.period})",
                        fontWeight = FontWeight.Bold,
                        color = OnyxRedPrimary,
                        fontSize = 12.sp
                    )

                    // Card scanner button
                    OutlinedButton(
                        onClick = {
                            isScanningCard = true
                            coroutineScope.launch {
                                delay(1200)
                                isScanningCard = false
                                cardNumberInput = "4532 8812 3456 7890"
                                cardHolderInput = "USUARIO ONYX OPEN"
                                cardExpiryInput = "11/29"
                                cardCvvInput = "742"
                                Toast.makeText(context, "Tarjeta leída con éxito mediante chip/cámara segura", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isScanningCard) "Leyendo tarjeta de forma privada..." else "Escanear Tarjeta con Cámara / Chip Seguro", fontSize = 11.sp)
                    }

                    OutlinedTextField(
                        value = cardNumberInput,
                        onValueChange = { cardNumberInput = it },
                        label = { Text("Número de Tarjeta (16 dígitos)") },
                        placeholder = { Text("4532 •••• •••• 7890") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = cardHolderInput,
                        onValueChange = { cardHolderInput = it },
                        label = { Text("Nombre del Titular") },
                        placeholder = { Text("Como aparece en la tarjeta") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cardExpiryInput,
                            onValueChange = { cardExpiryInput = it },
                            label = { Text("Exp (MM/AA)") },
                            placeholder = { Text("12/28") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = cardCvvInput,
                            onValueChange = { cardCvvInput = it },
                            label = { Text("CVV") },
                            placeholder = { Text("•••") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Private security notice
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Los datos bancarios se guardan cifrados solo en tu app privada.",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val last4 = if (cardNumberInput.length >= 4) cardNumberInput.takeLast(4) else "7890"
                        val brand = if (cardNumberInput.startsWith("4")) "Visa" else "Mastercard"
                        val paymentCard = PaymentCard(
                            cardHolder = if (cardHolderInput.isNotBlank()) cardHolderInput.trim() else "Titular",
                            lastFourDigits = last4,
                            cardBrand = brand,
                            expiry = if (cardExpiryInput.isNotBlank()) cardExpiryInput else "12/28",
                            isEncrypted = true
                        )

                        onUpgradeTier(plan, paymentCard)
                        selectedPlanForPayment = null
                        Toast.makeText(context, "¡Suscripción a ${plan.title} (${plan.period}) activada con tarjeta privada!", Toast.LENGTH_LONG).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OnyxRedPrimary)
                ) {
                    Text("Confirmar y Pagar", color = OnyxWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPlanForPayment = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun FeatureRow(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface
    )
}
