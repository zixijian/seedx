package com.example.seedx.mc

class PerlinNoise(val random: JavaRandom) {
    private val p = IntArray(512)

    init {
        val permutation = IntArray(256) { it }
        // Simple shuffle
        for (i in 255 downTo 1) {
            val j = random.nextInt(i + 1)
            val temp = permutation[i]
            permutation[i] = permutation[j]
            permutation[j] = temp
        }
        for (i in 0..255) {
            p[i] = permutation[i]
            p[i + 256] = permutation[i]
        }
    }

    fun noise(x: Double, y: Double, z: Double): Double {
        val X = x.toInt() and 255
        val Y = y.toInt() and 255
        val Z = z.toInt() and 255

        val xf = x - x.toInt()
        val yf = y - y.toInt()
        val zf = z - z.toInt()

        val u = fade(xf)
        val v = fade(yf)
        val w = fade(zf)

        val A = p[X] + Y
        val AA = p[A] + Z
        val AB = p[A + 1] + Z
        val B = p[X + 1] + Y
        val BA = p[B] + Z
        val BB = p[B + 1] + Z

        return lerp(w, lerp(v, lerp(u, grad(p[AA], xf, yf, zf),
                                       grad(p[BA], xf - 1, yf, zf)),
                               lerp(u, grad(p[AB], xf, yf - 1, zf),
                                       grad(p[BB], xf - 1, yf - 1, zf))),
                       lerp(v, lerp(u, grad(p[AA + 1], xf, yf, zf - 1),
                                       grad(p[BA + 1], xf - 1, yf, zf - 1)),
                               lerp(u, grad(p[AB + 1], xf, yf - 1, zf - 1),
                                       grad(p[BB + 1], xf - 1, yf - 1, zf - 1))))
    }

    private fun fade(t: Double) = t * t * t * (t * (t * 6 - 15) + 10)
    private fun lerp(t: Double, a: Double, b: Double) = a + t * (b - a)
    private fun grad(hash: Int, x: Double, y: Double, z: Double): Double {
        val h = hash and 15
        val u = if (h < 8) x else y
        val v = if (h < 4) y else if (h == 12 || h == 14) x else z
        return (if (h and 1 == 0) u else -u) + (if (h and 2 == 0) v else -v)
    }
}
