// Cpu version of Bubble sort

void swap(__global int *, int, int);

__kernel void sort(__global int * array, int const n)
{
    for (int i = n - 1; i >= 0; i--) {
        for (int j = 0; j < i; j++) {
            if (array[j] > array[j + 1]) {
                swap(array, j, j + 1);
            }
        }
    }
}

void swap(__global int * array, int i1, int i2) {
    int temp = array[i1];
    array[i1] = array[i2];
    array[i2] = temp;
}
