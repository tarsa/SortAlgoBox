package pl.tarsa.sortalgobox.opencl

import org.jocl.CL._
import org.jocl._

class CLContextDescription(val platformName: String,
                           val deviceName: String,
                           val vendorName: String,
                           val deviceType: Long,
                           platformId: cl_platform_id,
                           deviceId: cl_device_id) {
  def matchesFragments(platformNameFragment: Option[String],
                       deviceNameFragment: Option[String],
                       vendorNameFragment: Option[String]): Boolean = {
    def fragmentMatches(fragmentOpt: Option[String], name: String) = {
      fragmentOpt match {
        case Some(fragment) => name.contains(fragment)
        case None => true
      }
    }
    val platformMatches = fragmentMatches(platformNameFragment, platformName)
    val deviceMatches = fragmentMatches(deviceNameFragment, deviceName)
    val vendorMatches = fragmentMatches(vendorNameFragment, vendorName)
    platformMatches && deviceMatches && vendorMatches
  }

  def toCLDeviceContext: (cl_device_id, cl_context) = {
    val contextProperties = new cl_context_properties()
    contextProperties.addProperty(CL_CONTEXT_PLATFORM, platformId)
    val context = clCreateContext(contextProperties, 1, Array(deviceId),
      null, null, null)
    (deviceId, context)
  }
}

object CLContextDescription {
  def unapply(clContext: CLContextDescription) = Option(clContext).map {
    c => (c.platformName, c.deviceName, c.vendorName)
  }

  def enumerate(): List[CLContextDescription] = {
    val platformsIds = {
      val numPlatforms = Array(0)
      clGetPlatformIDs(0, null, numPlatforms)

      val platformsArray = Array.ofDim[cl_platform_id](numPlatforms(0))
      clGetPlatformIDs(platformsArray.length, platformsArray, null)
      platformsArray.toList
    }

    val platformsWithDevices = platformsIds.map { platformId =>
      val numDevices = Array(0)
      clGetDeviceIDs(platformId, CL_DEVICE_TYPE_ALL, 0, null, numDevices)

      val devicesIdsArray = Array.ofDim[cl_device_id](numDevices(0))
      clGetDeviceIDs(platformId, CL_DEVICE_TYPE_ALL, numDevices(0),
        devicesIdsArray, null)

      (platformId, devicesIdsArray.toList)
    }

    platformsWithDevices.flatMap { case (platformId, devicesIds) =>
      val platformName = getString(platformId, CL_PLATFORM_NAME)
      devicesIds.map { deviceId =>
        val deviceName = getString(deviceId, CL_DEVICE_NAME)
        val vendorName = getString(deviceId, CL_DEVICE_VENDOR)
        val deviceTypeArray = Array(0L)
        clGetDeviceInfo(deviceId, CL_DEVICE_TYPE, Sizeof.cl_long,
          Pointer.to(deviceTypeArray), null)
        val deviceType = deviceTypeArray(0)

        new CLContextDescription(platformName, deviceName, vendorName,
          deviceType, platformId, deviceId)
      }
    }
  }

  private def getString(deviceId: cl_device_id, paramName: Int): String = {
    val size = Array(0L)
    clGetDeviceInfo(deviceId, paramName, 0, null, size)
    val buffer = Array.ofDim[Byte](size(0).toInt)
    clGetDeviceInfo(deviceId, paramName, buffer.length, Pointer.to(buffer),
      null)
    new String(buffer, 0, buffer.length - 1)
  }

  private def getString(platformId: cl_platform_id, paramName: Int): String = {
    val size = Array(0L)
    clGetPlatformInfo(platformId, paramName, 0, null, size)
    val buffer = Array.ofDim[Byte](size(0).toInt)
    clGetPlatformInfo(platformId, paramName, buffer.length, Pointer.to(buffer),
      null)
    new String(buffer, 0, buffer.length - 1)
  }
}
