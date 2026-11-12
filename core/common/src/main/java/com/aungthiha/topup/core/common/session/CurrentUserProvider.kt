package com.aungthiha.topup.core.common.session

import kotlinx.coroutines.flow.StateFlow

/**
 * Identity of the signed-in Supabase user, derived from the active session.
 *
 * Used only to scope *local* caches (Room rows, DataStore keys) to the current account.
 * Remote reads never send this id; the backend derives the user from the session and RLS.
 */
interface CurrentUserProvider {
    /** `null` while signed out or while the session is still being restored. */
    val userId: StateFlow<String?>
}
