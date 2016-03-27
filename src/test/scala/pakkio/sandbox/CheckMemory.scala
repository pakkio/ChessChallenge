package pakkio.sandbox

import java.lang.management.ManagementFactory


trait CheckMemory {
  this: Testable =>
  val mbean = ManagementFactory.getMemoryMXBean
  //mbean.setVerbose(true)
  val m = mbean.getHeapMemoryUsage

  var _1GB: Double = math.pow(2, 30)
  var _1MB: Double = math.pow(2, 20)
  var _1KB: Double = math.pow(2, 10)

  def fmt(x:Double) = f"$x%.2f"
  def f(x: Long): String = {
    if (x >= _1GB) return fmt(x / _1GB) + " GB"
    if (x >= _1MB) return fmt(x / _1MB) + " MB"
    if (x >= _1KB) return fmt(x / _1KB) + " KB"
    x + "B"
  }

  def showMemory = {
    mbean.gc
    println(label)
    println(s"Memory used: ${f(m.getUsed)}, max: ${f(m.getMax)}, committed: ${f(m.getCommitted)}")
  }

}
