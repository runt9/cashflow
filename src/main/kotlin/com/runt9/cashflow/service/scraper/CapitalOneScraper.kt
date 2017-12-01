package com.runt9.cashflow.service.scraper

import com.runt9.cashflow.model.entity.Account
import com.runt9.cashflow.model.entity.AccountType
import com.runt9.cashflow.model.entity.Bank
import com.runt9.cashflow.model.entity.Transaction
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class CapitalOneScraper : AbstractScraper("https://capitalone.com") {
    override fun login(username: String, password: String) {
        driver.findElement(By.id("btnLoginAccountTypeNew")).click()
        WebDriverWait(driver, 1).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"account-log-in-new\"]/fieldset/ul/li[2]/label")))
        driver.findElement(By.xpath("//*[@id=\"account-log-in-new\"]/fieldset/ul/li[2]/label")).click()
        driver.findElement(By.id("login-hb-uid")).sendKeys(username)
        driver.findElement(By.id("login-hb-pw")).sendKeys(password)
        driver.findElement(By.id("login-submit-bank-hb")).click()
        WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.className("account_list")))
    }

    override fun gatherAccounts(bank: Bank): List<Account> {
        return driver.findElements(By.className("account_list"))
                .filter { it.findElements(By.className("deposit_account_closed_error")).size == 0 }
                .map { accountElement ->
                    val accountType = if (accountElement.getAttribute("class").contains(Regex("(with-sm|small_biz)"))) AccountType.BANK_ACCOUNT else AccountType.CREDIT_CARD

                    return@map Account(
                            accountType = accountType,
                            name = accountElement.findElement(By.cssSelector("a")).getAttribute("title"),
                            balance = BigDecimal(accountElement.findElement(By.className("act_bal")).text.replace(Regex("[^\\d.]"), "")),
                            bank = bank
                    )
                }
    }

    override fun getAccountTransactions(account: Account): List<Transaction> {
        Thread.sleep(1000L)
        driver.findElement(By.xpath("//*[@title='${account.name}']")).click()

        val transactions = if (account.accountType == AccountType.CREDIT_CARD) getCreditCardTransactions(account) else getBankAccountTransactions(account)

        driver.navigate().back()
        return transactions
    }

    private fun getBankAccountTransactions(account: Account): List<Transaction> {
        WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.id("sortingTable")))
        // Keep loading more transactions
        while (driver.findElements(By.id("viewMoreLink")).size > 0) {
            val ele = driver.findElement(By.id("viewMoreLink"))
            (driver as JavascriptExecutor).executeScript("arguments[0].scrollIntoView(true);", ele)
            ele.click()
            Thread.sleep(1000L) // TODO: Wait with exception catch?
        }

        return driver.findElements(By.cssSelector(".pending-row, .drawer-row")).map { ele ->
            val dateStr = ele.findElement(By.className("date")).text
            val date = if (dateStr == "Pending") LocalDate.now() else LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("MMM d, yyyy"))
            val amount = BigDecimal(ele.findElement(By.className("amount")).text.replace(Regex("[^\\d.]"), ""))
            val description = ele.findElement(By.className("description")).text.trim()

            val status = when {
//                dateStr == "Pending" -> Transaction.Type.Pending
                description == "IOD INTEREST PAID" -> Transaction.Type.InterestEarned
                amount.compareTo(BigDecimal.ZERO) == 1 && description.matches(Regex("\\s+deposit\\s+")) -> Transaction.Type.Income // TODO: Split between income/return
                description.matches(Regex("\\s+fee\\s+")) -> Transaction.Type.Fee
                else -> Transaction.Type.Sale // TODO: Payment
            }

            return@map Transaction(
                    account = account,
                    date = date,
                    type = status,
                    vendor = description,
                    amount = amount
            )
        }
    }

    private fun getCreditCardTransactions(account: Account): List<Transaction> {
        WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.className("transactions-table")))

        Select(driver.findElement(By.id("dateSelect"))).selectByValue("6") // Value 6 is "Statements"
        val transactions = ArrayList<Transaction>()
        Select(driver.findElement(By.id("statement_dates"))).options.forEach {
            it.click()

            driver.findElement(By.id("drawer_toggle_ID")).click()
        }

        return driver.findElements(By.cssSelector(".pending-row, .drawer-row")).map { ele ->
            val dateStr = ele.findElement(By.className("date")).text
            val date = if (dateStr == "Pending") LocalDate.now() else LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("MMM d, yyyy"))
            val amount = BigDecimal(ele.findElement(By.className("amount")).text.replace(Regex("[^\\d.]"), ""))
            val description = ele.findElement(By.className("description")).text.trim()

            val status = when {
//                dateStr == "Pending" -> Transaction.Type.Pending
                description == "IOD INTEREST PAID" -> Transaction.Type.InterestEarned
                amount.compareTo(BigDecimal.ZERO) == 1 && description.matches(Regex("\\s+deposit\\s+")) -> Transaction.Type.Income // TODO: Split between income/return
                description.matches(Regex("\\s+fee\\s+")) -> Transaction.Type.Fee
                else -> Transaction.Type.Sale // TODO: Payment
            }

            return@map Transaction(
                    account = account,
                    date = date,
                    type = status,
                    vendor = description,
                    amount = amount
            )
        }
    }
}