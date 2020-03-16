package zio.gui.zswing.components

import java.awt.Component

import javax.swing.JPanel
import zio.Task

class ZLayoutPanel[C <: ZLayoutManager.LayoutConstraints](val component: JPanel)
    extends ZContainer[JPanel] {

  def add[T <: Component](comp: ZComponent[T], constraints: C): Task[Unit] =
    Task {
      component.add(comp.component, constraints.constraintValue)
    }
  def add[T <: Component](comp: Task[ZComponent[T]],
                          constraints: C): Task[Unit] = comp map { comp =>
    component.add(comp.component, constraints.constraintValue)
  }
}
