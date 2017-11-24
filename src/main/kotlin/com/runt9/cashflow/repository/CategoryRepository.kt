package com.runt9.cashflow.repository

import com.runt9.cashflow.model.entity.Category
import org.springframework.data.repository.CrudRepository

interface CategoryRepository : CrudRepository<Category, Long>