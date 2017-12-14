package com.runt9.cashflow.model.entity

import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.*

@Entity
data class Transaction(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long = 0,

        @ManyToOne
        var account: Account = Account(),

        @ManyToOne
        val category: Category? = null,

        @Column(columnDefinition = "Date")
        var date: LocalDate = LocalDate.now(),

        @Enumerated(EnumType.STRING)
        var type: Type = Type.Sale,

        @ManyToOne
        var merchant: Merchant = Merchant(),

        var pending: Boolean = false,
        var amount: BigDecimal = BigDecimal.ZERO
) {
        enum class Type { Sale, Payment, Return, Fee, InterestEarned, Income, Pending }
}