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
        jint type // 0: 4Hut, 1: 4Fortress, 2: 3Hut, 3: 3Fortress(SSV)
) {
    Generator g;
    setupGenerator(&g, MC_1_21, 0);

    // Determine dimension
    int dimension = (type == 1 || type == 3) ? DIM_NETHER : DIM_OVERWORLD;
    applySeed(&g, dimension, seed);

    StructureConfig config;
    int structType = (type == 0 || type == 2) ? Swamp_Hut : Fortress;
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
            int valid_count = 0;
            std::vector<Pos> valid_pos;

            for (int i = 0; i < 4; ++i) {
                int dx = i % 2;
                int dz = i / 2;
                Pos pos;
                if (getStructurePos(structType, MC_1_21, (uint64_t)seed, rx + dx, rz + dz, &pos)) {
                    int is_valid = 1;
                    if (type == 0 || type == 2) { // Witch Hut
                        int b = getBiomeAt(&g, 4, pos.x, 64, pos.z);
                        if (b != swampland) is_valid = 0;
                    } else if (type == 3) { // 3Fortress Soul Sand Valley
                        int b = getBiomeAt(&g, 4, pos.x, 64, pos.z);
                        if (b != soul_sand_valley) is_valid = 0;
                    }

                    if (is_valid) {
                        valid_count++;
                        valid_pos.push_back(pos);
                    }
                }
            }

            int required = (type == 0 || type == 1) ? 4 : 3;

            if (valid_count >= required) {
                // Check if there's a center point within maxDist of ALL these valid structures
                // Use a simple bounding box of the valid positions to find a candidate center
                int minX = valid_pos[0].x, maxX = valid_pos[0].x, minZ = valid_pos[0].z, maxZ = valid_pos[0].z;
                for(size_t i=1; i<valid_pos.size(); i++) {
                    if(valid_pos[i].x < minX) minX = valid_pos[i].x;
                    if(valid_pos[i].x > maxX) maxX = valid_pos[i].x;
                    if(valid_pos[i].z < minZ) minZ = valid_pos[i].z;
                    if(valid_pos[i].z > maxZ) maxZ = valid_pos[i].z;
                }

                int midX = (minX + maxX) / 2;
                int midZ = (minZ + maxZ) / 2;

                bool allClose = true;
                for(size_t i=0; i<valid_pos.size(); i++) {
                    long long dx = valid_pos[i].x - midX;
                    long long dz = valid_pos[i].z - midZ;
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
