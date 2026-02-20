package com.mobile_client.services

import com.mobile_client.utils.Account

class AccountService private constructor() {
    companion object {
        val instance: AccountService by lazy { AccountService() }
    }
    private var token: String? = null
    private var accountInfo: Account? = null

    fun setAccount(account: Account, token: String) {
        this.accountInfo = account
        this.token = token
    }

    fun getToken(): String? = token
    fun getAccountInfo(): Account? = accountInfo
    fun getUsername(): String = accountInfo?.username ?: ""
    fun clear() {
        token = null
        accountInfo = null
    }
}
