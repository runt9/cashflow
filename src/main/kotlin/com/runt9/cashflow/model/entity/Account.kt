package com.runt9.cashflow.model.entity

import java.math.BigDecimal
import javax.persistence.*

@Entity
data class Account(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Long = 0,

        @Enumerated(EnumType.STRING)
        val accountType: AccountType = AccountType.NONE,

        @ManyToOne
        val bank: Bank = Bank(),

        @OneToMany
        @JoinColumn(name = "account_id")
        val transactions: List<Transaction> = ArrayList(),

        val name: String = "",
        val balance: BigDecimal = BigDecimal.ZERO

)