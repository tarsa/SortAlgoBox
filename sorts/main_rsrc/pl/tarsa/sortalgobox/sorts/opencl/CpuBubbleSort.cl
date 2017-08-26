// Cpu version of Bubble Sort

void swap(__global int * const, int const, int const);

__kernel void sort(__global int * const array, int const n) {
    for (int i = n - 1; i > 0; i--) {
        for (int j = 0; j < i; j++) {
            if (array[j] > array[j + 1]) {
                swap(array, j, j + 1);
            }
        }
    }
}

void swap(__global int * const array, int const i1, int const i2) {
    int const temp = array[i1];
    array[i1] = array[i2];
    array[i2] = temp;
}
