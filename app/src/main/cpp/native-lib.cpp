#include <jni.h>
#include <string>
#include <vector>
#include <cstdio>
#include <algorithm>
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
        jint type
) {
    Generator g;
    setupGenerator(&g, MC_1_21, 0);

    bool isNether = (type >= 4);
    applySeed(&g, isNether ? DIM_NETHER : DIM_OVERWORLD, seed);

    StructureConfig config;
    int structType = (type < 4) ? Swamp_Hut : Fortress;
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

    int required = 1;
    if (type == 0 || type == 4) required = 4;
    else if (type == 1 || type == 5) required = 3;
    else if (type == 2 || type == 6) required = 2;

    for (int rz = startRegionZ; rz < endRegionZ; ++rz) {
        for (int rx = startRegionX; rx < endRegionX; ++rx) {
            Pos p[4];
            std::vector<Pos> valid_pos;

            for (int i = 0; i < 4; ++i) {
                int dx = i % 2;
                int dz = i / 2;
                Pos pos;
                if (getStructurePos(structType, MC_1_21, (uint64_t)seed, rx + dx, rz + dz, &pos)) {
                    int b = getBiomeAt(&g, 4, pos.x, 64, pos.z);
                    bool biomeValid = false;
                    if (type < 4) { // Witch Hut
                        // Only Swamp (swamp, swamp_hills, mangrove_swamp)
                        if (b == swamp || b == swamp_hills || b == mangrove_swamp) biomeValid = true;
                    } else { // Fortress
                        // Only Soul Sand Valley as requested
                        if (b == soul_sand_valley) biomeValid = true;
                    }

                    if (biomeValid) {
                        valid_pos.push_back(pos);
                    }
                }
            }

            if ((int)valid_pos.size() >= required) {
                if (required > 1) {
                    // Check all combinations of 'required' size
                    int n = valid_pos.size();
                    std::vector<int> indices(required);
                    for(int i=0; i<required; ++i) indices[i] = i;

                    while(true) {
                        int minX = valid_pos[indices[0]].x, maxX = valid_pos[indices[0]].x;
                        int minZ = valid_pos[indices[0]].z, maxZ = valid_pos[indices[0]].z;
                        for(int i=1; i<required; ++i) {
                            if(valid_pos[indices[i]].x < minX) minX = valid_pos[indices[i]].x;
                            if(valid_pos[indices[i]].x > maxX) maxX = valid_pos[indices[i]].x;
                            if(valid_pos[indices[i]].z < minZ) minZ = valid_pos[indices[i]].z;
                            if(valid_pos[indices[i]].z > maxZ) maxZ = valid_pos[indices[i]].z;
                        }
                        int midX = (minX + maxX) / 2;
                        int midZ = (minZ + maxZ) / 2;

                        bool allClose = true;
                        for(int i=0; i<required; ++i) {
                            long long dx = valid_pos[indices[i]].x - midX;
                            long long dz = valid_pos[indices[i]].z - midZ;
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

                        // Next combination
                        int i = required - 1;
                        while(i >= 0 && indices[i] == n - required + i) i--;
                        if(i < 0) break;
                        indices[i]++;
                        for(int j=i+1; j<required; ++j) indices[j] = indices[i] + j - i;
                    }
                } else {
                    // Single structure
                    for(auto& pos : valid_pos) {
                        long long dcx = pos.x - centerX;
                        long long dcz = pos.z - centerZ;
                        if (dcx*dcx + dcz*dcz <= (long long)range * range) {
                            char buf[128];
                            sprintf(buf, "%d, %d", pos.x, pos.z);
                            results.push_back(buf);
                        }
                    }
                }
            }
        }
    }

    std::sort(results.begin(), results.end());
    results.erase(std::unique(results.begin(), results.end()), results.end());

    jobjectArray ret = (jobjectArray)env->NewObjectArray(results.size(), env->FindClass("java/lang/String"), env->NewStringUTF(""));
    for (size_t i = 0; i < results.size(); ++i) {
        env->SetObjectArrayElement(ret, i, env->NewStringUTF(results[i].c_str()));
    }

    return ret;
}
