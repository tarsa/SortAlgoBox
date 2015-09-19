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
package pl.tarsa.sortalgobox.natives.crossverify

import java.io.PrintStream
import java.util.Scanner

import pl.tarsa.sortalgobox.natives.build._
import pl.tarsa.sortalgobox.tests.NativesUnitSpecBase

import scala.io.Source

class NativeNumberCodecSpec extends NativesUnitSpecBase {
  behavior of "NativeNumberCodec"

  import NativeNumberCodecSpec._

  for ((Test(testName, testMode, _), testIndex) <- tests.zipWithIndex) {
    it should testName in {
      val specProcess = testNativesCache.runCachedProgram(buildConfig)
      try {
        val pipeTo = new PrintStream(specProcess.getOutputStream)
        pipeTo.println(testIndex)
        pipeTo.flush()
        val pipeFrom = new Scanner(specProcess.getInputStream)
        val Array(a, b, c) = Array.fill[Boolean](3)(pipeFrom.nextInt() != 0)
        import TestMode._
        testMode match {
          case SerializeInt | SerializeLong =>
            assert(a, ", buffer position")
            assert(b, ", contents")
            assert(c, ", error status")
          case DeserializeInt | DeserializeLong =>
            assert(a, ", decoded value")
            assert(b, ", buffer position")
            assert(c, ", error status")
        }
      } finally {
        specProcess.waitFor()
      }
    }
  }
}

object NativeNumberCodecSpec {

  object TestMode extends Enumeration {
    type TestMode = Value
    val SerializeInt, DeserializeInt, SerializeLong, DeserializeLong = Value
  }

  import TestMode._

  case class Test(name: String, mode: TestMode, params: String)

  val tests: List[Test] = List[(String, TestMode, String)](
    ("fail on serializing negative int", SerializeInt,
      "0, -1, nullptr, 0, NumberCodec::NegativeValue"),
    ("serialize int zero", SerializeInt,
      "1, 0, new int8_t[1]{0}, 1, NumberCodec::OK"),
    ("serialize positive int", SerializeInt,
      "9, 1234567, new int8_t[3]{-121, -83, 75}, 3, NumberCodec::OK"),
    ("serialize max int", SerializeInt,
      "9, INT32_MAX, new int8_t[5]{-1, -1, -1, -1, 7}, 5, NumberCodec::OK"),
    ("fail on int serialization when not enough remaining space", SerializeInt,
      "2, INT32_MAX, new int8_t[2]{-1, -1}, 2, NumberCodec::BufferOverflow"),
    ("fail on deserialization of empty buffer for int", DeserializeInt,
      "nullptr, 0, -1, 0, NumberCodec::BufferUnderflow"),
    ("fail on deserialization of unfinished negative sequence for int",
      DeserializeInt, "new int8_t[3]{-1, -2, -3}, 3, -1, 3, " +
      "NumberCodec::BufferUnderflow"),
    ("deserialize int zero", DeserializeInt,
      "new int8_t[1]{0}, 1, 0, 1, NumberCodec::OK"),
    ("fully deserialize weirdly encoded int zero", DeserializeInt,
      "new int8_t[8]{-128, -128, -128, -128, -128, -128, -128, 0}, 8, 0, 8, " +
        "NumberCodec::OK"),
    ("deserialize positive int", DeserializeInt,
      "new int8_t[3]{-121, -83, 75}, 3, 1234567, 3, NumberCodec::OK"),
    ("deserialize max int", DeserializeInt,
      "new int8_t[5]{-1, -1, -1, -1, 7}, 5, INT32_MAX, 5, NumberCodec::OK"),
    ("fail on deserialization of slightly too big number for int",
      DeserializeInt, "new int8_t[5]{-1, -1, -1, -1, 8}, 5, -1, 5, " +
      "NumberCodec::ValueOverflow"),
    ("fail on deserialization of way too big number for int", DeserializeInt,
      "new int8_t[6]{-1, -1, -1, -1, -1, 1}, 6, -1, 5, " +
        "NumberCodec::ValueOverflow"),
    ("fail on serializing negative long", SerializeLong,
      "0, -1L, nullptr, 0, NumberCodec::NegativeValue"),
    ("serialize long zero", SerializeLong,
      "1, 0, new int8_t[1]{0}, 1, NumberCodec::OK"),
    ("serialize positive long", SerializeLong,
      "9, 123456789000L, new int8_t[6]{-120, -76, -28, -12, -53, 3}, 6, " +
        "NumberCodec::OK"),
    ("serialize max long", SerializeLong,
      "9, INT64_MAX, new int8_t[9]{-1, -1, -1, -1, -1, -1, -1, -1, 127}, 9," +
        "NumberCodec::OK"),
    ("fail on long serialization when not enough remaining space",
      SerializeLong, "2, INT64_MAX, new int8_t[2]{-1, -1}, 2, " +
      "NumberCodec::BufferOverflow"),
    ("fail on deserialization of empty buffer for long", DeserializeLong,
      "nullptr, 0, -1, 0, NumberCodec::BufferUnderflow"),
    ("fail on deserialization of unfinished negative sequence for long",
      DeserializeLong, "new int8_t[3]{-1, -2, -3}, 3, -1, 3, " +
      "NumberCodec::BufferUnderflow"),
    ("deserialize long zero", DeserializeLong,
      "new int8_t[1]{0}, 1, 0, 1, NumberCodec::OK"),
    ("fully deserialize weirdly encoded long zero", DeserializeLong,
      "new int8_t[19]{-128, -128, -128, -128, -128, -128, -128, -128, -128, " +
        "-128, -128, -128, -128, -128, -128, -128, -128, -128, 0}, 19, 0, " +
        "19, NumberCodec::OK"),
    ("deserialize positive long", DeserializeLong,
      "new int8_t[6]{-120, -76, -28, -12, -53, 3}, 6, 123456789000L, 6, " +
        "NumberCodec::OK"),
    ("deserialize max long", DeserializeLong,
      "new int8_t[9]{-1, -1, -1, -1, -1, -1, -1, -1, 127}, 9, INT64_MAX, 9, " +
        "NumberCodec::OK"),
    ("fail on deserialization of slightly too big number for long",
      DeserializeLong, "new int8_t[10]{-1, -1, -1, -1, -1, -1, -1, -1, -1, " +
      "1}, 10, -1, 10, NumberCodec::ValueOverflow"),
    ("fail on deserialization of way too big number for long", DeserializeLong,
      "new int8_t[11]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0}, 11, -1, " +
      "10, NumberCodec::ValueOverflow")
  ).map(Test.tupled)

