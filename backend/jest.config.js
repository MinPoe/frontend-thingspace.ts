module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  roots: ['<rootDir>/src'],
  testMatch: ['**/__tests__/**/*.test.ts', '**/?(*.)+(spec|test).ts'],
  transform: {
    '^.+\\.ts$': ['ts-jest', {
      tsconfig: 'tsconfig.test.json'
    }],
  },
  collectCoverageFrom: [
    'src/**/*.ts',
    '!src/**/*.d.ts',
    '!src/**/__tests__/**',
    '!src/index.ts',
  ],
  coverageDirectory: 'coverage',
  moduleFileExtensions: ['ts', 'js', 'json'],
  setupFiles: ['<rootDir>/src/__tests__/setup.ts'],
  setupFilesAfterEnv: ['<rootDir>/src/__tests__/suppress-console.ts'],
  globalSetup: undefined,
  globalTeardown: undefined,
  testTimeout: 30000, // 30 seconds for database operations
  moduleNameMapper: {
    '^(\\.{1,2}/.*)\\.js$': '$1',
  },
  // Enhanced output for better visibility of failed tests
  verbose: true, // Shows individual test names
  bail: false, // Continue running all tests even if one fails
  errorOnDeprecated: false,
  // Better error reporting
  displayName: {
    name: 'Backend Tests',
    color: 'blue',
  },
  // Suppress console output during tests (but keep test output)
  silent: false, // Keep test output visible
};

