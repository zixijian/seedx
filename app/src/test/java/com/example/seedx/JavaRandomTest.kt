package com.example.seedx

import com.example.seedx.mc.JavaRandom
import org.junit.Test
import org.junit.Assert.*

class JavaRandomTest {
    @Test
    fun testNextInt() {
        val random = JavaRandom(12345L)
        val v1 = random.nextInt(100)
        val v2 = random.nextInt(100)
        assertTrue(v1 in 0..99)
        assertTrue(v2 in 0..99)
        assertNotEquals(v1, v2)
    }
}
