package com.runt9.cashflow.controller

import com.runt9.cashflow.model.RefreshType
import com.runt9.cashflow.model.dto.BankLogin
import com.runt9.cashflow.service.BankService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.transaction.Transactional

@RestController
@RequestMapping("/api/banks")
@Transactional
class BankController(
        private val bankService: BankService
) {
    // TODO: Use session, not request param
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBank(@RequestBody loginInfo: BankLogin, @RequestParam(required = true) encryptionKey: String) {
        bankService.createBank(loginInfo, encryptionKey)
    }

    @GetMapping("/refresh")
    fun refreshBanks(@RequestParam(required = true) encryptionKey: String, @RequestParam refreshType: RefreshType = RefreshType.PARTIAL) {
        bankService.refreshBanks(encryptionKey, refreshType)
    }
}