plugins {
    id("java")
    id("application")
}

group = "moe.das"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.apptasticsoftware:rssreader:3.5.0")
    implementation("org.apache.commons:commons-text:1.11.0")
    implementation("com.github.Carleslc.Simple-YAML:Simple-Yaml:1.8.4")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    compileOnly("ch.qos.logback:logback-core:1.4.14")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
}

application {
    mainClass.set("moe.das.boorubot.Main")
}
