Parameters of SortingAlgorithmsBenchmark's sorts and their porting status

- Item size - depends on items to be sorted
- Flag size - 1 bit
- Queue index size - 1 int
- Queue item size - depends on items to be sorted

|Name|Done?|Items|Queue (index)|Queue (items)|Flags|
|---|---|---:|---:|---:|---:|
|sortheapbinaryaheadsimplevarianta|No|1 \* N|
|sortheapbinaryaheadsimplevariantb|No|1 \* N|
|sortheapbinarycached|No|1 \* N|||1 \* N|
|sortheapbinarycascadingvarianta|No|1 \* N|64|
|sortheapbinarycascadingvariantb|No|1 \* N|64 - 10|
|sortheapbinarycascadingvariantc|No|1 \* N|(64 - 12) \* 5 \* 2|(64 - 12) \* 5|
|sortheapbinarycascadingvariantd|No|1 \* N|(64 - 12) \* 3|64 - 12|
|sortheapbinaryclusteredvarianta|No|1 \* N|
|sortheapbinaryclusteredvariantb|No|1 \* N|
|sortheapbinaryonebasedvarianta|Yes|1 \* N|
|sortheapbinaryonebasedvariantb|No|1 \* N|
|sortheaphybrid|No|1 \* N|
|sortheaphybridcascading|No|1 \* N|(64 - 8) \* 5 \* 2 + 64|
|sortheapquaternarycascadingvarianta|No|1 \* N|64|
|sortheapquaternaryvarianta|No|1 \* N|
|sortheapquaternaryvariantb|No|1 \* N|
|sortheapsimddwordcascadingvariantb|No|1 \* N|64|
|sortheapsimddwordcascadingvariantc|No|1 \* N|64|
|sortheapsimddwordvariantb|No|1 \* N|
|sortheapsimddwordvariantc|No|1 \* N|
|sortheapternarycascadingvarianta|No|1 \* N|64|
|sortheapternaryclusteredvarianta|No|1 \* N|
|sortheapternaryclusteredvariantb|No|1 \* N|
|sortheapternaryonebasedvarianta|No|1 \* N|
|sortheapternaryonebasedvariantb|No|1 \* N|
|sortquickrandomized|No|1 \* N|