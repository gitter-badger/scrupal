/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                                  *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                                  *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                                  *
  *      http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                                  *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.utils

import java.io.PrintStream
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.immutable.Queue
import scala.collection.mutable

/** Profiler Module For Manual Code Instrumentation
  * This module provides thread aware profiling at microsecond level granularity with manually inserted code Instrumentation. You can instrument a
  * block of code by wrapping it in Profiling.profile("any name you like"). For example, like this:
  * {{{
  * Profiling.profile("Example Code") { "example code" }
  * }}}
  * The result of the Profiling.profile call will be whatever the block returns ("example code" in this case).
  * You can print the results out with print_profile_data or log it with log_profile_data. Once printed or logged, the data is reset
  * and a new capture starts. The printout is grouped by threads and nested Profiling.profile calls are accounted for.
  * Note that when profiling_enabled is false, there is almost zero overhead. In particular, expressions involving computing the argument to
  * profile method will not be computed because it is a functional by-name argument that is not evaluated if profiling is disabled.
  */
object Profiler extends ScrupalComponent {

  var profiling_enabled = false

  class ThreadInfo {
    var profile_data = Queue.empty[(Int, Long, Long, String, Int)]
    val depth_tracker = new AtomicInteger(0)
  }

  val thread_infos = mutable.Map.empty[Thread, ThreadInfo]

  private final val tinfo = new ThreadLocal[ThreadInfo] {
    override def initialValue() : ThreadInfo = {
      val result = new ThreadInfo()
      thread_infos.put(Thread.currentThread(), result)
      result
    }
    override def remove() : Unit = {
      super.remove()
      thread_infos.remove(Thread.currentThread())
    }
  }

  private val id_counter = new AtomicInteger(0)

  def profile[R](what : ⇒ String)(block : ⇒ R) : R = {
    if (profiling_enabled) {
      val ti = tinfo.get()
      val id = id_counter.incrementAndGet()
      val depth = ti.depth_tracker.getAndIncrement
      val t0 = System.nanoTime()
      try {
        block // call-by-name
      } finally {
        val t1 = System.nanoTime()
        ti.depth_tracker.decrementAndGet()
        synchronized {
          ti.profile_data = ti.profile_data.enqueue((id, t0, t1, what, depth))
        }
      }
    } else {
      block
    }
  }

  def format_profile_data : StringBuilder = {
    val str = new StringBuilder(4096)
    if (profiling_enabled) {
      for ((thread, ti) ← thread_infos) {
        str.append("\nTHREAD(").append(thread.getId).append("): ").append(thread.getName)
        str.append("[").append(thread.getThreadGroup).append("]\n")
        for ((id, t0, t1, msg, depth) ← ti.profile_data.sortBy { x ⇒ x._1 }) {
          val time_len : Double = (t1 - t0) / 1000000.0D
          str.append((t0 / 1000000000.0D).formatted("%1$ 12.3f")).append(" - ").append(time_len.formatted("%1$ 10.3f")).append(" ")
          for (i ← 1 to depth) str.append(".")
          str.append(msg).append("\n")
        }
      }
    }
    thread_infos.clear()
    str
  }

  def print_profile_data(out : PrintStream) = {
    if (profiling_enabled) {
      out.print(format_profile_data.toString())
    }
  }

  def log_profile_data(msg : String) = {
    if (profiling_enabled) {
      log.debug("Profile Data For: " + msg + "\n" + format_profile_data)
    }
  }

}
