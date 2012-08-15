/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2009-2010, Jesse Eichar          **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scalaio.test.fs

import org.junit.Assert._
import org.junit.Test
import scalax.file._

import java.io.File
import java.net.URI

abstract class FsPathObjectTests extends Fixture {

  @Test
  def path_object_should_implicitly_create_path_from_string(): Unit = {
    import scalax.file.ImplicitConversions.string2path

    { // brackets needed so compiler doesn't think later implicit was a mistake
      assertSame(FileSystem.default, "nonsense path".fileSystem)
    }

    implicit val fs = fixture.fs
    val p:Path = "hi"
    assertSame(fs, "path".fileSystem)
  }

  @Test
  def path_object_should_implicitly_create_jfile_from_path(): Unit = {
    import scalax.file.ImplicitConversions.scalaPath2jfile

    val p = FileSystem.default.createTempFile()
    val file:java.io.File = p
    assertSame(p.jpath.toFile.getCanonicalPath, file.getCanonicalPath)
  }

  @Test
  def path_object_extract_path_segments(): Unit = {
    fixture.fs("a", "b") match {
      case Path("a", _) => () // good
      case _ =>  fail("Path extractor should have extracted path segments")
    }

  }

  @Test
  def path_object_should_implicitly_create_path_from_a_java_file() : Unit = {
    import scalax.file.ImplicitConversions.jfile2path

    assertSame(FileSystem.default, new File("nonsense path").fileSystem)

    implicit val fs = fixture.fs
    assertSame(FileSystem.default, new File("path").fileSystem)
  }

  @Test
  def path_object_should_create_paths_from_a_string() : Unit = {
    assertSame(FileSystem.default, Path("nonsense path").fileSystem)
  }

  @Test
  def path_object_can_create_paths_from_a_uri() : Unit = {
    assertEquals(FileSystem.default, Path(new URI("file:///tmp/")).get.fileSystem)
    val path = fixture.path
    val uri = path.toURI
    val fromURI = Path(uri).get
    assertEquals(fixture.fs, fromURI.fileSystem)
    assertEquals(path, fromURI)
  }
}
