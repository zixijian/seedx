#include <jni.h>
#include <vector>
#include <cmath>
#include "cubiomes/generator.h"
#include "cubiomes/finders.h"

extern "C"
JNIEXPORT jlongArray JNICALL
Java_com_jules_seedx_Finder_findClustersNative(JNIEnv *env, jobject thiz, jlong seed, jint centerX,
                                               jint centerZ, jint range, jint maxDistToPlayer,
                                               jint minCount, jint type) {
    int mc = MC_1_21;
    Generator g;
    setupGenerator(&g, mc, 0);

    int structType = (type == 0) ? Swamp_Hut : Fortress;
    int dimension = (type == 0) ? DIM_OVERWORLD : DIM_NETHER;

    applySeed(&g, dimension, seed);

    StructureConfig sconf;
    if (!getStructureConfig(structType, mc, &sconf)) return env->NewLongArray(0);

    int regSize = sconf.regionSize;
    int regionSizeInBlocks = regSize * 16;

    int startRX = (centerX - range) / regionSizeInBlocks - 1;
    int endRX = (centerX + range) / regionSizeInBlocks + 1;
    int startRZ = (centerZ - range) / regionSizeInBlocks - 1;
    int endRZ = (centerZ + range) / regionSizeInBlocks + 1;

    std::vector<Pos> found;
    for (int rx = startRX; rx <= endRX; rx++) {
        for (int rz = startRZ; rz <= endRZ; rz++) {
            Pos p;
            if (getStructurePos(structType, mc, seed, rx, rz, &p)) {
                if (abs(p.x - centerX) <= range && abs(p.z - centerZ) <= range) {
                    if (isViableStructurePos(structType, &g, p.x, p.z, 0)) {
                        if (type == 1) {
                            int b = getBiomeAt(&g, 4, p.x >> 2, 64, p.z >> 2);
                            if (b != soul_sand_valley) continue;
                        }
                        found.push_back(p);
                    }
                }
            }
        }
    }

    std::vector<Pos> clusters;
    for (size_t i = 0; i < found.size(); i++) {
        std::vector<Pos> nearby;
        for (size_t j = 0; j < found.size(); j++) {
            double dx = found[i].x - found[j].x;
            double dz = found[i].z - found[j].z;
            if (sqrt(dx*dx + dz*dz) <= maxDistToPlayer * 2) {
                nearby.push_back(found[j]);
            }
        }

        if (nearby.size() >= (size_t)minCount) {
            double avgX = 0, avgZ = 0;
            for (const auto& p : nearby) {
                avgX += p.x;
                avgZ += p.z;
            }
            avgX /= nearby.size();
            avgZ /= nearby.size();

            int iavgX = (int)round(avgX);
            int iavgZ = (int)round(avgZ);

            bool ok = true;
            for (const auto& p : nearby) {
                double dx = iavgX - p.x;
                double dz = iavgZ - p.z;
                if (sqrt(dx*dx + dz*dz) > maxDistToPlayer) {
                    ok = false;
                    break;
                }
            }

            if (ok) {
                bool exists = false;
                for (const auto& c : clusters) {
                    if (abs(c.x - iavgX) < 16 && abs(c.z - iavgZ) < 16) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) clusters.push_back({iavgX, iavgZ});
            }
        }
    }

    jlongArray result = env->NewLongArray(clusters.size() * 2);
    jlong *elements = env->GetLongArrayElements(result, nullptr);
    for (size_t i = 0; i < clusters.size(); i++) {
        elements[i * 2] = clusters[i].x;
        elements[i * 2 + 1] = clusters[i].z;
    }
    env->ReleaseLongArrayElements(result, elements, 0);
    return result;
}
