package net.lambdaserve.views.jte

import gg.jte.output.WriterOutput
import gg.jte.resolve.{DirectoryCodeResolver, ResourceCodeResolver}
import gg.jte.{ContentType, TemplateEngine}
import net.lambdaserve.codec.EntityEncoder

import java.io.{OutputStream, OutputStreamWriter}
import java.nio.file.Path

class JteTemplateSupport(
  sourcePath: String,
  usePrecompiled: Boolean,
  generatedClassesPath: String = "target/jte-classes"
):
  val resolver =
    if sourcePath.startsWith("classpath:") then
      ResourceCodeResolver(sourcePath.substring(10))
    else DirectoryCodeResolver(Path.of(sourcePath))

  val templateEngine =
    if usePrecompiled then TemplateEngine.createPrecompiled(ContentType.Html)
    else TemplateEngine.create(resolver, Path.of(generatedClassesPath), ContentType.Html)

  def render[A](templateName: String, model: A, outputStream: OutputStream): Unit =
    val osw = new OutputStreamWriter(outputStream)
    templateEngine.render(templateName, model, WriterOutput(osw))
    osw.flush()

object JteTemplateSupport:
  def default: JteTemplateSupport =
    JteTemplateSupport("src/main/jte", false)

case class TemplateOutput[T](templateName: String, model: T)

object TemplateOutput:
  def apply(templateName: String): TemplateOutput[Nothing] =
    TemplateOutput(templateName, null.asInstanceOf[Nothing])

trait JteTemplateEncoder(jteTemplate: JteTemplateSupport):
  val contentType = "text/html; charset=UTF-8"

  given tagEncoder[T]: EntityEncoder[TemplateOutput[T]] with
    def bodyWriter(responseEntity: TemplateOutput[T]): OutputStream => Unit =
      os =>
        jteTemplate.render(responseEntity.templateName, responseEntity.model, os)
        os.flush()

    override val contentTypeHeader: String = contentType
end JteTemplateEncoder

object JteTemplateEncoder extends JteTemplateEncoder(JteTemplateSupport.default)
