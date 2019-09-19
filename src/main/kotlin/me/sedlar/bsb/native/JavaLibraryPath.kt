package me.sedlar.bsb.native

import java.util.*

object JavaLibraryPath {

    fun add(pathToAdd: String) {
        val usrPathsField = ClassLoader::class.java.getDeclaredField("usr_paths")
        usrPathsField.isAccessible = true

        val paths = usrPathsField.get(null) as Array<*>

        for (path in paths) {
            if (path == pathToAdd) {
                return
            }
        }

        val newPaths = Arrays.copyOf(paths, paths.size + 1)
        newPaths[newPaths.size - 1] = pathToAdd
        usrPathsField.set(null, newPaths)
    }
}