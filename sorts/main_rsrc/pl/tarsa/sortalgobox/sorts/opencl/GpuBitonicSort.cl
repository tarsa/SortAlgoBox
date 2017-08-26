// Gpu version of Bitonic Sort

void sortPair(__global int * const, int const, int const, int const);
void swap(__global int * const, int const, int const);

__kernel void firstPhase(__global int * const array, int const n,
                         int const phase) {
    int const i = get_global_id(0);
    int const halfBlockSize = 1 << (phase - 1);
    int const upper = (i + (i & -halfBlockSize)) ^ (halfBlockSize - 1);
    int const lower = upper ^ ((halfBlockSize << 1) - 1);
    sortPair(array, n, upper, lower);
}

__kernel void followingPhase(__global int * const array, int const n,
                             int const phase) {
    int const i = get_global_id(0);
    int const halfBlockSize = 1 << (phase - 1);
    int const upper = i + (i & -halfBlockSize);
    int const lower = upper + halfBlockSize;
    sortPair(array, n, upper, lower);
}

void sortPair(__global int * const array, int const n,
              int const first, int const second) {
    if (second < n && array[first] > array[second]) {
        swap(array, first, second);
    }
}

void swap(__global int * const array, int const i1, int const i2) {
    int const temp = array[i1];
    array[i1] = array[i2];
    array[i2] = temp;
}
