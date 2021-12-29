# Docker build script for Gradle applications
#
# The name of the application is specified by an argument, and the root
# directory of the Gradle project is expected to be the build context. The build
# process will build and install the application, and the default command will
# be to run it.


# Don't need an old JDK version here
# Just use it for consistency
FROM openjdk:8u171-alpine

# Copy the source tree into the container
WORKDIR /usr/local/src/
COPY . /usr/local/src/

# Build
RUN [ "./gradlew", "installDist" ]


# Downgrade so LDAP exploitation still works
FROM openjdk:8u171-alpine

# Name of the application
# Used for locating the install files
ARG APP_NAME

# Install
# The run script is renamed `app`
COPY --from=0 \
    /usr/local/src/build/install/${APP_NAME}/bin/${APP_NAME} \
    /usr/local/bin/app
COPY --from=0 \
    /usr/local/src/build/install/${APP_NAME}/lib/ \
    /usr/local/lib/

# Default command is to run the application with no arguments
CMD [ "/usr/local/bin/app" ]
