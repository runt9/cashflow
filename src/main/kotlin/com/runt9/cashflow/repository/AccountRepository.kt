package com.runt9.cashflow.repository

import com.runt9.cashflow.model.Account
import org.springframework.data.repository.CrudRepository

interface AccountRepository : CrudRepository<Account, Long>