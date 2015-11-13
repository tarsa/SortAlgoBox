/**
 * Copyright (C) 2015 Piotr Tarsa ( http://github.com/tarsa )
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the author be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software. If you use this software
 * in a product, an acknowledgment in the product documentation would be
 * appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 *
 */
package pl.tarsa.sortalgobox.natives.common

import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.file.{Files, Paths, Path}
import java.util.Scanner

import pl.tarsa.sortalgobox.natives.build._
import pl.tarsa.sortalgobox.tests.NativesUnitSpecBase

import scala.io.Source

class NativeBufferedIoSpec extends NativesUnitSpecBase {
  behavior of "NativeBufferedIo"

  import NativeBufferedIoSpec._

  val testReadFromFile = new Test("read from file") {
    type T = Path

    def before() = {
      val path = Files.createTempFile(NativesCache.rootTempDir, "SortAlgoBox",
        "")
      val source = Iterator.iterate(5)(state => (state * 131) + 33)
      val content = source.take(100).map(_.toByte).toArray
      Files.write(path, content)
      (path, List(path.toString))
    }

    def body = s"""void $functionName() {
    std::string path;
    std::cin >> path;
    FILE * const inputFile = fopen(path.c_str(), "rb");
    BufferedReader reader(inputFile, 5);

    bool valid = true;
    int32_t state = 5;
    for (size_t i = 0; valid && (i < 100); i++) {
        int32_t const expected = state & 255;
        state = (state * 131) + 33;
        valid &= reader.read() == expected;
    }

    std::cout << valid << std::endl;

    fclose(inputFile);
}"""

    def after(path: Path) = {
      path.toFile.delete()
    }
  }

  val testDelayedWriteWorks = new Test("delay opening file until first write") {
    type T = Path

    def before() = {
      val dir = Files.createTempDirectory(NativesCache.rootTempDir, "test")
      val path = dir.resolve("output")
      (dir, List(path.toString))
    }

    def body = s"""void $functionName() {
    std::string path;
    std::cin >> path;

    BufferedWriter writer(path, 5);

    writer.write(1);
    writer.write(2);
    writer.write(3);
    writer.write(4);
    writer.write(5);

    std::cout << true << std::endl;
}"""

    def after(path: Path) = {
      val dir = path
      Files.delete(dir)
    }
  }

  val testWriteToFile = new Test("write to file") {
    type T = Path

    def before() = {
      val dir = Files.createTempDirectory(NativesCache.rootTempDir, "test")
      val path = dir.resolve("output")
      (dir, List(path.toString))
    }

    def body = s"""void $functionName() {
    std::string path;
    std::cin >> path;

    BufferedWriter writer(path, 5);

    writer.write(10);
    writer.write(20);
    writer.write(30);
    writer.write(40);
    writer.write(50);
    writer.write(60);
    writer.write(70);

    writer.flush(true);

    std::cout << true << std::endl;
}"""

    def after(path: Path) = {
      val dir = path
      val output = dir.resolve("output")
      val content = Files.readAllBytes(output)
      Files.delete(output)
      Files.delete(dir)
      content shouldEqual Array[Byte](10, 20, 30, 40, 50, 60, 70)
    }
  }

  val tests = List[Test](testReadFromFile, testDelayedWriteWorks,
    testWriteToFile)
  val buildConfig = makeBuildConfig(tests)

  NativesCache.rootTempDir.toFile.mkdir()

  for ((test, testIndex) <- tests.zipWithIndex) {
    it should test.name in {
      val (state, params) = test.before()
      val specProcess = testNativesCache.runCachedProgram(buildConfig)
      try {
        val pipeTo = new PrintStream(specProcess.getOutputStream)
        pipeTo.println(testIndex)
        params.foreach(pipeTo.println)
        pipeTo.flush()
        val pipeFrom = new Scanner(specProcess.getInputStream)
        assert(pipeFrom.nextInt() != 0)
      } finally {
        specProcess.destroyForcibly().waitFor()
        test.after(state)
      }
    }
  }
}

object NativeBufferedIoSpec {

  abstract case class Test(name: String) {

    val filteredName = name.replace(' ', '_')

    val functionName = s"test_$filteredName"

    def body: String

    type T

    def before(): (T, List[String])
    
    def after(state: T): Unit
  }

  def makeBuildConfig(tests: List[Test]) = {
    val license_header = Source.fromInputStream(getClass.getResourceAsStream(
      "/pl/tarsa/sortalgobox/license_header"), "UTF-8").mkString

    val system_includes = List("cstdlib", "cstring", "iostream")
      .map(s => s"<$s>")
    val local_includes = List("buffered_io.hpp")
      .map('"' + _ + '"')

    def stringify_include(quotedFile: String) = s"#include $quotedFile"

    val includes_string = List(system_includes, local_includes)
      .map(_.map(stringify_include).mkString("\n")).mkString("", "\n\n", "\n")

    val enums_string = "\nenum test_t {\n" +
      tests.map(_.filteredName).zipWithIndex.map { case (filteredName, index) =>
        s"    $filteredName = $index"
      }.mkString("", ",\n", "\n") + "};\n"

    val namespace = "\nusing namespace tarsa;\n"

    val tests_bodies = tests.map(_.body).mkString("\n", "\n\n", "\n")

    val main_body = {
      val cases = tests.map { test =>
        import test._
        "" +
          s"        case $filteredName:\n" +
          s"            $functionName();\n" +
          s"            break;"
      }.mkString("\n")
      s"""
int main(int argc, char** argv) {
    int32_t test;
    std::cin >> test;
    switch (test) {
$cases
        default:
            return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}
"""
    }

    val sourceCode = license_header + includes_string + enums_string +
      namespace + tests_bodies + main_body

    val mainSourceFile = "spec.cpp"
    val numberCodecComponent = NativeBuildComponentFromResource(
      "/pl/tarsa/sortalgobox/natives/", "buffered_io.hpp")
    val specBuildComponent = NativeBuildComponentFromString(sourceCode,
      mainSourceFile)

    NativeBuildConfig(Seq(numberCodecComponent, specBuildComponent),
      mainSourceFile)
  }
}
