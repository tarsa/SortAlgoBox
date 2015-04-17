// Cpu version of Merge Sort

void bottomUpMerge(__global int * const, __global int * const,
                   int const, int const, int const);

void arrayCopy(__global int * const, int const,
               __global int * const, int const,
               int const);

__kernel void sort(__global int * const array, int const n,
                   __global int * const buffer1, int const size1) {
    for (long width = 1; width < n; width *= 2) {
        for (long i = 0; i < n; i +=  width * 2) {
            long start = min(i, (long) n);
            long med = min(start + width, (long) n);
            long next = min(med + width, (long) n);
            bottomUpMerge(array, buffer1, (int) start, (int) med, (int) next);
        }
        arrayCopy(buffer1, 0, array, 0, n);
    }
}

void bottomUpMerge(__global int * const array, __global int * const buffer1,
                   int const start, int const med, int const next) {
    int left = start;
    int right = med;
    int dest = start;
    while (left < med && right < next) {
        if (array[left] <= array[right]) {
            buffer1[dest] = array[left];
            left++;
        } else {
            buffer1[dest] = array[right];
            right++;
        }
        dest++;
    }
    arrayCopy(array, left, buffer1, dest, med - left);
    arrayCopy(array, right, buffer1, dest + med - left, next - right);

}

void arrayCopy(__global int * const source, int const sourceStart,
               __global int * const target, int const targetStart,
               int const items) {
    int sIndex = sourceStart;
    int tIndex = targetStart;
    for (int i = 0; i < items; i++) {
        target[tIndex++] = source[sIndex++];
    }
}
