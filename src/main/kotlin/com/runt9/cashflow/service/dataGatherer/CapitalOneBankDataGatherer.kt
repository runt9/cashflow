package com.runt9.cashflow.service.dataGatherer

import com.runt9.cashflow.model.entity.Account
import com.runt9.cashflow.model.entity.AccountType
import com.runt9.cashflow.model.entity.Bank
import com.runt9.cashflow.model.entity.Transaction
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class CapitalOneBankDataGatherer : DataGatherer {
    private val driver: ChromeDriver

    init {
        System.setProperty("webdriver.chrome.driver", "/usr/lib/chromium-browser/chromedriver")
        val options = ChromeOptions()
        options.addArguments("--window-size=1920,1080")
        driver = ChromeDriver(options)
    }

    override fun login(username: String, password: String) {
        driver.get("https://capitalone.com")
        driver.findElement(By.id("btnLoginAccountTypeNew")).click()
        WebDriverWait(driver, 1).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"account-log-in-new\"]/fieldset/ul/li[2]/label")))
        driver.findElement(By.xpath("//*[@id=\"account-log-in-new\"]/fieldset/ul/li[2]/label")).click()
        driver.findElement(By.id("login-hb-uid")).sendKeys(username)
        driver.findElement(By.id("login-hb-pw")).sendKeys(password)
        driver.findElement(By.id("login-submit-bank-hb")).click()
        WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.className("account_list")))
    }

    override fun gatherAccounts(bank: Bank): List<Account> {
        return driver.findElements(By.id("per_accts"))
                .map { accountElement ->
                    return@map Account(
                            accountType = AccountType.BANK_ACCOUNT,
                            name = accountElement.findElement(By.cssSelector("a")).getAttribute("title").trim(),
                            balance = BigDecimal(accountElement.findElement(By.className("act_bal")).text.replace(Regex("[^\\d.]"), "")),
                            bank = bank
                    )
                }
    }

    override fun getAccountTransactions(account: Account): List<Transaction> {
        Thread.sleep(1000L)
        driver.findElement(By.xpath("//*[@title='${account.name}\n']")).click()

        WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.id("sortingTable")))

        val transactions = driver.findElements(By.cssSelector(".pending-row, .drawer-row")).map { ele ->
            val date = LocalDate.parse(ele.getAttribute("data-posted-date"), DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            val amount = BigDecimal(ele.findElement(By.className("amount")).text.replace(Regex("[^\\d.]"), ""))
            val description = ele.findElement(By.className("description")).text.trim()

            val status = when {
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

        driver.close()
        return transactions
    }
}