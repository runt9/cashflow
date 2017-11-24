package com.runt9.cashflow.service

import com.runt9.cashflow.model.dto.BankLogin
import com.runt9.cashflow.model.entity.Bank
import com.runt9.cashflow.model.entity.BankType
import com.runt9.cashflow.repository.BankRepository
import com.runt9.cashflow.service.scraper.ScraperFactory
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.stereotype.Service
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Service
class BankService(
        private val bankRepository: BankRepository
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

    fun refreshBanks(encryptionKey: String) {
        val banks = bankRepository.findAll()
        banks
                .filter { it.bankType == BankType.CHASE }
                .forEach {
                    val login = getBankLogin(it, encryptionKey)
                    val scraper = ScraperFactory.loadScraper(login.bankType)
                    scraper.login(login.loginName, login.password)
                    scraper.cleanup()
                }
    }
}