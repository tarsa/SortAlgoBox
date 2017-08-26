/*
 * Copyright (C) 2015, 2016 Piotr Tarsa ( http://github.com/tarsa )
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
package pl.tarsa.sortalgobox.common.system

import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files

import org.apache.commons.io.FileUtils
import pl.tarsa.sortalgobox.common.SortAlgoBoxConfiguration.rootTempDir

object ProcessUtils {
  def runSynchronously(commandLine: Seq[String], directory: File,
    stdInLines: Seq[String]): ProcessRunResult = {
    val stdStreamsDir = Files.createTempDirectory(rootTempDir, "stdStreams")
    try {
      val stdInFile = stdStreamsDir.resolve("stdIn").toFile
      val stdOutFile = stdStreamsDir.resolve("stdOut").toFile
      val stdErrFile = stdStreamsDir.resolve("stdErr").toFile
      FileUtils.write(stdInFile, stdInLines.mkString("", "\n", "\n"), UTF_8)
      val builder = new ProcessBuilder(commandLine: _*).directory(directory)
      builder.redirectInput(stdInFile)
      builder.redirectOutput(stdOutFile)
      builder.redirectError(stdErrFile)
      val process = builder.start()
      val exitValue = process.waitFor()
      val stdOut = FileUtils.readFileToString(stdOutFile, UTF_8)
      val stdErr = FileUtils.readFileToString(stdErrFile, UTF_8)
      ProcessRunResult(exitValue, stdOut, stdErr)
    } finally {
      FileUtils.deleteDirectory(stdStreamsDir.toFile)
    }
  }
}
