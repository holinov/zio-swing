package zio.gui.zswing.components

import java.awt.Component
import java.awt.event.ActionEvent

import javax.swing.{ JPanel, ListModel }
import zio.gui.zswing.components.ZLayoutManager.{ BorderLayoutConstraints, LayoutConstraints, NoConstraints }
import zio.Task

trait ZComponentFactory[F[_]] {
  //////////////////////////
  // Usual components
  //////////////////////////

  def comboBox(elements: Seq[String]): F[ZComboBox]

  def label(text: String): F[ZLabel]

  def textField(cols: Int): F[ZTextField]
  def textField: F[ZTextField]

  def button[T](text: String, onClick: ActionEvent => F[T]): F[ZButton]

  def list[T]: F[ZList[T]]
  def list[T](ts: T*): F[ZList[T]]
  def list[T](mdl: ListModel[T]): F[ZList[T]]

  //////////////////////////
  // Layout panel components
  //////////////////////////
  def panel[C <: LayoutConstraints](mgr: ZLayoutManager[C]): F[ZLayoutPanel[C]]
  def panel[C <: LayoutConstraints](mgr: JPanel => F[ZLayoutManager[C]]): F[ZLayoutPanel[C]]

  def scroll: F[ZLayoutPanel[NoConstraints.type]]

  def flow: F[ZLayoutPanel[NoConstraints.type]]
  def flow(cmps: ZComponent[Component]*): F[ZLayoutPanel[NoConstraints.type]]

  def box(vertical: Boolean): F[ZLayoutPanel[NoConstraints.type]]

  def vBox: F[ZLayoutPanel[NoConstraints.type]] = box(true)
  def hHox: F[ZLayoutPanel[NoConstraints.type]] = box(false)

  def border: F[ZLayoutPanel[BorderLayoutConstraints]]
}

object ZComponentFactory {
  implicit val taskComponents: ZComponentFactory[Task] = ZComponent
}
