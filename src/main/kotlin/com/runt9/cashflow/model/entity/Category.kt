package com.runt9.cashflow.model.entity

import javax.persistence.*

@Entity
data class Category(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Long = 0,

        @OneToOne
        val parent: Category? = null,

        val name: String = ""
)