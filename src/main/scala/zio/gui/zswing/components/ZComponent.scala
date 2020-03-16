package zio.gui.zswing.components

import java.awt.{ Component, Dimension, PopupMenu }
import java.awt.event.ActionEvent

import javax.swing._
import zio._
import zio.gui.zswing.components.ZLayoutManager._
import zio.gui.zswing.components.ZList.ObservableListModel

trait ZComponent[+T <: Component] {
  def component: T

  def setPreferredSize(w: Int, h: Int): Task[Unit] =
    ZIO.effect(component.setPreferredSize(new Dimension(w, h)))
  def setPreferredSize(dim: Dimension): Task[Unit] =
    ZIO.effect(component.setPreferredSize(dim))

  def setMinimumSize(dim: Dimension): Task[Unit] =
    ZIO.effect(component.setMinimumSize(dim))
  def setMaximumSize(dim: Dimension): Task[Unit] =
    ZIO.effect(component.setMaximumSize(dim))

  def add(popup: PopupMenu): Task[Unit] = ZIO.effect(component.add(popup))

  def prop[P](prop: T => P): UIO[P]       = ZIO.effectTotal(prop(component))
  def propM[P](prop: T => UIO[P]): UIO[P] = prop(component)

  def propW[P](setter: T => P => Unit)(propVal: P): Task[Unit] = ZIO.effect {
    setter(component)(propVal)
  }
}

object ZComponent extends ZComponentFactory[Task] {

  def apply[T <: Component](cmp: T): ZComponent[T] = wrap(cmp)

  def wrap[T <: Component](cmp: T): ZComponent[T] = new ZComponent[T] {
    override def component: T = cmp
  }

  override def button[T](text: String, onClick: ActionEvent => Task[T]): Task[ZButton] =
    for {
      jb <- ZIO.effect(new JButton(text))
      _ <- ZIO
            .effectAsync[Any, Throwable, T](
              cb =>
                jb.addActionListener(
                  (e: ActionEvent) => cb(onClick(e).refineToOrDie)
                )
            )
            .fork
    } yield new ZButton(jb)

  def label(text: String): Task[ZLabel] = Task(new ZLabel(new JLabel(text)))
  def textField(cols: Int): Task[ZTextField] =
    Task(new ZTextField(new JTextField(cols)))
  def textField: Task[ZTextField] = textField(10)
  def comboBox(elements: Seq[String]): Task[ZComboBox] =
    Task(new ZComboBox(new JComboBox[String](elements.toArray)))

  def list[T]: Task[ZList[T]] =
    ZList.ObservableListModel.make[T].flatMap(mdl => ZList.build(mdl))
  def list[T](mdl: ListModel[T]): Task[ZList[T]] = ZList.build(mdl)
  def list[T](ts: T*): Task[ZList[T]] =
    ObservableListModel.make[T].tap(_.addItems(ts)) >>= (mdl => list(mdl))

  // Layouts
  def panel[C <: LayoutConstraints](mgr: ZLayoutManager[C]): Task[ZLayoutPanel[C]] =
    panel(_ => ZIO.succeed(mgr))

  def panel[C <: LayoutConstraints](mgr: JPanel => Task[ZLayoutManager[C]]): Task[ZLayoutPanel[C]] =
    for {
      panel   <- Task(new JPanel())
      manager <- mgr(panel)
      _       <- Task(panel.setLayout(manager.create))
    } yield new ZLayoutPanel(panel)

  def scroll: Task[ZLayoutPanel[NoConstraints.type]] = panel(Flow)
  def flow: Task[ZLayoutPanel[NoConstraints.type]]   = panel(Flow)
  def flow(cmps: ZComponent[Component]*): Task[ZLayoutPanel[NoConstraints.type]] = panel(Flow) flatMap { flow =>
    ZIO.foreach(cmps)(flow.add).as(flow)
  }

  def box(vertical: Boolean): Task[ZLayoutPanel[NoConstraints.type]] =
    panel(p => ZIO.succeed(Box(ZContainer(p), vertical)))

  def border: Task[ZLayoutPanel[BorderLayoutConstraints]] = panel[BorderLayoutConstraints](Border)
}
