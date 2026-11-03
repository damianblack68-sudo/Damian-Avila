package com.example.engine

import com.example.domain.model.BlueprintCota
import com.example.domain.model.BlueprintRoom
import com.example.domain.model.BlueprintType
import com.example.domain.model.EngineeringBlueprint

object EngineeringBlueprintEngine {

    fun isBlueprintRequest(input: String): Boolean {
        val lower = input.lowercase()
        return lower.contains("plano") ||
                lower.contains("planos") ||
                lower.contains("medidas") ||
                lower.contains("arquitectura") ||
                lower.contains("arquitectonico") ||
                lower.contains("arquitectónico") ||
                lower.contains("casa") ||
                lower.contains("departamento") ||
                lower.contains("chasis") ||
                lower.contains("carro") ||
                lower.contains("auto") ||
                lower.contains("vehiculo") ||
                lower.contains("vehículo") ||
                lower.contains("pieza mecanica") ||
                lower.contains("cad") ||
                lower.contains("ingenieria") ||
                lower.contains("ingeniería")
    }

    fun generateBlueprint(prompt: String): EngineeringBlueprint {
        val lower = prompt.lowercase()

        return when {
            lower.contains("carro") || lower.contains("auto") || lower.contains("chasis") || lower.contains("vehiculo") || lower.contains("vehículo") -> {
                generateVehicleChassisBlueprint(prompt)
            }
            lower.contains("mecanica") || lower.contains("mecánica") || lower.contains("pieza") || lower.contains("engranaje") || lower.contains("motor") -> {
                generateMechanicalCadBlueprint(prompt)
            }
            else -> {
                generateArchitecturalHouseBlueprint(prompt)
            }
        }
    }

    private fun generateArchitecturalHouseBlueprint(prompt: String): EngineeringBlueprint {
        val rooms = listOf(
            BlueprintRoom(
                name = "Sala de Estar",
                xRatio = 0.08f,
                yRatio = 0.12f,
                widthRatio = 0.42f,
                heightRatio = 0.36f,
                widthMeasure = 5.20,
                heightMeasure = 4.20,
                area = 21.84,
                subLabel = "Piso porcelanato rectificado"
            ),
            BlueprintRoom(
                name = "Comedor & Cocina",
                xRatio = 0.52f,
                yRatio = 0.12f,
                widthRatio = 0.40f,
                heightRatio = 0.36f,
                widthMeasure = 4.80,
                heightMeasure = 4.20,
                area = 20.16,
                subLabel = "Muro húmedo e isla integrada"
            ),
            BlueprintRoom(
                name = "Recámara Principal",
                xRatio = 0.08f,
                yRatio = 0.52f,
                widthRatio = 0.42f,
                heightRatio = 0.36f,
                widthMeasure = 4.60,
                heightMeasure = 4.00,
                area = 18.40,
                subLabel = "Vestidor y baño en suite"
            ),
            BlueprintRoom(
                name = "Recámara 2 / Estudio",
                xRatio = 0.52f,
                yRatio = 0.52f,
                widthRatio = 0.24f,
                heightRatio = 0.36f,
                widthMeasure = 3.20,
                heightMeasure = 4.00,
                area = 12.80,
                subLabel = "Ventilación cruzada"
            ),
            BlueprintRoom(
                name = "Baño Completo",
                xRatio = 0.78f,
                yRatio = 0.52f,
                widthRatio = 0.14f,
                heightRatio = 0.36f,
                widthMeasure = 1.80,
                heightMeasure = 4.00,
                area = 7.20,
                subLabel = "Ventilación cenital"
            )
        )

        val cotas = listOf(
            // Cota superior total (Ancho 10.00 m)
            BlueprintCota(
                label = "10.00 m",
                startXRatio = 0.08f,
                startYRatio = 0.06f,
                endXRatio = 0.92f,
                endYRatio = 0.06f,
                isHorizontal = true
            ),
            // Cota lateral izquierda total (Largo 12.00 m)
            BlueprintCota(
                label = "12.00 m",
                startXRatio = 0.03f,
                startYRatio = 0.12f,
                endXRatio = 0.03f,
                endYRatio = 0.88f,
                isHorizontal = false
            ),
            // Cotas parciales
            BlueprintCota(
                label = "5.20 m",
                startXRatio = 0.08f,
                startYRatio = 0.49f,
                endXRatio = 0.50f,
                endYRatio = 0.49f,
                isHorizontal = true
            ),
            BlueprintCota(
                label = "4.80 m",
                startXRatio = 0.52f,
                startYRatio = 0.49f,
                endXRatio = 0.92f,
                endYRatio = 0.49f,
                isHorizontal = true
            )
        )

        val notes = listOf(
            "Cimentación: Zapata corrida de concreto armado f'c = 250 kg/cm² con varilla corrugada grado 42.",
            "Muros: Tabique rojo recocido confinado con dalas y castillos de 15 x 15 cm.",
            "Altura libre de entrepiso: 2.70 m. Pendiente pluvial en azotea: 2.0%.",
            "Instalación eléctrica: Tubería poliducto corrugado 3/4\" con cable calibre 12 y 10 AWG.",
            "Diseñado bajo el Reglamento de Construcciones y Estándares de Arquitectura Técnica."
        )

        return EngineeringBlueprint(
            title = "Plano Residencial Casa Habitación • 10.00 x 12.00 m",
            type = BlueprintType.ARCHITECTURAL_HOUSE,
            totalWidthMeasure = 10.0,
            totalHeightMeasure = 12.0,
            totalAreaFormatted = "120.00 m² Construcción",
            engineerNotes = notes,
            roomsOrComponents = rooms,
            cotas = cotas,
            designerInfo = "Ing. Damian Ávila • Onyx Architecture & CAD"
        )
    }

