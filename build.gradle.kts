/*
 * linker
 * Copyright (C) 2020 raatiniemi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

plugins {
    kotlin("jvm") version "1.3.72"
    id("java")

    jacoco
    id("de.jansauer.printcoverage") version "2.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.13.1"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = "me.raatiniemi"
version = "0.0.1"

repositories {
    mavenCentral()
    jcenter {
        content {
            // just allow to include kotlinx projects
            // detekt needs 'kotlinx-html' for the html report
            includeGroup("org.jetbrains.kotlinx")
        }
    }
}

detekt {
    buildUponDefaultConfig = true

    reports {
        html.enabled = false
        txt.enabled = false
        xml.enabled = false
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.0")

    testImplementation("junit:junit:4.11")
}

tasks.withType<Test> {
    finalizedBy(tasks.withType<JacocoReport>())
}

tasks.withType<JacocoReport> {
    dependsOn(tasks.withType<Test>())

    reports {
        xml.isEnabled = true
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Implementation-Title"] = "Linker"
        attributes["Implementation-Version"] = archiveVersion
        attributes["Main-Class"] = "me.raatiniemi.linker.MainKt"
    }
}
