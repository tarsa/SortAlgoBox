// Cpu version of Insertion Sort

void swap(__global int *, int, int);

__kernel void sort(__global int * array, int const n)
{
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

void swap(__global int * array, int i1, int i2) {
    int temp = array[i1];
    array[i1] = array[i2];
    array[i2] = temp;
}
