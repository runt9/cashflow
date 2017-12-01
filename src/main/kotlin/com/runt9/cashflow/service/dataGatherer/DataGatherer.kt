package com.runt9.cashflow.service.dataGatherer

import com.runt9.cashflow.model.entity.Account
import com.runt9.cashflow.model.entity.Bank
import com.runt9.cashflow.model.entity.Transaction

interface DataGatherer {
    fun login(username: String, password: String)
    fun gatherAccounts(bank: Bank): List<Account>
    fun getAccountTransactions(account: Account): List<Transaction>
}