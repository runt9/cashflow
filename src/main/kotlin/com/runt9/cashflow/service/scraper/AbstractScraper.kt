package com.runt9.cashflow.service.scraper

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

abstract class AbstractScraper(url: String) : Scraper {
    protected val driver: ChromeDriver

    init {
        System.setProperty("webdriver.chrome.driver", "/usr/lib/chromium-browser/chromedriver")
        val options = ChromeOptions()
        options.addArguments("--window-size=1920,1080")
        driver = ChromeDriver(options)
        driver.get(url)
    }

    override fun cleanup() {
        driver.quit()
    }
}