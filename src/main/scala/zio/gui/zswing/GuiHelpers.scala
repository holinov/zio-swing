package zio.gui.zswing

import zio._
import zio.gui.zswing.components.{ ZComponentFactory, ZFrame }

sealed trait DialogResult
case object Unset  extends DialogResult
case object Ok     extends DialogResult
case object Cancel extends DialogResult

trait GuiHelpers[F[_]] {
  def dialog[T, R](title: String)(builder: ZFrame => F[T])(reader: T => F[R]): F[R]
  def askSelection(title: String, elements: Seq[String])(implicit ui: ZComponentFactory[F]): F[(DialogResult, String)]
}

object GuiHelpers {
  implicit val taskUiHelpers: GuiHelpers[Task] = new GuiHelpers[Task] {
    def dialog[T, R](title: String)(builder: ZFrame => Task[T])(reader: T => Task[R]): Task[R] =
      for {
        frame <- ZFrame.make(title)
        state <- builder(frame)
        _     <- frame.packAndShow
        fiber <- frame.waitWindowClosed.fork
        _     <- fiber.join
        res   <- reader(state)
      } yield res

    def askSelection(title: String, elements: Seq[String])(
      implicit ui: ZComponentFactory[Task]
    ): Task[(DialogResult, String)] =
      dialog(title)(
        frame =>
          for {
            vBox <- ui.box(true)

            selector <- ui.flow
            combo    <- ui.comboBox(elements)
            _        <- selector.add(combo)

            buttons   <- ui.flow
            resultRef <- Ref.make[DialogResult](Unset)

            button1 <- ui.button("OK", _ => resultRef.set(Ok) *> frame.dispose)
            button2 <- ui
                        .button("CANCEL", _ => resultRef.set(Cancel) *> frame.dispose)

            _ <- buttons.addAll(button1, button2)
            _ <- vBox.addAll(selector, buttons)
            _ <- frame.show(vBox)
          } yield (combo, resultRef)
      ) {
        case (box, value) => value.get <*> box.selected
      }
  }

  def apply[F[_]](implicit gh: GuiHelpers[F]): GuiHelpers[F] = gh
}
