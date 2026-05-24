package com.example.seedx.mc

class JavaRandom(seed: Long) {
    private var seed: Long = (seed xor 0x5DEECE66DL) and ((1L shl 48) - 1)

    fun next(bits: Int): Int {
        seed = (seed * 0x5DEECE66DL + 0xBL) and ((1L shl 48) - 1)
        return (seed ushr (48 - bits)).toInt()
    }

    fun nextInt(n: Int): Int {
        if (n <= 0) throw IllegalArgumentException("n must be positive")
        if ((n and -n) == n) {
            return ((n.toLong() * next(31)) shr 31).toInt()
        }
        var bits: Int
        var val_: Int
        do {
            bits = next(31)
            val_ = bits % n
        } while (bits - val_ + (n - 1) < 0)
        return val_
    }

    fun setSeed(seed: Long) {
        this.seed = (seed xor 0x5DEECE66DL) and ((1L shl 48) - 1)
    }
}
