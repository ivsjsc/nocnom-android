package vn.ivsjsc.nocnom.domain.nutrition

import kotlin.math.abs
import kotlin.math.round
import vn.ivsjsc.nocnom.domain.model.MacroEnergyConsistency

data class MacroEnergyAssessment(
    val macroEnergyKcal: Double? = null,
    val deltaPct: Double? = null,
    val consistency: MacroEnergyConsistency,
)

private fun round1(value: Double): Double = round(value * 10.0) / 10.0

fun calculateMacroEnergyKcal(proteinG: Double, carbsG: Double, fatG: Double): Double =
    round1(proteinG * 4.0 + carbsG * 4.0 + fatG * 9.0)

fun assessMacroEnergyConsistency(
    calories: Double,
    proteinG: Double?,
    carbsG: Double?,
    fatG: Double?,
): MacroEnergyAssessment {
    val complete = proteinG != null && carbsG != null && fatG != null &&
        proteinG in 0.0..500.0 && carbsG in 0.0..500.0 && fatG in 0.0..500.0
    if (calories !in 1.0..5000.0 || !complete) {
        return MacroEnergyAssessment(consistency = MacroEnergyConsistency.NOT_APPLICABLE)
    }

    val energy = calculateMacroEnergyKcal(proteinG!!, carbsG!!, fatG!!)
    val delta = round1(abs(energy - calories) / calories * 100.0)
    val consistency = when {
        delta <= 10.0 -> MacroEnergyConsistency.CONSISTENT
        delta <= 20.0 -> MacroEnergyConsistency.REVIEW
        else -> MacroEnergyConsistency.INCONSISTENT
    }
    return MacroEnergyAssessment(energy, delta, consistency)
}
