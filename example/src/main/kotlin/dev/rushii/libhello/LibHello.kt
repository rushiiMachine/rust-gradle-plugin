package dev.rushii.libhello

public class LibHello {
	/**
	 * Invokes the native method through JNI to return `Hello ${name}!`
	 */
	public external fun helloWorld(name: String): String

	// This essentially acts like a static initializer block for the LibHello class
	private companion object {
		init {
			System.loadLibrary("libhello")
		}
	}
}
