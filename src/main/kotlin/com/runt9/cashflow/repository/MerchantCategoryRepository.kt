package com.runt9.cashflow.repository

import com.runt9.cashflow.model.entity.MerchantCategory
import com.runt9.cashflow.model.entity.MerchantCategoryKey
import org.springframework.data.repository.CrudRepository

interface MerchantCategoryRepository : CrudRepository<MerchantCategory, MerchantCategoryKey> {
    fun findByMerchant_Name(name: String): MerchantCategory?
}