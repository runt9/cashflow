package com.runt9.cashflow.repository

import com.runt9.cashflow.model.entity.Bank
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface BankRepository : CrudRepository<Bank, Long>