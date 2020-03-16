package zio.gui.zswing.components

import javax.swing.JComboBox
import zio.{Task, UIO, ZIO}

final class ZComboBox(val component: JComboBox[String])
    extends ZComponent[JComboBox[String]] {
  def selectedIndex(idx: Int): Task[Unit] = propW(_.setSelectedIndex)(idx)
  def selectedIndex: UIO[Int] = prop(_.getSelectedIndex)
  def selected: ZIO[Any, Nothing, String] =
    selectedIndex.map(component.getItemAt)
}
