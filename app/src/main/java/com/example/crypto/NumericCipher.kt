package com.example.crypto

import java.math.BigInteger

/**
 * Deterministic numeric cipher engine.
 * Combines the full integer password with cycling individual password digits
 * so both single-digit and multi-digit numeric passwords produce reliable, reversible shifts.
 */
object NumericCipher {

  private val INTEGER_REGEX = Regex("^-?\\d+$")
  private val TOKEN_SPLIT_REGEX = Regex("[\\s,]+")

  sealed interface EncryptResult {
    data class Success(val cipherText: String, val characterCount: Int) : EncryptResult

    data object InvalidPassword : EncryptResult

    data object EmptyMessage : EncryptResult
  }

  sealed interface DecryptResult {
    data class Success(val plainText: String, val tokenCount: Int) : DecryptResult

    data object InvalidPassword : DecryptResult

    data object EmptyMessage : DecryptResult

    data class InvalidToken(val token: String) : DecryptResult

    data object OutOfRangeCharCode : DecryptResult
  }

  private class KeyEngine(private val baseValue: BigInteger, private val digits: List<Int>) {
    fun offsetAt(index: Int): BigInteger {
      val digit = digits[index % digits.size]
      return baseValue.add(BigInteger.valueOf(digit.toLong()))
    }
  }

  private fun parseKeyEngine(rawPassword: String): KeyEngine? {
    val trimmed = rawPassword.trim()
    if (trimmed.isEmpty() || !INTEGER_REGEX.matches(trimmed)) {
      return null
    }
    val baseValue = trimmed.toBigIntegerOrNull() ?: return null
    val sign = if (baseValue < BigInteger.ZERO) -1 else 1
    val digits = baseValue.abs().toString().map { (it - '0') * sign }
    return KeyEngine(baseValue = baseValue, digits = digits)
  }

  fun encrypt(rawPassword: String, plainText: String): EncryptResult {
    val engine = parseKeyEngine(rawPassword) ?: return EncryptResult.InvalidPassword
    if (plainText.isEmpty()) {
      return EncryptResult.EmptyMessage
    }

    val builder = StringBuilder()
    for (i in plainText.indices) {
      if (i > 0) builder.append(' ')
      val charCode = BigInteger.valueOf(plainText[i].code.toLong())
      val shifted = charCode.add(engine.offsetAt(i))
      builder.append(shifted.toString())
    }
    return EncryptResult.Success(cipherText = builder.toString(), characterCount = plainText.length)
  }

  fun decrypt(rawPassword: String, rawCipherText: String): DecryptResult {
    val engine = parseKeyEngine(rawPassword) ?: return DecryptResult.InvalidPassword
    val trimmed = rawCipherText.trim()
    if (trimmed.isEmpty()) {
      return DecryptResult.EmptyMessage
    }

    val tokens = trimmed.split(TOKEN_SPLIT_REGEX).filter { it.isNotEmpty() }
    val chars = CharArray(tokens.size)

    for (i in tokens.indices) {
      val token = tokens[i]
      if (!INTEGER_REGEX.matches(token)) {
        return DecryptResult.InvalidToken(token)
      }
      val numericValue = token.toBigIntegerOrNull() ?: return DecryptResult.InvalidToken(token)
      val originalCode = numericValue.subtract(engine.offsetAt(i))
      if (originalCode < BigInteger.ZERO || originalCode > BigInteger.valueOf(65535L)) {
        return DecryptResult.OutOfRangeCharCode
      }
      chars[i] = originalCode.toInt().toChar()
    }

    return DecryptResult.Success(plainText = String(chars), tokenCount = tokens.size)
  }
}
