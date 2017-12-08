package com.runt9.cashflow.repository

import com.runt9.cashflow.model.entity.Bank
import org.springframework.data.repository.CrudRepository

interface BankRepository : CrudRepository<Bank, Long>