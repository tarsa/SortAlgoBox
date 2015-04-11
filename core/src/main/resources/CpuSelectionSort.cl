// Cpu version of Selection Sort

void swap(__global int * const, int const, int const);

__kernel void sort(__global int * const array, int const n) {
    for (int i = 0; i < n - 1; i++) {
        int indexMin = i;
        for (int j = i + 1; j < n; j++) {
            if (array[j] < array[indexMin]) {
                indexMin = j;
            }
        }
        swap(array, i, indexMin);
    }
}

void swap(__global int * const array, int const i1, int const i2) {
    int const temp = array[i1];
    array[i1] = array[i2];
    array[i2] = temp;
}
