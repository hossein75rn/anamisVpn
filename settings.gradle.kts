pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { setUrl("https://maven.google.com") }
        maven { setUrl("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven { setUrl("https://maven.google.com") }
        maven { setUrl("https://jitpack.io") }
    }
}
rootProject.name = "V2rayNG"
include(":app")
