package dev.rushii.libhello

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

object LibHelloTest {
	private lateinit var libHello: LibHello

	@JvmStatic
	@BeforeAll
	fun init() {
		// Load class, loading our native lib
		libHello = LibHello()
	}

	@Test
	fun helloWorldTest() {
		assertEquals("Hello Juan!", libHello.helloWorld("Juan"))
	}
}
