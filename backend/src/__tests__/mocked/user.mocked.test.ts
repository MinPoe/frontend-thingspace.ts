/// <reference types="jest" />
import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';
import type { Request, Response, NextFunction } from 'express';

import { userModel } from '../../user.model';
import { workspaceModel } from '../../workspace.model';
import * as authMiddleware from '../../auth.middleware';
import { createTestApp, setupTestDatabase, TestData } from '../test-utils/test-helpers';
import { UserController } from '../../user.controller';
import { workspaceService } from '../../workspace.service';

const userController = new UserController();

// ---------------------------
// Test suite
// ---------------------------
describe('User API – Mocked Tests', () => {
  let mongo: MongoMemoryServer;
  let testData: TestData;
  let app: ReturnType<typeof createTestApp>;

  // Spin up in-memory Mongo
  beforeAll(async () => {
    mongo = await MongoMemoryServer.create();
    const uri = mongo.getUri();
    await mongoose.connect(uri);
    console.log('✅ Connected to in-memory MongoDB');
    
    // Create app after DB connection
    app = createTestApp();
  });

  // Tear down DB
  afterAll(async () => {
    await mongoose.disconnect();
    await mongo.stop({ doCleanup: true, force: true });
  });

  // Fresh DB state before each test
  beforeEach(async () => {
    testData = await setupTestDatabase(app);
  });

  // Clean mocks every test
  afterEach(() => {
    jest.restoreAllMocks();
    jest.clearAllMocks();
  });

  describe('PUT /api/user/profile - Update Profile, with mocks', () => {
    test('500 – update profile handles service error', async () => {
      // Mocked behavior: userModel.update throws database connection error
      // Input: profile update data
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(userModel, 'update').mockRejectedValueOnce(new Error('Database connection failed'));

      const res = await request(app)
        .put('/api/user/profile')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
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
        .put('/api/user/profile')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
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
        .put('/api/user/profile')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ profile: { name: 'Test' } });

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Failed to update user info');
    });

    test('404 when userModel.update returns null (line 39)', async () => {
      // Input: update request where userModel.update returns null
      // Expected status code: 404
      // Expected behavior: returns "User not found" error
      // Expected output: error message
      // This tests line 39 in user.controller.ts
      jest.spyOn(userModel, 'update').mockResolvedValueOnce(null);

      const res = await request(app)
        .put('/api/user/profile')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ profile: { name: 'Test' } });

      expect(res.status).toBe(404);
      expect(res.body.message).toBe('User not found');
    });
  });

  describe('DELETE /api/user/profile - Delete Profile, with mocks', () => {
    test('500 – delete profile handles service error', async () => {
      // Mocked behavior: workspaceModel.find throws database error
      // Input: authenticated user deletion request
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(workspaceModel, 'find').mockRejectedValueOnce(new Error('Database error'));

      const res = await request(app)
        .delete('/api/user/profile')
        .set('Authorization', `Bearer ${testData.testUserToken}`);

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
        .delete('/api/user/profile')
        .set('Authorization', `Bearer ${testData.testUserToken}`);

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
        .delete('/api/user/profile')
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Failed to delete user');
    });
  });

  describe('POST /api/user/fcm-token - Update FCM Token, with mocks', () => {
    test('400 – update FCM token handles validation error', async () => {
      // Mocked behavior: userModel.updateFcmToken throws validation error
      // Input: fcmToken in request body
      // Expected status code: 400
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(userModel, 'updateFcmToken').mockRejectedValueOnce(new Error('Invalid update data'));

      const res = await request(app)
        .post('/api/user/fcm-token')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
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
        .post('/api/user/fcm-token')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
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
        .post('/api/user/fcm-token')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ fcmToken: 'test-token' });

      expect(res.status).toBe(400);
      expect(res.body.message).toBe('Failed to update FCM token');
    });

    test('404 when userModel.updateFcmToken returns null (line 117)', async () => {
      // Input: FCM token update where userModel.updateFcmToken returns null
      // Expected status code: 404
      // Expected behavior: returns "User not found" error
      // Expected output: error message
      // This tests line 117 in user.controller.ts
      jest.spyOn(userModel, 'updateFcmToken').mockResolvedValueOnce(null);

      const res = await request(app)
        .post('/api/user/fcm-token')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ fcmToken: 'test-token' });

      expect(res.status).toBe(404);
      expect(res.body.message).toBe('User not found');
    });
  });

  describe('GET /api/user/:id - Get User By ID, with mocks', () => {
    test('500 – get user by ID handles service error', async () => {
      // Mocked behavior: userModel.findById throws database error
      // Input: user ID in URL params
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(userModel, 'findById').mockRejectedValueOnce(new Error('Database error'));

      const res = await request(app)
        .get(`/api/user/${testData.testUserId}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Internal server error');
    });

    test('500 – get user by ID handles non-Error thrown value', async () => {
      // Mocked behavior: userModel.findById throws non-Error value
      // Input: user ID in URL params
      // Expected status code: 500 or error handler
      // Expected behavior: next(error) called
      // Expected output: error handled by error handler
      jest.spyOn(userModel, 'findById').mockRejectedValueOnce('String error');

      const res = await request(app)
        .get(`/api/user/${testData.testUserId}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

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
        .get(`/api/user/${testData.testUserId}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Internal server error');
    });

    test('next(error) called when non-Error is thrown (direct controller call, lines 161-169)', async () => {
      // Input: non-Error value thrown
      // Expected behavior: logger.error called, then next(error) called for non-Error (lines 161-169)
      // Expected output: next receives the non-Error value
      // This tests lines 161-169 in user.controller.ts by calling controller directly
      const req = {
        params: { id: testData.testUserId },
        user: { _id: new mongoose.Types.ObjectId(testData.testUserId) },
      } as any;
      const res = {
        status: jest.fn().mockReturnThis(),
        json: jest.fn().mockReturnThis(),
      } as any;
      const next = jest.fn();

      // Mock userModel.findById to throw a non-Error value
      jest.spyOn(userModel, 'findById').mockRejectedValueOnce({ custom: 'error object' });

      await userController.getUserById(req, res, next);

      // Verify that next was called with the non-Error object
      expect(next).toHaveBeenCalledWith({ custom: 'error object' });
      expect(res.status).not.toHaveBeenCalled();
    });

    test('error.message || fallback works when Error has no message (line 165 - direct controller call)', async () => {
      // Input: Error instance without message property
      // Expected behavior: fallback message is used (line 164-165)
      // Expected output: 500 with fallback message
      // Must call controller directly to avoid asyncHandler intercepting
      const req = {
        params: { id: testData.testUserId },
        user: { _id: new mongoose.Types.ObjectId(testData.testUserId) },
      } as any;
      const jsonMock = jest.fn();
      const res = {
        status: jest.fn().mockReturnThis(),
        json: jsonMock,
      } as any;
      const next = jest.fn();

      const errorWithoutMessage = new Error();
      delete (errorWithoutMessage as any).message;
      jest.spyOn(userModel, 'findById').mockRejectedValueOnce(errorWithoutMessage);

      await userController.getUserById(req, res, next);

      expect(res.status).toHaveBeenCalledWith(500);
      expect(jsonMock).toHaveBeenCalledWith({ message: 'Failed to get user' });
    });

    test('Error with message is handled properly (line 164 - direct controller call)', async () => {
      // Input: Error instance with a message
      // Expected behavior: error message is returned (line 164)
      // Expected output: 500 with error message
      // Must call controller directly to test line 164
      const req = {
        params: { id: testData.testUserId },
        user: { _id: new mongoose.Types.ObjectId(testData.testUserId) },
      } as any;
      const jsonMock = jest.fn();
      const res = {
        status: jest.fn().mockReturnThis(),
        json: jsonMock,
      } as any;
      const next = jest.fn();

      jest.spyOn(userModel, 'findById').mockRejectedValueOnce(new Error('Custom error'));

      await userController.getUserById(req, res, next);

      expect(res.status).toHaveBeenCalledWith(500);
      expect(jsonMock).toHaveBeenCalledWith({ message: 'Custom error' });
    });
  });

  describe('GET /api/user/email/:email - Get User By Email, with mocks', () => {
    test('500 – get user by email handles service error', async () => {
      // Mocked behavior: userModel.findByEmail throws database error
      // Input: email in URL params
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(userModel, 'findByEmail').mockRejectedValueOnce(new Error('Database error'));

      const res = await request(app)
        .get('/api/user/email/testuser1@example.com')
        .set('Authorization', `Bearer ${testData.testUserToken}`);

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
        .get('/api/user/email/testuser1@example.com')
        .set('Authorization', `Bearer ${testData.testUserToken}`);

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
        .get('/api/user/email/testuser1@example.com')
        .set('Authorization', `Bearer ${testData.testUserToken}`);

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
        .get(`/api/workspace/${testData.testWorkspaceId}/members`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Database error');
    });

    test('userModel.findByIds throws error on database failure (covers lines 146-147)', async () => {
      // Mocked behavior: Database connection error triggers catch block
      // Input: array of user IDs
      // Expected behavior: error is caught and rethrown with message
      // Expected output: Error with message "Failed to find users"
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
      
      // Temporarily disconnect mongoose to trigger a database error
      const originalReadyState = mongoose.connection.readyState;
      await mongoose.disconnect();

      const userIds = [
        new mongoose.Types.ObjectId(testData.testUserId),
      ];

      await expect(userModel.findByIds(userIds)).rejects.toThrow('Failed to find users');
      expect(consoleErrorSpy).toHaveBeenCalledWith('Error finding users by IDs:', expect.any(Error));

      // Reconnect mongoose
      if (originalReadyState === 1) {
        await mongoose.connect(mongo.getUri());
      }
      consoleErrorSpy.mockRestore();
    });

    test('userModel.findByGoogleId returns null when user not found (covers !user branch)', async () => {
      // Input: googleId that doesn't exist
      // Expected behavior: returns null
      // Expected output: null
      const result = await userModel.findByGoogleId('non-existent-google-id');
      expect(result).toBeNull();
    });

    test('userModel.findByGoogleId throws error on database failure (covers lines 161-162)', async () => {
      // Mocked behavior: Database connection error triggers catch block
      // Input: googleId string
      // Expected behavior: error is caught and rethrown with message
      // Expected output: Error with message "Failed to find user"
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
      
      // Temporarily disconnect mongoose to trigger a database error
      const originalReadyState = mongoose.connection.readyState;
      await mongoose.disconnect();

      await expect(userModel.findByGoogleId('test-google-id')).rejects.toThrow('Failed to find user');
      expect(consoleErrorSpy).toHaveBeenCalledWith('Error finding user by Google ID:', expect.any(Error));

      // Reconnect mongoose
      if (originalReadyState === 1) {
        await mongoose.connect(mongo.getUri());
      }
      consoleErrorSpy.mockRestore();
    });

    test('userModel.findByGoogleId returns user when found', async () => {
      // Input: valid googleId
      // Expected behavior: returns user object
      // Expected output: user object
      const existingUser = await userModel.findByEmail('testuser1@example.com');
      expect(existingUser).not.toBeNull();
      const result = await userModel.findByGoogleId(existingUser!.googleId);
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

    test('userModel.update throws error when updateProfileSchema.parse fails (covers lines 113-114)', async () => {
      // Mocked behavior: updateProfileSchema.parse throws validation error
      // Input: invalid updateProfileReq
      // Expected behavior: error is caught and rethrown with message
      // Expected output: Error with message "Failed to update user"
      const loggerErrorSpy = jest.spyOn(require('../../logger.util').default, 'error').mockImplementation(() => {});
      
      // Mock updateProfileSchema.parse using jest.spyOn
      const { updateProfileSchema } = require('../../user.types');
      const parseSpy = jest.spyOn(updateProfileSchema, 'parse').mockImplementation(() => {
        throw new Error('Validation error');
      });

      await expect(
        userModel.update(new mongoose.Types.ObjectId(testData.testUserId), {
          profile: { name: 'Test', description: 'Test' },
        })
      ).rejects.toThrow('Failed to update user');
      expect(loggerErrorSpy).toHaveBeenCalledWith('Error updating user:', expect.any(Error));

      parseSpy.mockRestore();
      loggerErrorSpy.mockRestore();
    });

    test('userModel.delete throws error when findByIdAndDelete fails (covers lines 122-123)', async () => {
      // Mocked behavior: this.user.findByIdAndDelete throws database error
      // Input: userId
      // Expected behavior: error is caught and rethrown with message
      // Expected output: Error with message "Failed to delete user"
      const loggerErrorSpy = jest.spyOn(require('../../logger.util').default, 'error').mockImplementation(() => {});
      
      // Access the internal user model and mock its findByIdAndDelete method
      const internalUserModel = (userModel as any).user;
      const originalFindByIdAndDelete = internalUserModel.findByIdAndDelete;
      
      // Mock the findByIdAndDelete method using jest.fn() to properly reject
      const mockFindByIdAndDelete = jest.fn();
      mockFindByIdAndDelete.mockRejectedValue(new Error('Database error'));
      internalUserModel.findByIdAndDelete = mockFindByIdAndDelete;

      await expect(userModel.delete(new mongoose.Types.ObjectId(testData.testUserId))).rejects.toThrow('Failed to delete user');
      expect(loggerErrorSpy).toHaveBeenCalledWith('Error deleting user:', expect.any(Error));

      // Restore original method
      internalUserModel.findByIdAndDelete = originalFindByIdAndDelete;
      loggerErrorSpy.mockRestore();
    });

    test('userModel.findById throws error when findOne fails (covers lines 137-138)', async () => {
      // Mocked behavior: this.user.findOne throws database error
      // Input: userId
      // Expected behavior: error is caught and rethrown with message
      // Expected output: Error with message "Failed to find user"
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
      
      // Access the internal user model and mock its findOne method
      const internalUserModel = (userModel as any).user;
      const originalFindOne = internalUserModel.findOne;
      
      // Mock the findOne method using jest.fn() to properly reject
      const mockFindOne = jest.fn();
      mockFindOne.mockRejectedValue(new Error('Database error'));
      internalUserModel.findOne = mockFindOne;

      await expect(userModel.findById(new mongoose.Types.ObjectId(testData.testUserId))).rejects.toThrow('Failed to find user');
      expect(consoleErrorSpy).toHaveBeenCalledWith('Error finding user by Google ID:', expect.any(Error));

      // Restore original method
      internalUserModel.findOne = originalFindOne;
      consoleErrorSpy.mockRestore();
    });

    test('userModel.findByEmail throws error when findOne fails (covers lines 171-172)', async () => {
      // Mocked behavior: this.user.findOne throws database error
      // Input: email string
      // Expected behavior: error is caught and rethrown with message
      // Expected output: Error with message "Failed to find user"
      const loggerErrorSpy = jest.spyOn(require('../../logger.util').default, 'error').mockImplementation(() => {});
      
      // Access the internal user model and mock its findOne method
      const internalUserModel = (userModel as any).user;
      const originalFindOne = internalUserModel.findOne;
      
      // Mock the findOne method using jest.fn() to properly reject
      const mockFindOne = jest.fn();
      mockFindOne.mockRejectedValue(new Error('Database error'));
      internalUserModel.findOne = mockFindOne;

      await expect(userModel.findByEmail('test@example.com')).rejects.toThrow('Failed to find user');
      expect(loggerErrorSpy).toHaveBeenCalledWith('Error finding user by email:', expect.any(Error));

      // Restore original method
      internalUserModel.findOne = originalFindOne;
      loggerErrorSpy.mockRestore();
    });
  });

  describe('User routes - user authentication edge cases', () => {
    const buildAppWithMockedAuth = async (userMock: any) => {
      jest.resetModules();

      // Mock authenticateToken before requiring routes
      jest.doMock('../../auth.middleware', () => ({
        authenticateToken: async (req: Request, res: Response, next: NextFunction) => {
          req.user = userMock;
          next();
        },
      }));

      const helpers = await import('../test-utils/test-helpers.js') as typeof import('../test-utils/test-helpers');
      return helpers.createTestApp();
    };

    afterEach(() => {
      jest.resetModules();
      jest.dontMock('../../auth.middleware');
    });

    test('GET /api/user/profile - 401 when req.user is undefined (lines 15-16)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests lines 15-16 in user.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      const res = await request(appInstance)
        .get('/api/user/profile')
        .set('Authorization', 'Bearer fake-token');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('PUT /api/user/profile - 401 when req.user is undefined (line 33)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests line 33 in user.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      const res = await request(appInstance)
        .put('/api/user/profile')
        .set('Authorization', 'Bearer fake-token')
        .send({ profile: { name: 'Test' } });

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('DELETE /api/user/profile - 401 when req.user is undefined (line 65)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests line 65 in user.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      const res = await request(appInstance)
        .delete('/api/user/profile')
        .set('Authorization', 'Bearer fake-token');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('POST /api/user/fcm-token - 401 when req.user is undefined (line 107)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests line 107 in user.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      const res = await request(appInstance)
        .post('/api/user/fcm-token')
        .set('Authorization', 'Bearer fake-token')
        .send({ fcmToken: 'test-token' });

      expect(res.status).toBe(401);
      expect(res.body.message).toBe('User not authenticated');
    });
  });
});

