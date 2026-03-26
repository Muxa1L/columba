package com.lxmf.messenger.viewmodel

import android.content.Context
import com.lxmf.messenger.R

import com.lxmf.messenger.data.model.FrequencyRegion

/**
 * Validation result for a single field.
 */
data class FieldValidation(
    val isValid: Boolean,
    val errorMessage: String? = null,
)

/**
 * Validation result for the entire RNode configuration.
 */
data class ConfigValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val frequencyError: String? = null,
    val bandwidthError: String? = null,
    val spreadingFactorError: String? = null,
    val codingRateError: String? = null,
    val txPowerError: String? = null,
    val stAlockError: String? = null,
    val ltAlockError: String? = null,
)

/**
 * Input parameters for RNode configuration validation.
 */
data class RNodeConfigInput(
    val name: String,
    val frequency: String,
    val bandwidth: String,
    val spreadingFactor: String,
    val codingRate: String,
    val txPower: String,
    val stAlock: String,
    val ltAlock: String,
    val region: FrequencyRegion?,
)

/**
 * Validates RNode configuration parameters.
 *
 * Extracted from RNodeWizardViewModel to reduce class complexity and improve testability.
 */
@Suppress("TooManyFunctions")
object RNodeConfigValidator {
    // Hardware limits
    private const val MIN_BANDWIDTH = 7800
    private const val MAX_BANDWIDTH = 1625000
    private const val MIN_SF = 5
    private const val MAX_SF = 12
    private const val MIN_CR = 5
    private const val MAX_CR = 8
    private const val MIN_TX_POWER = 0
    private const val DEFAULT_MAX_TX_POWER = 22

    // Default frequency range (when no region is selected)
    private const val DEFAULT_MIN_FREQ = 137_000_000L
    private const val DEFAULT_MAX_FREQ = 3_000_000_000L

    private fun string(
        context: Context?,
        resId: Int,
        fallback: String,
        vararg args: Any,
    ): String =
        runCatching {
            if (context == null) {
                if (args.isEmpty()) fallback else fallback.format(*args)
            } else if (args.isEmpty()) {
                context.getString(resId).takeIf { it.isNotBlank() } ?: fallback
            } else {
                context.getString(resId, *args).takeIf { it.isNotBlank() } ?: fallback.format(*args)
            }
        }.getOrElse {
            if (args.isEmpty()) fallback else fallback.format(*args)
        }

    private fun invalidNumber(context: Context?): String =
        string(context, R.string.offline_map_download_invalid_number, "Invalid number")

    private fun minValue(context: Context?, value: Any): String =
        string(context, R.string.rnode_validation_min_value, "Must be >= %s", value.toString())

    private fun maxValue(context: Context?, value: Any): String =
        string(context, R.string.rnode_validation_max_value, "Must be <= %s", value.toString())

    private fun validateName(
        name: String,
        context: Context?,
    ): FieldValidation =
        if (name.isBlank()) {
            FieldValidation(false, string(context, R.string.rnode_validation_interface_name_required, "Interface name is required"))
        } else {
            FieldValidation(true)
        }

    /**
     * Validate the interface name.
     */
    fun validateName(name: String): FieldValidation {
        return validateName(name, null)
    }

    fun validateName(
        context: Context,
        name: String,
    ): FieldValidation = validateName(name, context)

    /**
     * Validate frequency against region limits.
     */
    fun validateFrequency(
        value: String,
        region: FrequencyRegion?,
    ): FieldValidation {
        return validateFrequency(value, region, null)
    }

    fun validateFrequency(
        context: Context,
        value: String,
        region: FrequencyRegion?,
    ): FieldValidation = validateFrequency(value, region, context)

    private fun validateFrequency(
        value: String,
        region: FrequencyRegion?,
        context: Context?,
    ): FieldValidation {
        val freq = value.toLongOrNull()
        val minFreq = region?.frequencyStart ?: DEFAULT_MIN_FREQ
        val maxFreq = region?.frequencyEnd ?: DEFAULT_MAX_FREQ

        return when {
            value.isBlank() -> FieldValidation(true) // Allow empty while typing
            freq == null -> FieldValidation(false, invalidNumber(context))
            freq < minFreq || freq > maxFreq -> {
                val minMhz = minFreq / 1_000_000.0
                val maxMhz = maxFreq / 1_000_000.0
                FieldValidation(
                    false,
                    string(context, R.string.rnode_validation_frequency_range, "Must be %.1f-%.1f MHz", minMhz, maxMhz),
                )
            }
            else -> FieldValidation(true)
        }
    }

