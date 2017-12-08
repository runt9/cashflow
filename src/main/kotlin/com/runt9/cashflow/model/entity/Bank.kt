package com.runt9.cashflow.model.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*

@Entity
data class Bank(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Long = 0,

        @Enumerated(EnumType.STRING)
        val bankType: BankType = BankType.NONE,

        @JsonIgnore
        @Column(length = 1000)
        var loginData: String = ""
)