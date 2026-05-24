package com.example.seedx.mc

import kotlin.math.abs

class SeedSearcher(
    val seed: Long,
    val centerX: Int,
    val centerZ: Int,
    val range: Int,
    val playerDistance: Int,
    val type: StructureType,
    val onResult: (BlockPos) -> Unit,
    val onProgress: (Float) -> Unit
) {
    private val locator = StructureLocator(seed)

    fun search() {
        val startChunkX = (centerX - range) / 16
        val endChunkX = (centerX + range) / 16
        val startChunkZ = (centerZ - range) / 16
        val endChunkZ = (centerZ + range) / 16

        val total = (endChunkX - startChunkX + 1).toLong() * (endChunkZ - startChunkZ + 1)
        var count = 0L

        // Logic for finding quads:
        // A "quad" center (px, pz) must be within playerDistance of 4 distinct structure points.
        // We can iterate through regions (spacing x spacing chunks).

        val regionSpacing = type.spacing
        val regStartX = (centerX - range) / (regionSpacing * 16)
        val regEndX = (centerX + range) / (regionSpacing * 16)
        val regStartZ = (centerZ - range) / (regionSpacing * 16)
        val regEndZ = (centerZ + range) / (regionSpacing * 16)

        val totalRegions = (regEndX - regStartX + 1).toLong() * (regEndZ - regStartZ + 1)
        var regionCount = 0L

        for (rx in regStartX..regEndX) {
            for (rz in regStartZ..regEndZ) {
                // Check if this region and its neighbors can form a quad
                // A quad is formed by 4 adjacent regions: (rx, rz), (rx+1, rz), (rx, rz+1), (rx+1, rz+1)

                val p1 = locator.getStructurePos(rx * regionSpacing, rz * regionSpacing, type)
                val p2 = locator.getStructurePos((rx + 1) * regionSpacing, rz * regionSpacing, type)
                val p3 = locator.getStructurePos(rx * regionSpacing, (rz + 1) * regionSpacing, type)
                val p4 = locator.getStructurePos((rx + 1) * regionSpacing, (rz + 1) * regionSpacing, type)

                val quadCenter = findQuadCenter(p1, p2, p3, p4)
                if (quadCenter != null) {
                    // Check if all 4 are within playerDistance
                    if (distSq(quadCenter, p1) <= playerDistance * playerDistance &&
                        distSq(quadCenter, p2) <= playerDistance * playerDistance &&
                        distSq(quadCenter, p3) <= playerDistance * playerDistance &&
                        distSq(quadCenter, p4) <= playerDistance * playerDistance) {

                        // Biome check would go here
                        if (validateBiomes(p1, p2, p3, p4)) {
                           onResult(quadCenter)
                        }
                    }
                }

                regionCount++
                onProgress(regionCount.toFloat() / totalRegions)
            }
        }
    }

    private fun findQuadCenter(p1: BlockPos, p2: BlockPos, p3: BlockPos, p4: BlockPos): BlockPos? {
        val minX = minOf(p1.x, p2.x, p3.x, p4.x)
        val maxX = maxOf(p1.x, p2.x, p3.x, p4.x)
        val minZ = minOf(p1.z, p2.z, p3.z, p4.z)
        val maxZ = maxOf(p1.z, p2.z, p3.z, p4.z)

        val centerX = (minX + maxX) / 2
        val centerZ = (minZ + maxZ) / 2

        return BlockPos(centerX, centerZ)
    }

    private fun distSq(p1: BlockPos, p2: BlockPos): Int {
        val dx = p1.x - p2.x
        val dz = p1.z - p2.z
        return dx * dx + dz * dz
    }

    private val biomeSource = BiomeSource(seed)

    private fun validateBiomes(p1: BlockPos, p2: BlockPos, p3: BlockPos, p4: BlockPos): Boolean {
        return when (type) {
            StructureType.SWAMP_HUT -> {
                biomeSource.isSwamp(p1.x, p1.z) &&
                biomeSource.isSwamp(p2.x, p2.z) &&
                biomeSource.isSwamp(p3.x, p3.z) &&
                biomeSource.isSwamp(p4.x, p4.z)
            }
            StructureType.FORTRESS -> {
                biomeSource.isNether(p1.x, p1.z) &&
                biomeSource.isNether(p2.x, p2.z) &&
                biomeSource.isNether(p3.x, p3.z) &&
                biomeSource.isNether(p4.x, p4.z)
            }
        }
    }
}
