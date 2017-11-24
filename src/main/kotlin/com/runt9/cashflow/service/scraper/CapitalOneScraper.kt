package com.runt9.cashflow.service.scraper

import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait


class CapitalOneScraper : AbstractScraper("https://capitalone.com") {
    override fun login(username: String, password: String) {
        driver.findElement(By.id("btnLoginAccountTypeNew")).click()
        WebDriverWait(driver, 1).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"account-log-in-new\"]/fieldset/ul/li[2]/label")))
        driver.findElement(By.xpath("//*[@id=\"account-log-in-new\"]/fieldset/ul/li[2]/label")).click()
        driver.findElement(By.id("login-hb-uid")).sendKeys(username)
        driver.findElement(By.id("login-hb-pw")).sendKeys(password)
        driver.findElement(By.id("login-submit-bank-hb")).click()
    }
}