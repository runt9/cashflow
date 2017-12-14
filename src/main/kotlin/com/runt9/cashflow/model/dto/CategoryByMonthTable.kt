package com.runt9.cashflow.model.dto

import java.math.BigDecimal

data class CategoryByMonthTable(val category: String, val month: String, val amount: BigDecimal)