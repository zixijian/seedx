#include <jni.h>
#include <string>
#include <vector>
#include <cstdio>
#include "cubiomes/generator.h"
#include "cubiomes/finders.h"

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_jules_seedx_SeedXNative_findQuadStructures(
        JNIEnv* env,
        jobject /* this */,
        jlong seed,
        jint centerX,
        jint centerZ,
        jint range,
        jint maxDist,
        jint type // 0 for Witch Hut, 1 for Fortress
) {
    Generator g;
    setupGenerator(&g, MC_1_21, 0);
    applySeed(&g, DIM_OVERWORLD, seed);

    StructureConfig config;
    int structType = (type == 0) ? Swamp_Hut : Fortress;
    if (!getStructureConfig(structType, MC_1_21, &config)) {
        return env->NewObjectArray(0, env->FindClass("java/lang/String"), env->NewStringUTF(""));
    }

    int spacing = config.regionSize;
    int regionRange = (range / (spacing * 16)) + 2;
    int startRegionX = (centerX / (spacing * 16)) - regionRange;
    int startRegionZ = (centerZ / (spacing * 16)) - regionRange;
    int endRegionX = (centerX / (spacing * 16)) + regionRange;
    int endRegionZ = (centerZ / (spacing * 16)) + regionRange;

    std::vector<std::string> results;

    for (int rz = startRegionZ; rz < endRegionZ; ++rz) {
        for (int rx = startRegionX; rx < endRegionX; ++rx) {
            Pos p[4];
            int valid = 1;
            for (int i = 0; i < 4; ++i) {
                int dx = i % 2;
                int dz = i / 2;
                if (!getStructurePos(structType, MC_1_21, (uint64_t)seed, rx + dx, rz + dz, &p[i])) {
                    valid = 0;
                    break;
                }

                // Biome check
                if (structType == Swamp_Hut) {
                    int b = getBiomeAt(&g, 4, p[i].x, 64, p[i].z);
                    if (b != swampland) {
                        valid = 0;
                        break;
                    }
                }
            }

            if (valid) {
                int minX = p[0].x, maxX = p[0].x, minZ = p[0].z, maxZ = p[0].z;
                for(int i=1; i<4; i++) {
                    if(p[i].x < minX) minX = p[i].x;
                    if(p[i].x > maxX) maxX = p[i].x;
                    if(p[i].z < minZ) minZ = p[i].z;
                    if(p[i].z > maxZ) maxZ = p[i].z;
                }

                int midX = (minX + maxX) / 2;
                int midZ = (minZ + maxZ) / 2;

                bool allClose = true;
                for(int i=0; i<4; i++) {
                    long long dx = p[i].x - midX;
                    long long dz = p[i].z - midZ;
                    if (dx*dx + dz*dz > (long long)maxDist * maxDist) {
                        allClose = false;
                        break;
                    }
                }

                if (allClose) {
                    long long dcx = midX - centerX;
                    long long dcz = midZ - centerZ;
                    if (dcx*dcx + dcz*dcz <= (long long)range * range) {
                        char buf[128];
                        sprintf(buf, "%d, %d", midX, midZ);
                        results.push_back(buf);
                    }
                }
            }
        }
    }

    jobjectArray ret = (jobjectArray)env->NewObjectArray(results.size(), env->FindClass("java/lang/String"), env->NewStringUTF(""));
    for (size_t i = 0; i < results.size(); ++i) {
        env->SetObjectArrayElement(ret, i, env->NewStringUTF(results[i].c_str()));
    }

    return ret;
}
