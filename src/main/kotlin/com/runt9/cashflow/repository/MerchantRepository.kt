package com.runt9.cashflow.repository

import com.runt9.cashflow.model.entity.Merchant
import org.springframework.data.repository.CrudRepository

interface MerchantRepository : CrudRepository<Merchant, Long> {
    fun findByName(name: String): Merchant?
}