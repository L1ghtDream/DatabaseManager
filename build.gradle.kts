plugins {
    id("java")
    id("maven-publish")
}

group = "dev.lightdream"
version = "5.0.24"

repositories {
    mavenCentral()
    maven("https://repo.lightdream.dev/")
    maven("https://mvnrepository.com/artifact/org.hibernate/hibernate-community-dialects")
    maven("https://mvnrepository.com/artifact/org.hibernate.common/hibernate-commons-annotations")
    maven(url = "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://cursemaven.com/")
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    flatDir {
        dirs("libs")
    }
    maven ( "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    // LightDream
    implementation("dev.lightdream:logger:3.3.14")
    implementation("dev.lightdream:lambda:4.1.23")
    implementation("dev.lightdream:message-builder:3.1.19")

    // Driver
    implementation("org.xerial:sqlite-jdbc:3.42.0.0") // Driver - SQLite
    implementation("mysql:mysql-connector-java:8.0.33") // Driver - MYSQL

    // Utils
    implementation("org.reflections:reflections:0.10.2")

    compileOnly("org.jetbrains:annotations:24.0.1")
    annotationProcessor("org.jetbrains:annotations:24.0.1")

    implementation("com.google.code.gson:gson:2.10.1")

    compileOnly("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.28")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.28")
    testImplementation("org.projectlombok:lombok:1.18.28")

    // Tests
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Hibernate
    implementation("org.hibernate.orm:hibernate-core:6.2.7.Final")
    implementation("org.hibernate:hibernate-community-dialects:6.2.7.Final")
    implementation("org.hibernate.common:hibernate-commons-annotations:6.0.6.Final")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        val githubURL = project.findProperty("github.url") ?: ""
        val githubUsername = project.findProperty("github.auth.username") ?: ""
        val githubPassword = project.findProperty("github.auth.password") ?: ""

        val selfURL = project.findProperty("self.url") ?: ""
        val selfUsername = project.findProperty("self.auth.username") ?: ""
        val selfPassword = project.findProperty("self.auth.password") ?: ""

        maven(url = githubURL as String) {
            name = "github"
            credentials(PasswordCredentials::class) {
                username = githubUsername as String
                password = githubPassword as String
            }
        }

        maven(url = selfURL as String) {
            name = "self"
            credentials(PasswordCredentials::class) {
                username = selfUsername as String
                password = selfPassword as String
            }
        }
    }
}

tasks.register("publishGitHub") {
    dependsOn("publishMavenPublicationToGithubRepository")
    description = "Publishes to GitHub"
}

tasks.register("publishSelf") {
    dependsOn("publishMavenPublicationToSelfRepository")
    description = "Publishes to Self hosted repository"
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
