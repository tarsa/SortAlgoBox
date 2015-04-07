package pl.tarsa.sortalgobox.opencl

import org.jocl.CL._
import org.jocl._

object CLContextsManager {
  lazy val contextsDescriptions = CLContextDescription.enumerate()

  def createCpuContext(): (cl_device_id, cl_context) = {
    contextsDescriptions
      .filter(_.matchesFragments(Option("Intel"), None, None))
      .head.toCLDeviceContext
  }

  def createGpuContext(): (cl_device_id, cl_context) = {
    contextsDescriptions
      .filter(_.deviceType == CL_DEVICE_TYPE_GPU)
      .head.toCLDeviceContext
  }
}
