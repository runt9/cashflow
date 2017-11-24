package com.runt9.cashflow.service.scraper

import com.runt9.cashflow.model.entity.BankType

class ScraperFactory {
    companion object {
        fun loadScraper(bankType: BankType): Scraper = when (bankType) {
            BankType.CAPITALONE -> CapitalOneScraper()
            BankType.CHASE -> ChaseScraper()
            else -> {
                throw RuntimeException("Invalid bank type $bankType")
            }
        }
    }
}