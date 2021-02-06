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
    kotlin("jvm") version "1.4.20"

    id("com.cinnober.gradle.semver-git") version "3.0.0" apply false
    id("de.jansauer.printcoverage") version "2.0.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.13.1"
    id("com.github.johnrengelman.shadow") version "6.0.0" apply false
}

group = "me.raatiniemi.linker"

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
    input = files(
        "cli/src"
    )

    reports {
        html.enabled = false
        txt.enabled = false
        xml.enabled = false
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://kotlin.bintray.com/kotlinx/")
    }

    ext.set("nextVersion", "patch")
    apply(plugin = "com.cinnober.gradle.semver-git")
    apply(plugin = "jacoco")
    apply(plugin = "de.jansauer.printcoverage")
    apply(plugin = "com.github.johnrengelman.shadow")

    tasks.withType<Test> {
        finalizedBy(tasks.withType<JacocoReport>())
    }

    tasks.withType<JacocoReport> {
        dependsOn(tasks.withType<Test>())

        reports {
            xml.isEnabled = true
        }
    }
}
