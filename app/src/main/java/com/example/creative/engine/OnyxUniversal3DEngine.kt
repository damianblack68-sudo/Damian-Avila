package com.example.creative.engine

import java.util.Locale

/**
 * ONYX UNIVERSAL 3D ENGINE
 * 
 * Misión: Transformar cualquier idea del usuario (personaje, animal, objeto, paisaje,
 * producto o logo) en un prompt profesional optimizado para generación 3D en estilo
 * cinematográfico animado híbrido Pixar x Disney x Dreamworks.
 */
object OnyxUniversal3DEngine {

    enum class SubjectCategory(val label: String, val backgroundDescriptor: String) {
        CHARACTER("character", "soft blurred studio background"),
        OBJECT("object", "soft clean studio background with ambient rim lighting"),
        ENVIRONMENT("environment", "magical sky"),
        PRODUCT("product", "dark studio with particles")
    }

    data class Universal3DPromptResult(
        val originalIdea: String,
        val enhancedIdeaEnglish: String,
        val detectedCategory: SubjectCategory,
        val finalPrompt: String,
        val negativePrompt: String
    )

    private const val NEGATIVE_PROMPT_UNIVERSAL =
        "no 2d, no flat illustration, no photorealistic ugly, no low quality, no blurry, " +
        "no distorted anatomy, no extra fingers, no extra limbs, no scary, no horror, " +
        "no watermark, no text, no signature, no deformed"

    /**
     * Detecta automáticamente si la idea corresponde a un personaje, objeto, entorno o producto.
     */
    fun detectCategory(idea: String): SubjectCategory {
        val lower = idea.lowercase(Locale.getDefault())

        val environmentKeywords = listOf(
            "paisaje", "bosque", "montaña", "playa", "ciudad", "castillo",
            "valle", "isla", "cueva", "planeta", "cielo", "espacio", "jardín",
            "calle", "habitación", "casa", "edificio", "templo", "lago", "río",
            "landscape", "forest", "mountain", "beach", "city", "castle",
            "valley", "island", "cave", "sky", "garden", "room", "house", "building"
        )
        if (environmentKeywords.any { lower.contains(it) }) {
            return SubjectCategory.ENVIRONMENT
        }

        val characterKeywords = listOf(
            "personaje", "chico", "chica", "niño", "niña", "hombre", "mujer",
            "animal", "perro", "gato", "zorro", "oso", "león", "dragón", "robot",
            "hada", "monstruo", "guerrero", "mago", "princesa", "astronauta",
            "alien", "conejo", "pájaro", "lobo", "tigre", "panda", "ratón",
            "character", "boy", "girl", "man", "woman", "kid", "dog", "cat",
            "fox", "bear", "lion", "dragon", "warrior", "wizard", "hero"
        )
        if (characterKeywords.any { lower.contains(it) }) {
            return SubjectCategory.CHARACTER
        }

        val productKeywords = listOf(
            "producto", "botella", "perfume", "zapatilla", "zapato", "reloj",
            "celular", "teléfono", "auriculares", "lata", "bebida", "empaque",
            "cosmético", "logo", "packaging", "sneaker", "watch", "perfume",
            "can", "drink", "product", "bottle", "headphone"
        )
        if (productKeywords.any { lower.contains(it) }) {
            return SubjectCategory.PRODUCT
        }

        return SubjectCategory.OBJECT
    }

    /**
     * Traduce y potencia términos del español al inglés para optimizar la respuesta de motores neuronales.
     */
    fun enhanceAndTranslateToEnglish(idea: String): String {
        var text = idea.trim()
        val translations = listOf(
            Regex("(?i)\\bun zorro\\b") to "a cute clever fox",
            Regex("(?i)\\bun gato\\b") to "an adorable playful cat",
            Regex("(?i)\\bun perro\\b") to "a joyful fluffy puppy",
            Regex("(?i)\\bun robot\\b") to "a friendly futuristic little robot",
            Regex("(?i)\\bun dragón\\b") to "a charming tiny dragon",
            Regex("(?i)\\bun oso\\b") to "a cuddly soft bear",
            Regex("(?i)\\bun conejo\\b") to "a charming fluffy bunny",
            Regex("(?i)\\bcon mochila\\b") to "wearing a tiny adventurer backpack",
            Regex("(?i)\\bcon lentes\\b") to "wearing oversized cute glasses",
            Regex("(?i)\\bcon brújula\\b") to "holding a small brass compass",
            Regex("(?i)\\buna casa\\b") to "a cozy whimsical modern house",
            Regex("(?i)\\bun carro\\b") to "a stylized aerodynamic mini concept car",
            Regex("(?i)\\bun auto\\b") to "a stylized concept car",
            Regex("(?i)\\bun café\\b") to "a warm steaming ceramic coffee cup",
            Regex("(?i)\\buna hamburguesa\\b") to "a delicious juicy stylized gourmet burger",
            Regex("(?i)\\bun teléfono\\b") to "a sleek holographic futuristic smartphone",
            Regex("(?i)\\bun reloj\\b") to "a luxury stylized chronograph wristwatch",
            Regex("(?i)\\bun perfume\\b") to "an exquisite luxury glass perfume bottle"
        )

        for ((pattern, replacement) in translations) {
            text = text.replace(pattern, replacement)
        }

        return text
    }

    /**
     * Transforma cualquier idea en el FORMATO ÚNICO UNIVERSAL en INGLÉS.
     */
    fun transformToUniversal3DPrompt(idea: String): Universal3DPromptResult {
        val category = detectCategory(idea)
        val enhancedIdea = enhanceAndTranslateToEnglish(idea)

        val finalPrompt = "Create a single image of $enhancedIdea, as a ${category.label}, " +
                "3D animated, Pixar x Disney x Dreamworks hybrid style, stylized, ultra detailed, " +
                "cute and appealing, high quality materials with subsurface scattering, " +
                "soft fluffy or glossy texture depending on subject, Unreal Engine 5 render, " +
                "Octane Render, volumetric lighting, soft studio lighting + beautiful rim light for separation, " +
                "cinematic composition, sharp focus, centered, 85mm lens, depth of field, " +
                "vibrant saturated colors, ultra clean background with ${category.backgroundDescriptor}, " +
                "8k, ultra sharp --ar 16:9"

        return Universal3DPromptResult(
            originalIdea = idea,
            enhancedIdeaEnglish = enhancedIdea,
            detectedCategory = category,
            finalPrompt = finalPrompt,
            negativePrompt = NEGATIVE_PROMPT_UNIVERSAL
        )
    }
}
