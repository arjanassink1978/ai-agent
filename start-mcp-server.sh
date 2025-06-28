#!/bin/bash

echo "Starting GitHub MCP Server..."

# Navigate to the MCP server directory
cd mcp-server

# Build the project
echo "Building MCP server..."
mvn clean package -DskipTests

# Start the server on port 8081
echo "Starting MCP server on port 8081..."
java -jar target/github-mcp-server-1.0.0.jar --server.port=8081 