    /**
     * Validate bandwidth.
     */
    fun validateBandwidth(value: String): FieldValidation {
        return validateBandwidth(value, null)
    }

    fun validateBandwidth(
        context: Context,
        value: String,
    ): FieldValidation = validateBandwidth(value, context)

    private fun validateBandwidth(
        value: String,
        context: Context?,
    ): FieldValidation {
        val bw = value.toIntOrNull()
        return when {
            value.isBlank() -> FieldValidation(true) // Allow empty while typing
            bw == null -> FieldValidation(false, invalidNumber(context))
            bw < MIN_BANDWIDTH -> FieldValidation(false, string(context, R.string.rnode_validation_bandwidth_min, "Must be >= 7.8 kHz"))
            bw > MAX_BANDWIDTH -> FieldValidation(false, string(context, R.string.rnode_validation_bandwidth_max, "Must be <= 1625 kHz"))
            else -> FieldValidation(true)
        }
    }

    /**
     * Validate spreading factor.
     */
    fun validateSpreadingFactor(value: String): FieldValidation {
        return validateSpreadingFactor(value, null)
    }

    fun validateSpreadingFactor(
        context: Context,
        value: String,
    ): FieldValidation = validateSpreadingFactor(value, context)

    private fun validateSpreadingFactor(
        value: String,
        context: Context?,
    ): FieldValidation {
        val sf = value.toIntOrNull()
        return when {
            value.isBlank() -> FieldValidation(true) // Allow empty while typing
            sf == null -> FieldValidation(false, invalidNumber(context))
            sf < MIN_SF -> FieldValidation(false, minValue(context, MIN_SF))
            sf > MAX_SF -> FieldValidation(false, maxValue(context, MAX_SF))
            else -> FieldValidation(true)
        }
    }

    /**
     * Validate coding rate.
     */
    fun validateCodingRate(value: String): FieldValidation {
        return validateCodingRate(value, null)
    }

    fun validateCodingRate(
        context: Context,
        value: String,
    ): FieldValidation = validateCodingRate(value, context)

    private fun validateCodingRate(
        value: String,
        context: Context?,
    ): FieldValidation {
        val cr = value.toIntOrNull()
        return when {
            value.isBlank() -> FieldValidation(true) // Allow empty while typing
            cr == null -> FieldValidation(false, invalidNumber(context))
            cr < MIN_CR -> FieldValidation(false, minValue(context, MIN_CR))
            cr > MAX_CR -> FieldValidation(false, maxValue(context, MAX_CR))
            else -> FieldValidation(true)
        }
    }

    /**
     * Validate TX power against region limits.
     */
    fun validateTxPower(
        value: String,
        region: FrequencyRegion?,
    ): FieldValidation {
        return validateTxPower(value, region, null)
    }

    fun validateTxPower(
        context: Context,
        value: String,
        region: FrequencyRegion?,
    ): FieldValidation = validateTxPower(value, region, context)

    private fun validateTxPower(
        value: String,
        region: FrequencyRegion?,
        context: Context?,
    ): FieldValidation {
        val maxPower = region?.maxTxPower ?: DEFAULT_MAX_TX_POWER
        val txp = value.toIntOrNull()
        return when {
            value.isBlank() -> FieldValidation(true) // Allow empty while typing
            txp == null -> FieldValidation(false, invalidNumber(context))
            txp < MIN_TX_POWER -> FieldValidation(false, minValue(context, MIN_TX_POWER))
            txp > maxPower ->
                FieldValidation(
                    false,
                    string(context, R.string.rnode_validation_tx_power_max, "Max: %d dBm", maxPower),
                )
            else -> FieldValidation(true)
        }
    }

    /**
     * Validate airtime limit against region duty cycle.
     */
    fun validateAirtimeLimit(
        value: String,
        region: FrequencyRegion?,
    ): FieldValidation {
        return validateAirtimeLimit(value, region, null)
    }

