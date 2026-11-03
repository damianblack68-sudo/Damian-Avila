package com.example.domain.model

enum class ProfileType(val label: String, val iconName: String) {
    PERSONAL("Vida Personal", "Person"),
    LABORAL("Trabajo / Estudio", "Work")
}

enum class PriorityLevel(val label: String, val colorHex: Long) {
    URGENTE("Urgente", 0xFFFF1744),
    ALTA("Alta", 0xFFFF5252),
    MEDIA("Media", 0xFFFF9100),
    BAJA("Baja", 0xFF00E676)
}

enum class VoicePersona(
    val title: String,
    val description: String,
    val pitch: Float,
    val speechRate: Float
) {
    ONYX_EXECUTIVE("Onyx Ejecutivo", "Masculina, grave, imponente y segura (nivel ejecutivo)", 0.76f, 0.95f),
    ONYX_NOVA("Onyx Nova", "Clara, equilibrada y moderna", 1.0f, 1.0f),
    ONYX_QUANTUM("Onyx Quantum", "Profunda, analítica y precisa", 0.82f, 1.02f),
    ONYX_HORIZON("Onyx Horizon", "Cálida, empática y motivadora", 1.10f, 0.98f),
    ONYX_SYNTH("Onyx Synth", "Futurista, concisa y rápida", 1.25f, 1.15f)
}

enum class EmotionalTone(val label: String, val promptHint: String) {
    ANALITICO("Analítico", "Responde con rigor lógico y matemático."),
    EMPATICO("Empático", "Responde con comprensión y calidez humana."),
    ENFOCADO("Enfocado", "Responde de forma concisa y directa al grano."),
    CREATIVO("Creativo", "Responde con metáforas, mapas y soluciones innovadoras.")
}

enum class ChartType {
    BAR,
    LINE,
    PIE
}

data class ChartPoint(
    val label: String,
    val value: Float
)

data class ChartData(
    val title: String,
    val type: ChartType,
    val points: List<ChartPoint>,
    val unit: String = ""
)

data class MindMapNode(
    val id: String,
    val label: String,
    val subnodes: List<MindMapNode> = emptyList()
)

data class ClarifyingQuestion(
    val question: String,
    val options: List<String>
)

data class StoryboardScene(
    val sceneNumber: Int,
    val title: String,
    val narration: String,
    val visualPrompt: String,
    val durationSeconds: Int
)

data class VideoScript(
    val title: String,
    val targetTopic: String,
    val totalDurationSeconds: Int,
    val scenes: List<StoryboardScene>
)

data class StudyTopic(
    val title: String,
    val platform: String,
    val duration: String,
    val tag: String
)

// --- Engine & Study Assistant Models ---

data class CodeProjectPayload(
    val title: String,
    val language: String, // "html", "javascript", "python", "kotlin"
    val filename: String,
    val code: String,
    val htmlPreviewContent: String? = null,
    val explanation: String = ""
)

enum class BlueprintType(val title: String, val scale: String, val unit: String) {
    ARCHITECTURAL_HOUSE("Plano Arquitectónico Residencial", "Escala 1:50", "metros (m)"),
    VEHICLE_CHASSIS("Plano Técnico Automotriz / Chasis", "Escala 1:20", "metros (m)"),
    MECHANICAL_CAD("Plano de Precisión Mecánica CAD", "Escala 1:5", "milímetros (mm)")
}

data class BlueprintRoom(
    val name: String,
    val xRatio: Float, // 0.0 to 1.0 inside drawing grid
    val yRatio: Float,
    val widthRatio: Float,
    val heightRatio: Float,
    val widthMeasure: Double,
    val heightMeasure: Double,
    val area: Double,
    val subLabel: String = ""
)

data class BlueprintCota(
    val label: String,
    val startXRatio: Float,
    val startYRatio: Float,
    val endXRatio: Float,
    val endYRatio: Float,
    val isHorizontal: Boolean
)

data class EngineeringBlueprint(
    val title: String,
    val type: BlueprintType,
    val totalWidthMeasure: Double,
    val totalHeightMeasure: Double,
    val totalAreaFormatted: String,
    val engineerNotes: List<String>,
    val roomsOrComponents: List<BlueprintRoom>,
    val cotas: List<BlueprintCota>,
    val designerInfo: String = "Ing. Damian Ávila • Onyx CAD Studio",
    val dateString: String = "2026-10-30"
)

