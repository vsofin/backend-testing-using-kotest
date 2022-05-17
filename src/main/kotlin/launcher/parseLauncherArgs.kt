package launcher

internal fun parseLauncherArgs(args: List<String>): LauncherArgs {
   val a = parseArgs(args)
   return LauncherArgs(
      testpath = "tests",
      packageName = a["package"],
      spec = a["spec"],
      termcolor = a["termcolor"],
      listener = a["listener"] ?: a["writer"] ?: a["reporter"],
      tagExpression = a["tags"],
   )
}

/**
 * Parses args in the format --name value.
 */
private fun parseArgs(args: List<String>): Map<String, String> {
   val argsmap = mutableMapOf<String, String>()
   var name = ""
   var value = ""
   args.forEach {
      if (it.startsWith("--")) {
         if (name.isNotBlank()) {
            argsmap[name] = value
            value = ""
         }
         name = it.drop(2)
      } else {
         value = if (value.isEmpty()) it else "$value $it"
      }
   }
   if (name.isNotBlank())
      argsmap[name] = value
   return argsmap.toMap()
}