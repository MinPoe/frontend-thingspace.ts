/// <reference types="jest" />
import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';

import { userModel } from '../user.model';
import { workspaceModel } from '../workspace.model';
import { MediaService } from '../media.service';
import { createTestApp, setupTestDatabase, TestData } from './test-helpers';
import { UserController } from '../user.controller';
import { workspaceService } from '../workspace.service';

const app = createTestApp();
const userController = new UserController();

// ---------------------------
// Test suite
// ---------------------------
describe('User API – Mocked Tests', () => {
  let mongo: MongoMemoryServer;
  let testData: TestData;

  // Spin up in-memory Mongo
  beforeAll(async () => {
    mongo = await MongoMemoryServer.create();
    const uri = mongo.getUri();
    await mongoose.connect(uri);
    console.log('✅ Connected to in-memory MongoDB');
  });

  // Tear down DB
  afterAll(async () => {
    await mongoose.disconnect();
    await mongo.stop();
  });

  // Fresh DB state before each test
  beforeEach(async () => {
    testData = await setupTestDatabase();
  });

  // Clean mocks every test
  afterEach(() => {
    jest.restoreAllMocks();
    jest.clearAllMocks();
  });

  describe('PUT /api/users/profile - Update Profile, with mocks', () => {
    test('500 – update profile handles service error', async () => {
      // Mocked behavior: userModel.update throws database connection error
      // Input: profile update data
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(userModel, 'update').mockRejectedValueOnce(new Error('Database connection failed'));

      const res = await request(app)
        .put('/api/users/profile')
        .set('x-test-user-id', testData.testUserId)
        .send({ profile: { name: 'Test' } });

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Database connection failed');
    });

    test('500 – update profile handles non-Error thrown value', async () => {
      // Mocked behavior: userModel.update throws non-Error value
      // Input: profile update data
      // Expected status code: 500 or error handler
      // Expected behavior: next(error) called
      // Expected output: error handled by error handler
      jest.spyOn(userModel, 'update').mockRejectedValueOnce('String error');

      const res = await request(app)
        .put('/api/users/profile')
        .set('x-test-user-id', testData.testUserId)
        .send({ profile: { name: 'Test' } });

      // Should call next(error) which might be handled by error handler
      // In our case, it should still return 500
      expect(res.status).toBeGreaterThanOrEqual(500);
    });

    test('500 – update profile handles error without message (covers error.message || fallback)', async () => {
      // Mocked behavior: userModel.update throws Error without message property
      // Input: profile update data
      // Expected status code: 500
      // Expected behavior: fallback message used when error.message is undefined
      // Expected output: fallback error message
      const errorWithoutMessage = new Error();
      delete (errorWithoutMessage as any).message;
      jest.spyOn(userModel, 'update').mockRejectedValueOnce(errorWithoutMessage);

      const res = await request(app)
        .put('/api/users/profile')
        .set('x-test-user-id', testData.testUserId)
        .send({ profile: { name: 'Test' } });

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Failed to update user info');
    });
  });

  describe('DELETE /api/users/profile - Delete Profile, with mocks', () => {
    test('500 – delete profile handles service error', async () => {
      // Mocked behavior: workspaceModel.find throws database error
      // Input: authenticated user deletion request
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(workspaceModel, 'find').mockRejectedValueOnce(new Error('Database error'));

      const res = await request(app)
        .delete('/api/users/profile')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Database error');
    });

    test('500 – delete profile handles non-Error thrown value', async () => {
      // Mocked behavior: workspaceModel.find throws non-Error value
      // Input: authenticated user deletion request
      // Expected status code: 500 or error handler
      // Expected behavior: next(error) called
      // Expected output: error handled by error handler
      jest.spyOn(workspaceModel, 'find').mockRejectedValueOnce('String error');

      const res = await request(app)
        .delete('/api/users/profile')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBeGreaterThanOrEqual(500);
    });

    test('500 – delete profile handles error without message (covers error.message || fallback)', async () => {
      // Mocked behavior: workspaceModel.find throws Error without message property
      // Input: authenticated user deletion request
      // Expected status code: 500
      // Expected behavior: fallback message used when error.message is undefined
      // Expected output: fallback error message
      const errorWithoutMessage = new Error();
      delete (errorWithoutMessage as any).message;
      jest.spyOn(workspaceModel, 'find').mockRejectedValueOnce(errorWithoutMessage);

      const res = await request(app)
        .delete('/api/users/profile')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Failed to delete user');
    });
  });

  describe('POST /api/users/fcm-token - Update FCM Token, with mocks', () => {
    test('400 – update FCM token handles validation error', async () => {
      // Mocked behavior: userModel.updateFcmToken throws validation error
      // Input: fcmToken in request body
      // Expected status code: 400
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(userModel, 'updateFcmToken').mockRejectedValueOnce(new Error('Invalid update data'));

      const res = await request(app)
        .post('/api/users/fcm-token')
        .set('x-test-user-id', testData.testUserId)
        .send({ fcmToken: 'test-token' });

      expect(res.status).toBe(400);
      expect(res.body.message).toBe('Invalid update data');
    });

    test('400 – update FCM token handles non-Error thrown value', async () => {
      // Mocked behavior: userModel.updateFcmToken throws non-Error value
      // Input: fcmToken in request body
      // Expected status code: 400 or error handler
      // Expected behavior: next(error) called
      // Expected output: error handled by error handler
      jest.spyOn(userModel, 'updateFcmToken').mockRejectedValueOnce('String error');

      const res = await request(app)
        .post('/api/users/fcm-token')
        .set('x-test-user-id', testData.testUserId)
        .send({ fcmToken: 'test-token' });

      expect(res.status).toBeGreaterThanOrEqual(400);
    });

    test('400 – update FCM token handles error without message (covers error.message || fallback)', async () => {
      // Mocked behavior: userModel.updateFcmToken throws Error without message property
      // Input: fcmToken in request body
      // Expected status code: 400
      // Expected behavior: fallback message used when error.message is undefined
      // Expected output: fallback error message
      const errorWithoutMessage = new Error();
      delete (errorWithoutMessage as any).message;
      jest.spyOn(userModel, 'updateFcmToken').mockRejectedValueOnce(errorWithoutMessage);

      const res = await request(app)
        .post('/api/users/fcm-token')
        .set('x-test-user-id', testData.testUserId)
        .send({ fcmToken: 'test-token' });

      expect(res.status).toBe(400);
      expect(res.body.message).toBe('Failed to update FCM token');
    });
  });

  describe('GET /api/users/:id - Get User By ID, with mocks', () => {
    test('500 – get user by ID handles service error', async () => {
      // Mocked behavior: userModel.findById throws database error
      // Input: user ID in URL params
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(userModel, 'findById').mockRejectedValueOnce(new Error('Database error'));

      const res = await request(app)
        .get(`/api/users/${testData.testUserId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Database error');
    });

    test('500 – get user by ID handles non-Error thrown value', async () => {
      // Mocked behavior: userModel.findById throws non-Error value
      // Input: user ID in URL params
      // Expected status code: 500 or error handler
      // Expected behavior: next(error) called
      // Expected output: error handled by error handler
      jest.spyOn(userModel, 'findById').mockRejectedValueOnce('String error');

      const res = await request(app)
        .get(`/api/users/${testData.testUserId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBeGreaterThanOrEqual(500);
    });

    test('500 – get user by ID handles error without message (covers error.message || fallback)', async () => {
      // Mocked behavior: userModel.findById throws Error without message property
      // Input: user ID in URL params
      // Expected status code: 500
      // Expected behavior: fallback message used when error.message is undefined
      // Expected output: fallback error message
      const errorWithoutMessage = new Error();
      delete (errorWithoutMessage as any).message;
      jest.spyOn(userModel, 'findById').mockRejectedValueOnce(errorWithoutMessage);

      const res = await request(app)
        .get(`/api/users/${testData.testUserId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Failed to get user');
    });
  });

  describe('GET /api/users/email/:email - Get User By Email, with mocks', () => {
    test('500 – get user by email handles service error', async () => {
      // Mocked behavior: userModel.findByEmail throws database error
      // Input: email in URL params
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(userModel, 'findByEmail').mockRejectedValueOnce(new Error('Database error'));

      const res = await request(app)
        .get('/api/users/email/testuser1@example.com')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Database error');
    });

    test('500 – get user by email handles non-Error thrown value', async () => {
      // Mocked behavior: userModel.findByEmail throws non-Error value
      // Input: email in URL params
      // Expected status code: 500 or error handler
      // Expected behavior: next(error) called
      // Expected output: error handled by error handler
      jest.spyOn(userModel, 'findByEmail').mockRejectedValueOnce('String error');

      const res = await request(app)
        .get('/api/users/email/testuser1@example.com')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBeGreaterThanOrEqual(500);
    });

    test('500 – get user by email handles error without message (covers error.message || fallback)', async () => {
      // Mocked behavior: userModel.findByEmail throws Error without message property
      // Input: email in URL params
      // Expected status code: 500
      // Expected behavior: fallback message used when error.message is undefined
      // Expected output: fallback error message
      const errorWithoutMessage = new Error();
      delete (errorWithoutMessage as any).message;
      jest.spyOn(userModel, 'findByEmail').mockRejectedValueOnce(errorWithoutMessage);

      const res = await request(app)
        .get('/api/users/email/testuser1@example.com')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Failed to get user');
    });

    test('400 – get user by email handles empty email (covers !email branch)', async () => {
      // Mocked behavior: email param is empty/undefined
      // Input: empty email in URL params (tested via direct controller call or route manipulation)
      // Expected status code: 400
      // Expected behavior: validation error returned
      // Expected output: error message
      // We need to call the controller directly to test this branch since Express routes may not match empty params
      const req = {
        params: { email: '' },
        user: { _id: new mongoose.Types.ObjectId(testData.testUserId) },
      } as any;
      const res = {
        status: jest.fn().mockReturnThis(),
        json: jest.fn().mockReturnThis(),
      } as any;
      const next = jest.fn();

      await userController.getUserByEmail(req, res, next);

      expect(res.status).toHaveBeenCalledWith(400);
      expect(res.json).toHaveBeenCalledWith({ message: 'Invalid email' });
    });
  });

  describe('User Model Methods - Coverage Tests', () => {
    test('500 – getWorkspaceMembers handles findByIds error (covers userModel.findByIds catch branch)', async () => {
      // Mocked behavior: userModel.findByIds throws database error
      // Input: workspaceId with members
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message
      jest.spyOn(userModel, 'findByIds').mockRejectedValueOnce(new Error('Database error'));

      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Database error');
    });

    test('userModel.findByGoogleId returns null when user not found (covers !user branch)', async () => {
      // Input: googleId that doesn't exist
      // Expected behavior: returns null
      // Expected output: null
      const result = await userModel.findByGoogleId('non-existent-google-id');
      expect(result).toBeNull();
    });

    test('userModel.findByGoogleId returns user when found', async () => {
      // Input: valid googleId
      // Expected behavior: returns user object
      // Expected output: user object
      const result = await userModel.findByGoogleId('test-google-id-1');
      expect(result).not.toBeNull();
      expect(result?.email).toBe('testuser1@example.com');
    });

    test('userModel.findByIds returns multiple users', async () => {
      // Input: array of user IDs
      // Expected behavior: returns array of user objects
      // Expected output: array of users
      const userIds = [
        new mongoose.Types.ObjectId(testData.testUserId),
        new mongoose.Types.ObjectId(testData.testUser2Id),
      ];
      const result = await userModel.findByIds(userIds);
      expect(result).toHaveLength(2);
      expect(result[0]._id.toString()).toBe(testData.testUserId);
      expect(result[1]._id.toString()).toBe(testData.testUser2Id);
    });
  });
});

