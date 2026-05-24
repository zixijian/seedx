package com.example.seedx.mc

data class BlockPos(val x: Int, val z: Int)

enum class StructureType(val spacing: Int, val separation: Int, val salt: Int) {
    SWAMP_HUT(32, 8, 14357617),
    FORTRESS(35, 8, 30084232)
}

class StructureLocator(val seed: Long) {
    private val random = JavaRandom(0)

    fun getStructurePos(chunkX: Int, chunkZ: Int, type: StructureType): BlockPos {
        val var4 = chunkX / type.spacing
        val var5 = chunkZ / type.spacing

        val var8 = var4 * type.spacing
        val var9 = var5 * type.spacing

        val s = seed + (var4 * 341873128712L) + (var5 * 132897987541L) + type.salt
        random.setSeed(s)

        val offsetX = random.nextInt(type.spacing - type.separation)
        val offsetZ = random.nextInt(type.spacing - type.separation)

        return BlockPos((var8 + offsetX) * 16 + 8, (var9 + offsetZ) * 16 + 8)
    }
}
