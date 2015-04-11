package pl.tarsa.sortalgobox.opencl

import scala.collection.mutable.ArrayBuffer

case class TimeLineEvent(name: String, time: Long)

trait TimeLine {
  def append(eventName: String): Unit
  def printOut(): Unit
}

class RealTimeLine extends TimeLine {
  private val hiddenHead = TimeLineEvent("", 0)
  private val eventList = ArrayBuffer[TimeLineEvent](hiddenHead)

  override def append(name: String): Unit = {
    eventList.append(TimeLineEvent(name, System.nanoTime()))
  }

  override def printOut(): Unit = {
    for (Seq(oldEvent, TimeLineEvent(name, time)) <- eventList.sliding(2)) {
      print(if (oldEvent eq hiddenHead) {
        f"${"n/a"}%16s"
      } else {
        f"${(time - oldEvent.time) / 1e6}%16.6f"
      })
      println(f" ${time / 1e6}%16.6f $name")
    }
  }
}

object FakeTimeLine extends TimeLine {
  override def append(eventName: String): Unit = ()

  override def printOut(): Unit = ()
}
