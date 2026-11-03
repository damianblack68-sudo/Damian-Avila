package com.example.engine

import com.example.domain.model.ConceptMapData
import com.example.domain.model.ConceptNode
import com.example.domain.model.ConceptRelationship

object ConceptMapEngine {

    fun isConceptMapRequest(input: String): Boolean {
        val lower = input.lowercase()
        return lower.contains("mapa conceptual") ||
                lower.contains("mapa conectual") ||
                lower.contains("mapas conceptuales") ||
                lower.contains("mapa mental") ||
                lower.contains("red conceptual") ||
                lower.contains("esquema de estudio")
    }

    fun generateConceptMap(prompt: String): ConceptMapData {
        val lower = prompt.lowercase()

        return when {
            lower.contains("software") || lower.contains("programacion") || lower.contains("programación") || lower.contains("app") -> {
                generateSoftwareEngineeringMap()
            }
            lower.contains("fisica") || lower.contains("física") || lower.contains("energia") || lower.contains("energía") -> {
                generatePhysicsEnergyMap()
            }
            lower.contains("inteligencia artificial") || lower.contains("ia") || lower.contains("machine learning") -> {
                generateAiConceptMap()
            }
            else -> {
                generateGeneralEngineeringStudyMap(prompt)
            }
        }
    }

    private fun generateAiConceptMap(): ConceptMapData {
        val nodes = listOf(
            ConceptNode("n1", "Inteligencia Artificial", "Disciplina computacional que emula facultades cognitivas", "CORE"),
            ConceptNode("n2", "Machine Learning", "Algoritmos que aprenden patrones a partir de datos empíricos", "MAIN"),
            ConceptNode("n3", "Deep Learning", "Redes neuronales artificiales profundas con capas ocultas", "MAIN"),
            ConceptNode("n4", "Modelos Generativos", "Sistemas capaces de sintetizar texto, código, planos y arte", "SUB"),
            ConceptNode("n5", "Razonamiento Lógico", "Cadena de deducciones analíticas (Chain of Thought)", "SUB"),
            ConceptNode("n6", "Aplicación en Ingeniería", "Automatización CAD, simulación numérica y diseño asistido", "EXAMPLE")
        )

        val relations = listOf(
            ConceptRelationship("n1", "n2", "engloba a"),
            ConceptRelationship("n2", "n3", "evoluciona hacia"),
            ConceptRelationship("n3", "n4", "potencia"),
            ConceptRelationship("n1", "n5", "requiere de"),
            ConceptRelationship("n4", "n6", "se implementa en"),
            ConceptRelationship("n5", "n6", "garantiza rigor en")
        )

        return ConceptMapData(
            centralTheme = "Inteligencia Artificial & Ingeniería Cognitiva",
            nodes = nodes,
            relationships = relations,
            studySummary = "La IA moderna conecta el aprendizaje estadístico con el razonamiento deductivo para resolver desafíos de ingeniería y estudio."
        )
    }

    private fun generateSoftwareEngineeringMap(): ConceptMapData {
        val nodes = listOf(
            ConceptNode("n1", "Arquitectura de Software", "Estructura y diseño de sistemas escalables", "CORE"),
            ConceptNode("n2", "Frontend & UI", "Interfaces interactivas responsivas en HTML/CSS/Compose", "MAIN"),
            ConceptNode("n3", "Backend & Lógica", "APIs, servidores de procesamiento y modelos matemáticos", "MAIN"),
            ConceptNode("n4", "Bases de Datos", "Almacenamiento persistente, ACID y consistencia", "SUB"),
            ConceptNode("n5", "Seguridad & Privacidad", "Encriptación local, autenticación y credenciales", "SUB")
        )

        val relations = listOf(
            ConceptRelationship("n1", "n2", "organiza la"),
            ConceptRelationship("n1", "n3", "coordina el"),
            ConceptRelationship("n3", "n4", "persiste en"),
            ConceptRelationship("n1", "n5", "protege mediante"),
            ConceptRelationship("n2", "n3", "se comunica con")
        )

        return ConceptMapData(
            centralTheme = "Ingeniería de Software & Desarrollo Full-Stack",
            nodes = nodes,
            relationships = relations,
            studySummary = "Un sistema de software robusto separa la presentación del cómputo y blinda los datos mediante capas desacopladas."
        )
    }

    private fun generatePhysicsEnergyMap(): ConceptMapData {
        val nodes = listOf(
            ConceptNode("n1", "Energía Mecánica Total", "Suma de energía cinética y potencial en un sistema", "CORE"),
            ConceptNode("n2", "Energía Cinética (Ec)", "Energía asociada al movimiento de la masa: ½ m·v²", "MAIN"),
            ConceptNode("n3", "Energía Potencial (Ep)", "Energía almacenada por posición o deformación elástica", "MAIN"),
            ConceptNode("n4", "Principio de Conservación", "La energía no se crea ni destruye, solo se transforma", "SUB"),
            ConceptNode("n5", "Trabajo Mecánico (W)", "Fuerza aplicada a lo largo de un desplazamiento", "SUB")
        )

        val relations = listOf(
            ConceptRelationship("n1", "n2", "está compuesta por"),
            ConceptRelationship("n1", "n3", "está compuesta por"),
            ConceptRelationship("n1", "n4", "obedece el"),
            ConceptRelationship("n5", "n2", "modifica la"),
            ConceptRelationship("n5", "n1", "determina variación en")
        )

        return ConceptMapData(
            centralTheme = "Termodinámica y Mecánica: Conservación de la Energía",
            nodes = nodes,
            relationships = relations,
            studySummary = "En sistemas conservativos sin fricción disipativa, la variación de energía mecánica total es estrictamente nula."
        )
    }

    private fun generateGeneralEngineeringStudyMap(prompt: String): ConceptMapData {
        val theme = prompt.take(35).ifBlank { "Metodología de Ingeniería" }
        val nodes = listOf(
            ConceptNode("n1", theme, "Núcleo conceptual central del tema", "CORE"),
            ConceptNode("n2", "Fundamento Teórico", "Leyes físicas, matemáticas y axiomas de partida", "MAIN"),
            ConceptNode("n3", "Modelado Matemático", "Ecuaciones diferenciales y simplificación analítica", "MAIN"),
            ConceptNode("n4", "Simulación & Planos", "Verificación gráfica CAD y cotas métricas", "SUB"),
            ConceptNode("n5", "Validación Experimental", "Contraste con datos reales y factor de seguridad", "SUB")
        )

        val relations = listOf(
            ConceptRelationship("n1", "n2", "se fundamenta en"),
            ConceptRelationship("n2", "n3", "se formaliza mediante"),
            ConceptRelationship("n3", "n4", "se proyecta en"),
            ConceptRelationship("n4", "n5", "se corrobora en"),
            ConceptRelationship("n5", "n1", "optimiza el")
        )

        return ConceptMapData(
            centralTheme = theme,
            nodes = nodes,
            relationships = relations,
            studySummary = "El ciclo de estudio en ingeniería parte de la teoría, pasa por el modelado y se consolida con planos y validación."
        )
    }
}