    private fun generateVehicleChassisBlueprint(prompt: String): EngineeringBlueprint {
        val components = listOf(
            BlueprintRoom(
                name = "Vano Motor & Tren Delantero",
                xRatio = 0.10f,
                yRatio = 0.25f,
                widthRatio = 0.22f,
                heightRatio = 0.50f,
                widthMeasure = 1.05,
                heightMeasure = 1.65,
                area = 1.73,
                subLabel = "Motor V6 Bi-Turbo / Inversores"
            ),
            BlueprintRoom(
                name = "Monocasco de Seguridad (Cockpit)",
                xRatio = 0.34f,
                yRatio = 0.18f,
                widthRatio = 0.38f,
                heightRatio = 0.64f,
                widthMeasure = 2.40,
                heightMeasure = 1.85,
                area = 4.44,
                subLabel = "Fibra de Carbono & Jaula Roll-Cage"
            ),
            BlueprintRoom(
                name = "Eje Trasero & Baterías / Difusor",
                xRatio = 0.74f,
                yRatio = 0.25f,
                widthRatio = 0.18f,
                heightRatio = 0.50f,
                widthMeasure = 1.20,
                heightMeasure = 1.65,
                area = 1.98,
                subLabel = "Diferencial de deslizamiento limitado"
            )
        )

        val cotas = listOf(
            // Longitud total del vehículo: 4.65 m
            BlueprintCota(
                label = "Longitud Total: 4.65 m",
                startXRatio = 0.10f,
                startYRatio = 0.10f,
                endXRatio = 0.92f,
                endYRatio = 0.10f,
                isHorizontal = true
            ),
            // Batalla (Distancia entre ejes): 2.75 m
            BlueprintCota(
                label = "Batalla (Ejes): 2.75 m",
                startXRatio = 0.21f,
                startYRatio = 0.86f,
                endXRatio = 0.83f,
                endYRatio = 0.86f,
                isHorizontal = true
            ),
            // Ancho total de carrocería: 1.85 m
            BlueprintCota(
                label = "Ancho: 1.85 m",
                startXRatio = 0.04f,
                startYRatio = 0.18f,
                endXRatio = 0.04f,
                endYRatio = 0.82f,
                isHorizontal = false
            )
        )

        val notes = listOf(
            "Estructura: Chasis tubular espacial de aleación CrMo 4130 con subcuadros desmontables.",
            "Distribución estática de pesos: 48% eje delantero / 52% eje trasero.",
            "Suspensión: Doble horquilla oscilante independiente con amortiguadores coilover regulables.",
            "Coeficiente de resistencia aerodinámica de arrastre: Cx = 0.29.",
            "Frenos: Discos carbocerámicos ventilados con pinzas monobloque de 6 pistones."
        )

        return EngineeringBlueprint(
            title = "Plano Técnico Automotriz • Chasis de Alto Rendimiento",
            type = BlueprintType.VEHICLE_CHASSIS,
            totalWidthMeasure = 4.65,
            totalHeightMeasure = 1.85,
            totalAreaFormatted = "8.60 m² Huella Dinámica",
            engineerNotes = notes,
            roomsOrComponents = components,
            cotas = cotas,
            designerInfo = "Ing. Damian Ávila • Onyx Automotive Engineering"
        )
    }

