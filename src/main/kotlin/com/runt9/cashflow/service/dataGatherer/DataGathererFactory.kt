package com.runt9.cashflow.service.dataGatherer

import com.runt9.cashflow.model.entity.BankType
import com.runt9.cashflow.service.MerchantService
import org.springframework.stereotype.Service

@Service
class DataGathererFactory(private val merchantService: MerchantService) {
    fun loadDataGatherer(bankType: BankType): DataGatherer = when (bankType) {
        BankType.CHASE -> ChaseDataGatherer(merchantService)
        BankType.CAPITALONE_CC -> CapitalOneCcDataGatherer(merchantService)
        BankType.CAPITALONE_BANK -> CapitalOneBankDataGatherer(merchantService)
        else -> {
            throw RuntimeException("Invalid bank type $bankType")
        }
    }
}