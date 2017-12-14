package com.runt9.cashflow.model.entity

import java.io.Serializable
import javax.persistence.*

@Entity
data class MerchantCategory(
        @EmbeddedId
        val id: MerchantCategoryKey = MerchantCategoryKey(),

        @ManyToOne
        @MapsId("merchantId")
        val merchant: Merchant = Merchant(),

        @ManyToOne
        @MapsId("categoryId")
        val category: Category = Category()
)

@Embeddable
data class MerchantCategoryKey(val merchantId: Long = 0, val categoryId: Long = 0) : Serializable