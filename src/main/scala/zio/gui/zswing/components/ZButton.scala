package zio.gui.zswing.components

import javax.swing.JButton
import zio.{Task, UIO}

final class ZButton(val component: JButton) extends ZComponent[JButton] {
  def text: UIO[String] = prop(_.getText)
  def text(text: String): Task[Unit] = propW(_.setText)(text)
}
