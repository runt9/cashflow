package com.runt9.cashflow.service.dataGatherer

import com.runt9.cashflow.model.entity.Account
import com.runt9.cashflow.model.entity.AccountType
import com.runt9.cashflow.model.entity.Bank
import com.runt9.cashflow.model.entity.Transaction
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.Select
import java.math.BigDecimal


class CapitalOneCcDataGatherer : AbstractDataGatherer() {
    override fun login(username: String, password: String) {
        driver.get("https://capitalone.com")
        driver.findElement(By.id("btnLoginAccountTypeNew")).click()
        waitForVisibility(By.xpath("//*[@id=\"account-log-in-new\"]/fieldset/ul/li[1]/label"))
        driver.findElement(By.xpath("//*[@id=\"account-log-in-new\"]/fieldset/ul/li[1]/label")).click()
        driver.findElement(By.id("login-us-credit-cards-uid")).sendKeys(username)
        driver.findElement(By.id("login-us-credit-cards-pw")).sendKeys(password)
        driver.findElement(By.id("login-submit-card-us")).click()
        waitForVisibility(By.className("bricklet"))
    }

    override fun gatherAccounts(bank: Bank): List<Account> {
        driver.get("https://servicese.capitalone.com/ui/#/accounts/transactions")
        waitForVisibility(By.id("transactions_page_container"))
        val accountDropdown = Select(driver.findElement(By.id("account_name")))
        return accountDropdown.options.filter { it.getAttribute("class").contains("closed").not() }.map {
            accountDropdown.selectByValue(it.getAttribute("value"))
            waitForVisibility(By.id("current-balance-amount"))

            return@map Account(
                    accountType = AccountType.CREDIT_CARD,
                    bank = bank,
                    name = it.text.substringBefore(" ..."),
                    balance = driver.findElement(By.id("current-balance-amount")).toBigDecimal()
            )
        }
    }

    override fun getAccountTransactions(account: Account): List<Transaction> {
        // TODO: Older transactions
        val accountDropdown = Select(driver.findElement(By.id("account_name")))
        accountDropdown.selectByValue(accountDropdown.options.find { it.text.contains(account.name) }?.getAttribute("value"))
        waitForVisibility(By.id("transactionsBricklet"))
        driver.findElement(By.id("drawer_toggle_ID")).click()

        return driver.findElements(By.cssSelector(".tr.transaction")).map {
            val category = it.findElement(By.cssSelector(".category span")).text
            val amount = it.findElement(By.cssSelector(".amount")).toBigDecimal()

            val type = when {
                category == "Payment" -> Transaction.Type.Payment
                category == "Fee" || category == "Interest Charge" -> Transaction.Type.Fee
                amount < BigDecimal.ZERO -> Transaction.Type.Return
                else -> Transaction.Type.Sale
            }

            return@map Transaction(
                    account = account,
                    type = type,
                    pending = it.getAttribute("class").contains("pending"),
                    date = it.findElement(By.cssSelector(".date span")).text.toLocalDate("MM/dd/yy"),
                    vendor = it.findElement(By.cssSelector(".merchant")).text.trim(),
                    amount = amount
            )
        }
    }
}