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
package pl.tarsa.sortalgobox.natives

import pl.tarsa.sortalgobox.natives.CompilerOptions._

case class CompilerOptions(
  compiler: String = defaultCompiler,
  languageStandardOpt: Option[String] = defaultLanguageStandard,
  optimizationLevelOpt: Option[String] = defaultOptimizationLevel,
  defines: Seq[CompilerDefine] = defaultDefines,
  options: Seq[String] = defaultOptions,
  executableFileName: String = defaultExecutableFileName) {

  private def serializeCompiler = Seq(compiler)

  private def serializeLanguageStandard = languageStandardOpt match {
    case Some(languageStandard) => Seq(s"-std=$languageStandard")
    case None => Seq.empty[String]
  }

  private def serializeOptimizationLevel = optimizationLevelOpt.toSeq

  private def serializeDefines = defines.map(_.serialize)

  def serializeOptions = options

  private def serializeExecutableFileName = Seq("-o", executableFileName)

  def serialize: Seq[String] = Seq(serializeCompiler, serializeLanguageStandard,
    serializeOptimizationLevel, serializeDefines, serializeOptions,
    serializeExecutableFileName).flatten
}

object CompilerOptions {
  val defaultCompiler: String = "g++"
  val defaultLanguageStandard: Option[String] = Some("c++11")
  val defaultOptimizationLevel: Option[String] = Some("-O2")
  val defaultDefines: Seq[CompilerDefine] = Seq(
    CompilerDefine("xstr(s)", Some("str(s)")),
    CompilerDefine("str(s)", Some("#s")))
  val defaultOptions: Seq[String] = Seq("-fopenmp", "-mavx2")
  val defaultExecutableFileName: String = "program"

  val default = CompilerOptions()
}
