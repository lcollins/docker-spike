plugins {
    // Add plugins here if needed for the root project
}

allprojects {
    repositories {
        mavenCentral()
    }
}

//subprojects {
    //apply(plugin = "java") // or other shared plugins if needed

    //tasks.register("clean") {
    //    doLast {
    //        delete(buildDir)
    //    }
    //}
//}