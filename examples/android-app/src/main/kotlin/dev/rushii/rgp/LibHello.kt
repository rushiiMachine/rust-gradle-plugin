package dev.rushii.rgp

class LibHello {
	/**
	 * Invokes the native method through JNI to return `Hello ${name}!`
	 */
	public external fun helloWorld(name: String): String

	private companion object {
		init {
			// This loads the native library from the APK's bundled JNI libs
			System.loadLibrary("hello")
		}
	}
}
