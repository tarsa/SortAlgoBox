__kernel void DumpSamples_1(__global uint *array, uint n, uint chunkSize) {
    mwc64x_state_t rng;
    MWC64X_SeedStreams(&rng, 0, chunkSize);

    uint start = get_global_id(0) * chunkSize;
    uint end = min(n, start + chunkSize);
    for (uint i = start; i < end; i++) {
        array[i] = MWC64X_NextUint(&rng);
    }
}

__kernel void DumpSamples_2(__global uint *array, uint n, uint chunkSize) {
    mwc64xvec2_state_t rng;
    MWC64XVEC2_SeedStreams(&rng, 0, chunkSize);

    uint2 base = (uint2) get_global_id(0) * 2 * chunkSize;
    for (uint i = 0; i < chunkSize; i++) {
        uint2 x = MWC64XVEC2_NextUint2(&rng);
        uint2 idx = base + (uint2) (0, 1) * chunkSize + i;
        if (idx.s0 < n) array[idx.s0] = x.s0;
        if (idx.s1 < n) array[idx.s1] = x.s1;
    }
}

__kernel void DumpSamples_4(__global uint *array, uint n, uint chunkSize) {
    mwc64xvec4_state_t rng;
    MWC64XVEC4_SeedStreams(&rng, 0, chunkSize);

    uint4 base = (uint4) get_global_id(0) * 4 * chunkSize;
    for (uint i = 0; i < chunkSize; i++) {
        uint4 x = MWC64XVEC4_NextUint4(&rng);
        uint4 idx = base + (uint4) (0, 1, 2, 3) * chunkSize + i;
        if (idx.s0 < n) array[idx.s0] = x.s0;
        if (idx.s1 < n) array[idx.s1] = x.s1;
        if (idx.s2 < n) array[idx.s2] = x.s2;
        if (idx.s3 < n) array[idx.s3] = x.s3;
    }
}

__kernel void DumpSamples_8(__global uint *array, uint n, uint chunkSize) {
    mwc64xvec8_state_t rng;
    MWC64XVEC8_SeedStreams(&rng, 0, chunkSize);

    uint8 base = (uint8) get_global_id(0) * 8 * chunkSize;
    for (uint i = 0; i < chunkSize; i++) {
        uint8 x = MWC64XVEC8_NextUint8(&rng);
        uint8 idx = base + (uint8) (0, 1, 2, 3, 4, 5, 6, 7) * chunkSize + i;
        if (idx.s0 < n) array[idx.s0] = x.s0;
        if (idx.s1 < n) array[idx.s1] = x.s1;
        if (idx.s2 < n) array[idx.s2] = x.s2;
        if (idx.s3 < n) array[idx.s3] = x.s3;
        if (idx.s4 < n) array[idx.s4] = x.s4;
        if (idx.s5 < n) array[idx.s5] = x.s5;
        if (idx.s6 < n) array[idx.s6] = x.s6;
        if (idx.s7 < n) array[idx.s7] = x.s7;
    }
}
