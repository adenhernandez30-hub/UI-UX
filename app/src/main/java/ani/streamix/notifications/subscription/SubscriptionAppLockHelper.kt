package ani.streamix.notifications.subscription

import ani.streamix.others.calc.CalcActivity
import ani.streamix.settings.saving.PrefManager
import ani.streamix.settings.saving.PrefName

object SubscriptionAppLockHelper {
    fun isAppLocked(): Boolean {
        return PrefManager.getVal<String>(PrefName.AppPassword).isNotEmpty() && !CalcActivity.hasPermission
    }
}