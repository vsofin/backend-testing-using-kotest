package ru.vladimirsofin.launcher

import com.github.ajalt.mordant.TermColors
import io.kotest.common.runBlocking
import io.kotest.engine.listener.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {

   val launcherArgs = parseLauncherArgs(args.toList())

   val collector = CollectingTestEngineListener()
   val listener = CompositeTestEngineListener(
      listOf(
         collector,
         LoggingTestEngineListener,
         ThreadSafeTestEngineListener( PinnedSpecTestEngineListener(EnhancedConsoleTestEngineListener(TermColors())) ),
      )
   )

   runBlocking {
      setupLauncher(launcherArgs, listener).fold(
         { it.async() },
         {
            // if we couldn't create the launcher we'll display those errors
            listener.engineStarted()
            listener.engineFinished(listOf(it))
         },
      )
   }

   // there could be threads in the background that will stop the launcher shutting down
   // for example if a test keeps a thread running,
   // so we must force the exit
   if (collector.errors)
      exitProcess(-1)
   else
      exitProcess(0)
}
