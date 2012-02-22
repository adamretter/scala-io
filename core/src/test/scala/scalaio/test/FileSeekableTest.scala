package scalaio.test

import scalax.io.{ Resource, Seekable }
import org.junit.rules.TemporaryFolder
import org.junit.{ After, Before }
import org.junit.Assert._
import scalax.io._
import scalax.io.managed.SeekableByteChannelResource
import scalax.io.managed.InputStreamResource
import java.io.{IOException, RandomAccessFile, File, InputStream}

abstract class AbstractFileSeekableTest extends AbstractSeekableTests[SeekableByteChannel] with SeekableTestUtils[SeekableByteChannel] {
  var folder: TemporaryFolder = _

  @Before
  def before {
    folder = new TemporaryFolder()
    folder.create()
  }

  @After
  def after {
    folder.delete()
  }
  lazy val file = new File(folder.getRoot, "testfile")
  def forceErrorOnAccess = {
    file.delete()
    file.getParentFile.setReadOnly()
  }

  def canAddCloseAction = false
  override def scalaIoException_On_Write_Error_by_default{
    intercept[IOException] {
      errorOnWriteOut.write("hi")
    }
  }
  override def scalaIoException_On_Read_Error_by_default {
    intercept[IOException] {
      input(AbstractInputTests.ErrorOnRead,() => (), () => ()).bytes.head
    }
  }
}
class FileStringSeekableTest extends AbstractFileSeekableTest {
  def openResource(openFunction: () => Unit, closeAction: CloseAction[SeekableByteChannel]): SeekableResource[_] = {
    folder.delete()
    folder.create()
    Resource.fromFile(file.getAbsolutePath).addCloseAction(closeAction)
  }
}

class FileSeekableTest extends AbstractFileSeekableTest {
  def openResource(openFunction: () => Unit, closeAction: CloseAction[SeekableByteChannel]): SeekableResource[_] = {
    folder.delete()
    folder.create()
    Resource.fromFile(file).addCloseAction(closeAction)
  }
}

class RandomAccessFileSeekableTest extends AbstractFileSeekableTest {
  def openResource(openFunction: () => Unit, closeAction: CloseAction[SeekableByteChannel]): SeekableResource[_] = {
    folder.delete()
    folder.create()
    file.getParentFile.mkdirs()
    file.createNewFile()
    Resource.fromRandomAccessFile(new RandomAccessFile(file, "rw")).addCloseAction(closeAction)
  }


  /**
   * Overriding because this resource cannot append
   */
  override def openOutput: Unit = {
    var closes = 0;
    val (in,out) = open(CloseAction((c:Any) => closes += 1))
    out match {
      case out:OutputResource[_] =>

        assertEquals(0,closes)
        out.write("whoop!")
        assertEquals(1,closes)

        for (opened <- out.outputProcessor) {
          opened.write("hello")
          opened.write(" ")
          opened.write("world")
        }
        assertEquals(2,closes)
        assertEquals("hello world",in.slurpString)
      case _ => ()
    }
  }
}

class StraightCreationSeekableTest extends AbstractFileSeekableTest {
  override def canExecuteOpenFunction = true
  override def canAddCloseAction = true
  def openResource(openFunction: () => Unit, closeAction: CloseAction[SeekableByteChannel]): SeekableResource[_] = {
    folder.delete()
    folder.create()
    def channel = {
      openFunction()
      SeekableByteChannel.fromFile(file.getAbsolutePath)
    }
    Resource.fromSeekableByteChannel(channel).addCloseAction(closeAction)
  }
}
