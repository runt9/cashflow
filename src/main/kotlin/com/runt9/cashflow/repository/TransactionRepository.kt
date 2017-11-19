package com.runt9.cashflow.repository

import com.runt9.cashflow.model.Transaction
import org.springframework.data.repository.CrudRepository

interface TransactionRepository : CrudRepository<Transaction, Long>