# Docker build script for Gradle applications
#
# The name of the application is specified by an argument, and the root
# directory of the Gradle project is expected to be the build context. The build
# process will build and install the application, and the default command will
# be to run it.

# Downgrade so LDAP exploitation still works
FROM openjdk:8u181

# Name of the application to build
# Used for locating the install files
ARG APP_NAME

# Copy the source tree into the container
WORKDIR /usr/local/src/
COPY . /usr/local/src/

# Build
RUN [ "./gradlew", "installDist" ]
# Install
# The run script is renamed `app`
RUN \
    cp ./build/install/${APP_NAME}/bin/${APP_NAME} /usr/local/bin/app && \
    cp ./build/install/${APP_NAME}/lib/*.jar       /usr/local/lib/

# Default command is to run the application with no arguments
CMD [ "/usr/local/bin/app" ]
