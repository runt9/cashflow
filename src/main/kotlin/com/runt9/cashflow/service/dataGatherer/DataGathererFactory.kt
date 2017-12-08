package com.runt9.cashflow.service.dataGatherer

import com.runt9.cashflow.model.entity.BankType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class DataGathererFactory(private val restTemplate: RestTemplate) {
    fun loadDataGatherer(bankType: BankType): DataGatherer = when (bankType) {
        BankType.CHASE -> ChaseDataGatherer(restTemplate)
        BankType.CAPITALONE_CC -> CapitalOneCcDataGatherer(restTemplate)
        BankType.CAPITALONE_BANK -> CapitalOneBankDataGatherer()
        else -> {
            throw RuntimeException("Invalid bank type $bankType")
        }
    }
}