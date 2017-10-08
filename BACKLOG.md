Backlog:

 * convert native C++ sorts to use items agents
 * replace asserts with matchers in tests
 * add ActorRef wrappers with typed `tell` forwarders
 * remove native C++ items handlers when we are left with items agents only
 * make witness classes for native C++ items agents instead of useless
   inheritance of empty methods
 * use specialization in Scala items agents
 * implement guards in generated native C++ headers (eg enums)
 * decide on int8_t or uint8_t in native C++ number codecs and buffered I/O
 * implement missing unit tests
 * reorganize project structure to make more sense
 * improve web-based GUI
 * add charts to web-based GUI
 * remove JavaFX GUI once web GUI has charts
 * implement flexible central configuration
 * add support for LLVM/ Clang (that could e.g. make tests run quicker)
 * change license to Apache 2.0
 * use lenses from https://github.com/adamw/quicklens
