with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace('org.mockito:mockito-core:4.11.0', 'org.mockito:mockito-core:5.11.0')
content = content.replace('org.mockito:mockito-inline:4.11.0', 'org.mockito:mockito-inline:5.11.0')

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
