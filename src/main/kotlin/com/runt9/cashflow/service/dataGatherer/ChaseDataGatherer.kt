package com.runt9.cashflow.service.dataGatherer

import com.runt9.cashflow.model.entity.Account
import com.runt9.cashflow.model.entity.AccountType
import com.runt9.cashflow.model.entity.Bank
import com.runt9.cashflow.model.entity.Transaction
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.Select


class ChaseDataGatherer : AbstractDataGatherer() {
    override fun login(username: String, password: String) {
        driver.get("https://chaseonline.chase.com/Logon.aspx")
        driver.findElement(By.id("UserID")).sendKeys(username)
        driver.findElement(By.id("Password")).sendKeys(password)
        driver.findElement(By.id("logon")).click()
        waitForVisibility(By.id("megamenu-MyAccounts"))
    }

    override fun gatherAccounts(bank: Bank): List<Account> {
        driver.get("https://cards.chase.com/CC/Account/Activity")
        waitForVisibility(By.id("ActiveAccountIndex"))
        val accountDropdown = Select(driver.findElement(By.id("ActiveAccountIndex")))
        return accountDropdown.options.filter { it.getAttribute("value").isNotEmpty() }.map { it.getAttribute("value") }.map {
            driver.get("https://cards.chase.com/CC/Account/Activity/$it")
            waitForVisibility(By.className("ui-selectmenu-status"))
            val balancePrefix = "//*[@id='AccountDetailTable' and not(contains(@class, 'noHeading'))]/tbody/tr/td[1]/table[1]/tbody/tr[%d]/td[2]"
            return@map Account(
                    bank = bank,
                    accountId = it,
                    accountType = AccountType.CREDIT_CARD,
                    name = driver.findElement(By.cssSelector(".card-linklist:not(.card-noprint) .ui-selectmenu-status")).text.substringBefore(" (..."),
                    balance = driver.findElement(By.xpath(balancePrefix.replace("%d", "1"))).toBigDecimal() // Current
                            .add(driver.findElement(By.xpath(balancePrefix.replace("%d", "2"))).toBigDecimal()) // Pending
            )
        }
    }

    override fun getAccountTransactions(account: Account): List<Transaction> {
        // TODO: Older transactions
        driver.get("https://cards.chase.com/CC/Account/Activity/${account.accountId}")
        waitForVisibility(By.className("ui-selectmenu-status"))
        return driver.findElements(By.className("summary")).map {
            return@map Transaction(
                    account = account,
                    date = it.findElement(By.xpath("td[2]")).text.toLocalDate("MM/dd/yyyy"),
                    pending = it.findElement(By.xpath("td[4]")).text == "Pending",
                    type = Transaction.Type.valueOf(it.findElement(By.xpath("td[4]")).text),
                    vendor = it.findElement(By.xpath("td[5]")).text,
                    amount = it.findElement(By.xpath("td[7]")).toBigDecimal()
            )
        }
    }
}