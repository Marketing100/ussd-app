package sn.mo.ussdapp.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat

data class SimSlot(
    val displayName: String,
    val subscriptionId: Int,
    val phoneAccountHandle: PhoneAccountHandle?
)

class UssdDialer(private val context: Context) {

    private fun hasPermissions(): Boolean {
        val callPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
        val phoneStatePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
        return callPermission == PackageManager.PERMISSION_GRANTED &&
            phoneStatePermission == PackageManager.PERMISSION_GRANTED
    }

    fun listActiveSims(): List<SimSlot> {
        if (!hasPermissions()) return emptyList()

        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)
            as SubscriptionManager
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        val activeSubscriptions: List<SubscriptionInfo> =
            subscriptionManager.activeSubscriptionInfoList ?: emptyList()

        val phoneAccounts = telecomManager.callCapablePhoneAccounts

        return activeSubscriptions.map { sub ->
            val matchingAccount = phoneAccounts.firstOrNull { handle ->
                val account = telecomManager.getPhoneAccount(handle)
                account?.address?.schemeSpecificPart?.contains(sub.number.orEmpty()) == true ||
                    handle.id.contains(sub.subscriptionId.toString())
            }
            SimSlot(
                displayName = sub.displayName?.toString() ?: "SIM ${sub.simSlotIndex + 1}",
                subscriptionId = sub.subscriptionId,
                phoneAccountHandle = matchingAccount
            )
        }
    }

    fun dial(ussdCode: String, sim: SimSlot) {
        if (!hasPermissions()) {
            throw SecurityException("CALL_PHONE / READ_PHONE_STATE non accordees")
        }

        val encoded = Uri.encode(ussdCode)
        val uri = Uri.parse("tel:$encoded")

        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val extras = android.os.Bundle().apply {
            sim.phoneAccountHandle?.let {
                putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, it)
            }
        }

        telecomManager.placeCall(uri, extras)
    }

    companion object {
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE
        )
    }
}
