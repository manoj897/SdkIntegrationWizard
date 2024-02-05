package com.mj.wizard

import java.io.File
import java.nio.file.Paths
import java.util.Scanner

class SdkWizard {
    companion object {

        enum class FileTypes {
            Manifest,
            Gradle,
            MainActivity,
            MainFragment
        }

        @JvmStatic
        fun main(args: Array<String>) {
            var activityName = null
            println("hello this is SDK integration wizard")
            val currentPath = Paths.get("").toAbsolutePath().toString()
            println(currentPath)
            val rootFile = File(currentPath)
            println("rootfile is $rootFile")


            val listOfFiles = mutableListOf<File>()
            val outputlist = hashMapOf<FileTypes, File>()
            println("Select application you want to integrate InMobi Ads")
            var i = 0
            rootFile.walk()
                .filter {
                    it.name.contains("build.gradle") ||
                            it.name.contains("build.gradle.kts")
                }.iterator()
                .forEach {
                    val fileContents = it.readText()

                    when {
                        it.name.contains("build.gradle") -> {
                            val regex = Regex("""plugins[\s\n]*\{[\s\n]*id[\s\n]*\(?["']com.android.application""")
                            val matchResult = regex.find(fileContents)
                            if (matchResult != null) {
                                //find the path name that precedes build.gradle
                                if (it.name == "build.gradle") {
                                    println("${i++}. ${it.absolutePath.dropLast(13).split("/").last()}")
                                } else {
                                    println("${i++}. ${it.absolutePath.dropLast(17).split("/").last()}")
                                }
                                listOfFiles.add(it)
                            }
                        }
                    }
                }


            val scanner = Scanner(System.`in`)
            var input: String? = null
            try {

                println("Please input which application")
                input = scanner.nextLine()
                System.out.printf("User input was: %s%n", input)

            } catch (e: IllegalStateException) {
                // System.in has been closed
                println("System.in was closed; exiting")
            } catch (e: NoSuchElementException) {
                println("System.in was closed; exiting")
            }

            val file = listOfFiles.find { file ->
                file.absolutePath.contains(input!!)
            }
            val parentFile = file?.parentFile
            if (parentFile == null) {
                println("Could not find the build.gradle file")
                return
            }
            println("parentFile of selected applicaction - $parentFile")
            findFiles(parentFile, outputlist)
            //get the file from outputList which matches the user input


            println("Modifying the following build.gradle file $file")
            var fileContents = file.readText()
            val regexGradle = Regex("""dependencies[\s\n]*\{""")
            val regexGradleKotlin = Regex("""dependencies[\s\n]*\{""")
            val resultGradle = regexGradle.replace(
                fileContents, """dependencies {
                |    implementation 'com.inmobi.sdk:inmobi-ads:10.1.0'""".trimMargin()
            )
            val resultGradleKotlin = regexGradleKotlin.replace(
                fileContents, """dependencies {
                |    implementation("com.inmobi.sdk:inmobi-ads:10.1.0")""".trimMargin()
            )
            println("Successfully added the latest dependency to the build.gradle file")
            if (file.name.contains("kts")) {
                file.writeText(resultGradleKotlin)
            } else {
                file.writeText(resultGradle)
            }
        }

        private fun findFiles(
            rootFile: File,
            outputlist: HashMap<FileTypes, File>,
        ) {
            rootFile.walk()
                .filter {
                    it.absolutePath.contains("src/main") &&
                            (it.name.contains("AndroidManifest.xml") ||
                                    it.name.contains("MainActivity") ||
                                    it.name.contains("MainFragment")) ||
                            it.name.contains("build.gradle") ||
                            it.name.contains("build.gradle.kts")
                }.iterator()
                .forEach {
                    //find if the file content contains 'com.android.application' inside the plugins block
                    println("working on file ${it.absolutePath}")
                    println("comparing it.name, ${it.name} == outputlist[FileTypes.MainActivity]?.name  ${outputlist[FileTypes.MainActivity]?.name} =   ${it.name == outputlist[FileTypes.MainActivity]?.name}")

                    when {
                        it.name.contains("build.gradle") -> {
                            val fileContents = it.readText()
                            val regex = Regex("""plugins[\s\n]*\{[\s\n]*id[\s\n]*\(?["']com.android.application""")
                            val matchResult = regex.find(fileContents)
                            if (matchResult != null) {
                                //find the path name that precedes build.gradle
                                if (it.name == "build.gradle") {
                                    println(it.absolutePath.dropLast(13).split("/").last())
                                } else {
                                    println(it.absolutePath.dropLast(17).split("/").last())
                                }
                                outputlist[FileTypes.Gradle] = it
                            }
                        }

                        it.name.contains("AndroidManifest.xml") -> {
                            val fileContents = it.readText()
                            val regex = Regex("""<activity([\s\S]*?)(</activity>|/>)""")
                            regex.findAll(fileContents)
                                .find { matchResult -> matchResult.value.contains("android.intent.action.MAIN") }
                                ?.let { matchResult ->
                                    println("Found an activity in the manifest file")
                                    outputlist[FileTypes.Manifest] = it
                                    //find the activity name from the matchResult
                                    val activityName = matchResult.value.split("android:name=")[1].split("\"")[1]
                                    println("Activity name is $activityName")
                                    //if activityName starts with . then it is a relative path, else it is an absolute path. with that information, create a path to the MainActivity file
                                    println(
                                        "activityname after replacing the quotes ${
                                            activityName.replace("\"", "").drop(1)
                                        }"
                                    )

                                    val mainActivityFile = if (activityName.startsWith(".")) {
                                        it.parentFile.resolve(activityName.replace("\"", "").drop(1) + ".kt")
                                    } else {
                                        it.parentFile.resolve(activityName.replace("\"", "") + ".kt")
                                    }
                                    println("MainActivity file path is $mainActivityFile")
                                    outputlist[FileTypes.MainActivity] = mainActivityFile

                                }
                        }


                        it.name == outputlist[FileTypes.MainActivity]?.name -> {
                            println("Modifying the main activity to add code snippets")
                            val fileContents = it.readText()
                            //Add the InMobi SDK initialization code after super.onCreate(savedInstanceState)
                            val regex = Regex("""super.onCreate\(savedInstanceState\)""")

//                            val regex = Regex("""override fun onCreate(savedInstanceState: Bundle\?)[\s\S]* \{[\s\S]*super.onCreate\(savedInstanceState\)[\s\S]*}""")
                            val matchResult = regex.find(fileContents)
                            if (matchResult != null) {
                                println("Found the onCreate method in the MainActivity file")
                                val result = regex.replace(
                                    fileContents, """super.onCreate(savedInstanceState)
                                    |        //InMobi SDK initialization code
                                    |        InMobiSdk.init(this, "YOUR_ACCOUNT_ID")
                                    |        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG)
                                    |        InMobiSdk.setGDPRConsent(consent, consentType)
                                    |        InMobiSdk.setCCPAConsent(ccpaConsent)
                                    |        InMobiSdk.setAgeRestrictedUser(true)
                                    |        InMobiSdk.setDoNotTrack(true   )""".trimMargin()
                                )
                                it.writeText(result)
                            }
                        }
                    }
                }
        }
    }
}