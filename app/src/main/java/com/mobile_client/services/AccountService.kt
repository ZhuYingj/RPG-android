package com.mobile_client.services

import com.mobile_client.utils.Account

class AccountService private constructor() {
    companion object {
        val instance: AccountService by lazy { AccountService() }
    }

    var token: String? = null
        private set

    var accountInfo: Account? = null
        private set

    val username: String
        get() = accountInfo?.username ?: ""

    fun setAccount(account: Account, token: String) {
        this.accountInfo = account
        this.token = token
    }

    fun clear() {
        token = null
        accountInfo = null
    }
}
