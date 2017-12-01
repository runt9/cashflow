package com.runt9.cashflow.service.scraper

import com.runt9.cashflow.model.entity.Account
import com.runt9.cashflow.model.entity.AccountType
import com.runt9.cashflow.model.entity.Bank
import com.runt9.cashflow.model.entity.Transaction
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter



class ChaseScraper : AbstractScraper("https://secure01b.chase.com/web/auth/dashboard") {
    override fun login(username: String, password: String) {
        WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(By.id("logonbox")))
        driver.switchTo().frame(driver.findElement(By.id("logonbox")))
        driver.findElement(By.id("userId-input-field")).sendKeys(username)
        driver.findElement(By.id("password-input-field")).sendKeys(password)
        driver.findElement(By.id("signin-button")).click()
        WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.id("accountsList")))
    }

    override fun gatherAccounts(bank: Bank): List<Account> {
        driver.manage().cookies
        return driver.findElements(By.cssSelector("#accountsList > .ui-listview"))
                .map { accountElement ->
                    return@map Account(
                            accountType = AccountType.CREDIT_CARD, // If get chase bank account, update this
                            name = accountElement.findElement(By.cssSelector("p.ui-li-desc")).text,
                            balance = BigDecimal(accountElement.findElement(By.className("account-value")).text.replace(Regex("[^\\d.-]"), "")),
                            bank = bank
                    )
                }
    }

    override fun getAccountTransactions(account: Account): List<Transaction> {
        val transactions = ArrayList<Transaction>()

        Thread.sleep(1000L)
        driver.findElement(By.xpath("//*[contains(text(), '${account.name}')]/../../../../..")).findElement(By.cssSelector("a.account-activity")).click()
        WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#accountactivity.ui-page-active")))
        // Keep loading more transactions
        while (driver.findElement(By.id("loadMoreActivitiesButton")).isDisplayed) {
            val ele = driver.findElement(By.id("loadMoreActivitiesButton"))
            (driver as JavascriptExecutor).executeScript("arguments[0].scrollIntoView(true);", ele)
            ele.click()
            Thread.sleep(1000L) // TODO: Wait with exception catch?
        }

        var transaction = Transaction()
        var lastLine: String? = null // TODO: Enum
        driver.findElements(By.cssSelector(".activity-list li")).forEachIndexed { i, ele ->
            if (i == 0) {
                return@forEachIndexed
            } else if (lastLine == null || lastLine == "AMOUNT") {
                transaction = Transaction(vendor = ele.text, account = account)
                lastLine = "TITLE"
            } else if (lastLine == "TITLE") {
                transaction.date = LocalDate.parse(ele.findElement(By.cssSelector("h3.account-value")).text, DateTimeFormatter.ofPattern("MMM d, yyyy"))
                lastLine = "DATE"
            } else if (lastLine == "DATE") {
                transaction.type = Transaction.Type.valueOf(ele.findElement(By.cssSelector("h3.account-value")).text)
                lastLine = "STATUS"
            } else if (lastLine == "STATUS") {
                transaction.amount = BigDecimal(ele.findElement(By.cssSelector("h3.account-value")).text.replace(Regex("[^\\d.]"), ""))
                lastLine = "AMOUNT"
                transactions.add(transaction)
            }
        }

        driver.navigate().back()
        return transactions
    }
}