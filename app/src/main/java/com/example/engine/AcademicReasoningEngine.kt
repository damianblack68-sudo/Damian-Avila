package com.example.engine

import com.example.domain.model.AcademicReasoningStep
import com.example.domain.model.AcademicSolution

object AcademicReasoningEngine {

    fun isAcademicRequest(input: String): Boolean {
        val lower = input.lowercase()
        return lower.contains("tarea") ||
                lower.contains("razonar") ||
                lower.contains("razonamiento") ||
                lower.contains("explicar") ||
                lower.contains("paso a paso") ||
                lower.contains("fisica") ||
                lower.contains("física") ||
                lower.contains("quimica") ||
                lower.contains("química") ||
                lower.contains("estudio") ||
                lower.contains("universidad") ||
                lower.contains("secundaria") ||
                lower.contains("escuela") ||
                lower.contains("problema") ||
                lower.contains("termodinamica") ||
                lower.contains("cinematica") ||
                lower.contains("newton")
    }

    fun reason(prompt: String): AcademicSolution {
        val lower = prompt.lowercase()

        return when {
            lower.contains("quimica") || lower.contains("química") || lower.contains("mol") || lower.contains("reaccion") -> {
                reasonChemistryProblem(prompt)
            }
            lower.contains("circuito") || lower.contains("voltaje") || lower.contains("ohm") || lower.contains("corriente") -> {
                reasonElectricalEngineering(prompt)
            }
            lower.contains("termodinamica") || lower.contains("calor") || lower.contains("temperatura") || lower.contains("gas") -> {
                reasonThermodynamics(prompt)
            }
            else -> {
                reasonPhysicsMechanics(prompt)
            }
        }
    }

    private fun reasonPhysicsMechanics(prompt: String): AcademicSolution {
        val data = mapOf(
            "Masa del objeto (m)" to "15.0 kg",
            "Fuerza aplicada (F)" to "120.0 N a 30° sobre la horizontal",
            "Coeficiente de fricción cinética (μk)" to "0.20",
            "Aceleración gravitacional (g)" to "9.81 m/s²"
        )

        val laws = listOf(
            "Segunda Ley de Newton: ΣF = m · a",
            "Equilibrio perpendicular al plano: ΣFy = 0  =>  N + F·sen(θ) - m·g = 0",
            "Fuerza de fricción cinética: fk = μk · N",
            "Dinámica horizontal: ΣFx = m · ax  =>  F·cos(θ) - fk = m · ax"
        )

        val steps = listOf(
            AcademicReasoningStep(
                stepNumber = 1,
                stepTitle = "Descomposición Vectorial de Fuerzas",
                description = "Proyectamos la fuerza oblicua en componentes ortogonales cartesianas.",
                formulaOrLaw = "Fx = F · cos(30°), Fy = F · sen(30°)",
                deduction = "Fx = 120 · 0.866 = 103.92 N hacia la derecha. Fy = 120 · 0.500 = 60.00 N hacia arriba."
            ),
            AcademicReasoningStep(
                stepNumber = 2,
                stepTitle = "Cálculo de la Fuerza Normal (N)",
                description = "Dado que no existe aceleración en el eje vertical (ay = 0), la normal compensa el peso menos el alivio de la componente vertical de tracción.",
                formulaOrLaw = "N = m·g - Fy",
                deduction = "N = (15.0 kg · 9.81 m/s²) - 60.00 N = 147.15 N - 60.00 N = 87.15 N."
            ),
            AcademicReasoningStep(
                stepNumber = 3,
                stepTitle = "Determinación de la Fricción Dinámica (fk)",
                description = "La resistencia por rozamiento depende del contacto real con la superficie rugosa.",
                formulaOrLaw = "fk = μk · N",
                deduction = "fk = 0.20 · 87.15 N = 17.43 N opuesta al movimiento."
            ),
            AcademicReasoningStep(
                stepNumber = 4,
                stepTitle = "Aplicación de la 2ª Ley de Newton & Aceleración",
                description = "Obtenemos la fuerza neta aceleradora horizontal dividida entre la inercia másica del cuerpo.",
                formulaOrLaw = "ax = (Fx - fk) / m",
                deduction = "ax = (103.92 N - 17.43 N) / 15.0 kg = 86.49 N / 15.0 kg = 5.766 m/s²."
            ),
            AcademicReasoningStep(
                stepNumber = 5,
                stepTitle = "Verificación Dimensional & Consistencia Física",
                description = "Comprobación de unidades del Sistema Internacional: [N / kg] = [(kg·m/s²) / kg] = [m/s²].",
                formulaOrLaw = "[ax] = L · T⁻²",
                deduction = "El valor es coherente, positivo y respeta el principio de causalidad newtoniana."
            )
        )

        return AcademicSolution(
            subject = "Física Clásica & Mecánica Newtoniana",
            topicTitle = "Dinámica de Cuerpos con Fricción en Plano Horizontal",
            initialData = data,
            lawsAndPrinciples = laws,
            chainOfThoughtSteps = steps,
            finalConclusion = "La aceleración del bloque es ax = 5.77 m/s² hacia la derecha con fuerza normal N = 87.15 N.",
            studyAdvice = "Recuerda siempre trazar el Diagrama de Cuerpo Libre (DCL). El componente vertical de la fuerza externa reduce la normal, disminuyendo la fricción."
        )
    }

