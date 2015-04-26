// Cpu version of Bitonic Sort

int getPhasesPerBlock(int const);
void sortPair(__global int * const, int const, int const, int const);
void swap(__global int * const, int const, int const);

__kernel void sort(__global int * const array, int const n) {
    int const phasesPerBlock = getPhasesPerBlock(n);
    for (int phasesInBlock = 1; phasesInBlock <= phasesPerBlock;
        phasesInBlock++) {
        for (int phase = phasesInBlock; phase >= 1; phase--) {
            bool const firstPhaseInBlock = phase == phasesInBlock;
            int const halfBlockSize = 1 << (phase - 1);
            if (firstPhaseInBlock) {
                for (int i = 0; i < n / 2; i++) {
                    int const upper = (i + (i & -halfBlockSize))
                        ^ (halfBlockSize - 1);
                    int const lower = upper ^ ((halfBlockSize << 1) - 1);
                    sortPair(array, n, upper, lower);
                }
            } else {
                for (int i = 0; i < n / 2; i++) {
                    int const upper = i + (i & -halfBlockSize);
                    int const lower = upper + halfBlockSize;
                    sortPair(array, n, upper, lower);
                }
            }
        }
    }
}

int getPhasesPerBlock(int const n) {
    int phases = 1;
    while ((1L << (phases - 1)) <= n - 1) {
        phases++;
    }
    return phases - 1;
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
