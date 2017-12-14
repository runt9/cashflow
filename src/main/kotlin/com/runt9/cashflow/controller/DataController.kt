package com.runt9.cashflow.controller

import com.runt9.cashflow.model.dto.CategoryByMonthTable
import com.runt9.cashflow.service.DataService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/data")
class DataController(val dataService: DataService) {
    @GetMapping("/categoryByMonthTable")
    fun getCategoryByMonthTable(): List<CategoryByMonthTable> = dataService.getCategoryByMonthTable()
}