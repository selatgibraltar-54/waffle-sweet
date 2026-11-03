package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.vm.VirtualFileSystem
import com.example.vm.VmEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NanoVM", appName)
  }

  @Test
  fun `virtual file system operations`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val fs = VirtualFileSystem(context)
    val files = fs.listDirectory("/home/user")
    assertTrue(files.isNotEmpty())

    fs.createFile("/home/user/test.txt", "Hello VM")
    val file = fs.getFile("/home/user/test.txt")
    assertNotNull(file)
    assertEquals("Hello VM", file?.content)
  }

  @Test
  fun `vm engine command execution`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine = VmEngine(context)

    // Run help
    engine.executeCommand("help")
    assertTrue(engine.terminalLines.value.any { it.text.contains("NanoVM Micro-Shell") })

    // Run free
    engine.executeCommand("free")
    assertTrue(engine.terminalLines.value.any { it.text.contains("Host Android JVM Memory") })
  }
}
