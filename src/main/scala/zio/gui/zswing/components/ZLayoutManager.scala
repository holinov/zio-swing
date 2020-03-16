package zio.gui.zswing.components

import java.awt.{BorderLayout, Container, FlowLayout, LayoutManager}

import javax.swing.BoxLayout

sealed trait ZLayoutManager[C] {
  def create: LayoutManager
}

object ZLayoutManager {
  sealed trait LayoutConstraints {
    def hasLayoutConstrains: Boolean
    def constraintValue: Any
  }

  final case object NoConstraints extends LayoutConstraints {
    val hasLayoutConstrains: Boolean = false
    val constraintValue: Unit = ()
  }

  sealed trait Constraint[T] extends LayoutConstraints {
    val hasLayoutConstrains: Boolean = true
  }

  final object Flow extends ZLayoutManager[NoConstraints.type] {
    override def create: LayoutManager = new FlowLayout()
  }

  final case class Box(container: ZContainer[Container], vertical: Boolean)
      extends ZLayoutManager[NoConstraints.type] {
    override def create: LayoutManager =
      new BoxLayout(
        container.component,
        if (vertical) BoxLayout.Y_AXIS else BoxLayout.X_AXIS
      )
  }

  sealed abstract class BorderLayoutConstraints(str: String)
      extends Constraint[String] {
    override def constraintValue: Any = str
  }
  final object Border extends ZLayoutManager[BorderLayoutConstraints] {
    object Constraints {
      final case object Center
          extends BorderLayoutConstraints(BorderLayout.CENTER)

      final case object North
          extends BorderLayoutConstraints(BorderLayout.NORTH)
      final case object South
          extends BorderLayoutConstraints(BorderLayout.SOUTH)
      final case object East extends BorderLayoutConstraints(BorderLayout.EAST)
      final case object West extends BorderLayoutConstraints(BorderLayout.WEST)

      final case object BeforeFirstLine
          extends BorderLayoutConstraints(BorderLayout.BEFORE_FIRST_LINE)
      final case object AfterLastLine
          extends BorderLayoutConstraints(BorderLayout.AFTER_LAST_LINE)
      final case object BeforeLineBegins
          extends BorderLayoutConstraints(BorderLayout.BEFORE_LINE_BEGINS)
      final case object AfterLineEnds
          extends BorderLayoutConstraints(BorderLayout.AFTER_LINE_ENDS)

      final case object PageStart
          extends BorderLayoutConstraints(BorderLayout.PAGE_START)
      final case object PageEnd
          extends BorderLayoutConstraints(BorderLayout.PAGE_END)
      final case object LineStart
          extends BorderLayoutConstraints(BorderLayout.LINE_START)
      final case object LineEnd
          extends BorderLayoutConstraints(BorderLayout.LINE_END)
    }

    override def create: LayoutManager = new BorderLayout()
  }

}
