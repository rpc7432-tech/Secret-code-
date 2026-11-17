package com.example.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NumericCipherTest {

  @Test
  fun `encrypt and decrypt Hello with 1234 matches reference JS output`() {
    val encrypted = NumericCipher.encrypt("1234", "Hello")
    assertTrue(encrypted is NumericCipher.EncryptResult.Success)
    val cipherText = (encrypted as NumericCipher.EncryptResult.Success).cipherText
    // 'H'(72)+1235=1307, 'e'(101)+1236=1337, 'l'(108)+1237=1345, 'l'(108)+1238=1346, 'o'(111)+1235=1346
    assertEquals("1307 1337 1345 1346 1346", cipherText)

    val decrypted = NumericCipher.decrypt("1234", cipherText)
    assertTrue(decrypted is NumericCipher.DecryptResult.Success)
    assertEquals("Hello", (decrypted as NumericCipher.DecryptResult.Success).plainText)
  }

  @Test
  fun `decrypt accepts comma and whitespace separated numbers`() {
    val decrypted = NumericCipher.decrypt("1234", "1307, 1337,1345\n1346   1346")
    assertEquals(
      NumericCipher.DecryptResult.Success(plainText = "Hello", tokenCount = 5),
      decrypted,
    )
  }

  @Test
  fun `negative password round-trips accurately`() {
    val message = "Secret Academic Note!"
    val encrypted = NumericCipher.encrypt("-42", message) as NumericCipher.EncryptResult.Success
    val decrypted =
      NumericCipher.decrypt("-42", encrypted.cipherText) as NumericCipher.DecryptResult.Success
    assertEquals(message, decrypted.plainText)
  }

  @Test
  fun `unicode and emojis round-trip accurately`() {
    val message = "Hi 🔐 Café"
    val encrypted = NumericCipher.encrypt("98765", message) as NumericCipher.EncryptResult.Success
    val decrypted =
      NumericCipher.decrypt("98765", encrypted.cipherText) as NumericCipher.DecryptResult.Success
    assertEquals(message, decrypted.plainText)
  }

  @Test
  fun `invalid password and invalid tokens return expected error types`() {
    assertEquals(NumericCipher.EncryptResult.InvalidPassword, NumericCipher.encrypt("abc", "Hi"))
    assertEquals(NumericCipher.EncryptResult.EmptyMessage, NumericCipher.encrypt("123", ""))
    assertEquals(
      NumericCipher.DecryptResult.InvalidToken("abc"),
      NumericCipher.decrypt("123", "200 abc 300"),
    )
    assertEquals(
      NumericCipher.DecryptResult.OutOfRangeCharCode,
      NumericCipher.decrypt("1234", "0"),
    )
  }
}
