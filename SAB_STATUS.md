Parameters of SortingAlgorithmsBenchmark's sorts and their porting status

- Item size - depends on items to be sorted
- Flag size - 1 bit
- Queue index size - 1 int

|Name|Done?|Items|Queue|Flags|
|---|---|---:|---:|---:|
|sortheapbinaryaheadsimplevarianta|No|1 \* N|
|sortheapbinaryaheadsimplevariantb|No|1 \* N|
|sortheapbinarycached|No|1 \* N||1 \* N|
|sortheapbinarycascadingvarianta|No|1 \* N|64|
|sortheapbinarycascadingvariantb|No|1 \* N|64 - 10|
|sortheapbinaryclusteredvarianta|No|1 \* N|
|sortheapbinaryclusteredvariantb|No|1 \* N|
|sortheapbinaryonebasedvarianta|Yes|1 \* N|
|sortheapbinaryonebasedvariantb|No|1 \* N|
|sortheapsimddwordcascadingvariantb|No|1 \* N|64|
|sortheapsimddwordcascadingvariantc|No|1 \* N|64|
|sortheapsimddwordvariantb|No|1 \* N|
|sortheapsimddwordvariantc|No|1 \* N|
|sortquickrandomized|No|1 \* N|
