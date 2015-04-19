// Cpu version of Shell Sort

void swap(__global int * const, int const, int const);

__kernel void sort(__global int * const array, int const n,
                   __global int * const gaps, int const nGaps) {
    for (int gapIndex = 0; gapIndex < nGaps; gapIndex++) {
        int const gap = gaps[gapIndex];
        for (int i = gap; i < n; i++) {
            int j = i;
            while (j >= gap && array[j - gap] > array[j]) {
                swap(array, j - gap, j);
                j -= gap;
            }
        }
    }
}

void swap(__global int * const array, int const i1, int const i2) {
    int const temp = array[i1];
    array[i1] = array[i2];
    array[i2] = temp;
}
