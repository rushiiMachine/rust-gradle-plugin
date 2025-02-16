package dev.rushii.libhello

class LibHello {
	/**
	 * Invokes the native method through JNI to return `Hello ${name}!`
	 */
	external fun helloWorld(name: String): String

	private companion object {
		init {
			// This requires the library to be on the java.library.path
			System.loadLibrary("hello")
		}
	}
}
