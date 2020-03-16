package zio.gui.zswing.components

import javax.swing.JTextField
import zio.{Task, UIO}

final class ZTextField(val component: JTextField)
    extends ZComponent[JTextField] {
  def text: UIO[String] = prop(_.getText)
  def text(text: String): Task[Unit] = propW(_.setText)(text)
}
