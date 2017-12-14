package com.runt9.cashflow.model.entity

import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.*

@Entity
data class Account(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long = 0,

        @Enumerated(EnumType.STRING)
        val accountType: AccountType = AccountType.NONE,

        @ManyToOne
        val bank: Bank = Bank(),

        @OneToMany
        @JoinColumn(name = "account_id")
        var transactions: List<Transaction> = ArrayList(),

        val accountId: String = "",
        val name: String = "",
        val balance: BigDecimal = BigDecimal.ZERO,
        var lastRefresh: LocalDate? = null
)