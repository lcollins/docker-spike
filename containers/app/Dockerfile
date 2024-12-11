# Use a lightweight base image
FROM alpine:3.18

# Set working directory
WORKDIR /app

# Copy the entrypoint script
COPY entrypoint.sh /app/

# Make the script executable
RUN chmod +x /app/entrypoint.sh

# Set the entrypoint to the script
ENTRYPOINT ["/app/entrypoint.sh"]