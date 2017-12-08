package com.runt9.cashflow.service.dataGatherer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.runt9.cashflow.model.entity.Account
import com.runt9.cashflow.model.entity.AccountType
import com.runt9.cashflow.model.entity.Bank
import com.runt9.cashflow.model.entity.Transaction
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.search.FlagTerm


class CapitalOneCcDataGatherer(private val restTemplate: RestTemplate) : DataGatherer {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AccountsResult(val accounts: List<AccountResult> = emptyList())

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AccountResult(val cardAccount: CardAccountResult = CardAccountResult())

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class CardAccountResult(
            val id: String = "",
            val accountTreatment: String = "",
            val currentBalance: BigDecimal = BigDecimal.ZERO,
            val nameInfo: NameInfo = NameInfo()
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class NameInfo(val nickname: String = "")

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TransactionsApiResult(val transactions: TransactionsResult = TransactionsResult())

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TransactionsResult(val pending: List<CapitalOnePendingTransaction> = emptyList(), val posted: List<CapitalOnePostedTransaction> = emptyList())

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class CapitalOnePendingTransaction(
            val amount: CapitalOneTransactionAmount = CapitalOneTransactionAmount(),
            val authorizationType: String = "",
            val description: String = "",
            val displayDate: String = "",
            val details: String = "" // Used for categorization later
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class CapitalOnePostedTransaction(
            val amount: CapitalOneTransactionAmount = CapitalOneTransactionAmount(),
            val transactionCategoryCode: String = "",
            val description: String = "",
            val displayDate: String = "",
            val details: String = "" // Used for categorization later
    )

    data class CapitalOneTransactionAmount(val amount: BigDecimal = BigDecimal.ZERO)

    override fun login(username: String, password: String) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val data = HashMap<String, Any>()
        data.put("username", username)
        data.put("password", password)
        val request = HttpEntity<Map<String, Any>>(data, headers)

        val loginRetval = restTemplate.postForObject("https://verified.capitalone.com/signincontroller-web/signincontroller/signin", request, Map::class.java)
        if (loginRetval["loginStatus"] == "CHALLENGE") {
            try {
                restTemplate.getForEntity("https://verified.capitalone.com/challengeapp-server-web/challenge/method/getChallengeMethod?channelType=WEB&client=SICAPP", Any::class.java)
            } catch (e: Exception) {
                throw e
            }

//            val pinData = hashMapOf("id" to "FFG/fWL8WoEYpa0N75jjoKyxeSlyUKJjU/HiCz2iY+M=", "contactPointType" to "email")
//            val sendPinRequest = HttpEntity<Map<String, Any>>(pinData, headers)
//
//            try {
//                restTemplate.postForEntity("https://verified.capitalone.com/challengeapp-server-web/challenge/otp/sendPin", sendPinRequest, Map::class.java)
//            } catch (e: HttpServerErrorException) {
//                throw e
//            }
//
//            val oneTimeCode = getGmailOneTimeCode()
//            val validateData = hashMapOf("pin" to oneTimeCode)
//            val validatePinRequest = HttpEntity<Map<String, Any>>(validateData, headers)
//
//            try {
//                restTemplate.postForEntity("https://verified.capitalone.com/challengeapp-server-web/challenge/otp/validatePin", validatePinRequest, Any::class.java)
//            } catch (e: HttpServerErrorException) {
//                throw e
//            }

            try {
                restTemplate.getForEntity("https://verified.capitalone.com/challengeapp-server-web/challenge/swiftid/sendNotification?statusCheckMethod=EVENT", Any::class.java)
            } catch (e: HttpServerErrorException) {
                throw e
            }

            Thread.sleep(1000L)

            try {
                restTemplate.getForEntity("https://verified.capitalone.com/challengeapp-server-web/challenge/swiftid/getSwiftIdStatus", Any::class.java)
            } catch (e: HttpServerErrorException) {
                throw e
            }
        }
    }

    override fun gatherAccounts(bank: Bank): List<Account> {
        val accountsResult = restTemplate.getForObject("https://services1.capitalone.com/api/v2/accounts", AccountsResult::class.java)
        return accountsResult.accounts
                .map { it.cardAccount }
                .filter { it.accountTreatment != "CLOSED_CLOSED" }
                .map { acc ->
                    Account(
                            bank = bank,
                            accountType = AccountType.CREDIT_CARD,
                            name = acc.nameInfo.nickname,
                            accountId = acc.id,
                            balance = acc.currentBalance
                    )
                }
    }

    override fun getAccountTransactions(account: Account): List<Transaction> {
        val transactions = restTemplate.getForObject("https://services1.capitalone.com/api/v2/accounts/{accountId}/transactions?startDate={startDate}&endDate={endDate}", TransactionsApiResult::class.java,
                account.accountId, LocalDate.now().minusDays(90).format(DateTimeFormatter.ofPattern("yyyy-M-d")), LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-d")))
        val pendingTransactions = transactions.transactions.pending.map { transaction ->
            return@map Transaction(
                    account = account,
                    pending = true,
                    date = LocalDate.parse(transaction.displayDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")),
                    amount = transaction.amount.amount,
                    vendor = transaction.description,
                    type = when (transaction.authorizationType) {
                        "Payment" -> Transaction.Type.Payment
                        "Balance-Adjustment" -> Transaction.Type.Return
                        "Membership-Fee" -> Transaction.Type.Fee
                        "Finance-Charge" -> Transaction.Type.Fee
                        else -> Transaction.Type.Sale
                    }
            )
        }

        val postedTransactions = transactions.transactions.posted.map { transaction ->
            return@map Transaction(
                    account = account,
                    date = LocalDate.parse(transaction.displayDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")),
                    amount = transaction.amount.amount,
                    vendor = transaction.description,
                    type = when (transaction.transactionCategoryCode) {
                        "PAYMENT" -> Transaction.Type.Payment
                        "BALANCE-ADJUSTMENT" -> Transaction.Type.Return
                        "MEMBERSHIP-FEE" -> Transaction.Type.Fee
                        "FINANCE-CHARGE" -> Transaction.Type.Fee
                        else -> Transaction.Type.Sale
                    }
            )
        }

        return pendingTransactions + postedTransactions
    }

    private fun getGmailOneTimeCode(): String {
        val session = Session.getDefaultInstance(Properties())
        val store = session.getStore("imaps")
        // TODO: Hide login info
        val inbox = store.getFolder("Inbox")
        var body: String? = null
        while (body == null) {
            Thread.sleep(2000L)
            inbox.open(Folder.READ_ONLY)
            val message = inbox.search(FlagTerm(Flags(Flags.Flag.SEEN), false)).find { it.subject.matches(Regex("Here's the one-time code you requested")) }
            if (message != null) {
                body = (message.content as Multipart).getBodyPart(0).content.toString()
            }
            inbox.close()
        }

        return Regex("Here's your one-time code: (\\d{6})").find(body)?.groupValues?.get(1) ?: throw RuntimeException("Failed to parse one-time code from email")
    }
}