data class AcademicReasoningStep(
    val stepNumber: Int,
    val stepTitle: String,
    val description: String,
    val formulaOrLaw: String? = null,
    val deduction: String
)

data class AcademicSolution(
    val subject: String,
    val topicTitle: String,
    val initialData: Map<String, String>,
    val lawsAndPrinciples: List<String>,
    val chainOfThoughtSteps: List<AcademicReasoningStep>,
    val finalConclusion: String,
    val studyAdvice: String
)

data class ConceptRelationship(
    val sourceId: String,
    val targetId: String,
    val connectorWord: String
)

data class ConceptNode(
    val id: String,
    val label: String,
    val detail: String = "",
    val category: String = "MAIN"
)

data class ConceptMapData(
    val centralTheme: String,
    val nodes: List<ConceptNode>,
    val relationships: List<ConceptRelationship>,
    val studySummary: String
)

enum class UserRole {
    ADMIN_CREATOR,
    STANDARD_USER
}

enum class SubscriptionTier(
    val title: String,
    val priceMxn: Int,
    val period: String,
    val description: String,
    val hasAds: Boolean,
    val isVerified: Boolean,
    val canCustomizeLogo: Boolean,
    val hasOnyxPlus: Boolean,
    val hasOnyxEvolution: Boolean,
    val hasNearbyConnect: Boolean,
    val canCustomizeBanner: Boolean
) {
    FREE(
        title = "Gratis",
        priceMxn = 0,
        period = "Permanente",
        description = "Acceso a IA, mensajería P2P con QR y workspace Notion. Los usuarios gratis pueden subir foto de perfil (el banner es exclusivo de Evolution).",
        hasAds = true,
        isVerified = false,
        canCustomizeLogo = false,
        hasOnyxPlus = false,
        hasOnyxEvolution = false,
        hasNearbyConnect = false,
        canCustomizeBanner = false
    ),
    BASIC_20(
        title = "Sin Anuncios",
        priceMxn = 20,
        period = "7 Días",
        description = "Elimina de forma total todos los banners y publicidad de la app por 7 días continuos.",
        hasAds = false,
        isVerified = false,
        canCustomizeLogo = false,
        hasOnyxPlus = false,
        hasOnyxEvolution = false,
        hasNearbyConnect = false,
        canCustomizeBanner = false
    ),
    PRO_50(
        title = "Onyx Pro Creador",
        priceMxn = 50,
        period = "15 Días",
        description = "Cero anuncios por 15 días, insignia de verificación oficial en tu cuenta, personalización de logo y alta calidad en creación de video/imagen IA.",
        hasAds = false,
        isVerified = true,
        canCustomizeLogo = true,
        hasOnyxPlus = false,
        hasOnyxEvolution = false,
        hasNearbyConnect = false,
        canCustomizeBanner = false
    ),
    VIP_100(
        title = "Onyx Plus VIP",
        priceMxn = 100,
        period = "3 Meses",
        description = "Cero anuncios por 3 meses, temas y decoración de app, cambio de logo exterior, catálogo de videos y motor Onyx Plus con mayor fluidez.",
        hasAds = false,
        isVerified = true,
        canCustomizeLogo = true,
        hasOnyxPlus = true,
        hasOnyxEvolution = false,
        hasNearbyConnect = false,
        canCustomizeBanner = false
    ),
    EVOLUTION_1000(
        title = "Onyx Evolution Master",
        priceMxn = 1000,
        period = "1 Año",
        description = "Todo lo de 100 por 1 año + Motor Onyx Evolution, Conexión Dispositivos Cercanos y personalización exclusiva de Banner de perfil.",
        hasAds = false,
        isVerified = true,
        canCustomizeLogo = true,
        hasOnyxPlus = true,
        hasOnyxEvolution = true,
        hasNearbyConnect = true,
        canCustomizeBanner = true
    )
}

