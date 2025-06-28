const { spawn } = require('child_process');
const os = require('os');

console.log('🛑 Stopping GitHub MCP Server...');

const platform = os.platform();

if (platform === 'win32') {
    // Windows
    const findProcess = spawn('netstat', ['-ano'], {
        stdio: 'pipe',
        shell: true
    });

    findProcess.stdout.on('data', (data) => {
        const lines = data.toString().split('\n');
        const port3001Line = lines.find(line => line.includes(':3001') && line.includes('LISTENING'));
        
        if (port3001Line) {
            const parts = port3001Line.trim().split(/\s+/);
            const pid = parts[parts.length - 1];
            
            if (pid && !isNaN(pid)) {
                console.log(`🔍 Found MCP server process: ${pid}`);
                
                const killProcess = spawn('taskkill', ['/PID', pid, '/F'], {
                    stdio: 'inherit',
                    shell: true
                });
                
                killProcess.on('close', (code) => {
                    if (code === 0) {
                        console.log(`✅ Killed MCP server process ${pid}`);
                    } else {
                        console.error(`❌ Failed to kill process ${pid}`);
                    }
                });
            }
        } else {
            console.log('✅ No MCP server process found on port 3001');
        }
    });

    findProcess.on('error', (error) => {
        console.error('❌ Error finding MCP server process:', error.message);
    });
} else {
    // Unix-like systems (macOS, Linux)
    const findProcess = spawn('lsof', ['-ti:3001'], {
        stdio: 'pipe',
        shell: true
    });

    findProcess.stdout.on('data', (data) => {
        const pids = data.toString().trim().split('\n').filter(pid => pid);
        
        if (pids.length === 0) {
            console.log('✅ No MCP server process found on port 3001');
            return;
        }
        
        console.log(`🔍 Found MCP server processes: ${pids.join(', ')}`);
        
        // Kill each process
        pids.forEach(pid => {
            const killProcess = spawn('kill', ['-9', pid], {
                stdio: 'inherit',
                shell: true
            });
            
            killProcess.on('close', (code) => {
                if (code === 0) {
                    console.log(`✅ Killed MCP server process ${pid}`);
                } else {
                    console.error(`❌ Failed to kill process ${pid}`);
                }
            });
        });
    });

    findProcess.on('error', (error) => {
        console.error('❌ Error finding MCP server process:', error.message);
    });

    findProcess.on('close', (code) => {
        if (code !== 0) {
            console.log('✅ No MCP server process found or already stopped');
        }
    });
} 