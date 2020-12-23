plugins { kotlin("jvm") }

dependencies {
    implementation(project(":library"))
    implementation(project(":group:library"))
    implementation(project(":group:library:nested"))
}
