package com.mj.wizard

import java.io.File
import java.nio.file.Paths
import java.util.Scanner

//                val regex = Regex("""plugins[\s\n]*\{[\s\n]*id[\s\n]*'com.android.application'""")


//|         InMobiSdk.init(this, "asodkjsssjsjsjsjsjsjsjsjsjsjsjss", null, SdkInitializationListener {
//                                    |                if (it == null) {
//                                    |                    Log.e("", "InMobisdk init success")
//                                    |                } else {
//                                    |                    Log.e("", "InMobisdk init failed - " + it.message)
//                                    |                }
//                                    |            })
class SdkWizard {
    companion object {

        enum class FileTypes {
            Manifest,
            Gradle,
            MainActivity,
            PathToRoot,
            PathToNameSpace,
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
            outputlist[FileTypes.PathToRoot] = parentFile
            modifyFiles(parentFile, outputlist)
            addInMobiFiles(parentFile, outputlist)
            //get the file from outputList which matches the user input

        }

        private fun addInMobiFiles(parentFile: File, outputlist: HashMap<FileTypes, File>) {
            val file = outputlist[FileTypes.PathToRoot]
            //find the namespace of the file
            println("File is $file")
            //concat the path of /src/main/java/com/inmobi/ads/test to the file
            val inmobiAdsPackage = file?.resolve("src/main/java/com/inmobi/ads/helper")
            println("inmobipackage is $inmobiAdsPackage")
            inmobiAdsPackage?.mkdirs()
            val inmobiAdsModalPackage = inmobiAdsPackage?.resolve("modal")
            inmobiAdsModalPackage?.mkdirs()

            //create a new file InMobiConfigurations.kt
            val inMobiConfigurationsFile = inmobiAdsModalPackage?.resolve("InMobiConfigurations.kt")
            inMobiConfigurationsFile?.createNewFile()

            //create a new file InMobiSdkManager.kt
            val inMobiSdkManagerFile = inmobiAdsPackage?.resolve("InMobiSdkManager.kt")
            inMobiSdkManagerFile?.createNewFile()

            //copy the contents of the InMobiConfigurations.kt and InMobiSdkManager.kt from the assets folder from wizard to the newly created files
            //get currentPath of this file

            //read the contents of the InMobiConfigurations.kt and InMobiSdkManager.kt from the jar resources

            //get resources stream from the jar
            val configurationResources = SdkWizard::class.java.getResourceAsStream("/InMobiConfigurations.txt")
            val sdkManagerResources = SdkWizard::class.java.getResourceAsStream("/InMobiSdkManager.txt")
            //write the contents of the resources to the newly created files
            if (configurationResources != null) {
                inMobiConfigurationsFile?.writeBytes(configurationResources.readAllBytes())
            }
            if (sdkManagerResources != null) {
                inMobiSdkManagerFile?.writeBytes(sdkManagerResources.readAllBytes())
            }



//            val inMobiConfigurationsAsset = File("InMobiConfigurations.txt")
//            val inMobiSdkManagerAsset = File("InMobiSdkManager.txt")
//            inMobiConfigurationsAsset.copyTo(inMobiConfigurationsFile!!, true)
//            inMobiSdkManagerAsset.copyTo(inMobiSdkManagerFile!!, true)
            println("Successfully added the InMobiConfigurations.kt and InMobiSdkManager.kt files to the project")


        }


        private fun modifyFiles(
            rootFile: File,
            outputlist: HashMap<FileTypes, File>,
        ) {
            var tempMainActivityFile: String? = null
            rootFile.walk()
                .filter {
                    it.absolutePath.contains("src/main") &&
                            (it.name.contains("AndroidManifest.xml") ||
                                    it.name.contains("MainActivity") ||
                                    it.name.contains("MainFragment")) ||
                            it.name.contains("build.gradle") ||
                            it.name.contains("build.gradle.kts")
                }.iterator()
                .forEach { it ->
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

                            println("Modifying the following build.gradle file ${it.name}")
                            val regexGradle = Regex("""dependencies[\s\n]*\{""")
                            val regexGradleKotlin = Regex("""dependencies[\s\n]*\{""")
                            val resultGradle = regexGradle.replace(
                                fileContents, """dependencies {
                                |    implementation 'com.inmobi.monetization:inmobi-ads:9.1.0'""".trimMargin()
                            )
                            val resultGradleKotlin = regexGradleKotlin.replace(
                                fileContents, """dependencies {
                                |    implementation("com.inmobi.monetization:inmobi-ads:9.1.0")""".trimMargin()
                            )
                            println("Successfully added the latest dependency to the build.gradle file")
                            if (it.name.contains("kts")) {
                                it.writeText(resultGradleKotlin)
                            } else {
                                it.writeText(resultGradle)
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
                                    var activityName = matchResult.value.split("android:name=")[1].split("\"")[1]
                                    println("Activity name is $activityName")
                                    //if activityName starts with . then it is a relative path, else it is an absolute path. with that information, create a path to the MainActivity file
                                    println(
                                        "activityname after replacing the quotes ${
                                            activityName.replace("\"", "").drop(1).also { activityName = it }
                                        }"
                                    )
                                    tempMainActivityFile = activityName
                                }
                        }


                        it.name == tempMainActivityFile || it.name == """$tempMainActivityFile.kt""" -> {
                            println("Found the main activity file path - ${it.absolutePath}")
                            outputlist[FileTypes.MainActivity] = it

                            println("Modifying the main activity to add code snippets")
                            var fileContents = it.readText()
                            //Add the InMobi SDK initialization code after super.onCreate(savedInstanceState)
                            val regex = Regex("""super.onCreate\(savedInstanceState\)""")

//                            val regex = Regex("""override fun onCreate(savedInstanceState: Bundle\?)[\s\S]* \{[\s\S]*super.onCreate\(savedInstanceState\)[\s\S]*}""")
                            val matchResult = regex.find(fileContents)
                            if (matchResult != null) {
                                println("Found the onCreate method in the MainActivity file")
                                val result = regex.replace(
                                    fileContents, """super.onCreate(savedInstanceState)
                                    |        //InMobi SDK initialization code                                    
                                    |        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG)
                                    |        val sdkConfigurations = InMobiConfigurations(this, "asodkjsssjsjsjsjsjsjsjsjsjsjsjss", InMobiSdk.LogLevel.DEBUG, null)
                                    |        val inMobiSdkManager = InMobiSdkManager(sdkConfigurations)
                                    |        inMobiSdkManager.initInMobiSdk()

                                    |        inMobiSdkManager.loadAndAttachBanner(inMobiSdkManager.defaultBannerPlacementInfo)
                                    
                                    |        inMobiSdkManager.loadInterstitial(inMobiSdkManager.defaultInterstitialPlacementInfo)
                                    |        inMobiSdkManager.showInterstitial(inMobiSdkManager.defaultInterstitialPlacementInfo)
                                    
                                    |        inMobiSdkManager.loadAndAttachNative(inMobiSdkManager.defaultNativePlacementInfo)""".trimMargin()
                                )
                                it.writeText(result)
                            }
                            fileContents = it.readText()
                            //add import statments to the MainActivity file in case they are not present and maintain the order of the import statements with the existing ones
                            val importStatements = listOf(
                                "import com.inmobi.sdk.InMobiSdk",
                                "import com.inmobi.sdk.SdkInitializationListener",
                                "import com.inmobi.ads.helper.InMobiSdkManager",
                                "import com.inmobi.ads.helper.modal.InMobiConfigurations"
                            )

                            println("Adding the import statements to the MainActivity file")
                            val importIndex = fileContents.indexOfFirst { char -> char == '\n' }
                            val importStatement = importStatements.joinToString("\n")
                            val result = fileContents.substring(
                                0,
                                importIndex
                            ) + "\n" + importStatement + "\n" + fileContents.substring(importIndex)

                            val logImport = "import android.util.Log"
                            //filter all the import statements from the result
                            val resultImports = result.split("\n").filter { it.contains("import") }
                            //find if already log import is present
                            if (!resultImports.contains(logImport)) {
                                println("Adding the log import statement to the MainActivity file")
                                val logImportIndex = result.indexOfFirst { char -> char == '\n' }
                                val logResult = result.substring(
                                    0,
                                    logImportIndex
                                ) + "\n" + logImport + "\n" + result.substring(logImportIndex)
                                it.writeText(logResult)
                            } else {
                                it.writeText(result)
                            }


                        }
                    }
                }
        }
    }
}