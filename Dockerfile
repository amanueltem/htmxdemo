# Use a tiny base image
FROM ubuntu:24.04

# Install necessary libraries for GraalVM native images to run
RUN apt-get update && apt-get install -y zlib1g libstdc++6 && rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /app

# Copy the native binary from your target folder to the container
COPY target/htmxdemo /app/htmxdemo

# Make sure it's executable
RUN chmod +x /app/htmxdemo

# Expose the port Spring Boot runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["./htmxdemo"]