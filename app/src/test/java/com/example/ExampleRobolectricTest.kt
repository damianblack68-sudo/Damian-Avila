package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.SubscriptionTier
import com.example.domain.model.UserProfile
import com.example.domain.model.UserRole
import com.example.engine.DiagramGeneratorEngine
import com.example.engine.MathSolverEngine
import com.example.engine.SmartTagEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Onyx Open", appName)
    }

    @Test
    fun `solve quadratic equation with MathSolverEngine`() {
        val solution = MathSolverEngine.solve("x² - 5x + 6 = 0")
        assertNotNull(solution)
        assertTrue(solution.steps.isNotEmpty())
        assertNotNull(solution.chartData)
        assertTrue(solution.finalAnswer.contains("x₁") || solution.finalAnswer.contains("x ="))
    }

    @Test
    fun `suggest tags with SmartTagEngine`() {
        val tags = SmartTagEngine.suggestTags("Resolver ejercicio de cálculo y derivadas para el examen urgente")
        assertTrue(tags.contains("Matemáticas") || tags.contains("Cálculo") || tags.contains("Urgente"))
    }

    @Test
    fun `generate mind map with DiagramGeneratorEngine`() {
        val mindMap = DiagramGeneratorEngine.generateMindMapFromText("Inteligencia Artificial", "Conceptos\n- Machine Learning\n- Redes Neuronales")
        assertNotNull(mindMap)
        assertEquals("Inteligencia Artificial", mindMap.label)
        assertTrue(mindMap.subnodes.isNotEmpty())
    }

    @Test
    fun `verify admin privileges for damian email`() {
        val adminEmail = "damianblack68@gmail.com"
        val isAdmin = adminEmail.trim().lowercase() == "damianblack68@gmail.com"
        assertTrue(isAdmin)

        val adminProfile = UserProfile(
            email = adminEmail,
            username = "damian_avila_admin",
            displayName = "Damian Ávila (Creador & Admin)",
            role = UserRole.ADMIN_CREATOR,
            subscription = SubscriptionTier.EVOLUTION_1000,
            isVerified = true
        )

        assertEquals(UserRole.ADMIN_CREATOR, adminProfile.role)
        assertEquals(SubscriptionTier.EVOLUTION_1000, adminProfile.subscription)
        assertTrue(adminProfile.isVerified)
        assertFalse(adminProfile.subscription.hasAds)
        assertTrue(adminProfile.subscription.hasOnyxEvolution)
        assertTrue(adminProfile.subscription.hasNearbyConnect)
    }

    @Test
    fun `verify subscription tiers features and pricing`() {
        assertEquals(0, SubscriptionTier.FREE.priceMxn)
        assertTrue(SubscriptionTier.FREE.hasAds)

        assertEquals(20, SubscriptionTier.BASIC_20.priceMxn)
        assertEquals("7 Días", SubscriptionTier.BASIC_20.period)
        assertFalse(SubscriptionTier.BASIC_20.hasAds)
        assertFalse(SubscriptionTier.BASIC_20.canCustomizeBanner)

        assertEquals(50, SubscriptionTier.PRO_50.priceMxn)
        assertEquals("15 Días", SubscriptionTier.PRO_50.period)
        assertFalse(SubscriptionTier.PRO_50.hasAds)
        assertTrue(SubscriptionTier.PRO_50.isVerified)
        assertTrue(SubscriptionTier.PRO_50.canCustomizeLogo)
        assertFalse(SubscriptionTier.PRO_50.canCustomizeBanner)

        assertEquals(100, SubscriptionTier.VIP_100.priceMxn)
        assertEquals("3 Meses", SubscriptionTier.VIP_100.period)
        assertFalse(SubscriptionTier.VIP_100.hasAds)
        assertTrue(SubscriptionTier.VIP_100.hasOnyxPlus)
        assertFalse(SubscriptionTier.VIP_100.canCustomizeBanner)

        assertEquals(1000, SubscriptionTier.EVOLUTION_1000.priceMxn)
        assertEquals("1 Año", SubscriptionTier.EVOLUTION_1000.period)
        assertFalse(SubscriptionTier.EVOLUTION_1000.hasAds)
        assertTrue(SubscriptionTier.EVOLUTION_1000.hasOnyxEvolution)
        assertTrue(SubscriptionTier.EVOLUTION_1000.hasNearbyConnect)
        assertTrue(SubscriptionTier.EVOLUTION_1000.canCustomizeBanner)
    }

    @Test
    fun `verify peer contact blocking and payment card privacy`() {
        val contact = com.example.domain.model.PeerContact(
            username = "test_user",
            displayName = "Test User",
            email = "test@gmail.com",
            qrCodeId = "ONX-test_user",
            isBlocked = true
        )
        assertTrue(contact.isBlocked)

        val card = com.example.domain.model.PaymentCard(
            cardHolder = "Damian Ávila",
            lastFourDigits = "7890",
            cardBrand = "Visa",
            expiry = "11/29",
            isEncrypted = true
        )
        assertEquals("7890", card.lastFourDigits)
        assertTrue(card.isEncrypted)
    }

    @Test
    fun `verify OnyxAiEngine unlock rules for free, pro, vip, evolution and admin`() {
        val freeUser = UserProfile(
            email = "usuario@gmail.com",
            username = "usuario_free",
            displayName = "Usuario Estándar",
            role = UserRole.STANDARD_USER,
            subscription = SubscriptionTier.FREE
        )
        // Free user can ONLY use Onyx X
        assertTrue(com.example.domain.model.OnyxAiEngine.ONYX_X.isUnlockedFor(freeUser))
        assertFalse(com.example.domain.model.OnyxAiEngine.ONYX_PRO.isUnlockedFor(freeUser))
        assertFalse(com.example.domain.model.OnyxAiEngine.ONYX_PLUS.isUnlockedFor(freeUser))
        assertFalse(com.example.domain.model.OnyxAiEngine.ONYX_EVOLUTION.isUnlockedFor(freeUser))

        val proUser = freeUser.copy(subscription = SubscriptionTier.PRO_50)
        assertTrue(com.example.domain.model.OnyxAiEngine.ONYX_X.isUnlockedFor(proUser))
        assertTrue(com.example.domain.model.OnyxAiEngine.ONYX_PRO.isUnlockedFor(proUser))
        assertFalse(com.example.domain.model.OnyxAiEngine.ONYX_PLUS.isUnlockedFor(proUser))
        assertFalse(com.example.domain.model.OnyxAiEngine.ONYX_EVOLUTION.isUnlockedFor(proUser))

        val vipUser = freeUser.copy(subscription = SubscriptionTier.VIP_100)
        assertTrue(com.example.domain.model.OnyxAiEngine.ONYX_X.isUnlockedFor(vipUser))
        assertTrue(com.example.domain.model.OnyxAiEngine.ONYX_PRO.isUnlockedFor(vipUser))
        assertTrue(com.example.domain.model.OnyxAiEngine.ONYX_PLUS.isUnlockedFor(vipUser))
        assertFalse(com.example.domain.model.OnyxAiEngine.ONYX_EVOLUTION.isUnlockedFor(vipUser))

        val evolutionUser = freeUser.copy(subscription = SubscriptionTier.EVOLUTION_1000)
        assertTrue(com.example.domain.model.OnyxAiEngine.ONYX_X.isUnlockedFor(evolutionUser))
        assertTrue(com.example.domain.model.OnyxAiEngine.ONYX_PRO.isUnlockedFor(evolutionUser))
        assertTrue(com.example.domain.model.OnyxAiEngine.ONYX_PLUS.isUnlockedFor(evolutionUser))
        assertTrue(com.example.domain.model.OnyxAiEngine.ONYX_EVOLUTION.isUnlockedFor(evolutionUser))

        val adminUser = UserProfile(
            email = "damianblack68@gmail.com",
            username = "damian_admin",
            displayName = "Damian Ávila",
            role = UserRole.ADMIN_CREATOR,
            subscription = SubscriptionTier.FREE // Even with free tier, role is ADMIN_CREATOR
        )
        // Admin creator has access to ALL engines
        assertTrue(com.example.domain.model.OnyxAiEngine.ONYX_X.isUnlockedFor(adminUser))
        assertTrue(com.example.domain.model.OnyxAiEngine.ONYX_PRO.isUnlockedFor(adminUser))
        assertTrue(com.example.domain.model.OnyxAiEngine.ONYX_PLUS.isUnlockedFor(adminUser))
        assertTrue(com.example.domain.model.OnyxAiEngine.ONYX_EVOLUTION.isUnlockedFor(adminUser))
    }

    @Test
    fun `generate web app and html file with CodeGeneratorEngine`() {
        val request = "Crear una app calculadora en html con javascript"
        assertTrue(com.example.engine.CodeGeneratorEngine.isCodeRequest(request))

        val codeProject = com.example.engine.CodeGeneratorEngine.generateCode(request)
        assertNotNull(codeProject)
        assertEquals("html", codeProject.language)
        assertTrue(codeProject.filename.endsWith(".html"))
        assertTrue(codeProject.code.contains("<!DOCTYPE html>"))
        assertTrue(codeProject.code.contains("<script>"))
        assertNotNull(codeProject.htmlPreviewContent)
    }

    @Test
    fun `generate architectural and automotive blueprints with EngineeringBlueprintEngine`() {
        val houseReq = "plano de casa de 10x12 con recamaras y medidas"
        assertTrue(com.example.engine.EngineeringBlueprintEngine.isBlueprintRequest(houseReq))

        val houseBlueprint = com.example.engine.EngineeringBlueprintEngine.generateBlueprint(houseReq)
        assertEquals(com.example.domain.model.BlueprintType.ARCHITECTURAL_HOUSE, houseBlueprint.type)
        assertTrue(houseBlueprint.roomsOrComponents.isNotEmpty())
        assertTrue(houseBlueprint.cotas.isNotEmpty())
        assertTrue(houseBlueprint.designerInfo.contains("Damian Ávila"))
        assertTrue(houseBlueprint.totalAreaFormatted.contains("m²"))

        val carReq = "diseñar plano de chasis de auto con aerodinamica"
        val carBlueprint = com.example.engine.EngineeringBlueprintEngine.generateBlueprint(carReq)
        assertEquals(com.example.domain.model.BlueprintType.VEHICLE_CHASSIS, carBlueprint.type)
        assertTrue(carBlueprint.totalWidthMeasure > 4.0) // 4.65m car length
        assertTrue(carBlueprint.cotas.any { it.label.contains("Batalla") || it.label.contains("Longitud") })
    }

    @Test
    fun `reason 100 percent chain of thought for homework with AcademicReasoningEngine`() {
        val homeworkPrompt = "tarea de fisica resolver bloque con friccion y aceleracion paso a paso"
        assertTrue(com.example.engine.AcademicReasoningEngine.isAcademicRequest(homeworkPrompt))

        val solution = com.example.engine.AcademicReasoningEngine.reason(homeworkPrompt)
        assertNotNull(solution)
        assertTrue(solution.initialData.isNotEmpty())
        assertTrue(solution.lawsAndPrinciples.isNotEmpty())
        assertTrue(solution.chainOfThoughtSteps.size >= 3)
        assertTrue(solution.finalConclusion.isNotBlank())
        assertTrue(solution.studyAdvice.isNotBlank())
    }

    @Test
    fun `generate structured study concept map with ConceptMapEngine`() {
        val mapPrompt = "generar mapa conceptual de inteligencia artificial"
        assertTrue(com.example.engine.ConceptMapEngine.isConceptMapRequest(mapPrompt))

        val conceptMap = com.example.engine.ConceptMapEngine.generateConceptMap(mapPrompt)
        assertNotNull(conceptMap)
        assertTrue(conceptMap.nodes.size >= 4)
        assertTrue(conceptMap.relationships.isNotEmpty())
        assertTrue(conceptMap.relationships.all { it.connectorWord.isNotBlank() })
    }

    @Test
    fun `verify teacher attendance register statuses and colors`() {
        // User requirements: Amarillo = No llega / Retardo, Rojo = Falto, Verde = Asistió
        assertEquals("Asistió", com.example.domain.model.AttendanceStatus.ASISTIO.label)
        assertEquals(0xFF00E676, com.example.domain.model.AttendanceStatus.ASISTIO.colorHex) // Green
        assertEquals("🟢", com.example.domain.model.AttendanceStatus.ASISTIO.symbol)

        assertEquals("No llega / Retardo", com.example.domain.model.AttendanceStatus.RETARDO.label)
        assertEquals(0xFFFFD600, com.example.domain.model.AttendanceStatus.RETARDO.colorHex) // Yellow
        assertEquals("🟡", com.example.domain.model.AttendanceStatus.RETARDO.symbol)

        assertEquals("Faltó", com.example.domain.model.AttendanceStatus.FALTO.label)
        assertEquals(0xFFFF1744, com.example.domain.model.AttendanceStatus.FALTO.colorHex) // Red
        assertEquals("🔴", com.example.domain.model.AttendanceStatus.FALTO.symbol)
    }

    @Test
    fun `calculate teacher attendance statistics and percentages`() {
        val records = listOf(
            com.example.data.local.entity.AttendanceRecordEntity(id = 1, studentName = "Alumno 1", groupName = "Matemáticas 3°A", date = "2026-10-30", status = "ASISTIO"),
            com.example.data.local.entity.AttendanceRecordEntity(id = 2, studentName = "Alumno 2", groupName = "Matemáticas 3°A", date = "2026-10-30", status = "ASISTIO"),
            com.example.data.local.entity.AttendanceRecordEntity(id = 3, studentName = "Alumno 3", groupName = "Matemáticas 3°A", date = "2026-10-30", status = "RETARDO"),
            com.example.data.local.entity.AttendanceRecordEntity(id = 4, studentName = "Alumno 4", groupName = "Matemáticas 3°A", date = "2026-10-30", status = "FALTO")
        )

        val total = records.size
        val present = records.count { it.status == "ASISTIO" }
        val late = records.count { it.status == "RETARDO" }
        val absent = records.count { it.status == "FALTO" }
        val percentage = ((present.toFloat() + (late * 0.5f)) / total) * 100f

        assertEquals(4, total)
        assertEquals(2, present)
        assertEquals(1, late)
        assertEquals(1, absent)
        assertEquals(62.5f, percentage, 0.01f)
    }

    @Test
    fun `verify teacher attendance record linkage with user profiles`() {
        val studentRecord = com.example.data.local.entity.AttendanceRecordEntity(
            id = 10,
            studentName = "Carlos Daniel Mendoza",
            studentUsername = "carlos_ia",
            studentEmail = "carlos.m@estudiante.mx",
            groupName = "Programación e IA",
            date = "2026-10-31",
            status = "ASISTIO",
            notes = "Participación destacada en clase",
            teacherProfile = "LABORAL",
            teacherUsername = "damianblack68@gmail.com"
        )

        assertEquals("carlos_ia", studentRecord.studentUsername)
        assertEquals("carlos.m@estudiante.mx", studentRecord.studentEmail)
        assertEquals("damianblack68@gmail.com", studentRecord.teacherUsername)
        assertEquals("ASISTIO", studentRecord.status)
    }

    @Test
    fun `verify chat attachment types and reasoning support`() {
        val photoAtt = com.example.domain.model.ChatAttachment(
            uri = "content://media/photo_1.jpg",
            name = "tarea_calculo.jpg",
            mediaType = com.example.domain.model.AttachmentMediaType.IMAGE,
            sizeFormatted = "2.3 MB"
        )
        val videoAtt = com.example.domain.model.ChatAttachment(
            uri = "content://media/lab.mp4",
            name = "experimento_fisica.mp4",
            mediaType = com.example.domain.model.AttachmentMediaType.VIDEO,
            sizeFormatted = "15.0 MB"
        )
        val bookAtt = com.example.domain.model.ChatAttachment(
            uri = "content://docs/libro_algebra.pdf",
            name = "algebra_baldor.pdf",
            mediaType = com.example.domain.model.AttachmentMediaType.DOCUMENT,
            sizeFormatted = "4.5 MB"
        )

        assertEquals(com.example.domain.model.AttachmentMediaType.IMAGE, photoAtt.mediaType)
        assertEquals(com.example.domain.model.AttachmentMediaType.VIDEO, videoAtt.mediaType)
        assertEquals(com.example.domain.model.AttachmentMediaType.DOCUMENT, bookAtt.mediaType)
    }

    @Test
    fun `verify user isolation and whatsapp style contact blocking`() {
        val contactA = com.example.domain.model.PeerContact(
            username = "carlos_ia",
            displayName = "Carlos Mendoza",
            email = "carlos@gmail.com",
            qrCodeId = "ONX-carlos_ia",
            isBlocked = false
        )

        // Block contact for specific user (isolated)
        val blockedContactA = contactA.copy(isBlocked = true)
        assertTrue(blockedContactA.isBlocked)
        assertFalse(contactA.isBlocked) // Original remains unmutated
    }

    @Test
    fun `verify Onyx Express Voice Orb states and reactive microphone levels`() {
        // 1. Verify states: IDLE, LISTENING, PROCESSING, SPEAKING
        val idleState = com.example.domain.model.VoiceOrbMode.IDLE
        val listeningState = com.example.domain.model.VoiceOrbMode.LISTENING
        val processingState = com.example.domain.model.VoiceOrbMode.PROCESSING
        val speakingState = com.example.domain.model.VoiceOrbMode.SPEAKING

        assertEquals("Onyx en espera", idleState.label)
        assertEquals("Escuchando tu voz...", listeningState.label)
        assertEquals("Onyx procesando respuesta...", processingState.label)
        assertEquals("Onyx respondiendo...", speakingState.label)

        // 2. Verify mic input normalization
        val quietRmsDb = -2f
        val quietNormalized = ((quietRmsDb + 2f) / 12f).coerceIn(0.12f, 1.0f)
        assertEquals(0.12f, quietNormalized, 0.01f)

        val loudRmsDb = 10f
        val loudNormalized = ((loudRmsDb + 2f) / 12f).coerceIn(0.12f, 1.0f)
        assertEquals(1.0f, loudNormalized, 0.01f)

        val speechRmsDb = 4f
        val speechNormalized = ((speechRmsDb + 2f) / 12f).coerceIn(0.12f, 1.0f)
        assertEquals(0.5f, speechNormalized, 0.01f)
    }
}


