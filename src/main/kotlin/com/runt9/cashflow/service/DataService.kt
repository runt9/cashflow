package com.runt9.cashflow.service

import com.runt9.cashflow.model.dto.CategoryByMonthTable
import com.runt9.cashflow.model.entity.AccountType
import com.runt9.cashflow.model.entity.Transaction
import com.runt9.cashflow.repository.TransactionRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

@Service
class DataService(val transactionRepository: TransactionRepository) {
    fun getCategoryByMonthTable(): List<CategoryByMonthTable> {
        return transactionRepository.findAll()
                .filter { it.type != Transaction.Type.Payment }
                .sortedBy { it.date }
                .groupBy { Pair<String, String>(it.category?.name ?: "Uncategorized", it.date.format(DateTimeFormatter.ofPattern("MMM yyyy"))) }
                .map { (pair, rows) -> CategoryByMonthTable(
                        category = pair.first,
                        month = pair.second,
                        amount = rows.map { if (it.account.accountType == AccountType.CREDIT_CARD) -it.amount else it.amount }.reduce(BigDecimal::add))
                }
    }
}