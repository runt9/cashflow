package com.runt9.cashflow.service.dataGatherer

import com.runt9.cashflow.model.entity.BankType
import org.springframework.stereotype.Service

@Service
class DataGathererFactory {
    fun loadDataGatherer(bankType: BankType): DataGatherer = when (bankType) {
        BankType.CHASE -> ChaseDataGatherer()
        BankType.CAPITALONE_CC -> CapitalOneCcDataGatherer()
        BankType.CAPITALONE_BANK -> CapitalOneBankDataGatherer()
        else -> {
            throw RuntimeException("Invalid bank type $bankType")
        }
    }
}