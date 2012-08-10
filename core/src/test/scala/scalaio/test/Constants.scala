package scalaio.test

import language.reflectiveCalls
import java.io.{
InputStreamReader, BufferedReader
}
import scalax.io.Resource
import java.io.FilterInputStream

object Constants {
  final lazy val IMAGE = resource("resources/image.png", getClass())

  final lazy val IMAGE_FILE_SIZE = Resource.fromInputStream(IMAGE.openStream()).byteArray.size

  final lazy val TEXT_VALUE = "1\na\nA\n\u00E0\n\u00E4\n\u00A3\n\u2248\n\u331B\n\u0060\n"

  def resource(resourceName: String, base: Class[_]) = new {
    private val r = {
	    base.getClassLoader.getResource(resourceName) match {
	      case null => new java.net.URL(base.getClassLoader.getResource(".").toExternalForm+resourceName)
	      case url => url
	    }
    }
    
    def openStream(closeFunction: () => Unit = () => ()) = new FilterInputStream(r.openStream()){
      override def close() = {
        closeFunction()
        super.close()
      }
    }
  }
}
