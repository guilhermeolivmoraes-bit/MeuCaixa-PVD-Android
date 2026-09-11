with open("app/build.gradle.kts", "r") as f:
    content = f.read()

test_options = """
    testOptions {
        unitTests.all {
            jvmArgs("-Dnet.bytebuddy.experimental=true")
        }
    }
"""

if "testOptions" not in content:
    content = content.replace('    compileOptions {', test_options + '\n    compileOptions {')
    with open("app/build.gradle.kts", "w") as f:
        f.write(content)
