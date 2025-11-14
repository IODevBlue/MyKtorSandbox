package io.github.iodevblue.sandbox.ktor.playground.eadriaticleague

import kotlinx.serialization.Serializable

@Serializable
data class EALPlayer(
    val name: String,
    val category: String,
    val points: Double
)
