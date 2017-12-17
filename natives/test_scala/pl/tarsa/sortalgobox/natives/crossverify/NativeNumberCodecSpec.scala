/*
 * Copyright (C) 2015 - 2017 Piotr Tarsa ( http://github.com/tarsa )
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
 */
package pl.tarsa.sortalgobox.natives.crossverify

import pl.tarsa.sortalgobox.natives.build._
import pl.tarsa.sortalgobox.tests.NativesUnitSpecBase

class NativeNumberCodecSpec extends NativesUnitSpecBase {
  import NativeNumberCodecSpec._
  import TestMode._

  behavior of "NativeNumberCodec"

  for ((Test(testName, testMode, _), testIndex) <- tests.zipWithIndex) {
    it must testName in {
      val input = Seq(testIndex.toString)
      val execResult = testNativesCache.runCachedProgram(buildConfig, input)
      val flags = execResult.stdOut.lines.map(_.toInt != 0).toList
      testMode match {
        case SerializeBit | SerializeByte | SerializeInt | SerializeLong =>
          val List(a, b) = flags
          assert(a, ", contents")
          assert(b, ", error status")
        case DeserializeBit | DeserializeByte | DeserializeInt |
            DeserializeLong =>
          val List(a, b, c) = flags
          assert(a, ", decoded value")
          assert(b, ", buffer position")
          assert(c, ", error status")
      }
    }
  }
}

object NativeNumberCodecSpec extends NativeComponentsSupport {

  val filler = "0xb5"

  object TestMode extends Enumeration {
    type TestMode = Value
    val SerializeBit, DeserializeBit, SerializeByte, DeserializeByte,
    SerializeInt, DeserializeInt, SerializeLong, DeserializeLong = Value
  }
  import TestMode._

  case class Test(name: String, mode: TestMode, params: String)

