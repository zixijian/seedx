package com.jules.seedx

import kotlin.math.sqrt

data class BlockPos(val x: Int, val z: Int) {
    fun distanceTo(other: BlockPos): Double {
        val dx = (x - other.x).toDouble()
        val dz = (z - other.z).toDouble()
        return sqrt(dx * dx + dz * dz)
    }

    override fun toString(): String = "($x, $z)"
}

enum class StructureType(val displayName: String) {
    SWAMP_HUT("女巫小屋 (沼泽)"),
    FORTRESS_CROSSING("十字路口 (灵魂沙峡谷)")
}

class Finder {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    private external fun findClustersNative(
        seed: Long,
        centerX: Int,
        centerZ: Int,
        range: Int,
        maxDistToPlayer: Int,
        minCount: Int,
        type: Int
    ): LongArray

    fun findClusters(
        seed: Long,
        centerX: Int,
        centerZ: Int,
        range: Int,
        maxDistToPlayer: Int,
        minCount: Int,
        type: StructureType
    ): List<BlockPos> {
        val typeInt = if (type == StructureType.SWAMP_HUT) 0 else 1
        val raw = findClustersNative(seed, centerX, centerZ, range, maxDistToPlayer, minCount, typeInt)
        val results = mutableListOf<BlockPos>()
        for (i in 0 until raw.size / 2) {
            results.add(BlockPos(raw[i * 2].toInt(), raw[i * 2 + 1].toInt()))
        }
        return results
    }
}
