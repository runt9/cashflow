package com.runt9.cashflow.service.dataGatherer

import com.runt9.cashflow.model.entity.Account
import com.runt9.cashflow.model.entity.AccountType
import com.runt9.cashflow.model.entity.Bank
import com.runt9.cashflow.model.entity.Transaction
import org.openqa.selenium.By
import java.math.BigDecimal


class CapitalOneBankDataGatherer : AbstractDataGatherer() {
    override fun login(username: String, password: String) {
        driver.get("https://capitalone.com")
        driver.findElement(By.id("btnLoginAccountTypeNew")).click()
        waitForVisibility(By.xpath("//*[@id=\"account-log-in-new\"]/fieldset/ul/li[2]/label"))
        driver.findElement(By.xpath("//*[@id=\"account-log-in-new\"]/fieldset/ul/li[2]/label")).click()
        driver.findElement(By.id("login-hb-uid")).sendKeys(username)
        driver.findElement(By.id("login-hb-pw")).sendKeys(password)
        driver.findElement(By.id("login-submit-bank-hb")).click()
        waitForVisibility(By.id("per_accts"))
    }

    override fun gatherAccounts(bank: Bank): List<Account> {
        return driver.findElements(By.id("per_accts")).map {
            return@map Account(
                    bank = bank,
                    accountType = AccountType.BANK_ACCOUNT,
                    name = it.findElement(By.cssSelector("a")).getAttribute("title").trim(),
                    balance = it.findElement(By.className("act_bal")).toBigDecimal()
            )
        }
    }

    override fun getAccountTransactions(account: Account): List<Transaction> {
        // TODO: Older transactions
        driver.findElement(By.xpath("//*[@title='${account.name}\n']")).click()

        waitForVisibility(By.id("sortingTable"))

        return driver.findElements(By.cssSelector(".pending-row, .drawer-row")).map {
            val date = it.getAttribute("data-posted-date").toLocalDate("MM/dd/yyyy")
            val amount = it.findElement(By.className("amount")).toBigDecimal()
            val description = it.findElement(By.className("description")).text.trim()

            val status = when {
                description == "IOD INTEREST PAID" -> Transaction.Type.InterestEarned
                amount.compareTo(BigDecimal.ZERO) == 1 && description.matches(Regex("\\s+deposit\\s+")) -> Transaction.Type.Income // TODO: Split between income/return
                description.matches(Regex("\\s+fee\\s+")) -> Transaction.Type.Fee
                description.matches(Regex("\\s+(PMT|EPAY)\\s+")) -> Transaction.Type.Payment
                else -> Transaction.Type.Sale
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