// Cpu version of Quick Sort

void sort(__global int * const, int);
void subSort(__global int * const, int, int);
int selectPivot(__global int * const, int const, int const);
int sweepLower(__global int * const, int const, int const, int const);
int sweepEqual(__global int * const, int const, int const, int const);
void swapBufferedIndexes(__global int * const, int * const,
    int const, int const);
void swap(__global int * const, int const, int const);

__kernel void sort(__global int * const array, int const n) {
    subSort(array, 0, n);
}

void subSort(__global int * const array, int start, int after) {
    int stackStart[700];
    int stackAfter[700];
    int stackItems = 0;
    stackStart[0] = start;
    stackAfter[0] = after;
    stackItems = 1;
    while (stackItems > 0) {
        stackItems--;
        start = stackStart[stackItems];
        after = stackAfter[stackItems];
        if (after - start > 1) {
            int const pivot = selectPivot(array, start, after);
            int const afterLower = sweepLower(array, start, after, pivot);
            int const afterEqual = sweepEqual(array, afterLower, after, pivot);
            int const lowerItems = afterLower - start;
            int const higherItems = after - afterEqual;
            int const higherFirst = lowerItems < higherItems;
            stackStart[stackItems + 1 - higherFirst] = afterEqual;
            stackAfter[stackItems + 1 - higherFirst] = after;
            stackStart[stackItems + higherFirst] = start;
            stackAfter[stackItems + higherFirst] = afterLower;
            stackItems += 2;
        }
    }
}

int selectPivot(__global int * const array, int const start, int const after) {
    return array[start + (after - start) / 2];
}

int sweepLower(__global int * const array, int const start, int const after,
    int const pivot) {
    int buffer[700];
    int bufferedItems = 0;
    int afterLower = start;
    for (int i = start; i < after; i++) {
        if (array[i] < pivot) {
            buffer[bufferedItems++] = i;
        }
        if (bufferedItems == 700) {
            swapBufferedIndexes(array, buffer, 700, afterLower);
            afterLower += 700;
            bufferedItems = 0;
        }
    }
    swapBufferedIndexes(array, buffer, bufferedItems, afterLower);
    return afterLower + bufferedItems;
}

int sweepEqual(__global int * const array, int const start, int const after,
    int const pivot) {
    int buffer[700];
    int bufferedItems = 0;
    int afterEqual = start;
    for (int i = start; i < after; i++) {
        if (array[i] == pivot) {
            buffer[bufferedItems++] = i;
        }
        if (bufferedItems == 700) {
            swapBufferedIndexes(array, buffer, 700, afterEqual);
            afterEqual += 700;
            bufferedItems = 0;
        }
    }
    swapBufferedIndexes(array, buffer, bufferedItems, afterEqual);
    return afterEqual + bufferedItems;
}

void swapBufferedIndexes(__global int * const array, int * const buffer,
    int const bufferedItems, int const start) {
    for (int i = 0; i < bufferedItems; i++) {
        swap(array, start + i, buffer[i]);
    }
}

void swap(__global int * const array, int const i1, int const i2) {
    int const temp = array[i1];
    array[i1] = array[i2];
    array[i2] = temp;
}
