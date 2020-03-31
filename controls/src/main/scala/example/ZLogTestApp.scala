package example

import javax.swing.JScrollPane
import zio._
import zio.duration._
import zio.gui.zswing.components._
import zio.gui.zswing.components.ZLayoutManager.Border
import zio.gui.zswing.components.ZMenu._

object ZLogTestApp extends App {

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

    def buildMainScreen(implicit c: ZComponentFactory[Task], lf: ZLog.Factory[Task]) = {
      def leftMenu(contents: ZLabel) =
        for {
          lst  <- c.list((0 to 100).map(idx => s"SYMBOL $idx"): _*)
          _    <- lst.onSimpleSelect(selected => contents.text(selected))
          pane <- ZIO.effect(ZComponent(new JScrollPane(lst.component)))
          _    <- pane.setPreferredSize(100, 300)
        } yield pane

      def logCycle(l: ZLog[String]) =
        l.append(ZIO.effect(s" [ ${System.currentTimeMillis()} ] Tick  "))
          .repeat(Schedule.delayed(Schedule.duration(1.seconds)))
          .forever
          .fork

      def rightPart =
        for {
          container <- c.border
          log       <- lf.make[String]
          _         <- logCycle(log)
          _         <- container.add(log, Border.Constraints.Center)
        } yield container

      for {
        contents  <- c.label("TEST")
        root      <- c.box(false)
        leftMenu  <- leftMenu(contents)
        rightPart <- rightPart
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
