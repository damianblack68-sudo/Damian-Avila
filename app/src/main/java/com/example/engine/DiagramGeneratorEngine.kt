package com.example.engine

import com.example.domain.model.MindMapNode

object DiagramGeneratorEngine {

    fun generateMindMapFromText(title: String, content: String): MindMapNode {
        val lines = content.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("# ") }

        val rootTitle = if (title.isNotBlank()) title else "Concepto Central"

        val subnodes = mutableListOf<MindMapNode>()

        if (lines.isEmpty()) {
            return MindMapNode(
                id = "root",
                label = rootTitle,
                subnodes = listOf(
                    MindMapNode("node-1", "Hipótesis / Entrada", listOf(MindMapNode("sub-1", "Variables"), MindMapNode("sub-2", "Parámetros"))),
                    MindMapNode("node-2", "Proceso de Análisis", listOf(MindMapNode("sub-3", "Algoritmo"), MindMapNode("sub-4", "Filtro"))),
                    MindMapNode("node-3", "Resultado / Síntesis", listOf(MindMapNode("sub-5", "Conclusión")))
                )
            )
        }

        var currentGroup = "Fase 1"
        var currentItems = mutableListOf<MindMapNode>()
        var counter = 1

        for (line in lines) {
            val cleanLine = line.removePrefix("-").removePrefix("*").removePrefix("•").trim()
            if (cleanLine.startsWith("##") || cleanLine.endsWith(":")) {
                if (currentItems.isNotEmpty()) {
                    subnodes.add(MindMapNode("group-$counter", currentGroup, currentItems))
                    counter++
                    currentItems = mutableListOf()
                }
                currentGroup = cleanLine.replace("#", "").replace(":", "").trim()
            } else if (cleanLine.isNotBlank()) {
                val nodeLabel = if (cleanLine.length > 35) cleanLine.take(32) + "..." else cleanLine
                currentItems.add(MindMapNode("leaf-${System.currentTimeMillis()}-$counter", nodeLabel))
                counter++
            }
        }

        if (currentItems.isNotEmpty()) {
            subnodes.add(MindMapNode("group-$counter", currentGroup, currentItems))
        }

        if (subnodes.isEmpty()) {
            subnodes.add(
                MindMapNode(
                    id = "group-1",
                    label = "Puntos Clave",
                    subnodes = lines.take(3).mapIndexed { i, s ->
                        MindMapNode("leaf-$i", if (s.length > 30) s.take(27) + "..." else s)
                    }
                )
            )
        }

        return MindMapNode(
            id = "root",
            label = rootTitle,
            subnodes = subnodes
        )
    }

    fun generateFlowchartSteps(content: String): List<String> {
        val steps = mutableListOf<String>()
        steps.add("Inicio del Proceso")

        val lines = content.lines()
            .map { it.removePrefix("-").removePrefix("*").trim() }
            .filter { it.isNotBlank() && it.length > 3 }

        if (lines.isNotEmpty()) {
            for (line in lines.take(5)) {
                val stepText = if (line.length > 40) line.take(37) + "..." else line
                steps.add(stepText)
            }
        } else {
            steps.add("Recopilación y análisis de datos iniciales")
            steps.add("¿Cumple condiciones de optimización?")
            steps.add("Generar informe y exportación segura")
        }

        steps.add("Fin / Ejecución exitosa")
        return steps
    }
}
