package com.example.engine

object SmartTagEngine {

    private val keywordTagMap = mapOf(
        listOf("matemática", "matematicas", "calculo", "derivada", "integral", "limite", "algebra", "ecuacion", "parabola", "raiz") to listOf("Matemáticas", "Cálculo", "Álgebra"),
        listOf("estadistica", "media", "probabilidad", "datos", "muestra", "varianza", "desviacion", "grafico") to listOf("Estadística", "Data Science", "Análisis"),
        listOf("urgente", "inmediato", "hoy", "examen", "entrega", "deadline", "critico") to listOf("Urgente", "Prioridad Alta"),
        listOf("proyecto", "sprint", "notion", "roadmap", "plan", "estrategia", "tarea", "hitos") to listOf("Proyecto", "Notion", "Planificación"),
        listOf("reunion", "equipo", "cliente", "presentacion", "informe", "reporte") to listOf("Laboral", "Reporte", "Gestión"),
        listOf("codigo", "algoritmo", "programacion", "software", "android", "kotlin", "ia", "inteligencia") to listOf("Tecnología", "IA", "Desarrollo"),
        listOf("habito", "rutina", "salud", "ejercicio", "lectura", "personal", "meta") to listOf("Personal", "Hábitos", "Bienestar")
    )

    fun suggestTags(text: String): List<String> {
        val lower = text.lowercase()
        val suggested = mutableSetOf<String>()

        for ((keywords, tags) in keywordTagMap) {
            if (keywords.any { lower.contains(it) }) {
                suggested.addAll(tags)
            }
        }

        if (suggested.isEmpty()) {
            suggested.add("General")
            suggested.add("Productividad")
        }

        return suggested.take(4).toList()
    }
}
