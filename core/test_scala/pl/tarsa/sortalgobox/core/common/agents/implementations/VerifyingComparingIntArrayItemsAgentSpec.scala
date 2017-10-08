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
package pl.tarsa.sortalgobox.core.common.agents.implementations

import java.io.{ByteArrayInputStream, InputStream}

import org.scalatest.exceptions.TestFailedException
import pl.tarsa.sortalgobox.common.crossverify.TrackingEnums.ActionTypes._
import pl.tarsa.sortalgobox.core.common.agents.ComparingItemsAgent
import pl.tarsa.sortalgobox.core.crossverify.{
  PrematureEndOfInputException,
  PureNumberDecoder
}
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class VerifyingComparingIntArrayItemsAgentSpec extends CommonUnitSpecBase {
  typeBehavior[VerifyingComparingIntArrayItemsAgent]

  it must "fail for some operations and no recorded bytes" in {
    a[PrematureEndOfInputException] mustBe thrownBy {
      readTest(1, 2, 3)(_.compare0(1, 2))()
    }
  }

  it must "fail for no operations and some recorded bytes" in {
    a[TestFailedException] mustBe thrownBy {
      readTest(1, 2, 3)()(Get0.id, 2)
    }
  }

  it must "fail for mismatched operations and recorded bytes" in {
    a[TestFailedException] mustBe thrownBy {
      readTest(1, 2, 3)(_.swap0(1, 0))(Compare0.id, 3)
    }
  }

  it must "return correct size for empty array" in {
    readTest()(a => assert(a.size0 == 0))(Size0.id)
  }

  it must "return correct size for non-empty array" in {
    readTest(1, 2, 3)(a => assert(a.size0 == 3))(Size0.id)
  }

  it must "get proper values" in {
    readTest(5, 3, 2, 8)(
      a => assert(a.get0(0) == 5),
      a => assert(a.get0(1) == 3),
      a => assert(a.get0(2) == 2),
      a => assert(a.get0(3) == 8)
    )(Get0.id, 0, Get0.id, 1, Get0.id, 2, Get0.id, 3)
  }

  it must "set proper cells" in {
    writeTest(5, 3, 2, 8)(
      _.set0(3, 1),
      _.set0(1, 2),
      _.set0(0, 9),
      _.set0(3, 7)
    )(9, 2, 2, 7)(Set0.id, 3, Set0.id, 1, Set0.id, 0, Set0.id, 3)
  }

  it must "copy proper cells" in {
    writeTest(5, 3, 2, 8)(
      _.copy0(1, 0, 2)
    )(3, 2, 2, 8)(Copy0.id, 1, 0, 2)
  }

  it must "swap proper cells" in {
    writeTest(5, 3, 2, 8)(
      _.swap0(3, 0)
    )(8, 3, 2, 5)(Swap0.id, 3, 0)
  }

  it must "compare values properly" in {
    pureTest(
      a => assert(a.compare(1, 2) == -1),
      a => assert(a.compare(2, 1) == 1),
      a => assert(a.compare(1, 1) == 0)
    )(Compare.id, Compare.id, Compare.id)
  }

  it must "compare cells properly" in {
    readTest(5, 3, 2, 8, 5)(
      a => assert(a.compare0(0, 1) == 1),
      a => assert(a.compare0(2, 3) == -1),
      a => assert(a.compare0(0, 4) == 0)
    )(Compare0.id, 0, 1, Compare0.id, 2, 3, Compare0.id, 0, 4)
  }

  it must "work with different indexing bases" in {
    readTest(5, 3, 2, 8)(
      a => assert(a.withBase(0) eq a),
      a => assert(a.withBase(1).get0(1) == 5),
      a => assert(a.withBase(1).get0(4) == 8),
      a => assert(a.withBase(-1).get0(0) == 3),
      a => assert(a.withBase(-1).get0(2) == 8),
      a => assert(a.withBase(-1).withBase(1).get0(1) == 5),
      a => assert(a.withBase(-1).withBase(2).get0(2) == 5),
      a => assert(a.withBase(-1).withBase(2).get0(3) == 3)
    )(Seq(1, 4, 0, 2, 1, 2, 3).flatMap(Seq(Get0.id, _)): _*)
  }

  def verify(condition: Boolean): Unit = {
    if (!condition) {
      fail()
    }
  }

  def buildRecordingAgent(
      inputItems: Array[Int],
      recordedBytes: Seq[Int]): (InputStream, ComparingItemsAgent[Int]) = {
    val recordingStream =
      new ByteArrayInputStream(recordedBytes.map(_.toByte).toArray)
    val replayer = new PureNumberDecoder(recordingStream)
    val underlying = new ComparingIntArrayItemsAgent(inputItems)
    val recordingAgent =
      new VerifyingComparingIntArrayItemsAgent(replayer, underlying, verify)
    (recordingStream, recordingAgent)
  }

  def readTest(inputItems: Int*)(
      operations: (ComparingItemsAgent[Int] => Unit)*)(
      recordedBytes: Int*): Unit = {
    val (stream, agent) = buildRecordingAgent(inputItems.toArray, recordedBytes)
    operations.foreach(_(agent))
    assert(stream.read() == -1)

  }

  def writeTest(inputItems: Int*)(
      operations: (ComparingItemsAgent[Int] => Unit)*)(outputItems: Int*)(
      recordedBytes: Int*): Unit = {
    val array = inputItems.toArray
    val (stream, agent) = buildRecordingAgent(array, recordedBytes)
    operations.foreach(_(agent))
    assert(array.toSeq == outputItems)
    assert(stream.read() == -1)
  }

  def pureTest(operations: (ComparingItemsAgent[Int] => Unit)*)(
      recordedBytes: Int*): Unit = {
    val (stream, agent) = buildRecordingAgent(null, recordedBytes)
    operations.foreach(_(agent))
    assert(stream.read() == -1)
  }
}
