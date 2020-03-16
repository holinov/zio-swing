package zio.gui.zswing.components

import java.awt.{Component, Container}

import zio.Task

trait ZContainer[+T <: Container] extends ZComponent[T] {

  def add(name: String, comp: ZComponent[_ <: Component]): Task[Unit] =
    Task(component.add(name, comp.component))
  def add(comp: ZComponent[_ <: Component]): Task[Unit] =
    Task(component.add(comp.component))
  def add(comp: Task[ZComponent[_ <: Component]]): Task[Unit] =
    comp.map(comp => component.add(comp.component))
  def addAll(comps: ZComponent[_ <: Component]*): Task[Unit] =
    Task.foreach(comps)(add).unit
}

object ZContainer {
  def apply[T <: Container](c: T): ZContainer[T] = new ZContainer[T] {
    override def component: T = c
  }
}
