package zio.gui.zswing.components

import javax.swing.{JMenu, JMenuBar, JMenuItem}
import zio._

object ZMenu {
  final case class MenuRoot(childs: Seq[MenuPart]) {
    def renderOrNull: Task[JMenuBar] =
      if (this == MenuRoot.Empty) ZIO.succeed(null)
      else
        for {
          rt <- ZIO.runtime[Any]
          bar <- ZIO.effectTotal { new JMenuBar() }
          topItems <- ZIO.foreach(childs)(p => buildMenuPart(rt, p))
        } yield {
          topItems.foreach(bar.add)
          bar
        }
  }
  object MenuRoot {
    val Empty: MenuRoot = MenuRoot(Seq.empty)
  }
  private def buildMenuPart(rt: Runtime[Any], part: MenuPart): Task[JMenuItem] =
    part match {
      case SubMenu(name, childs) =>
        for {
          menuItem <- ZIO.effect(new JMenu(name))
          pats <- ZIO.foreach(childs)(p => buildMenuPart(rt, p))
          _ <- ZIO.effect(pats.foreach(menuItem.add))
        } yield menuItem
      case MenuItem(name, action) =>
        ZIO.effect {
          val item = new JMenuItem(name)
          item.addActionListener(_ => rt.unsafeRun(action))
          item
        }
    }

  sealed trait MenuPart
  final case class SubMenu(name: String, childs: Seq[MenuPart]) extends MenuPart
  final case class MenuItem(name: String, action: Task[Unit]) extends MenuPart

  def apply(childs: MenuPart*): MenuRoot = new MenuRoot(childs)

  def root(childs: MenuPart*): UIO[MenuRoot] =
    ZIO.effectTotal(new MenuRoot(childs))

  def menu(name: String)(childs: MenuPart*): MenuPart = SubMenu(name, childs)

  def item(name: String, action: Task[Unit]): MenuPart = MenuItem(name, action)

  def itemNoOp(name: String): MenuPart =
    MenuItem(name, ZIO.effect(println(s"Menu item: $name")))
}
