package com.github.buckcri.web3auth.model

import org.junit.jupiter.api.Test
import java.util.regex.Pattern
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthModelTest {

    @Test
    fun testAddressPattern() {
        assertTrue { Pattern.matches(ACCOUNT_REGEXP, "0xab5783935e2b9c8f3e3ee0751a60693bb7451b76")  }
        assertFalse { Pattern.matches(ACCOUNT_REGEXP, "0xf00")  }
    }
}