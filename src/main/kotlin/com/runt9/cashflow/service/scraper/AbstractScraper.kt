package com.runt9.cashflow.service.scraper

import org.openqa.selenium.chrome.ChromeDriver

abstract class AbstractScraper(url: String) : Scraper {
    protected val driver: ChromeDriver

    init {
        System.setProperty("webdriver.chrome.driver", "/usr/lib/chromium-browser/chromedriver")
        driver = ChromeDriver()
        driver.get(url)
    }

    override fun cleanup() {
        driver.quit()
    }
}