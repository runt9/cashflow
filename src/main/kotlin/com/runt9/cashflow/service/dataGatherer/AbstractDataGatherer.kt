package com.runt9.cashflow.service.dataGatherer

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

abstract class AbstractDataGatherer : DataGatherer {
    protected val driver: ChromeDriver

    init {
        System.setProperty("webdriver.chrome.driver", "/usr/lib/chromium-browser/chromedriver")
        val options = ChromeOptions()
        options.addArguments("--window-size=1920,1080")
        driver = ChromeDriver(options)
    }

    override fun cleanup() {
        driver.close()
    }

    protected fun waitForVisibility(by: By) {
        WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOfElementLocated(by))
    }

    protected fun WebElement.toBigDecimal() = BigDecimal(text.replace(Regex("[^\\d.-]"), ""))

    protected fun String.toLocalDate(format: String) = LocalDate.parse(this, DateTimeFormatter.ofPattern(format))!!
}