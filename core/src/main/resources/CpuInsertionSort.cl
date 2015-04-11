// Cpu version of Insertion Sort

void swap(__global int * const, int const, int const);

__kernel void sort(__global int * const array, int const n) {
    for (int i = 1; i < n; i++) {
        for (int j = i; j > 0; j--) {
            if (array[j - 1] > array[j]) {
                swap(array, j - 1, j);
            } else {
                break;
            }
        }
    }
}

void swap(__global int * const array, int const i1, int const i2) {
    int const temp = array[i1];
    array[i1] = array[i2];
    array[i2] = temp;
}
