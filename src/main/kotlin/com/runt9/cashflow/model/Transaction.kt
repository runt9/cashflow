package com.runt9.cashflow.model

import java.math.BigDecimal
import javax.persistence.*

@Entity
data class Transaction(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Long = 0,

        @ManyToOne
        val account: Account = Account(),

        @ManyToOne
        val category: Category? = null,

        val amount: BigDecimal = BigDecimal.ZERO
)