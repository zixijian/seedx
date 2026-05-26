cmake_minimum_required(VERSION 3.22.1)
project("seedx")

add_library(seedx SHARED native-lib.cpp
    cubiomes/layers.c
    cubiomes/generator.c
    cubiomes/finders.c
    cubiomes/util.c
    cubiomes/quadbase.c
)

target_include_directories(seedx PRIVATE cubiomes)

find_library(log-lib log)
target_link_libraries(seedx ${log-lib})
