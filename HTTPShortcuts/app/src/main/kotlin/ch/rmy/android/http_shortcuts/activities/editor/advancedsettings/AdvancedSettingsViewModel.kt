package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.commitAsync
import ch.rmy.android.http_shortcuts.extensions.context
import ch.rmy.android.http_shortcuts.utils.StringUtils
import io.reactivex.Completable

class AdvancedSettingsViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    fun setWaitForConnection(waitForConnection: Boolean): Completable =
        persistedRealm.commitAsync { realm ->
            getShortcut(realm)?.isWaitForNetwork = waitForConnection
        }

    fun setFollowRedirects(followRedirects: Boolean): Completable =
        persistedRealm.commitAsync { realm ->
            getShortcut(realm)?.followRedirects = followRedirects
        }

    fun setAcceptAllCertificates(acceptAllCertificates: Boolean): Completable =
        persistedRealm.commitAsync { realm ->
            getShortcut(realm)?.acceptAllCertificates = acceptAllCertificates
        }

    fun setTimeout(timeout: Int): Completable =
        persistedRealm.commitAsync { realm ->
            getShortcut(realm)?.timeout = timeout
        }

    fun getTimeoutSubtitle(shortcut: Shortcut): CharSequence =
        getTimeoutText(shortcut.timeout)

    fun getTimeoutText(timeout: Int) =
        StringUtils.getDurationText(context, timeout)

}