  val tests: List[Test] = List[(String, TestMode, String)](
    ("serialize false value",
     SerializeBit,
     "1, false, new int8_t[1]{0}, 1, NumberCodec::NoError"),
    ("serialize true value",
     SerializeBit,
     "1, true, new int8_t[1]{1}, 1, NumberCodec::NoError"),
    ("deserialize false value",
     DeserializeBit,
     "new int8_t[1]{0}, 1, false, 1, NumberCodec::NoError"),
    ("deserialize true value",
     DeserializeBit,
     "new int8_t[1]{1}, 1, true, 1, NumberCodec::NoError"),
    ("fail on unexpected value when deserializing boolean",
     DeserializeBit,
     "new int8_t[1]{8}, 1, false, 1, NumberCodec::ValueOverflow"),
    ("serialize positive byte",
     SerializeByte,
     "1, 100, new int8_t[1]{100}, 1, NumberCodec::NoError"),
    ("serialize negative byte",
     SerializeByte,
     "1, -100, new int8_t[1]{-100}, 1, NumberCodec::NoError"),
    ("deserialize positive byte",
     DeserializeByte,
     "new int8_t[1]{100}, 1, 100, 1, NumberCodec::NoError"),
    ("deserialize negative byte",
     DeserializeByte,
     "new int8_t[1]{-100}, 1, -100, 1, NumberCodec::NoError"),
    ("serialize int zero",
     SerializeInt,
     "1, 0, new int8_t[1]{0}, 1, NumberCodec::NoError"),
    ("serialize positive int",
     SerializeInt,
     "9, 1234567, new int8_t[4]{-114, -38, -106, 1}, 4, NumberCodec::NoError"),
    ("serialize negative int",
     SerializeInt,
     "9, -1234567, new int8_t[4]{-115, -38, -106, 1}, 4, NumberCodec::NoError"),
    ("serialize INT32_MAX",
     SerializeInt,
     "9, INT32_MAX, new int8_t[5]{-2, -1, -1, -1, 15}, 5, " +
       "NumberCodec::NoError"),
    ("serialize INT32_MIN",
     SerializeInt,
     "9, INT32_MIN, new int8_t[5]{-1, -1, -1, -1, 15}, 5, " +
       "NumberCodec::NoError"),
    ("fail on int serialization when not enough remaining space",
     SerializeInt,
     s"2, INT32_MAX, new int8_t[2]{-2, -1}, 2, NumberCodec::BufferedIoError"),
    ("fail on deserialization of empty buffer for int",
     DeserializeInt,
     "nullptr, 0, -1, 0, NumberCodec::BufferedIoError"),
    ("fail on deserialization of unfinished negative sequence for int",
     DeserializeInt,
     "new int8_t[3]{-1, -2, -3}, 3, -1, 3, " +
       "NumberCodec::BufferedIoError"),
    ("deserialize int zero",
     DeserializeInt,
     "new int8_t[1]{0}, 1, 0, 1, NumberCodec::NoError"),
    ("deserialize positive int",
     DeserializeInt,
     "new int8_t[4]{-114, -38, -106, 1}, 4, 1234567, 4, NumberCodec::NoError"),
    ("deserialize negative int",
     DeserializeInt,
     "new int8_t[4]{-115, -38, -106, 1}, 4, -1234567, 4, NumberCodec::NoError"),
    ("deserialize INT32_MAX",
     DeserializeInt,
     "new int8_t[5]{-2, -1, -1, -1, 15}, 5, INT32_MAX, 5, " +
       "NumberCodec::NoError"),
    ("deserialize INT32_MIN",
     DeserializeInt,
     "new int8_t[5]{-1, -1, -1, -1, 15}, 5, INT32_MIN, 5, " +
       "NumberCodec::NoError"),
    ("fail on deserialization of slightly too big number for int",
     DeserializeInt,
     "new int8_t[5]{-2, -1, -1, -1, 16}, 5, -1, 5, " +
       "NumberCodec::ValueOverflow"),
    ("fail on deserialization of way too big number for int",
     DeserializeInt,
     "new int8_t[6]{-1, -1, -1, -1, -1, 1}, 6, -1, 5, " +
       "NumberCodec::ValueOverflow"),
    ("serialize long zero",
     SerializeLong,
     "1, 0, new int8_t[1]{0}, 1, NumberCodec::NoError"),
    ("serialize positive long",
     SerializeLong,
     "9, 123456789000L, new int8_t[6]{-112, -24, -56, -23, -105, 7}, 6, " +
       "NumberCodec::NoError"),
    ("serialize negative long",
     SerializeLong,
     "9, -123456789000L, new int8_t[6]{-113, -24, -56, -23, -105, 7}, 6, " +
       "NumberCodec::NoError"),
    ("serialize INT64_MAX",
     SerializeLong,
     "10, INT64_MAX, new int8_t[10]{-2, -1, -1, -1, -1, -1, -1, -1, -1, 1}, " +
       "10, NumberCodec::NoError"),
    ("serialize INT64_MIN",
     SerializeLong,
     "10, INT64_MIN, new int8_t[10]{-1, -1, -1, -1, -1, -1, -1, -1, -1, 1}, " +
       "10, NumberCodec::NoError"),
    ("fail on long serialization when not enough remaining space",
     SerializeLong,
     s"2, INT64_MAX, new int8_t[2]{-2, -1}, 2, NumberCodec::BufferedIoError"),
    ("fail on deserialization of empty buffer for long",
     DeserializeLong,
     "nullptr, 0, -1, 0, NumberCodec::BufferedIoError"),
    ("fail on deserialization of unfinished negative sequence for long",
     DeserializeLong,
     "new int8_t[3]{-1, -2, -3}, 3, -1, 3, " +
       "NumberCodec::BufferedIoError"),
    ("deserialize long zero",
     DeserializeLong,
     "new int8_t[1]{0}, 1, 0, 1, NumberCodec::NoError"),
    ("deserialize positive long",
     DeserializeLong,
     "new int8_t[6]{-112, -24, -56, -23, -105, 7}, 6, 123456789000L, 6, " +
       "NumberCodec::NoError"),
    ("deserialize negative long",
     DeserializeLong,
     "new int8_t[6]{-113, -24, -56, -23, -105, 7}, 6, -123456789000L, 6, " +
       "NumberCodec::NoError"),
    ("deserialize INT64_MAX",
     DeserializeLong,
     "new int8_t[10]{-2, -1, -1, -1, -1, -1, -1, -1, -1, 1}, 10, INT64_MAX, " +
       "10, NumberCodec::NoError"),
    ("deserialize INT64_MIN",
     DeserializeLong,
     "new int8_t[10]{-1, -1, -1, -1, -1, -1, -1, -1, -1, 1}, 10, INT64_MIN, " +
       "10, NumberCodec::NoError"),
    ("fail on deserialization of slightly too big number for long",
     DeserializeLong,
     "new int8_t[10]{-2, -1, -1, -1, -1, -1, -1, -1, -1, 3}, 10, -1, 10, " +
       "NumberCodec::ValueOverflow"),
    ("fail on deserialization of way too big number for long",
     DeserializeLong,
     "new int8_t[11]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0}, 11, -1, " +
       "10, NumberCodec::ValueOverflow")
  ).map(Test.tupled)

