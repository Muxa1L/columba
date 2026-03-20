package com.lxmf.messenger.ui.model

import androidx.annotation.StringRes
import com.lxmf.messenger.R
import java.util.Calendar

/**
 * Duration options for location sharing.
 *
 * @property labelRes String resource for the duration option
 * @property durationMillis Duration in milliseconds, or null for computed/indefinite durations
 */
enum class SharingDuration(
    @StringRes val labelRes: Int,
    val displayText: String,
    val durationMillis: Long?,
) {
    FIFTEEN_MINUTES(R.string.settings_location_sharing_duration_fifteen_minutes, "15 min", 15 * 60 * 1000L),
    ONE_HOUR(R.string.settings_location_sharing_duration_one_hour, "1 hour", 60 * 60 * 1000L),
    FOUR_HOURS(R.string.settings_location_sharing_duration_four_hours, "4 hours", 4 * 60 * 60 * 1000L),
    UNTIL_MIDNIGHT(R.string.settings_location_sharing_duration_until_midnight, "Until midnight", null),
    INDEFINITE(R.string.settings_location_sharing_duration_until_i_stop, "Until I stop", null),
    ;

    /**
     * Calculate the end timestamp for this sharing duration.
     *
     * @param startTimeMillis The start time in milliseconds since epoch
     * @return The end time in milliseconds since epoch, or null for INDEFINITE
     */
    fun calculateEndTime(startTimeMillis: Long = System.currentTimeMillis()): Long? {
        return when (this) {
            INDEFINITE -> null
            UNTIL_MIDNIGHT -> {
                val calendar =
                    Calendar.getInstance().apply {
                        timeInMillis = startTimeMillis
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                calendar.timeInMillis
            }
            else -> startTimeMillis + (durationMillis ?: 0L)
        }
    }
}
