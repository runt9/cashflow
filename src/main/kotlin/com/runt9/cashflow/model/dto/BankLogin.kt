package com.runt9.cashflow.model.dto

import com.runt9.cashflow.model.entity.BankType
import java.io.Serializable

data class BankLogin (val bankType: BankType = BankType.NONE, val loginName: String = "", val password: String = "") : Serializable
