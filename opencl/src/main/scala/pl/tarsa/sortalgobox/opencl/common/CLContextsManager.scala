package pl.tarsa.sortalgobox.opencl.common

import org.jocl.CL._

object CLContextsManager {
  lazy val contextsDescriptions = CLContextDescription.enumerate()

  def createCpuContext(): CLDeviceContext = {
    contextsDescriptions
      .filter(_.matchesFragments(Option("Intel"), None, None))
      .head.toCLDeviceContext
  }

  def createGpuContext(): CLDeviceContext = {
    contextsDescriptions
      .filter(_.deviceType == CL_DEVICE_TYPE_GPU)
      .head.toCLDeviceContext
  }

  def withCpuContext(f: CLDeviceContext => Unit): Unit = {
    val deviceContext = createCpuContext()
    try {
      f(deviceContext)
    } finally {
      clReleaseContext(deviceContext.context)
    }
  }

  def withGpuContext(f: CLDeviceContext => Unit): Unit = {
    val deviceContext = createGpuContext()
    try {
      f(deviceContext)
    } finally {
      clReleaseContext(deviceContext.context)
    }
  }
}
