// Cpu version of Quick Sort

void sort(__global int * const, int);
void subSort(__global int * const, int, int);
int selectPivot(__global int * const, int const, int const);
int sweepLower(__global int * const, int const, int const, int const,
    int * const);
int sweepEqual(__global int * const, int const, int const, int const);
void swapBufferedIndexes(__global int * const, int * const,
    int const, int const);
void smallSort(__global int * const, int const, int const);
void swap(__global int * const, int const, int const);
void swapPrivate(int * const, int const, int const);

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
        if (after - start > 50) {
            int const pivot = selectPivot(array, start, after);
            int equalItems = 0;
            int const afterLower = sweepLower(array, start, after,
                pivot, &equalItems);
            int const lowerItems = afterLower - start;
            if (lowerItems < equalItems || equalItems > (after - start) / 4) {
                int const afterEqual = sweepEqual(array, afterLower, after,
                    pivot);
                int const higherItems = after - afterEqual;
                int const higherFirst = lowerItems < higherItems;
                stackStart[stackItems + 1 - higherFirst] = afterEqual;
                stackAfter[stackItems + 1 - higherFirst] = after;
                stackStart[stackItems + higherFirst] = start;
                stackAfter[stackItems + higherFirst] = afterLower;
            } else {
                int const higherOrEqualItems = after - afterLower;
                int const higherOrEqualFirst = lowerItems < higherOrEqualItems;
                stackStart[stackItems + 1 - higherOrEqualFirst] = afterLower;
                stackAfter[stackItems + 1 - higherOrEqualFirst] = after;
                stackStart[stackItems + higherOrEqualFirst] = start;
                stackAfter[stackItems + higherOrEqualFirst] = afterLower;
            }
            stackItems += 2;
        } else {
            smallSort(array, start, after);
        }
    }
}

int selectPivot(__global int * const array, int const start, int const after) {
    int const length = after - start;
    if (length < 100) {
        int pivots[3];
        pivots[0] = array[start];
        pivots[1] = array[start + length / 2];
        pivots[2] = array[after - 1];
        if (pivots[0] > pivots[1]) {
            swapPrivate(pivots, 0, 1);
        }
        if (pivots[1] > pivots[2]) {
            swapPrivate(pivots, 1, 2);
        }
        if (pivots[0] > pivots[1]) {
            swapPrivate(pivots, 0, 1);
        }
        return pivots[1];
    } else {
        int const seventh = length / 7;
        for (int i = 0; i < 5; i++) {
            swap(array, start + i, start + (i + 1) * seventh);
        }
        smallSort(array, start, start + 5);
        return array[start + 2];
    }
}

int sweepLower(__global int * const array, int const start, int const after,
    int const pivot, int * const equalItems) {
    int buffer[700];
    int bufferedItems = 0;
    int afterLower = start;
    int equal = 0;
    for (int i = start; i < after; i++) {
        buffer[bufferedItems] = i;
        bufferedItems += array[i] < pivot;
        equal += array[i] == pivot;
        if (bufferedItems == 700) {
            swapBufferedIndexes(array, buffer, 700, afterLower);
            afterLower += 700;
            bufferedItems = 0;
        }
    }
    *equalItems = equal;
    swapBufferedIndexes(array, buffer, bufferedItems, afterLower);
    return afterLower + bufferedItems;
}

int sweepEqual(__global int * const array, int const start, int const after,
    int const pivot) {
    int buffer[700];
    int bufferedItems = 0;
    int afterEqual = start;
    for (int i = start; i < after; i++) {
        buffer[bufferedItems] = i;
        bufferedItems += array[i] == pivot;
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

void smallSort(__global int * const array, int const start, int const after) {
    for (int i = start + 1; i < after; i++) {
        for (int j = i; j > start; j--) {
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

void swapPrivate(int * const array, int const i1, int const i2) {
    int const temp = array[i1];
    array[i1] = array[i2];
    array[i2] = temp;
}
