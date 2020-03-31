package zio.gui.zswing.components

import javax.swing.JList
import zio._
import zio.gui.zswing.components.ZList.ObservableListModel
import zio.stream._

final class ZLog[T](val component: JList[T], inputQueue: Queue[T]) extends ZComponent[JList[T]] {
  def append(logLine: => Task[T]): Task[Unit] = (logLine >>= inputQueue.offer).fork.unit
}
object ZLog {
  def apply[T](list: ZList[T], inputQueue: Queue[T]): ZLog[T] = new ZLog(list.component, inputQueue)

  trait Factory[F[_]] {
    def make[T](implicit fac: ZComponentFactory[F]): F[ZLog[T]]
  }

  implicit val taskInstance: ZLog.Factory[Task] = new ZLog.Factory[Task] {
    override def make[T](implicit fac: ZComponentFactory[Task]): Task[ZLog[T]] =
      for {
        mdl   <- ObservableListModel.make[T]
        queue <- Queue.unbounded[T]
        zlist <- fac.list(mdl)
        _ <- Stream
              .fromQueue(queue)
              .tap(v => mdl.addItem(v))
              .runDrain
              .fork
      } yield ZLog(zlist, queue)
  }
}
