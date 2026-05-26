#include <jni.h>
#include <string>
#include <vector>
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

    StructureConfig config;
    if (type == 0) {
        config = Swampland_Hut_Config;
    } else {
        config = Fortress_Config;
    }

    int regionRange = (range / (int)config.spacing) + 2;
    int startRegionX = (centerX / (int)config.spacing) - regionRange;
    int startRegionZ = (centerZ / (int)config.spacing) - regionRange;
    int endRegionX = (centerX / (int)config.spacing) + regionRange;
    int endRegionZ = (centerZ / (int)config.spacing) + regionRange;

    std::vector<std::string> results;

    for (int rz = startRegionZ; rz < endRegionZ; ++rz) {
        for (int rx = startRegionX; rx < endRegionX; ++rx) {
            Pos p[4];
            int valid = 1;
            for (int i = 0; i < 4; ++i) {
                int dx = i % 2;
                int dz = i / 2;
                if (!getStructurePos(config, seed, rx + dx, rz + dz, &p[i])) {
                    valid = 0;
                    break;
                }

                // Biome check
                if (type == 0) { // Witch Hut
                    int b = getBiomeAt(&g, 4, p[i].x, 64, p[i].z);
                    if (b != swampland) {
                        valid = 0;
                        break;
                    }
                }
                // Fortress doesn't need biome check in cubiomes for basic valid position usually,
                // but for 1.21 nether it's slightly different. Cubiomes handles nether biomes too.
            }

            if (valid) {
                // Find a center point that is within maxDist of all 4 structures
                // A simple approximation: check the bounding box center
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
                    double dx = p[i].x - midX;
                    double dz = p[i].z - midZ;
                    if (dx*dx + dz*dz > (double)maxDist * maxDist) {
                        allClose = false;
                        break;
                    }
                }

                if (allClose) {
                    // Check if midX, midZ is within search range
                    double dcx = midX - centerX;
                    double dcz = midZ - centerZ;
                    if (dcx*dcx + dcz*dcz <= (double)range * range) {
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
