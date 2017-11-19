package com.runt9.cashflow.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.runt9.cashflow.util.BCryptPasswordDeserializer
import javax.persistence.*

@Entity
data class Bank(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Long = 0,

        @Enumerated(EnumType.STRING)
        val bankType: BankType = BankType.NONE,

        val loginName: String = "",

        @JsonDeserialize(using = BCryptPasswordDeserializer::class)
        @JsonIgnore
        val password: String = ""
)