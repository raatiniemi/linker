plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-cli-jvm:0.3.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.0")

    testImplementation("junit:junit:4.11")
}

tasks.withType<Jar> {
    manifest {
        attributes["Implementation-Title"] = "linker-cli"
        attributes["Implementation-Version"] = project.version
        attributes["Main-Class"] = "me.raatiniemi.linker.MainKt"
    }
}
