package zio.gui.zswing.components

import java.awt.event.WindowEvent

sealed trait ZFrameEvent {
  def event: WindowEvent
}

object ZFrameEvent {
  case class Opened(event: WindowEvent)      extends ZFrameEvent
  case class Closing(event: WindowEvent)     extends ZFrameEvent
  case class Closed(event: WindowEvent)      extends ZFrameEvent
  case class Iconified(event: WindowEvent)   extends ZFrameEvent
  case class Deiconified(event: WindowEvent) extends ZFrameEvent
  case class Activated(event: WindowEvent)   extends ZFrameEvent
  case class Deactivated(event: WindowEvent) extends ZFrameEvent
}