    private fun reasonChemistryProblem(prompt: String): AcademicSolution {
        val data = mapOf(
            "Masa de propano (C3H8)" to "88.0 g",
            "Masa molar de C3H8" to "44.09 g/mol",
            "Masa molar de O2" to "32.00 g/mol",
            "Masa molar de CO2" to "44.01 g/mol",
            "Condiciones" to "Presión y temperatura normales (PTN)"
        )

        val laws = listOf(
            "Ecuación estequiométrica balanceada: C3H8 + 5 O2 → 3 CO2 + 4 H2O",
            "Ley de Conservación de la Materia de Lavoisier: Σ masas reactivos = Σ masas productos",
            "Conversión molar: n = masa (g) / Masa Molar (g/mol)"
        )

        val steps = listOf(
            AcademicReasoningStep(
                stepNumber = 1,
                stepTitle = "Balance de la Ecuación Química de Combustión Completa",
                description = "Ajustamos átomos de Carbono (3), Hidrógeno (8 -> 4 H2O) y Oxígeno (3*2 + 4 = 10 -> 5 O2).",
                formulaOrLaw = "1 mol C3H8 + 5 mol O2 → 3 mol CO2 + 4 mol H2O",
                deduction = "La relación molar es 1 : 5 : 3 : 4 perfectamente estequiométrica."
            ),
            AcademicReasoningStep(
                stepNumber = 2,
                stepTitle = "Cálculo de Moles del Reactivo Inicial (C3H8)",
                description = "Determinamos cuántos moles equivalen a los 88.0 gramos dados.",
                formulaOrLaw = "n(C3H8) = m / M",
                deduction = "n = 88.0 g / 44.09 g/mol ≈ 1.996 mol ≈ 2.00 moles de C3H8."
            ),
            AcademicReasoningStep(
                stepNumber = 3,
                stepTitle = "Determinación de Oxígeno Requerido y CO2 Producido",
                description = "Aplicamos el factor de conversión estequiométrico de la ecuación balanceada.",
                formulaOrLaw = "n(CO2) = n(C3H8) · (3 mol CO2 / 1 mol C3H8)",
                deduction = "n(CO2) = 2.00 · 3 = 6.00 moles de CO2. Oxígeno necesario = 2.00 · 5 = 10.00 moles de O2."
            ),
            AcademicReasoningStep(
                stepNumber = 4,
                stepTitle = "Conversión a Masa de Producto Emitido",
                description = "Calculamos la masa final en gramos de dióxido de carbono.",
                formulaOrLaw = "masa(CO2) = n · M = 6.00 mol · 44.01 g/mol",
                deduction = "masa(CO2) = 264.06 gramos de CO2."
            )
        )

        return AcademicSolution(
            subject = "Química General & Estequiometría",
            topicTitle = "Combustión Estequiométrica de Hidrocarburos",
            initialData = data,
            lawsAndPrinciples = laws,
            chainOfThoughtSteps = steps,
            finalConclusion = "La combustión completa de 88.0 g de propano genera 264.06 g de CO2 y requiere 320.0 g de O2.",
            studyAdvice = "Verifica siempre el balance atómico antes de calcular moles. Los coeficientes indican proporción de moles, nunca de masa directa."
        )
    }

