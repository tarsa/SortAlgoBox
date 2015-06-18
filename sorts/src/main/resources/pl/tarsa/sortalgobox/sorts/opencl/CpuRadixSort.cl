// Cpu version of Radix Sort

__kernel void sort(__global int * const array, int const n,
                   __global int * const buffer1, int const size1) {
    int const radix = 8;
    for (int shift = 0; shift < 32; shift += 8) {
        int counts[256];
        for (int i = 0; i < 256; i++) {
            counts[i] = 0;
        }
        for (int i = 0; i < n; i++) {
            int const key = ((array[i] ^ 0x80000000) >> shift) & 0xff;
            counts[key]++;
        }
        int prefixSums[256];
        prefixSums[0] = 0;
        for (int i = 1; i < 256; i++) {
            prefixSums[i] = prefixSums[i - 1] + counts[i - 1];
        }
        for (int i = 0; i < n; i++) {
            int const key = ((array[i] ^ 0x80000000) >> shift) & 0xff;
            buffer1[prefixSums[key]] = array[i];
            prefixSums[key]++;
        }
        for (int i = 0; i < n; i++) {
            array[i] = buffer1[i];
        }
    }
}
