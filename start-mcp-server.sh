#!/bin/bash

echo "Building GitHub MCP Server..."

# Build the MCP server
cd mcp-server
mvn clean package

if [ $? -eq 0 ]; then
    echo "MCP Server built successfully!"
    echo "Starting GitHub MCP Server on port 3001..."
    
    # Run the MCP server
    java -jar target/mcp-server-1.0.0.jar 3001
else
    echo "Failed to build MCP Server"
    exit 1
fi 