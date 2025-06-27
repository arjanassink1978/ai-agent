#!/bin/bash

# Setup script for OpenAI API Key
echo "Setting up OpenAI API Key..."

# Check if API key is provided as argument
if [ -z "$1" ]; then
    echo "Usage: source setup-env.sh YOUR_OPENAI_API_KEY"
    echo "Example: source setup-env.sh sk-1234567890abcdef..."
    exit 1
fi

# Export the API key
export OPENAI_API_KEY="$1"
echo "OpenAI API Key set successfully!"
echo "You can now run: mvn spring-boot:run"
