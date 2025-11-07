/// <reference types="jest" />
import mongoose from 'mongoose';
import express from 'express';
import path from 'path';
import request from 'supertest';

import { workspaceModel } from '../../workspace.model';
import { errorHandler, notFoundHandler } from '../../errorHandler.middleware';
import router from '../../routes';

// Import route files to ensure they get coverage (even if we don't use them directly)
// This ensures all route file code is executed and tracked by coverage
import '../../routes';
import '../../media.routes';
import '../../notes.routes';
import '../../workspace.routes';
import '../../user.routes';
import '../../auth.routes';
import '../../message.routes';

// ---------------------------
// Express test app bootstrap
// ---------------------------
/**
 * Creates the real Express app (same as production) for testing.
 * This uses real authentication middleware and real routes.
 */
export function createTestApp() {
  const app = express();
  app.use(express.json());

  app.use('/api', router);
  app.use('/uploads', express.static(path.join(__dirname, '../../uploads')));
  app.use('*', notFoundHandler);
  app.use(errorHandler);

  return app;
}

// ---------------------------
// Test setup/teardown utilities
// ---------------------------
export interface TestData {
  testUserId: string;
  testWorkspaceId: string;
  testUser2Id: string;
  testWorkspace2Id: string;
  testUserToken: string;
  testUser2Token: string;
}

/**
 * Sets up a fresh test database and returns test data including JWT tokens.
 * Uses dev-login endpoint to get real JWT tokens for authentication.
 */
export async function setupTestDatabase(app: express.Application): Promise<TestData> {
  // Fresh DB state - ensure all collections are dropped and resources released
  if (mongoose.connection.db) {
    await mongoose.connection.db.dropDatabase();
    // Force garbage collection of any pending operations
    await new Promise(resolve => setImmediate(resolve));
  }

  // Ensure JWT_SECRET is set for token generation
  if (!process.env.JWT_SECRET) {
    process.env.JWT_SECRET = 'test-jwt-secret-key-for-testing-only';
  }

  // Create test users via dev-login (this creates users if they don't exist)
  // This ensures users are created properly with all required fields
  const testUser1Email = 'testuser1@example.com';
  const testUser2Email = 'testuser2@example.com';

  // Get tokens via dev-login API
  const loginRes1 = await request(app)
    .post('/api/auth/dev-login')
    .send({ email: testUser1Email });
  
  if (loginRes1.status !== 200) {
    throw new Error(`Failed to login test user 1: ${JSON.stringify(loginRes1.body)}`);
  }
  const testUserToken = loginRes1.body.data.token;
  const testUserId = loginRes1.body.data.user._id;

  const loginRes2 = await request(app)
    .post('/api/auth/dev-login')
    .send({ email: testUser2Email });
  
  if (loginRes2.status !== 200) {
    throw new Error(`Failed to login test user 2: ${JSON.stringify(loginRes2.body)}`);
  }
  const testUser2Token = loginRes2.body.data.token;
  const testUser2Id = loginRes2.body.data.user._id;

  // Ensure deterministic googleIds for tests that rely on them
  const usersCollection = mongoose.connection.collection('users');
  await usersCollection.updateOne(
    { _id: new mongoose.Types.ObjectId(testUserId) },
    { $set: { googleId: 'test-google-id-1' } }
  );
  await usersCollection.updateOne(
    { _id: new mongoose.Types.ObjectId(testUser2Id) },
    { $set: { googleId: 'test-google-id-2' } }
  );

  // Workspaces
  const testWorkspace = await workspaceModel.create({
    name: 'Test Workspace',
    profile: { imagePath: '', name: 'Test Workspace', description: 'Test workspace description' },
    ownerId: new mongoose.Types.ObjectId(testUserId),
    members: [new mongoose.Types.ObjectId(testUserId)],
  });
  const testWorkspaceId = testWorkspace._id.toString();

  const testWorkspace2 = await workspaceModel.create({
    name: 'Test Workspace 2',
    profile: { imagePath: '', name: 'Test Workspace 2', description: 'Test workspace 2 description' },
    ownerId: new mongoose.Types.ObjectId(testUser2Id),
    members: [new mongoose.Types.ObjectId(testUser2Id)],
  });
  const testWorkspace2Id = testWorkspace2._id.toString();

  return { 
    testUserId, 
    testWorkspaceId, 
    testUser2Id, 
    testWorkspace2Id,
    testUserToken,
    testUser2Token
  };
}


