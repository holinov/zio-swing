package example

import java.awt.Dimension

import javax.swing.JScrollPane
import zio._
import zio.gui.zswing.components._
import zio.gui.zswing.components.ZLayoutManager._
import zio.gui.zswing.components.ZMenu._

object TestUiApp extends App {

  //  val logic = ZIOJFrameNew.make("WND TITLE")

  private def die(ex: Throwable): Int = {
    println(s"ERROR: $ex")
    1
  }

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {

    def buildMenu(frame: ZFrame) = root(
      menu("File")(item("Exit", frame.dispose)),
      menu("Sub2")(itemNoOp("Item 2"), itemNoOp("Item 3")),
      itemNoOp("Item 4")
    )

    def buildMainScreen(implicit c: ZComponentFactory[Task]) = {
      def leftMenu(contents: ZLabel) =
        for {
          lst  <- c.list((0 to 100).map(idx => s"SYMBOL $idx"): _*)
          _    <- lst.onSimpleSelect(selected => contents.text(selected))
          pane <- ZIO.effect(ZComponent(new JScrollPane(lst.component)))
          _    <- pane.setPreferredSize(100, 300)
        } yield pane

      def rightPart(contents: ZLabel) =
        for {
          container <- c.border
          buttons <- ZIO.foreach((1 to 10).map(v => s"Butt $v"))(
                      txt => c.button(txt, _ => contents.text(txt))
                    )
          actionButtonsRow <- c.flow(buttons: _*)

          _ <- contents.setMinimumSize(new Dimension(500, 500))

          _ <- container.add(actionButtonsRow, Border.Constraints.PageStart)
          _ <- container.add(contents, Border.Constraints.Center)
        } yield container

      for {
        contents  <- c.label("TEST")
        root      <- c.box(false)
        leftMenu  <- leftMenu(contents)
        rightPart <- rightPart(contents)
        _         <- root.addAll(leftMenu, rightPart)
      } yield root
    }

    val logic = for {
//      res <- GuiHelpers.askSelection("Q1", Seq("GO?", "1", "2"))
//      _   <- ZIO.effect(println(s"RR < $res"))
      content  <- buildMainScreen
      frame    <- ZFrame.make(s"UIBot")
      mainMenu <- buildMenu(frame)
      _        <- frame.setMenu(mainMenu)
      _        <- frame.show(content)
      _        <- frame.show.fork
      _        <- frame.waitWindowClosed
    } yield ()

    logic.fold(die, _ => 0)
  }
}
