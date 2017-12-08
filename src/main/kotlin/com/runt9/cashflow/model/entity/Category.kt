package com.runt9.cashflow.model.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Category(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Long = 0,

        val name: String = ""
)