    fun validateAirtimeLimit(
        context: Context,
        value: String,
        region: FrequencyRegion?,
    ): FieldValidation = validateAirtimeLimit(value, region, context)

    private fun validateAirtimeLimit(
        value: String,
        region: FrequencyRegion?,
        context: Context?,
    ): FieldValidation {
        val maxAirtime =
            region?.let {
                if (it.dutyCycle < 100) it.dutyCycle.toDouble() else null
            }
        val parsed = value.toDoubleOrNull()
        return when {
            value.isBlank() -> FieldValidation(true) // Empty is allowed (no limit)
            parsed == null -> FieldValidation(false, invalidNumber(context))
            parsed < 0 -> FieldValidation(false, minValue(context, 0))
            parsed > 100 -> FieldValidation(false, maxValue(context, "100%"))
            maxAirtime != null && parsed > maxAirtime ->
                FieldValidation(
                    false,
                    string(context, R.string.rnode_validation_airtime_limit_max, "Max: %s%% (regional limit)", maxAirtime.toString()),
                )
            else -> FieldValidation(true)
        }
    }

    /**
     * Validate the full configuration silently (no error messages, just pass/fail).
     */
    @Suppress("ReturnCount")
    fun validateConfigSilent(config: RNodeConfigInput): Boolean {
        if (!validateName(config.name).isValid) return false
        if (!validateFrequency(config.frequency, config.region).isValid) return false
        // For silent validation, require non-empty values
        if (config.frequency.isBlank()) return false
        if (!validateBandwidth(config.bandwidth).isValid) return false
        if (config.bandwidth.isBlank()) return false
        if (!validateSpreadingFactor(config.spreadingFactor).isValid) return false
        if (config.spreadingFactor.isBlank()) return false
        if (!validateCodingRate(config.codingRate).isValid) return false
        if (config.codingRate.isBlank()) return false
        if (!validateTxPower(config.txPower, config.region).isValid) return false
        if (config.txPower.isBlank()) return false
        if (!validateAirtimeLimit(config.stAlock, config.region).isValid) return false
        if (!validateAirtimeLimit(config.ltAlock, config.region).isValid) return false
        return true
    }

    /**
     * Validate the full configuration silently (no error messages, just pass/fail).
     * Convenience overload with individual parameters.
     */
    @Suppress("LongParameterList")
    fun validateConfigSilent(
        name: String,
        frequency: String,
        bandwidth: String,
        spreadingFactor: String,
        codingRate: String,
        txPower: String,
        stAlock: String,
        ltAlock: String,
        region: FrequencyRegion?,
    ): Boolean =
        validateConfigSilent(
            RNodeConfigInput(name, frequency, bandwidth, spreadingFactor, codingRate, txPower, stAlock, ltAlock, region),
        )

    /**
     * Validate the full configuration with error messages.
     */
    @Suppress("CyclomaticComplexMethod")
    fun validateConfig(config: RNodeConfigInput): ConfigValidationResult {
        return validateConfig(config, null)
    }

    fun validateConfig(
        context: Context,
        config: RNodeConfigInput,
    ): ConfigValidationResult = validateConfig(config, context)

