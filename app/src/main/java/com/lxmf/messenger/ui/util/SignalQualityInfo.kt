package com.lxmf.messenger.ui.util

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lxmf.messenger.R
import java.util.Locale

// Color constants for signal quality indicators
private val ColorExcellent = Color(0xFF4CAF50) // Green
private val ColorGood = Color(0xFF8BC34A) // Light Green
private val ColorFair = Color(0xFFFFC107) // Amber
private val ColorPoor = Color(0xFFFF9800) // Orange
private val ColorVeryPoor = Color(0xFFF44336) // Red

/**
 * Information about signal quality for display in UI.
 * Used for both RSSI (signal strength) and SNR (signal-to-noise ratio) metrics.
 */
data class SignalQualityInfo(
    val icon: ImageVector,
    val color: Color,
    val text: String,
    val subtitle: String,
)

/**
 * Get signal strength (RSSI) display information.
 *
 * RSSI thresholds are based on typical LoRa/BLE radio characteristics:
 * - Excellent: > -50 dBm (very close proximity or ideal conditions)
 * - Good: -50 to -70 dBm (reliable communication)
 * - Fair: -70 to -85 dBm (usable but may have occasional issues)
 * - Weak: -85 to -100 dBm (marginal, may need retries)
 * - Very Weak: < -100 dBm (at the edge of usability)
 *
 * @param rssi RSSI value in dBm (typically -30 to -120)
 * @return SignalQualityInfo with appropriate icon, color, and text
 */
fun getRssiInfo(rssi: Int): SignalQualityInfo =
    when {
        rssi > -50 ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellular4Bar,
                color = ColorExcellent,
                text = "Excellent ($rssi dBm)",
                subtitle = "Very strong signal",
            )
        rssi > -70 ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellular4Bar,
                color = ColorGood,
                text = "Good ($rssi dBm)",
                subtitle = "Strong signal",
            )
        rssi > -85 ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellularAlt,
                color = ColorFair,
                text = "Fair ($rssi dBm)",
                subtitle = "Moderate signal strength",
            )
        rssi > -100 ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellularAlt,
                color = ColorPoor,
                text = "Weak ($rssi dBm)",
                subtitle = "Low signal, may be unreliable",
            )
        else ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellularConnectedNoInternet0Bar,
                color = ColorVeryPoor,
                text = "Very Weak ($rssi dBm)",
                subtitle = "Marginal signal at edge of range",
            )
    }

fun getRssiInfo(
    context: Context,
    rssi: Int,
): SignalQualityInfo =
    when {
        rssi > -50 ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellular4Bar,
                color = ColorExcellent,
                text = context.getString(R.string.signal_quality_rssi_excellent_text, rssi),
                subtitle = context.getString(R.string.signal_quality_rssi_excellent_subtitle),
            )
        rssi > -70 ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellular4Bar,
                color = ColorGood,
                text = context.getString(R.string.signal_quality_rssi_good_text, rssi),
                subtitle = context.getString(R.string.signal_quality_rssi_good_subtitle),
            )
        rssi > -85 ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellularAlt,
                color = ColorFair,
                text = context.getString(R.string.signal_quality_rssi_fair_text, rssi),
                subtitle = context.getString(R.string.signal_quality_rssi_fair_subtitle),
            )
        rssi > -100 ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellularAlt,
                color = ColorPoor,
                text = context.getString(R.string.signal_quality_rssi_weak_text, rssi),
                subtitle = context.getString(R.string.signal_quality_rssi_weak_subtitle),
            )
        else ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellularConnectedNoInternet0Bar,
                color = ColorVeryPoor,
                text = context.getString(R.string.signal_quality_rssi_very_weak_text, rssi),
                subtitle = context.getString(R.string.signal_quality_rssi_very_weak_subtitle),
            )
    }

/**
 * Get signal quality (SNR) display information.
 *
 * SNR thresholds are based on typical LoRa radio characteristics:
 * - Excellent: > 10 dB (clear signal, very high reliability)
 * - Good: 5 to 10 dB (reliable communication)
 * - Fair: 0 to 5 dB (usable but may have some noise)
 * - Poor: -5 to 0 dB (noisy, may need retries)
 * - Very Poor: < -5 dB (very noisy, at the edge of decodability)
 *
 * @param snr SNR value in dB (typically -20 to +20)
 * @return SignalQualityInfo with appropriate icon, color, and text
 */
fun getSnrInfo(snr: Float): SignalQualityInfo {
    val snrStr = String.format(Locale.US, "%.1f", snr)
    return when {
        snr > 10f ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellular4Bar,
                color = ColorExcellent,
                text = "Excellent ($snrStr dB)",
                subtitle = "Clear signal, very low noise",
            )
        snr > 5f ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellular4Bar,
                color = ColorGood,
                text = "Good ($snrStr dB)",
                subtitle = "Low noise level",
            )
        snr > 0f ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellularAlt,
                color = ColorFair,
                text = "Fair ($snrStr dB)",
                subtitle = "Moderate noise level",
            )
        snr > -5f ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellularAlt,
                color = ColorPoor,
                text = "Poor ($snrStr dB)",
                subtitle = "High noise, may be unreliable",
            )
        else ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellularConnectedNoInternet0Bar,
                color = ColorVeryPoor,
                text = "Very Poor ($snrStr dB)",
                subtitle = "Very noisy, at decodability limit",
            )
    }
}

fun getSnrInfo(
    context: Context,
    snr: Float,
): SignalQualityInfo {
    val snrStr = String.format(Locale.US, "%.1f", snr)
    return when {
        snr > 10f ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellular4Bar,
                color = ColorExcellent,
                text = context.getString(R.string.signal_quality_snr_excellent_text, snrStr),
                subtitle = context.getString(R.string.signal_quality_snr_excellent_subtitle),
            )
        snr > 5f ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellular4Bar,
                color = ColorGood,
                text = context.getString(R.string.signal_quality_snr_good_text, snrStr),
                subtitle = context.getString(R.string.signal_quality_snr_good_subtitle),
            )
        snr > 0f ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellularAlt,
                color = ColorFair,
                text = context.getString(R.string.signal_quality_snr_fair_text, snrStr),
                subtitle = context.getString(R.string.signal_quality_snr_fair_subtitle),
            )
        snr > -5f ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellularAlt,
                color = ColorPoor,
                text = context.getString(R.string.signal_quality_snr_poor_text, snrStr),
                subtitle = context.getString(R.string.signal_quality_snr_poor_subtitle),
            )
        else ->
            SignalQualityInfo(
                icon = Icons.Default.SignalCellularConnectedNoInternet0Bar,
                color = ColorVeryPoor,
                text = context.getString(R.string.signal_quality_snr_very_poor_text, snrStr),
                subtitle = context.getString(R.string.signal_quality_snr_very_poor_subtitle),
            )
    }
}
