package zio.gui.zswing.components

import java.awt.event.{WindowEvent, WindowListener}
import java.awt.Container

import ZFrame.FramePromise
import ZFrameEvent._
import ZMenu._
import javax.swing.JFrame
import zio._

class ZFrame(jFrame: JFrame, onClose: FramePromise[Unit])
    extends ZContainer[JFrame] {
  override def component: JFrame = jFrame

  def waitWindowClosed: Task[Unit] = onClose.await

  def pack: Task[Unit] = ZIO.effect(jFrame.pack())

  def setVisible(visible: Boolean): Task[Unit] =
    ZIO.effect(jFrame.setVisible(visible))
  def show: Task[Unit] = setVisible(true)
  def hide: Task[Unit] = setVisible(false)
  def dispose: Task[Unit] = ZIO.effect(jFrame.dispose())

  def setMenu(mainMenu: MenuRoot): Task[Unit] =
    mainMenu.renderOrNull.map(jm => jFrame.setJMenuBar(jm))

  def setContent(cnt: ZContainer[Container]): Task[Unit] =
    Task(jFrame.setContentPane(cnt.component))
  def packAndShow: Task[Unit] = pack *> show

  def show(cnt: ZContainer[Container]): Task[Unit] =
    setContent(cnt) *> packAndShow
}

object ZFrame {
  type FramePromise[T] = Promise[Nothing, T]

  def make(title: String,
           mainMenu: MenuRoot = MenuRoot.Empty,
           traceEvents: Boolean = false): Task[ZFrame] = {
    def runMessageQueue(jFrame: JFrame,
                        windowEvents: Queue[ZFrameEvent],
                        onClosePromise: FramePromise[Unit]) = {
      val processOneMessage =
        for {
          msg <- windowEvents.take
          _ <- ZIO
            .effect(
              s"windowEvent: $msg isSame: ${msg.event.getWindow == jFrame}"
            )
            .when(traceEvents)
          _ <- msg match {
            case Closing(event) =>
              ZIO.effect(jFrame.dispose()).when(event.getWindow == jFrame)
            case Closed(_) => onClosePromise.succeed(())
            case _         => ZIO.unit
          }
        } yield ()

      processOneMessage.forever.fork
    }

    for {
      windowEvents <- Queue.unbounded[ZFrameEvent]
      rt <- ZIO.runtime[Any]
      frame <- ZIO.effect(new JFrame(title))
      menu <- mainMenu.renderOrNull
      _ <- ZIO.effect(frame.setJMenuBar(menu))
      windowListener <- ZIO.effect(new WindowListener {
        private def event(event: ZFrameEvent): Unit =
          rt.unsafeRun(windowEvents.offer(event).unit)

        override def windowOpened(e: WindowEvent): Unit = event(Opened(e))
        override def windowClosing(e: WindowEvent): Unit = event(Closing(e))
        override def windowClosed(e: WindowEvent): Unit = event(Closed(e))
        override def windowIconified(e: WindowEvent): Unit = event(Iconified(e))
        override def windowDeiconified(e: WindowEvent): Unit =
          event(Deiconified(e))
        override def windowActivated(e: WindowEvent): Unit = event(Activated(e))
        override def windowDeactivated(e: WindowEvent): Unit =
          event(Deactivated(e))
      })
      onClosePromise <- Promise.make[Nothing, Unit]
      _ <- runMessageQueue(frame, windowEvents, onClosePromise)
      _ <- ZIO.effect(frame.addWindowListener(windowListener))
    } yield new ZFrame(frame, onClosePromise)
  }
}
