package com.runt9.cashflow.service.scraper

interface Scraper {
    fun login(username: String, password: String)
    fun cleanup()
}