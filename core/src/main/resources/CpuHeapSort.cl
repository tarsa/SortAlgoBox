// Cpu version of Heap Sort

void swap(__global int * const, int const, int const);
void heapify(__global int * const, int const);
void drainHeap(__global int * const, int const);
void siftDown(__global int * const, int const, int const);

__kernel void sort(__global int * const array, int const n) {
    heapify(array, n);
    drainHeap(array, n);
}

void heapify(__global int * const array, int const n) {
    for (int i = n - 1; i >= 0; i--) {
        siftDown(array, i, n);
    }
}

void drainHeap(__global int * const array, int const n) {
    for (int i = n - 1; i > 0; i--) {
        swap(array, 0, i);
        siftDown(array, 0, i);
    }
}

void siftDown(__global int * const array, int const i, int const n) {
    int parent = i;
    while (true) {
        int const child1 = parent * 2 + 1;
        int const child2 = child1 + 1;
        int indexMax = parent;
        if (child1 < n && array[child1] > array[indexMax]) {
            indexMax = child1;
        }
        if (child2 < n && array[child2] > array[indexMax]) {
            indexMax = child2;
        }
        if (parent == indexMax) {
            break;
        } else {
            swap(array, parent, indexMax);
            parent = indexMax;
        }
    }
}

void swap(__global int * const array, int const i1, int const i2) {
    int const temp = array[i1];
    array[i1] = array[i2];
    array[i2] = temp;
}