enum class OnyxAiEngine(
    val title: String,
    val badge: String,
    val description: String,
    val requiredTier: SubscriptionTier,
    val speedMultiplier: String,
    val reasoningLevel: String
) {
    ONYX_X(
        title = "Onyx X",
        badge = "Base",
        description = "Motor IA estándar y ágil. Disponible para todos los usuarios de forma libre.",
        requiredTier = SubscriptionTier.FREE,
        speedMultiplier = "1.0x",
        reasoningLevel = "Estándar"
    ),
    ONYX_PRO(
        title = "Onyx Pro",
        badge = "Pro",
        description = "Alta fidelidad y deducción rápida para creadores y tareas complejas.",
        requiredTier = SubscriptionTier.PRO_50,
        speedMultiplier = "1.8x",
        reasoningLevel = "Avanzado"
    ),
    ONYX_PLUS(
        title = "Onyx Plus",
        badge = "Plus VIP",
        description = "Mayor fluidez y compresión profunda de contextos largos y creatividad.",
        requiredTier = SubscriptionTier.VIP_100,
        speedMultiplier = "2.5x",
        reasoningLevel = "Experto"
    ),
    ONYX_EVOLUTION(
        title = "Onyx Evolution",
        badge = "Master",
        description = "Máxima potencia cognitiva, ultrarrápido y comprensión contextual total.",
        requiredTier = SubscriptionTier.EVOLUTION_1000,
        speedMultiplier = "4.0x",
        reasoningLevel = "Supremo"
    );

    fun isUnlockedFor(user: UserProfile?): Boolean {
        if (user == null) return this == ONYX_X
        if (user.role == UserRole.ADMIN_CREATOR) return true
        return when (this) {
            ONYX_X -> true
            ONYX_PRO -> user.subscription in listOf(SubscriptionTier.PRO_50, SubscriptionTier.VIP_100, SubscriptionTier.EVOLUTION_1000)
            ONYX_PLUS -> user.subscription in listOf(SubscriptionTier.VIP_100, SubscriptionTier.EVOLUTION_1000)
            ONYX_EVOLUTION -> user.subscription == SubscriptionTier.EVOLUTION_1000
        }
    }
}


data class PaymentCard(
    val cardHolder: String,
    val lastFourDigits: String,
    val cardBrand: String,
    val expiry: String,
    val isEncrypted: Boolean = true
)

data class UserProfile(
    val email: String,
    val username: String,
    val displayName: String,
    val role: UserRole,
    val subscription: SubscriptionTier,
    val isVerified: Boolean = false,
    val qrCodeId: String = "ONX-$username",
    val customLogoAccent: Long = 0xFFFF2A54,
    val profilePhotoUri: String? = null,
    val bannerTheme: String = "ROJO_NEBULOSA", // Exclusive to Evolution
    val bannerColorStart: Long = 0xFFB71C1C,
    val bannerColorEnd: Long = 0xFF120307,
    val savedPaymentCard: PaymentCard? = null
)

data class PeerContact(
    val username: String,
    val displayName: String,
    val email: String,
    val qrCodeId: String,
    val lastMessage: String = "",
    val lastTimestamp: String = "12:00",
    val isNearby: Boolean = false,
    val isBlocked: Boolean = false,
    val chatCustomThemeColor: Long = 0xFFFF2A54
)

enum class AttachmentMediaType(val label: String, val iconName: String) {
    IMAGE("Foto / Imagen", "Image"),
    VIDEO("Video Grabación", "Video"),
    DOCUMENT("Documento / Libro / PDF", "Description")
}

data class ChatAttachment(
    val uri: String,
    val name: String,
    val mediaType: AttachmentMediaType,
    val sizeFormatted: String = "",
    val mimeType: String = ""
)

data class PeerMessage(
    val id: Long = System.currentTimeMillis(),
    val senderUsername: String,
    val receiverUsername: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachment: ChatAttachment? = null
)

enum class AttendanceStatus(val label: String, val colorHex: Long, val symbol: String) {
    ASISTIO("Asistió", 0xFF00E676, "🟢"),
    RETARDO("No llega / Retardo", 0xFFFFD600, "🟡"),
    FALTO("Faltó", 0xFFFF1744, "🔴")
}

data class StudentAttendanceRecord(
    val id: Long = 0,
    val studentName: String,
    val studentUsername: String = "",
    val studentEmail: String = "",
    val groupName: String,
    val date: String,
    val status: AttendanceStatus,
    val notes: String = "",
    val teacherProfile: String = "LABORAL",
    val teacherUsername: String = ""
)

data class AttendanceStats(
    val totalStudents: Int,
    val presentCount: Int,
    val lateCount: Int,
    val absentCount: Int,
    val attendancePercentage: Float
)

enum class VoiceOrbMode(val label: String) {
    IDLE("Onyx en espera"),
    LISTENING("Escuchando tu voz..."),
    PROCESSING("Onyx procesando respuesta..."),
    SPEAKING("Onyx respondiendo...")
}

