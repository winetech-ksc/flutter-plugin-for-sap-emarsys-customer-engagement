package com.emarsys.emarsys_sdk.commands

import com.emarsys.Emarsys
import com.emarsys.config.EmarsysConfig
import com.emarsys.emarsys_sdk.EmarsysCommand
import com.emarsys.emarsys_sdk.PushTokenHolder
import com.emarsys.emarsys_sdk.config.ConfigStorageKeys
import com.emarsys.emarsys_sdk.di.dependencyContainer


class SetupCommand : EmarsysCommand {

    override fun execute(parameters: Map<String, Any?>?, resultCallback: ResultCallback) {
        val sharedPreferences = dependencyContainer().sharedPreferences
        val sharedPreferencesEdit = sharedPreferences.edit()
        val contactFieldId: Int? = parameters?.get("contactFieldId") as Int?
        if (parameters != null && contactFieldId != null) {
            sharedPreferencesEdit.putInt(ConfigStorageKeys.CONTACT_FIELD_ID.name, contactFieldId)
            val configBuild = EmarsysConfig.Builder()
                .application(dependencyContainer().application)
                .contactFieldId(contactFieldId)

            (parameters["mobileEngageApplicationCode"] as String?).let {
                configBuild.mobileEngageApplicationCode(it)
                sharedPreferencesEdit.putString(
                    ConfigStorageKeys.MOBILE_ENGAGE_APPLICATION_CODE.name,
                    it
                )
            }

            (parameters["predictMerchantId"] as String?).let {
                configBuild.predictMerchantId(it)
                sharedPreferencesEdit.putString(
                    ConfigStorageKeys.PREDICT_MERCHANT_ID.name,
                    it
                )
            }

            val androidDisableAutomaticPushTokenSending =
                ((parameters["androidDisableAutomaticPushTokenSending"] as Boolean?) ?: false).also {
                    if (it) {
                        configBuild.disableAutomaticPushTokenSending()
                    }
                    sharedPreferencesEdit.putBoolean(
                        ConfigStorageKeys.ANDROID_DISABLE_AUTOMATIC_PUSH_TOKEN_SENDING.name,
                        it
                    )
                }

            (parameters["androidSharedPackageNames"] as List<String>?)?.let {
                configBuild.sharedPackageNames(parameters["androidSharedPackageNames"] as List<String>)
                sharedPreferencesEdit.putStringSet(
                    ConfigStorageKeys.ANDROID_SHARED_PACKAGE_NAMES.name,
                    it?.let { packageNames -> mutableSetOf(*packageNames.toTypedArray()) }
                )
            }

            (parameters["androidSharedSecret"] as String?)?.let {
                configBuild.sharedSecret(parameters["androidSharedSecret"] as String)
                sharedPreferencesEdit.putString(
                    ConfigStorageKeys.ANDROID_SHARED_SECRET.name,
                    it
                )
            }

            ((parameters["androidVerboseConsoleLoggingEnabled"] as Boolean?) ?: false).let {
                if (it) {
                    configBuild.enableVerboseConsoleLogging()
                }
                sharedPreferencesEdit.putBoolean(
                    ConfigStorageKeys.ANDROID_VERBOSE_CONSOLE_LOGGING_ENABLED.name,
                    it
                )
            }

            Emarsys.setup(configBuild.build())
            sharedPreferencesEdit.apply()
            if (!androidDisableAutomaticPushTokenSending) {
                if (PushTokenHolder.pushToken != null) {
                    Emarsys.push.pushToken = PushTokenHolder.pushToken
                } else {
                    PushTokenHolder.pushTokenObserver = { pushToken ->
                        pushToken?.let { Emarsys.push.pushToken = it }
                    }
                }
            }
        } else {
            throw IllegalArgumentException("ContactFieldId must not be null!")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}