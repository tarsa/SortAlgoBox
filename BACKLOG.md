Backlog:

 * separate event handlers from items agents
   * event handler can be ignoring, recording or verifying
   * reduces code duplication (no recording or verifying items agents)
   * allows to subclass items agent to provide custom operations
   * candidates for using custom items agents are SIMD heap sorts and
     verticalLeaderSelect operations
 * update README.md
 * convert native C++ sorts to use items agents
 * add ActorRef wrappers with typed `tell` forwarders
 * remove native C++ items handlers when we are left with items agents only
 * implement guards in generated native C++ headers (eg enums)
 * implement missing unit tests
 * reorganize project structure to make more sense
 * improve web-based GUI
 * add charts to web-based GUI
 * remove JavaFX GUI once web GUI has charts
 * implement flexible central configuration
 * add support for LLVM/ Clang (that could e.g. make tests run quicker)
 * change license to Apache 2.0
 * use lenses from https://github.com/adamw/quicklens
 * use ScalaCheck or ScalaProps for property-based testing
