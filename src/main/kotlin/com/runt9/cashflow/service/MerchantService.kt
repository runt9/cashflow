package com.runt9.cashflow.service

import com.runt9.cashflow.model.entity.Category
import com.runt9.cashflow.model.entity.Merchant
import com.runt9.cashflow.repository.MerchantCategoryRepository
import com.runt9.cashflow.repository.MerchantRepository
import org.springframework.stereotype.Service

@Service
class MerchantService(private val merchantRepository: MerchantRepository, private val merchantCategoryRepository: MerchantCategoryRepository) {
    fun getOrCreateMerchant(name: String) = (merchantRepository.findByName(name) ?: merchantRepository.save(Merchant(name = name)))!!
    fun getCategoryForMerchant(merchant: String): Category? = merchantCategoryRepository.findByMerchant_Name(merchant)?.category
}