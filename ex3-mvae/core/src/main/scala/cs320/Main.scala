package cs320

import org.jline.reader.{LineReaderBuilder, EndOfFileException, UserInterruptException, Expander, History}
import org.jline.terminal.TerminalBuilder

import scala.Console.{MAGENTA => M, CYAN => C, RESET}

object Main {

  val name = "MVAE"

  def main(args: Array[String]): Unit = {
    val terminal = TerminalBuilder.builder.build()
    val expander = new Expander {
      def expandHistory(history: History, line: String): String = line
      def expandVar(word: String): String = word
    }
    val reader = LineReaderBuilder.builder
      .terminal(terminal)
      .expander(expander)
      .build()
    def strs: LazyList[String] = (
      try {
        reader.readLine(s"\n$M$name>$RESET ")
      } catch {
        case _: EndOfFileException | _: UserInterruptException => ":q"
      }
    ) #:: strs

    println(s"Welcome to the $M$name$RESET REPL.")
    println(s"Type in :q, :quit, or the EOF character to terminate the REPL.")

    for (str <- strs.takeWhile(s => !eof(s)) if str.nonEmpty) {
      val opt = lift {
        val expr = Expr(str)
        println(s"  ${C}Parsed:$RESET $expr")
        expr
      }

      for (expr <- opt) {
        lift {
          val result = Implementation.interp(expr)
          println(s"  ${C}Result:$RESET (${result.mkString(", ")})")
        }
      }
    }
  }

  def eof(str: String): Boolean = str == ":quit" || str == ":q"

  def lift[T](res: => T): Option[T] = try {
    Some(res)
  } catch {
    case e: Throwable =>
      e.printStackTrace()
      None
  }
}
