package com.example.engine

data class ScannedDocumentResult(
    val title: String,
    val extractedRawText: String,
    val detectedType: DocumentType,
    val mathematicalFormula: String? = null,
    val suggestedAction: String,
    val confidenceScore: Float = 0.98f
)

enum class DocumentType(val label: String) {
    MATH_PROBLEM("Problema Matemático"),
    STUDY_NOTE("Nota de Estudio"),
    INVOICE_REPORT("Informe / Tabla de Datos"),
    EXAM_EXERCISE("Ejercicio de Examen")
}

object DocumentScanEngine {

    fun analyzeDocument(previewText: String? = null): ScannedDocumentResult {
        if (!previewText.isNullOrBlank()) {
            val isMath = previewText.contains("=") || previewText.contains("x") || previewText.contains("∫") || previewText.contains("lim") || previewText.contains("²")
            return if (isMath) {
                ScannedDocumentResult(
                    title = "Ecuación detectada mediante Visión IA",
                    extractedRawText = previewText,
                    detectedType = DocumentType.MATH_PROBLEM,
                    mathematicalFormula = previewText,
                    suggestedAction = "Resolver y desglosar con Onyx Math Solver"
                )
            } else {
                ScannedDocumentResult(
                    title = "Documento de texto analizado",
                    extractedRawText = previewText,
                    detectedType = DocumentType.STUDY_NOTE,
                    suggestedAction = "Convertir en nota Notion con etiquetas sugeridas"
                )
            }
        }

        // Default high-fidelity scan simulation of a textbook math equation
        return ScannedDocumentResult(
            title = "Página 142: Teorema Fundamental del Cálculo",
            extractedRawText = """∫ [x² · cos(x)] dx desde 0 hasta π
Determinar el área bajo la curva mediante integración por partes:
u = x², dv = cos(x) dx
du = 2x dx, v = sin(x)""",
            detectedType = DocumentType.MATH_PROBLEM,
            mathematicalFormula = "∫ (x² · cos(x)) dx en [0, π]",
            suggestedAction = "Ejecutar resolución paso a paso y graficar curva de integrando",
            confidenceScore = 0.99f
        )
    }
}
