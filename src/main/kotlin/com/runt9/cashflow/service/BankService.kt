package com.runt9.cashflow.service

import com.runt9.cashflow.model.RefreshType
import com.runt9.cashflow.model.dto.BankLogin
import com.runt9.cashflow.model.entity.Account
import com.runt9.cashflow.model.entity.Bank
import com.runt9.cashflow.model.entity.Transaction
import com.runt9.cashflow.repository.AccountRepository
import com.runt9.cashflow.repository.BankRepository
import com.runt9.cashflow.repository.TransactionRepository
import com.runt9.cashflow.service.dataGatherer.DataGathererFactory
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.stereotype.Service
import java.time.LocalDate
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Service
class BankService(
        private val bankRepository: BankRepository,
        private val accountRepository: AccountRepository,
        private val transactionRepository: TransactionRepository,
        private val dataGathererFactory: DataGathererFactory
) {
    private fun crypt(text: ByteArray, encryptionKey: String, cipherMode: Int): ByteArray {
        val aesKey = SecretKeySpec(encryptionKey.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(cipherMode, aesKey)
        return cipher.doFinal(text)
    }

    private fun encrypt(text: String, encryptionKey: String): String = Base64.encodeBase64String(crypt(text.toByteArray(), encryptionKey, Cipher.ENCRYPT_MODE))
    private fun decrypt(text: String, encryptionKey: String): String = String(crypt(Base64.decodeBase64(text), encryptionKey, Cipher.DECRYPT_MODE))

    fun createBank(loginInfo: BankLogin, encryptionKey: String) {
        // Turns username: foobar, password: bazpizzaz into "6+foobarbazpizzaz" before encrypting it
        // TODO: Likely needs more salt
        val loginData = encrypt("${loginInfo.loginName.length}+${loginInfo.loginName}${loginInfo.password}", encryptionKey)
        bankRepository.save(Bank(bankType = loginInfo.bankType, loginData = loginData))
    }

    private fun getBankLogin(bank: Bank, encryptionKey: String): BankLogin {
        val loginData = decrypt(bank.loginData, encryptionKey)
        // An example string is "6+foobarbazpizzaz" where the number before the plus is the login name length. We can
        // use that to split out the rest of the string, but store it as a single encrypted "salted" string in the DB
        val loginNameLen = loginData.substringBefore('+').toInt()
        val loginPair = loginData.substringAfter('+')
        return BankLogin(bank.bankType, loginPair.substring(0, loginNameLen), loginPair.substring(loginNameLen))
    }

    fun refreshBanks(encryptionKey: String, refreshType: RefreshType) {
        if (refreshType == RefreshType.FULL) {
            accountRepository.deleteAll()
            transactionRepository.deleteAll()
        }

        val banks = bankRepository.findAll()
        banks.forEach {
            val login = getBankLogin(it, encryptionKey)
            val dataGatherer = dataGathererFactory.loadDataGatherer(login.bankType)
            try {
                dataGatherer.login(login.loginName, login.password)
                val accounts = dataGatherer.gatherAccounts(it)

                if (refreshType == RefreshType.FULL) {
                    accountRepository.save(accounts)
                    transactionRepository.save(accounts.flatMap { dataGatherer.getAccountTransactions(it, refreshType) })
                } else {
                    accountRepository.save(mergeAccounts(accounts))
                    transactionRepository.save(accounts.flatMap { mergeTransactions(it, dataGatherer.getAccountTransactions(it, refreshType)) })
                }

                accounts.forEach { it.lastRefresh = LocalDate.now() }
                accountRepository.save(accounts)
            } finally {
                dataGatherer.cleanup()
            }
        }
    }

    private fun mergeTransactions(account: Account, transactions: List<Transaction>): List<Transaction> {
        val existingTransactions = transactionRepository.findByAccount(account)
        val mergedTransactions = arrayListOf<Transaction>()

        transactions.forEach { newTran ->
            val existingTran = existingTransactions.find { it.merchant.id == newTran.merchant.id && it.date == newTran.date && it.amount == newTran.amount }
            if (existingTran != null) {
                newTran.id = existingTran.id
            }

            mergedTransactions += newTran
        }

        return mergedTransactions
    }

    fun mergeAccounts(accounts: List<Account>): List<Account> {
        val existingAccounts = accountRepository.findAll()
        val mergedAccounts = arrayListOf<Account>()
        
        accounts.forEach { newAcc ->
            val existingAcc = existingAccounts.find { it.name == newAcc.name }
            if (existingAcc != null) {
                newAcc.id = existingAcc.id
            }

            mergedAccounts += newAcc
        }
        
        return mergedAccounts
    }
}