package com.runt9.cashflow.service.dataGatherer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.runt9.cashflow.model.entity.Account
import com.runt9.cashflow.model.entity.AccountType
import com.runt9.cashflow.model.entity.Bank
import com.runt9.cashflow.model.entity.Transaction
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class ChaseDataGatherer(private val restTemplate: RestTemplate) : DataGatherer {
    // TODO: Refactor these classes
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class RandomizeReturn(val inputId: String = "", val inputValue: String = "", val randomParameter: String = "")

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class LoginReturn(val smtoken: String = "")

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AppData(val cache: List<AppDataCache> = emptyList())

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AppDataCache(val response: Map<String, Any> = emptyMap())

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TransactionResult(val result: List<ChaseTransaction> = emptyList())

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ChaseTransaction(
            val amount: BigDecimal = BigDecimal.ZERO,
            val merchantName: String = "",
            val pending: Boolean = false,
            val transactionDate: String = "",
            val type: String = ""
    )

    override fun login(username: String, password: String) {
        val headers = HttpHeaders()
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36")

        val data = LinkedMultiValueMap<String, String>()
        data.add("type", "json")
        data.add("auth_reqid", System.currentTimeMillis().toString())
        val request = HttpEntity<MultiValueMap<String, String>>(data, headers)

        val randomizeRetval = restTemplate.postForObject("https://secure05b.chase.com/auth/fcc/randomize", request, Array<RandomizeReturn>::class.java)

        val loginData = LinkedMultiValueMap<String, String>()
        randomizeRetval.forEach {
            val dataVal = when (it.inputId) {
                "siteId" -> "C30"
                "contextId" -> "login"
                "userId" -> username
                "passwd" -> password
                "passwd_org" -> password
                "LOB" -> "LOB=RBGLogon"
                "deviceSignature" -> "{\"navigator\":{\"vendorSub\":\"\",\"productSub\":\"20030107\",\"vendor\":\"Google Inc.\",\"maxTouchPoints\":0,\"hardwareConcurrency\":4,\"cookieEnabled\":true,\"appCodeName\":\"Mozilla\",\"appName\":\"Netscape\",\"appVersion\":\"5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36\",\"platform\":\"Win32\",\"product\":\"Gecko\",\"userAgent\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36\",\"language\":\"en-US\",\"onLine\":true,\"doNotTrack\":\"1\"},\"plugins\":[],\"screen\":{\"availHeight\":1050,\"availWidth\":1920,\"colorDepth\":24,\"height\":1080,\"pixelDepth\":24,\"width\":1920},\"extra\":{\"javascript_ver\":\"2.0\",\"timezone\":360}}"
                "deviceCookie" -> "adtoken"
                "cacheId" -> it.inputValue
                else -> ""
            }

            loginData.add(it.randomParameter, dataVal)
        }
        loginData.add("type", "json")
        loginData.add("auth_mobile_mis", "Viewport_Horizontal=1905&Viewport_Vertical=507&Breakpoint=lg")
        val loginRequest = HttpEntity<MultiValueMap<String, String>>(loginData, headers)

        // This stores the cookie (SMSESSION) in the cookie jar so we don't have to store it ourselves
        restTemplate.postForObject("https://secure05b.chase.com/auth/fcc/login", loginRequest, LoginReturn::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    override fun gatherAccounts(bank: Bank): List<Account> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set("x-jpmc-csrf-token", "NONE")

        val request = HttpEntity<Any>(headers)
        val appData = restTemplate.postForObject("https://secure05b.chase.com/svc/rl/accounts/secure/v1/app/data/list", request, AppData::class.java)
        return (appData.cache[1].response["accountTiles"] as List<Map<String, Any>>).map {
            return@map Account(
                    accountType = AccountType.CREDIT_CARD, // Change if get a Chase bank account
                    bank = bank,
                    name = it["nickname"] as String,
                    balance = BigDecimal(((it["tileDetail"] as Map<String, Any>)["currentBalance"] as Double).toString()),
                    accountId = (it["accountId"] as Int).toString()
            )
        }
    }

    override fun getAccountTransactions(account: Account): List<Transaction> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set("x-jpmc-csrf-token", "NONE")

        val data = LinkedMultiValueMap<String, String>()
        data.add("accountId", account.accountId)
        data.add("filterTranType", "ALL")
        data.add("statementPeriodId", "ALL")

        val request = HttpEntity<MultiValueMap<String, String>>(data, headers)
        val transactionResult = restTemplate.postForObject("https://secure05b.chase.com/svc/rr/accounts/secure/v1/account/activity/card/list", request, TransactionResult::class.java)
        return transactionResult.result.map {
            Transaction(
                    account = account,
                    amount = it.amount,
                    vendor = it.merchantName,
                    date = LocalDate.parse(it.transactionDate, DateTimeFormatter.ofPattern("yyyyMMdd")),
                    pending = it.pending,
                    type = when(it.type) {
                        "P" -> Transaction.Type.Payment
                        "R" -> Transaction.Type.Return
                        "F" -> Transaction.Type.Fee
                        else -> Transaction.Type.Sale
                    }
            )
        }
    }
}