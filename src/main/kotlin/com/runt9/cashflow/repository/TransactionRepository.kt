package com.runt9.cashflow.repository

import com.runt9.cashflow.model.entity.Account
import com.runt9.cashflow.model.entity.Transaction
import org.springframework.data.repository.CrudRepository

interface TransactionRepository : CrudRepository<Transaction, Long> {
    fun findByAccount(account: Account): List<Transaction>
}