    @Suppress("CyclomaticComplexMethod")
    private fun validateConfig(
        config: RNodeConfigInput,
        context: Context?,
    ): ConfigValidationResult {
        val nameResult = validateName(config.name, context)
        val freqResult = validateFrequency(config.frequency, config.region, context)
        val bwResult = validateBandwidth(config.bandwidth, context)
        val sfResult = validateSpreadingFactor(config.spreadingFactor, context)
        val crResult = validateCodingRate(config.codingRate, context)
        val txpResult = validateTxPower(config.txPower, config.region, context)
        val stAlockResult = validateAirtimeLimit(config.stAlock, config.region, context)
        val ltAlockResult = validateAirtimeLimit(config.ltAlock, config.region, context)

        val frequencyError =
            if (config.frequency.isBlank()) {
                val minFreq = config.region?.frequencyStart ?: DEFAULT_MIN_FREQ
                val maxFreq = config.region?.frequencyEnd ?: DEFAULT_MAX_FREQ
                string(
                    context,
                    R.string.rnode_validation_frequency_required_range,
                    "Frequency must be %.1f-%.1f MHz",
                    minFreq / 1_000_000.0,
                    maxFreq / 1_000_000.0,
                )
            } else {
                freqResult.errorMessage
            }

        val bandwidthError =
            if (config.bandwidth.isBlank()) {
                string(context, R.string.rnode_validation_bandwidth_required_range, "Bandwidth must be 7.8-1625 kHz")
            } else {
                bwResult.errorMessage
            }

        val sfError =
            if (config.spreadingFactor.isBlank()) {
                string(context, R.string.rnode_validation_spreading_factor_required_range, "SF must be %d-%d", MIN_SF, MAX_SF)
            } else {
                sfResult.errorMessage
            }

        val crError =
            if (config.codingRate.isBlank()) {
                string(context, R.string.rnode_validation_coding_rate_required_range, "CR must be %d-%d", MIN_CR, MAX_CR)
            } else {
                crResult.errorMessage
            }

        val txPowerError =
            if (config.txPower.isBlank()) {
                val maxPower = config.region?.maxTxPower ?: DEFAULT_MAX_TX_POWER
                val regionName = config.region?.name ?: "this region"
                string(
                    context,
                    R.string.rnode_validation_tx_power_required_range_for_region,
                    "TX power must be %d-%d dBm for %s",
                    MIN_TX_POWER,
                    maxPower,
                    regionName,
                )
            } else {
                txpResult.errorMessage
            }

        val isValid =
            nameResult.isValid &&
                config.frequency.isNotBlank() && freqResult.isValid &&
                config.bandwidth.isNotBlank() && bwResult.isValid &&
                config.spreadingFactor.isNotBlank() && sfResult.isValid &&
                config.codingRate.isNotBlank() && crResult.isValid &&
                config.txPower.isNotBlank() && txpResult.isValid &&
                stAlockResult.isValid &&
                ltAlockResult.isValid

        return ConfigValidationResult(
            isValid = isValid,
            nameError = nameResult.errorMessage,
            frequencyError = frequencyError,
            bandwidthError = bandwidthError,
            spreadingFactorError = sfError,
            codingRateError = crError,
            txPowerError = txPowerError,
            stAlockError = stAlockResult.errorMessage,
            ltAlockError = ltAlockResult.errorMessage,
        )
    }

    /**
     * Validate the full configuration with error messages.
     * Convenience overload with individual parameters.
     */
    @Suppress("LongParameterList")
    fun validateConfig(
        name: String,
        frequency: String,
        bandwidth: String,
        spreadingFactor: String,
        codingRate: String,
        txPower: String,
        stAlock: String,
        ltAlock: String,
        region: FrequencyRegion?,
    ): ConfigValidationResult =
        validateConfig(
            RNodeConfigInput(name, frequency, bandwidth, spreadingFactor, codingRate, txPower, stAlock, ltAlock, region),
        )

    @Suppress("LongParameterList")
    fun validateConfig(
        context: Context,
        name: String,
        frequency: String,
        bandwidth: String,
        spreadingFactor: String,
        codingRate: String,
        txPower: String,
        stAlock: String,
        ltAlock: String,
        region: FrequencyRegion?,
    ): ConfigValidationResult =
        validateConfig(
            context,
            RNodeConfigInput(name, frequency, bandwidth, spreadingFactor, codingRate, txPower, stAlock, ltAlock, region),
        )

    /**
     * Get the maximum TX power for a region.
     */
    fun getMaxTxPower(region: FrequencyRegion?): Int {
        return region?.maxTxPower ?: DEFAULT_MAX_TX_POWER
    }

    /**
     * Get the frequency range for a region.
     */
    fun getFrequencyRange(region: FrequencyRegion?): Pair<Long, Long> {
        return if (region != null) {
            region.frequencyStart to region.frequencyEnd
        } else {
            DEFAULT_MIN_FREQ to DEFAULT_MAX_FREQ
        }
    }

    /**
     * Get the maximum airtime limit for a region (null if no limit).
     */
    fun getMaxAirtimeLimit(region: FrequencyRegion?): Double? {
        return region?.let {
            if (it.dutyCycle < 100) it.dutyCycle.toDouble() else null
        }
    }
}
