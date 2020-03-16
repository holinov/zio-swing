package zio.gui.zswing.components

import javax.swing.{ AbstractListModel, JList, ListModel }
import javax.swing.event.ListSelectionEvent
import zio._
import zio.stream._

object ZList {
  trait ObservableListModel[T] {
    def addItem(item: T): Task[Unit]
    def addItems(items: Seq[T]): Task[Unit]
    def refreshItems: Task[Unit]
  }

  object ObservableListModel {
    private class SeqRefListModelImpl[T](val stateRef: Ref[IndexedSeq[T]], rt: Runtime[Any])
        extends AbstractListModel[T]
        with ObservableListModel[T] {
      private def state = rt.unsafeRun(stateRef.get)

      override def getSize: Int                = state.size
      override def getElementAt(index: Int): T = state(index)

      def addItem(item: T): Task[Unit] = {
        def trim(i: Int) = if (i <= 0) 0 else i
        for {
          oldSize <- stateRef.get.map(_.size)
          updated <- stateRef.update(_ :+ item)
          _ <- ZIO.effect(
                fireIntervalAdded(this, trim(oldSize - 1), trim(updated.size - 1))
              )
        } yield ()
      }

      def addItems(items: Seq[T]): Task[Unit] =
        for {
          oldSize <- stateRef.get.map(_.size - 1).map(v => if (v > 0) v else 0)
          updated <- stateRef.update(_ ++ items)
          _       <- ZIO.effect(fireIntervalAdded(this, oldSize, updated.size - 1))
        } yield ()

      def refreshItems: Task[Unit] =
        stateRef.get.flatMap(
          state => ZIO.effect(fireContentsChanged(this, 0, state.size - 1))
        )
    }

    def make[T]: Task[AbstractListModel[T] with ObservableListModel[T]] =
      for {
        ref <- Ref.make(IndexedSeq.empty[T])
        rt  <- ZIO.runtime[Any]
      } yield new SeqRefListModelImpl(ref, rt)

  }

  def build(items: String*): Task[ZList[String]] =
    ObservableListModel
      .make[String]
      .tap(_.addItems(items))
      .map(mdl => new ZList(new JList(mdl)))

  def build[T](mdl: ListModel[T]): Task[ZList[T]] =
    Task(new ZList(new JList(mdl)))

  def build[T](mdl: ListModel[T], onSelected: T => Task[Unit]): Task[ZList[T]] =
    Task(new ZList(new JList(mdl))).tap(_.onSimpleSelect(onSelected))
}

class ZList[T](val component: JList[T]) extends ZComponent[JList[T]] {
  def selectedIndex(idx: Int): Task[Unit] = propW(_.setSelectedIndex)(idx)
  def selectedIndex: UIO[Int]             = prop(_.getSelectedIndex)
  def selected: Task[T]                   = prop(_.getSelectedValue)

  private def addSelectionListener(rt: Runtime[Any], act: (Int, Int) => Task[Unit]): Task[Unit] =
    Task(
      component.addListSelectionListener(
        (e: ListSelectionEvent) => rt.unsafeRun(act(e.getFirstIndex, e.getLastIndex))
      )
    ).unit

  def onSimpleSelect(act: T => Task[Unit]): Task[Unit] =
    for {
      rt            <- ZIO.runtime[Any]
      selectedQueue <- Queue.unbounded[(Int, Int, T)]
      lastValueRef  <- Ref.make((-1, -1, null.asInstanceOf[T]))
      _ <- addSelectionListener(
            rt,
            (f, t) => selected.flatMap(s => selectedQueue.offer((f, t, s)).fork.unit)
          )
      _ <- {
        val realAct: (Int, Int, T) => Task[Unit] = {
          case change @ (_, _, v) =>
            for {
              lastSelected <- lastValueRef.get
              _ <- (act(v) <* lastValueRef.set(change))
                    .when(lastSelected != change)
            } yield ()
        }

        Stream
          .fromQueue(selectedQueue)
          .tap(sel => realAct(sel._1, sel._2, sel._3))
          .runDrain
          .fork
      }
    } yield ()

}
