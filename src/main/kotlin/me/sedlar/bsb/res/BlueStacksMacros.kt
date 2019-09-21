package me.sedlar.bsb.res

import java.io.File
import java.nio.file.Files

object OSRSConfig {

    val FILE_NAMES = emptyArray<String>(
        /*"com.jagex.oldscape.android.cfg"*/
    )

    fun extract() {
        FILE_NAMES.forEach { configFileName ->
            val config = javaClass.getResourceAsStream("/configs/$configFileName")
            val configData = config.readBytes()

            config.close()

            File.listRoots().forEach { rootDir ->
                rootDir.walkTopDown().maxDepth(2)
                    .filter { it.isDirectory }
                    .filter { it.absolutePath.contains("BlueStacks") }
                    .forEach { blueStacks ->
                        blueStacks.walkBottomUp()
                            .filter { it.isDirectory }
                            .filter { it.name == "InputMapper" }
                            .filter { it.normalizedPath.contains("Engine/UserData/InputMapper") }
                            .forEach {
                                val userFiles = File(it, "UserFiles/")

                                if (!userFiles.exists()) {
                                    userFiles.mkdirs()
                                }

                                val target = File(userFiles, configFileName)

                                if (target.exists()) {
                                    target.delete()
                                }

                                Files.write(target.toPath(), configData)

                                println("Extracted config: ${target.normalizedPath}")
                            }
                    }
            }
        }
    }
}

val File.normalizedPath: String
    get() = this.absolutePath.replace(File.separatorChar, '/')