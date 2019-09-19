package me.sedlar.bsb.native

object OpenCVLoader {

    init {
        load()
    }

    fun is64System(): Boolean = System.getProperty("sun.arch.data.model").contains("64")

    fun load() {
        JavaLibraryPath.add("src/main/resources/lib/x${if (is64System()) "64" else "86"}/")
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
    }
}