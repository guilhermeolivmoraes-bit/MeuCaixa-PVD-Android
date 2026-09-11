with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace('    testImplementation("org.mockito:mockito-inline:5.11.0")\n', '')

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