    private fun generateMechanicalCadBlueprint(prompt: String): EngineeringBlueprint {
        val parts = listOf(
            BlueprintRoom(
                name = "Corona Dentada Helicoidal",
                xRatio = 0.15f,
                yRatio = 0.15f,
                widthRatio = 0.70f,
                heightRatio = 0.40f,
                widthMeasure = 180.0,
                heightMeasure = 85.0,
                area = 15300.0,
                subLabel = "Módulo m=3.5, 48 Dientes, Ángulo 20°"
            ),
            BlueprintRoom(
                name = "Eje de Transmisión & Chavetero",
                xRatio = 0.25f,
                yRatio = 0.60f,
                widthRatio = 0.50f,
                heightRatio = 0.25f,
                widthMeasure = 120.0,
                heightMeasure = 40.0,
                area = 4800.0,
                subLabel = "Ajuste h7 / tolerancia ISO ±0.015 mm"
            )
        )

        val cotas = listOf(
            BlueprintCota(
                label = "Ø Ext: 180.00 mm ±0.02",
                startXRatio = 0.15f,
                startYRatio = 0.08f,
                endXRatio = 0.85f,
                endYRatio = 0.08f,
                isHorizontal = true
            ),
            BlueprintCota(
                label = "Longitud: 140 mm",
                startXRatio = 0.08f,
                startYRatio = 0.15f,
                endXRatio = 0.08f,
                endYRatio = 0.85f,
                isHorizontal = false
            ),
            BlueprintCota(
                label = "Eje Ø 40.00 mm (H7)",
                startXRatio = 0.25f,
                startYRatio = 0.88f,
                endXRatio = 0.75f,
                endYRatio = 0.88f,
                isHorizontal = true
            )
        )

        val notes = listOf(
            "Material: Acero aleado AISI 4340 templado y revenido a 45 HRC.",
            "Tolerancias geométricas: Concentricidad 0.010 mm respecto a centros de rodamiento.",
            "Rugosidad superficial: Ra = 0.8 μm en flancos de diente; Ra = 0.4 μm en asiento de rodamiento.",
            "Tratamiento térmico: Nitruración gaseosa profunda de 0.5 mm para alta resistencia al desgaste."
        )

        return EngineeringBlueprint(
            title = "Plano Mecánico CAD • Reductor & Corona Helicoidal",
            type = BlueprintType.MECHANICAL_CAD,
            totalWidthMeasure = 180.0,
            totalHeightMeasure = 140.0,
            totalAreaFormatted = "Tolerancia ISO DIN 3962",
            engineerNotes = notes,
            roomsOrComponents = parts,
            cotas = cotas,
            designerInfo = "Ing. Damian Ávila • Onyx Mechanical CAD"
        )
    }
}
