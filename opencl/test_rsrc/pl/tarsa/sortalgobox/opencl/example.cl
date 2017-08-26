__kernel void example(uint x, __global uint * y) {
    *y = x * 5;
}
