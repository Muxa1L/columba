package com.lxmf.messenger.ui.screens

import androidx.annotation.StringRes
import com.lxmf.messenger.R

/**
 * Tabs for the Contacts screen.
 *
 * MY_CONTACTS: Shows saved contacts with location sharing indicators
 * NETWORK: Shows network announces (discovered peers)
 */
enum class ContactsTab(
    @StringRes val labelRes: Int,
    val displayName: String,
) {
    MY_CONTACTS(R.string.contacts_tab_my_contacts, "My Contacts"),
    NETWORK(R.string.contacts_tab_network, "Network"),
}
