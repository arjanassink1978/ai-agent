const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

console.log('🚀 Starting GitHub MCP Server...');

// Check if MCP server directory exists
const mcpServerPath = path.join(__dirname, '../../mcp-server');
if (!fs.existsSync(mcpServerPath)) {
    console.error('❌ MCP server directory not found at:', mcpServerPath);
    process.exit(1);
}

const jarPath = path.join(mcpServerPath, 'target/mcp-server-1.0.0.jar');

// Check if JAR already exists
if (fs.existsSync(jarPath)) {
    console.log('✅ MCP server JAR found, starting directly...');
    startMCPServer();
} else {
    console.log('📦 Building MCP server...');
    buildMCPServer();
}

function buildMCPServer() {
    const buildProcess = spawn('mvn', ['clean', 'package'], {
        cwd: mcpServerPath,
        stdio: 'inherit',
        shell: true
    });

    buildProcess.on('close', (code) => {
        if (code !== 0) {
            console.error('❌ Failed to build MCP server');
            process.exit(code);
        }
        
        console.log('✅ MCP server built successfully');
        startMCPServer();
    });
}

function startMCPServer() {
    console.log('🔧 Starting MCP server on port 3001...');
    
    if (!fs.existsSync(jarPath)) {
        console.error('❌ MCP server JAR not found at:', jarPath);
        process.exit(1);
    }
    
    const mcpProcess = spawn('java', ['-jar', jarPath, '3001'], {
        cwd: mcpServerPath,
        stdio: 'inherit',
        shell: true
    });
    
    mcpProcess.on('error', (error) => {
        console.error('❌ Failed to start MCP server:', error.message);
        process.exit(1);
    });
    
    mcpProcess.on('close', (code) => {
        console.log(`MCP server process exited with code ${code}`);
        process.exit(code);
    });
    
    // Handle process termination
    process.on('SIGINT', () => {
        console.log('\n🛑 Shutting down MCP server...');
        mcpProcess.kill('SIGINT');
    });
    
    process.on('SIGTERM', () => {
        console.log('\n🛑 Shutting down MCP server...');
        mcpProcess.kill('SIGTERM');
    });
} 