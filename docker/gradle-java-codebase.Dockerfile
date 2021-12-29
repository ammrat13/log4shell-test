# Docker build script for Gradle codebases
#
# This script will host a particular Gradle build's classes over HTTP. The root
# directory of the Gradle project is expected to be the build context, and the
# build process will build the application and copy over the class files.
#
# This Dockerfile only works for Java codebases.


# Don't need an old JDK version here
# Just use it for consistency
FROM openjdk:8u171-alpine

# Copy the source tree into the container
WORKDIR /usr/local/src/
COPY . /usr/local/src/

# Build
RUN [ "./gradlew", "classes" ]


# Use an HTTP server
FROM httpd:alpine

# Copy the classes
COPY --from=0 \
    /usr/local/src/build/classes/java/main/ \
    /usr/local/apache2/htdocs/
