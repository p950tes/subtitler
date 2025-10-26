# subtitler
Command line tool for managing SRT subtitle files (scrub, merge, shift timings).

### Build Requirements
* Java 25 JDK
* GraalVM 25 JDK (https://www.graalvm.org/downloads/)

### Building

Point the GRAALVM_HOME environment variable to the graalvm directory

Example:

```
export GRAALVM_HOME="/opt/graalvm/graalvm-jdk-25.0.1+8.1"
```

Then execute the nativeCompile gradle command:

```
./gradlew clean build nativeCompile
```
