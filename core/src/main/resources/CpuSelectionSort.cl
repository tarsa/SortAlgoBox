// Cpu version of Selection Sort

void swap(__global int *, int, int);

__kernel void sort(__global int * array, int const n)
{
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

void swap(__global int * array, int i1, int i2) {
    int temp = array[i1];
    array[i1] = array[i2];
    array[i2] = temp;
}
