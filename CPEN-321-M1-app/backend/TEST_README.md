# Notes API Test Suite

This test suite follows the M4 testing requirements with both **normal tests** (no mocking) and **mocked tests**.

## Setup

1. Install dependencies:
```bash
npm install
```

2. Set up test database:
   - By default, tests use MongoDB at `mongodb://localhost:27017/thingspace-test`
   - You can override with `MONGODB_TEST_URI` environment variable
   - Or tests will fall back to `MONGODB_URI` if set

3. Run tests (with coverage):
```bash
npm test
```

4. Run tests in watch mode:
```bash
npm run test:watch
```

## Code Coverage

Coverage runs automatically with `npm test`:
- Terminal output shows summary table
- HTML report generated in `coverage/index.html`
- Open `coverage/index.html` in your browser for detailed coverage
- Coverage shows: statements, branches, functions, lines

## Test Structure

### Part 1: Normal Tests (No Mocking)
These tests run against real database and API:

- **POST /api/notes** - Create note
  - Success case: Creates note with valid data
  - Failure cases: Missing workspaceId, empty fields

- **PUT /api/notes/:id** - Update note
  - Success case: Updates existing note
  - Failure cases: Non-existent note, unauthorized user

- **DELETE /api/notes/:id** - Delete note
  - Success case: Deletes own note
  - Failure cases: Non-existent note

- **GET /api/notes/:id** - Get single note
  - Success case: Retrieves own note
  - Failure cases: Non-existent note, unauthorized access

- **GET /api/notes** - Find notes
  - Success case: Filters by workspace and tags
  - Failure cases: Missing params, unauthorized workspace

- **POST /api/notes/:id/share** - Share note
  - Success case: Shares to valid workspace
  - Failure cases: Invalid workspace, unauthorized user

- **POST /api/notes/:id/copy** - Copy note
  - Success case: Creates copy in new workspace
  - Failure cases: Invalid workspace, unauthorized user

### Part 2: Mocked Tests (Jest Mocks)
These tests simulate uncontrollable failures:

- **Database failures**: Tests error handling when DB operations fail
- **Workspace service failures**: Tests error handling when workspace service is down
- **OpenAI API failures**: Verifies graceful degradation when embeddings fail

## Key Features

- ✅ Comprehensive coverage of all note endpoints
- ✅ Success paths and all error paths tested
- ✅ Real database integration for normal tests
- ✅ Mocked failures for untriggerable errors
- ✅ Proper cleanup between tests
- ✅ Mock authentication bypass
- ✅ Test isolation with fresh data per test

## Environment Variables

- `MONGODB_TEST_URI`: Test database connection string
- `MONGODB_URI`: Fallback database connection string
- `OPENAI_API_KEY`: Required for note creation (with embeddings)

## Notes

- Each test runs in isolation with a fresh database
- Mock auth middleware uses `x-test-user-id` header
- Tests assume MongoDB is running (use Docker if needed)
- OpenAI API errors are caught and don't crash the app

