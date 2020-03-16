package zio.gui.zswing.components

import javax.swing.JLabel
import zio.{Task, UIO}

final class ZLabel(val component: JLabel) extends ZComponent[JLabel] {
  def text: UIO[String] = prop(_.getText)
  def text(text: String): Task[Unit] = propW(_.setText)(text)
}
