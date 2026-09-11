with open("app/build.gradle.kts", "r") as f:
    content = f.read()

if "mockito-core" not in content:
    content = content.replace('testImplementation("junit:junit:4.13.2")', 'testImplementation("junit:junit:4.13.2")\n    testImplementation("org.mockito:mockito-core:4.11.0")\n    testImplementation("org.mockito:mockito-inline:4.11.0")')
    with open("app/build.gradle.kts", "w") as f:
        f.write(content)
