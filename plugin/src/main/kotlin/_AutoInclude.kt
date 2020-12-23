import com.pablisco.gradle.auto.include.AutoInclude
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.configure

/**
 * Extension to be used from scripts. Using default package to avoid imports.
 */
@Suppress("unused") // Api
public fun Settings.autoInclude(block: AutoInclude.() -> Unit): Unit = configure(block)
