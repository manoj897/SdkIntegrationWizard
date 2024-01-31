package com.mj.wizard

import java.io.File
import java.util.Scanner

class SdkWizard {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("hello this is SDK integration wizard")

            val path = File("/Users/manoj.krishnan/StudioProjects/android-sdk/")
            val list = path.walk().filter { it.name == "build.gradle" }
            println("Select application you want to integrate InMobi Ads")
            list.iterator().forEach {
                //find if the file content contains 'com.android.application' inside the plugins block
                val fileContents = it.readText()
                val regex = Regex("""plugins[\s\n]*\{[\s\n]*id[\s\n]*'com.android.application'""")
                val matchResult = regex.find(fileContents)
                if (matchResult != null) {
                    //find the path name that precedes build.gradle
                    println("${it.absolutePath.dropLast(13).split("/").last()}")
                }
            }

            val scanner = Scanner(System.`in`)
            var input: String? = null
            try {

                println("Please input which application")
                val then = System.currentTimeMillis()
                input = scanner.nextLine()
                val now = System.currentTimeMillis()
                System.out.printf("Waited %.3fs for user input%n", (now - then) / 1000.0)
                System.out.printf("User input was: %s%n", input)

            } catch (e: IllegalStateException) {
                // System.in has been closed
                println("System.in was closed; exiting")
            } catch (e: NoSuchElementException) {
                println("System.in was closed; exiting")
            }


            val file = File("/Users/manoj.krishnan/StudioProjects/android-sdk/$input/build.gradle")
            var fileContents = file.readText()
            val regex = Regex("""dependencies[\s\n]*\{""")
            val matchResult = regex.replace(
                fileContents, """dependencies {
                |    implementation 'com.inmobi.sdk:inmobi-ads:10.1.0'""".trimMargin()
            )
            println("Successfully added the latest dependency to the build.gradle file")
            file.writeText(matchResult)
        }
    }
}