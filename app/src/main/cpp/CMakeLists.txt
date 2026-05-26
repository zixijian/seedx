cmake_minimum_required(VERSION 3.22.1)

project("seedx")

add_library(cubiomes STATIC
    cubiomes/generator.c
    cubiomes/finders.c
    cubiomes/util.c
    cubiomes/layers.c
    cubiomes/quadbase.c
    cubiomes/noise.c
    cubiomes/biomes.c
    cubiomes/biomenoise.c
)

target_compile_options(cubiomes PRIVATE -fwrapv)

add_library(native-lib SHARED native-lib.cpp)

target_link_libraries(native-lib cubiomes log m)
