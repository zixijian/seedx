package com.jules.seedx

class SeedXNative {
    external fun findQuadStructures(
        seed: Long,
        centerX: Int,
        centerZ: Int,
        range: Int,
        maxDist: Int,
        type: Int
    ): Array<String>

    companion object {
        init {
            System.loadLibrary("seedx")
        }
    }
}
