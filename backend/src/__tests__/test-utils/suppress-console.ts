// Suppress console output during tests to make failures more visible
// This file runs after setup.ts but before each test file

// Store original console methods
const originalConsole = {
  log: console.log,
  error: console.error,
  warn: console.warn,
  info: console.info,
  debug: console.debug,
};

// Suppress console output during tests
// Only show console output if TEST_VERBOSE environment variable is set
if (!process.env.TEST_VERBOSE) {
  // Suppress console methods - use no-op functions
  global.console = {
    ...console,
    log: () => {},
    error: () => {},
    warn: () => {},
    info: () => {},
    debug: () => {},
  };
}