    private fun reasonElectricalEngineering(prompt: String): AcademicSolution {
        val data = mapOf(
            "Fuente de voltaje (Vs)" to "24.0 V",
            "Resistencia R1 (en serie)" to "8.0 Ω",
            "Resistencia R2 (en paralelo con R3)" to "12.0 Ω",
            "Resistencia R3 (en paralelo con R2)" to "24.0 Ω"
        )

        val laws = listOf(
            "Ley de Ohm: V = I · R",
            "Asociación de resistencias en paralelo: 1/Rp = 1/R2 + 1/R3",
            "Ley de Corrientes de Kirchhoff (LCK): ΣI_nodo = 0",
            "Divisor de Voltaje y Potencia Eléctrica: P = I² · R"
        )

        val steps = listOf(
            AcademicReasoningStep(
                stepNumber = 1,
                stepTitle = "Simplificación de la Rama en Paralelo (R2 // R3)",
                description = "Calculamos la resistencia equivalente del paralelo antes de sumar con R1.",
                formulaOrLaw = "Rp = (R2 · R3) / (R2 + R3)",
                deduction = "Rp = (12 · 24) / (12 + 24) = 288 / 36 = 8.0 Ω."
            ),
            AcademicReasoningStep(
                stepNumber = 2,
                stepTitle = "Resistencia Total Equivalente del Circuito (Req)",
                description = "Como R1 está en serie con el bloque equivalente Rp, sumamos directamente.",
                formulaOrLaw = "Req = R1 + Rp",
                deduction = "Req = 8.0 Ω + 8.0 Ω = 16.0 Ω."
            ),
            AcademicReasoningStep(
                stepNumber = 3,
                stepTitle = "Corriente Total del Circuito (Itotal)",
                description = "Aplicamos la Ley de Ohm a la fuente principal.",
                formulaOrLaw = "Itotal = Vs / Req",
                deduction = "Itotal = 24.0 V / 16.0 Ω = 1.50 A."
            ),
            AcademicReasoningStep(
                stepNumber = 4,
                stepTitle = "Distribución de Voltajes y Corrientes por Rama",
                description = "Calculamos caída de tensión en R1 y ramas individuales.",
                formulaOrLaw = "VR1 = Itotal · R1; Vp = Itotal · Rp",
                deduction = "VR1 = 1.5 A · 8 Ω = 12.0 V. Vp = 1.5 A · 8 Ω = 12.0 V. Corriente en R2 = 12V / 12Ω = 1.0 A. Corriente en R3 = 12V / 24Ω = 0.5 A. Suma = 1.0 + 0.5 = 1.5 A ✓ (Cumple LCK)."
            )
        )

        return AcademicSolution(
            subject = "Ingeniería Eléctrica & Teoría de Circuitos",
            topicTitle = "Análisis Mixto Serie-Paralelo en Corriente Continua",
            initialData = data,
            lawsAndPrinciples = laws,
            chainOfThoughtSteps = steps,
            finalConclusion = "Corriente total: 1.50 A. Potencia total disipada: 36.0 W (R1 disipa 18.0 W, R2 disipa 12.0 W, R3 disipa 6.0 W).",
            studyAdvice = "Usa el principio de conservación de la energía: la potencia entregada por la fuente (P = 24V · 1.5A = 36W) debe igualar a la suma de potencias disipadas en las resistencias."
        )
    }

    private fun reasonThermodynamics(prompt: String): AcademicSolution {
        val data = mapOf(
            "Sustancia" to "Gas Ideal Diatómico (Aire / N2)",
            "Moles (n)" to "3.0 mol",
            "Temperatura inicial (T1)" to "300 K (27 °C)",
            "Temperatura final (T2)" to "450 K (177 °C)",
            "Proceso" to "Isobárico a Presión Constante P = 101,325 Pa",
            "Capacidad calorífica molar Cp" to "(7/2) · R = 29.10 J/(mol·K)"
        )

        val laws = listOf(
            "Primera Ley de la Termodinámica: ΔU = Q - W",
            "Calor a presión constante: Q = n · Cp · ΔT",
            "Cambio de Energía Interna: ΔU = n · Cv · ΔT = n · (5/2)R · ΔT",
            "Trabajo de Expansión Isobárico: W = P · ΔV = n · R · ΔT"
        )

        val steps = listOf(
            AcademicReasoningStep(
                stepNumber = 1,
                stepTitle = "Cálculo del Calor Transferido al Sistema (Q)",
                description = "El calor entra a presión constante elevando temperatura de 300K a 450K (ΔT = 150 K).",
                formulaOrLaw = "Q = n · Cp · (T2 - T1)",
                deduction = "Q = 3.0 mol · 29.10 J/(mol·K) · 150 K = 13,095 J = 13.10 kJ."
            ),
            AcademicReasoningStep(
                stepNumber = 2,
                stepTitle = "Cálculo del Trabajo Realizado por Expansión (W)",
                description = "El gas se expande contra el medio ambiente ejerciendo trabajo positivo.",
                formulaOrLaw = "W = n · R · ΔT",
                deduction = "W = 3.0 mol · 8.314 J/(mol·K) · 150 K = 3,741.3 J = 3.74 kJ."
            ),
            AcademicReasoningStep(
                stepNumber = 3,
                stepTitle = "Variación de la Energía Interna del Gas (ΔU)",
                description = "Determinamos el incremento en energía cinética molecular promedio.",
                formulaOrLaw = "ΔU = Q - W  o  ΔU = n · Cv · ΔT",
                deduction = "ΔU = 13,095 J - 3,741 J = 9,354 J = 9.35 kJ. (Verificación por Cv: 3 · 20.785 · 150 = 9,353 J ✓)."
            )
        )

        return AcademicSolution(
            subject = "Termodinámica & Física Estadística",
            topicTitle = "Primera Ley en Procesos Isobáricos de Gases Ideales",
            initialData = data,
            lawsAndPrinciples = laws,
            chainOfThoughtSteps = steps,
            finalConclusion = "Calor absorbido Q = 13.10 kJ, Trabajo realizado W = 3.74 kJ, Incremento de energía interna ΔU = 9.35 kJ.",
            studyAdvice = "En un gas diatómico a temperatura ambiente, los grados de libertad traslacionales (3) y rotacionales (2) dan Cv = 5/2 R y Cp = 7/2 R."
        )
    }
}
