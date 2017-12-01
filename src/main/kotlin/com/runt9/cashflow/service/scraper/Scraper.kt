package com.runt9.cashflow.service.scraper

import com.runt9.cashflow.model.entity.Account
import com.runt9.cashflow.model.entity.Bank
import com.runt9.cashflow.model.entity.Transaction

interface Scraper {
    fun login(username: String, password: String)
    fun cleanup()
    fun gatherAccounts(bank: Bank): List<Account>
    fun getAccountTransactions(account: Account): List<Transaction>
}