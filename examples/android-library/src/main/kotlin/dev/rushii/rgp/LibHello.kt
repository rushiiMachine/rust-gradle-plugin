package dev.rushii.libhello

public class LibHello {
	/**
	 * Invokes the native method through JNI to return `Hello ${name}!`
	 */
	public external fun helloWorld(name: String): String

	private companion object {
		init {
			// The native library is bundled into the .aar artifact that is included when publishing,
			// which is then merged into APKs that depend on this library, and loaded from there.
			System.loadLibrary("hello")
		}
	}
}
