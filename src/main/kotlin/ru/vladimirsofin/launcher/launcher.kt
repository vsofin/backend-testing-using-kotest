package ru.vladimirsofin.launcher

import io.kotest.core.TagExpression
import io.kotest.core.descriptors.Descriptor
import io.kotest.core.descriptors.append
import io.kotest.core.descriptors.toDescriptor
import io.kotest.core.filter.TestFilter
import io.kotest.core.filter.TestFilterResult
import io.kotest.core.spec.Spec
import io.kotest.engine.TestEngineLauncher
import io.kotest.engine.listener.TestEngineListener
import io.kotest.framework.discovery.Discovery
import io.kotest.framework.discovery.DiscoveryRequest
import io.kotest.framework.discovery.DiscoveryResult
import io.kotest.framework.discovery.DiscoverySelector
import kotlin.reflect.KClass

/**
 * Creates a [TestEngineLauncher] to be used to launch the test engine.
 */
internal fun setupLauncher(
   args: LauncherArgs,
   listener: TestEngineListener,
): Result<TestEngineLauncher> = runCatching {

   val specClass = args.spec?.let { (Class.forName(it) as Class<Spec>).kotlin }
   val (specs, _, error) = specs(specClass, args.packageName)
   val filter = if (args.testpath == null || specClass == null) null else {
      TestPathTestCaseFilter(args.testpath, specClass)
   }

   if (error != null) throw error

   TestEngineLauncher(listener)
      .withExtensions(listOfNotNull(filter))
      .withTagExpression(args.tagExpression?.let { TagExpression(it) })
      .withClasses(specs)
}

/**
 * Returns the spec classes to execute by using an FQN class name, a package scan,
 * or a full scan.
 */
private fun specs(specClass: KClass<out Spec>?, packageName: String?): DiscoveryResult {
   // if the spec class was null, then we perform discovery to locate all the classes
   // otherwise that specific spec class is used
   return when (specClass) {
      null -> scan(packageName)
      else -> DiscoveryResult(listOf(specClass), emptyList(), null)
   }
}

private fun scan(packageName: String?): DiscoveryResult {
   val packageSelector = packageName?.let { DiscoverySelector.PackageDiscoverySelector(it) }
   val req = DiscoveryRequest(selectors = listOfNotNull(packageSelector))
   val discovery = Discovery(emptyList())
   return discovery.discover(req)
}

/**
 * Compares test descriptions to a given test path (delimited with ' -- ').
 * The comparison ignores test prefixes, so an application using the launcher should not
 * include test name prefixes in the test path.
 */
private class TestPathTestCaseFilter(
   private val testPath: String,
   spec: KClass<out Spec>,
) : TestFilter {

   private val target1 = testPath.trim().split(Descriptor.TestDelimiter)
      .fold(spec.toDescriptor() as Descriptor) { desc, name ->
         desc.append(name.trim())
      }

   // this is a hack where we append "should" to the first name, until 5.0 where we will
   // store names with affixes separately (right now word spec is adding them to the names at source)
   var should = true
   private val target2 = testPath.trim().split(Descriptor.TestDelimiter)
      .fold(spec.toDescriptor() as Descriptor) { desc, name ->
         if (should) {
            should = false
            desc.append("$name should")
         } else desc.append(name.trim())
      }

   override fun filter(descriptor: Descriptor): TestFilterResult {
      return when {
         target1.isOnPath(descriptor) ||
                 target2.isOnPath(descriptor) ||
                 descriptor.isOnPath(target1) ||
                 descriptor.isOnPath(target2) -> TestFilterResult.Include
         else -> TestFilterResult.Exclude("Excluded by test path filter: '$testPath'")
      }
   }
}