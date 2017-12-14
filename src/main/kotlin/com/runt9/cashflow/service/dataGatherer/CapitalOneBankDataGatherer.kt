package com.runt9.cashflow.service.dataGatherer

import com.runt9.cashflow.model.RefreshType
import com.runt9.cashflow.model.entity.Account
import com.runt9.cashflow.model.entity.AccountType
import com.runt9.cashflow.model.entity.Bank
import com.runt9.cashflow.model.entity.Transaction
import com.runt9.cashflow.service.MerchantService
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class CapitalOneBankDataGatherer(merchantService: MerchantService) : AbstractDataGatherer(merchantService) {
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

    override fun getAccountTransactions(account: Account, refreshType: RefreshType): List<Transaction> {
        driver.findElement(By.xpath("//*[@title='${account.name}\n']")).click()

        waitForVisibility(By.id("sortingTable"))

        driver.findElement(By.className("filter_d")).click()
        waitForVisibility(By.id("fixed_filter_date"))
        Select(driver.findElement(By.id("fixed_filter_date"))).selectByValue("custom")
        val startDate = if (refreshType == RefreshType.FULL || account.lastRefresh == null) LocalDate.now().minusDays(364) else account.lastRefresh!!
        val endDate = startDate.plusDays(364).clamp()

        driver.findElement(By.id("from_date")).sendKeys(startDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")))
        driver.findElement(By.id("to_date")).sendKeys(endDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")))
        val search = driver.findElement(By.id("findTransactionByDate"))
        search.click()
        WebDriverWait(driver, 30).until(ExpectedConditions.attributeContains(search, "class", "spinner")).not()

        return driver.findElements(By.cssSelector(".pending-row, .drawer-row")).map {
            val date = it.getAttribute("data-posted-date").toLocalDate("M/dd/yyyy")
            val amount = it.findElement(By.className("amount")).toBigDecimal()
            val description = it.findElement(By.className("description")).text.trim()

            val status = when {
                description == "IOD INTEREST PAID" -> Transaction.Type.InterestEarned
                amount.compareTo(BigDecimal.ZERO) == 1 && description.contains(Regex("\\s+deposit\\s+")) -> Transaction.Type.Income // TODO: Split between income/return
                description.contains(Regex("\\s+fee\\s+")) -> Transaction.Type.Fee
                description.contains(Regex("\\s+(PMT|EPAY)\\s+")) -> Transaction.Type.Payment
                else -> Transaction.Type.Sale
            }

            return@map Transaction(
                    account = account,
                    date = date,
                    type = status,
                    merchant = merchantService.getOrCreateMerchant(description),
                    amount = amount,
                    category = merchantService.getCategoryForMerchant(description)
            )
        }
    }
}