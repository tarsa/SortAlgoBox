# Sorting Algorithms Toolbox

Project goal:

 * provide a foundation for easy creation of new sorting algorithms
 
Implemented features:

 * executing sorting algorithms written in Java, Scala, 
   OpenCL (on both CPU and GPGPU) and C++
 * printing (to standard output) and graphing (using ScalaFX) benchmark results
 * generation of CMake projects for native C++ sorts
 * portable PRNG implementation (Mwc64x) in Scala, OpenCL (both CPU and GPGPU)
   and C++
 
Implemented sorting algorithms:

 * wrappers for standard Java sorts: Arrays.sort and Arrays.parallelSort
 * Scala sorts: Bitonic Sort, Bubble Sort, Heap Sort, Insertion Sort, 
   Merge Sort, Quick Sort, Radix Sort, Selection Sort, Shell Sort
 * OpenCL sorts for CPU: Bitonic Sort, Bubble Sort, Heap Sort, Insertion Sort,
   Merge Sort, Quick Sort, Radix Sort, Selection Sort, Shell Sort
 * OpenCL Bitonic Sort for GPGPU
 * native C++ sorts: Bubble Sort, wrapper for std::sort, 
   25 variants of Heap Sort, Quick Sort

Things to do after project checkout:

 * download Typesafe Activator or SBT and make it possible to invoke them from
   main project directory (e.g. by copying "activator", "activator.bat" and/ or
   "activator-launch-1.2.10.jar" into main project directory)
 * download and install OpenCL driver
 * create link to libOpenCL.so in main directory if needed, for example using
   "ln -s /usr/lib/libOpenCL.so.1 libOpenCL.so
 * run tests using "./activator test" or "sbt test"
 
Current basic goals:

 * applying Clean Code principles, making code well tested and testable
 * lots of sorting algorithms covered by elegant Scala implementations
 * lots (but maybe less) of sorting algorithms covered by performance optimized
   OpenCL implementations
 * thorough tests for Scala implementations
 * tests that check whether Scala and OpenCL implementations behave identically
   by checking if they do comparisons and swaps in the same order
 * making parallel performance optimized OpenCL implementations
 * making a benchmark suite that measures OpenCL implementations performance and
   sorting algorithms from Java standard libraries
 * make GPGPU implementations of (some) sorting algorithms
 
Current bonus goals:

 * making visualization of sorting in ScalaFX (JavaFX wrapper)
 * making performance optimized Scala or Java versions of sorting algorithms

Suggestions welcome.