  val systemIncludes: List[String] =
    List("cstdlib", "cstring", "iostream").map(s => s"<$s>")

  val localIncludes: List[String] =
    List("number_codec.hpp").map('"' + _ + '"')

  def stringifyInclude(quotedFile: String): String =
    s"#include $quotedFile"

  val includesStr: String =
    List(systemIncludes, localIncludes)
      .map(_.map(stringifyInclude).mkString("\n"))
      .mkString("", "\n\n", "\n")

  val enumsStr: String =
    tests.zipWithIndex
      .map {
        case (test, index) =>
          val filteredName = test.name.replace(' ', '_')
          s"    $filteredName = $index"
      }
      .mkString("\nenum test_t {\n", ",\n", "\n};\n")

  def testSerializeStr(camel: String, cType: String) =
    s"""
void testSerialize$camel(size_t const bufferSize, $cType const value,
        int8_t const * const expectedOutput, size_t const expectedOutputLength,
        tarsa::NumberCodec::error_t const expectedError) {
    uint8_t * const buffer = new uint8_t[bufferSize];
    memset(buffer, $filler, bufferSize);
    tarsa::BufferedArrayWriter * writer =
        new tarsa::BufferedArrayWriter(buffer, bufferSize, 1);
    tarsa::NumberEncoder e(writer);

    e.serialize$camel(value);
    e.flush();

    std::cout << (
        memcmp(buffer, expectedOutput, expectedOutputLength) == 0) << std::endl;
    std::cout << (e.getError() == expectedError) << std::endl;

    delete writer;
    delete [] buffer;
    delete [] expectedOutput;
}
"""

  def testDeserializeStr(camel: String, cType: String) =
    s"""
void testDeserialize$camel(int8_t const * const _input,
        size_t const inputLength, $cType const expectedValue,
        size_t const expectedInputPosition,
        tarsa::NumberCodec::error_t const expectedError) {
    uint8_t * const input = new uint8_t[inputLength];
    memcpy(input, _input, inputLength);
    tarsa::BufferedArrayReader * reader =
        new tarsa::BufferedArrayReader(input, inputLength, 5);
    tarsa::NumberDecoder d(reader);

    $cType const decodedValue = d.deserialize$camel();

    std::cout << (decodedValue == expectedValue) << std::endl;
    std::cout << (reader->getPosition() == expectedInputPosition) << std::endl;
    std::cout << (d.getError() == expectedError) << std::endl;

    delete reader;
    delete [] input;
    delete [] _input;
}
"""

  val mainBody: String = {
    val cases = tests
      .map {
        case Test(name, mode, params) =>
          val filteredName = name.replace(' ', '_')
          val modeString = mode match {
            case SerializeBit    => "SerializeBit"
            case DeserializeBit  => "DeserializeBit"
            case SerializeByte   => "SerializeByte"
            case DeserializeByte => "DeserializeByte"
            case SerializeInt    => "SerializeInt"
            case DeserializeInt  => "DeserializeInt"
            case SerializeLong   => "SerializeLong"
            case DeserializeLong => "DeserializeLong"
          }
          s"""        case $filteredName:
             |            test$modeString($params);
             |            break;""".stripMargin
      }
      .mkString("\n")

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

  val sourceCode: String = {
    val helperFunctions =
      for {
        builder <- List(testSerializeStr _, testDeserializeStr _)
        (coderType, cType) <- List("Bit" -> "bool",
                                   "Byte" -> "int8_t",
                                   "Int" -> "int32_t",
                                   "Long" -> "int64_t")
      } yield {
        builder(coderType, cType)
      }
    (includesStr :: enumsStr :: helperFunctions ::: List(mainBody)).mkString
  }

  val mainSourceFile = "spec.cpp"
  val specBuildComponent =
    NativeBuildComponentFromString(sourceCode, mainSourceFile)
  val buildConfig = NativeBuildConfig(
    makeResourceComponents(
      ("/pl/tarsa/sortalgobox/natives/", "buffered_io.hpp"),
      ("/pl/tarsa/sortalgobox/natives/crossverify/", "number_codec.hpp")
    ) :+ specBuildComponent,
    mainSourceFile
  )
}