  val license_header = Source.fromInputStream(getClass.getResourceAsStream(
    "/pl/tarsa/sortalgobox/license_header"), "UTF-8").mkString

  val system_includes = List("cstdlib", "cstring", "iostream").map(s => s"<$s>")
  val local_includes = List("numbercodec.hpp").map('"' + _ + '"')

  def stringify_include(quotedFile: String) = s"#include $quotedFile"

  val includes_string = List(system_includes, local_includes)
    .map(_.map(stringify_include).mkString("\n")).mkString("", "\n\n", "\n")

  val enums_string = "\nenum test_t {\n" + tests.map(_.name).zipWithIndex.map {
    case (name, index) =>
      val filteredName = name.replace(' ', '_')
      s"    $filteredName = $index"
  }.mkString("", ",\n", "\n") + "};\n"

  def test_serialize_string(camel: String, bits: String) = s"""
void testSerialize$camel(ssize_t const bufferSize, int${bits}_t const value,
        int8_t const * const expectedOutput, ssize_t const expectedOutputLength,
        tarsa::NumberCodec::error_t const expectedError) {
    int8_t * const buffer = new int8_t[bufferSize];
    tarsa::NumberEncoder e(buffer, bufferSize);

    e.serialize$camel(value);

    bool const lengthIsGood = e.getBufferPosition() == expectedOutputLength;
    std::cout << lengthIsGood << std::endl;
    std::cout << (lengthIsGood && (memcmp(buffer, expectedOutput,
            expectedOutputLength * sizeof (int8_t)) == 0)) << std::endl;
    std::cout << (e.getError() == expectedError) << std::endl;

    delete [] buffer;
    delete [] expectedOutput;
}
"""

  val test_serialize_int_string = test_serialize_string("Int", "32")
  val test_serialize_long_string = test_serialize_string("Long", "64")

  def test_deserialize_string(camel: String, bits: String) = s"""
void testDeserialize$camel(int8_t const * const input, ssize_t const inputLength,
        int${bits}_t const expectedValue, ssize_t const expectedInputPosition,
        tarsa::NumberCodec::error_t const expectedError) {
    tarsa::NumberDecoder d(input, inputLength);

    int${bits}_t const decodedValue = d.deserialize$camel();

    std::cout << (decodedValue == expectedValue) << std::endl;
    std::cout << (d.getBufferPosition() == expectedInputPosition) << std::endl;
    std::cout << (d.getError() == expectedError) << std::endl;

    delete [] input;
}
"""

  val test_deserialize_int_string = test_deserialize_string("Int", "32")
  val test_deserialize_long_string = test_deserialize_string("Long", "64")

  val main_body = {
    val cases = tests.map { case Test(name, mode, params) =>
      val filteredName = name.replace(' ', '_')
      val modeString = mode match {
        case SerializeInt => "SerializeInt"
        case DeserializeInt => "DeserializeInt"
        case SerializeLong => "SerializeLong"
        case DeserializeLong => "DeserializeLong"
      }
      "" +
        s"        case $filteredName:\n" +
        s"            test$modeString($params);\n" +
        s"            break;"
    }.mkString("\n")
    s"""
using namespace tarsa;

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
    test_serialize_int_string + test_serialize_long_string +
    test_deserialize_int_string + test_deserialize_long_string + main_body

  val mainSourceFile = "spec.cpp"
  val numberCodecComponent = NativeBuildComponentFromResource(
    "/pl/tarsa/sortalgobox/natives/crossverify/", "numbercodec.hpp")
  val specBuildComponent = NativeBuildComponentFromString(sourceCode,
    mainSourceFile)
  val buildConfig = NativeBuildConfig(Seq(numberCodecComponent,
    specBuildComponent), mainSourceFile)
}
