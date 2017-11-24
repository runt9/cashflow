package com.runt9.cashflow.service.scraper

import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

class ChaseScraper : AbstractScraper("https://secure01b.chase.com/web/auth/dashboard") {
    override fun login(username: String, password: String) {
        WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(By.id("logonbox")))
        driver.switchTo().frame(driver.findElement(By.id("logonbox")))
        driver.findElement(By.id("userId-input-field")).sendKeys(username)
        driver.findElement(By.id("password-input-field")).sendKeys(password)
        driver.findElement(By.id("signin-button")).click()
    }
}