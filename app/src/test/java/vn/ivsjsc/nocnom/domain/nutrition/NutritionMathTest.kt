package vn.ivsjsc.nocnom.domain.nutrition

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import vn.ivsjsc.nocnom.domain.model.MacroEnergyConsistency

class NutritionMathTest {
    @Test
    fun macroEnergy_usesFourFourNine() {
        assertThat(calculateMacroEnergyKcal(25.0, 50.0, 10.0)).isEqualTo(390.0)
    }

    @Test
    fun missingMacro_isNotApplicable() {
        val result = assessMacroEnergyConsistency(500.0, 20.0, null, 10.0)
        assertThat(result.consistency).isEqualTo(MacroEnergyConsistency.NOT_APPLICABLE)
    }

    @Test
    fun deltaThresholds_matchWebContract() {
        val result = assessMacroEnergyConsistency(400.0, 25.0, 50.0, 10.0)
        assertThat(result.consistency).isEqualTo(MacroEnergyConsistency.CONSISTENT)
    }
}
