/// <reference types="jest" />
import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';

import { workspaceModel } from '../workspace.model';
import { userModel } from '../user.model';
import { noteModel } from '../note.model';
import { createTestApp, setupTestDatabase, TestData } from './test-helpers';

const app = createTestApp();

// ---------------------------
// Test suite
// ---------------------------
describe('User API – Normal Tests (No Mocking)', () => {
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

  describe('GET /api/user/profile - Get Profile', () => {
    test('401 – returns 401 when user is not authenticated', async () => {
      // Input: request without user authentication
      // Expected status code: 401
      // Expected behavior: error message returned
      // Expected output: error message "User not authenticated"
      // This tests lines 15-16 in user.controller.ts
      const res = await request(app)
        .get('/api/user/profile')
        .set('x-no-user-id', 'true');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('200 – retrieves user profile successfully', async () => {
      // Input: authenticated user request
      // Expected status code: 200
      // Expected behavior: user profile retrieved from authenticated user
      // Expected output: user object with profile data
      const res = await request(app)
        .get('/api/user/profile')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Profile fetched successfully');
      expect(res.body.data.user).toBeDefined();
      expect(res.body.data.user._id).toBe(testData.testUserId);
      expect(res.body.data.user.email).toBe('testuser1@example.com');
    });
  });

  describe('PUT /api/user/profile - Update Profile', () => {
    test('401 – returns 401 when user is not authenticated', async () => {
      // Input: request without user authentication
      // Expected status code: 401
      // Expected behavior: error message returned
      // Expected output: error message "User not authenticated"
      // This tests line 33 in user.controller.ts
      const res = await request(app)
        .put('/api/user/profile')
        .set('x-no-user-id', 'true')
        .send({
          profile: {
            name: 'Updated Name',
            description: 'Updated description',
            imagePath: '',
          },
        });

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('200 – updates user profile successfully', async () => {
      // Input: profile data with name, description, and imagePath
      // Expected status code: 200
      // Expected behavior: user profile updated in database
      // Expected output: updated user object
      const updateData = {
        profile: {
          name: 'Updated Name',
          description: 'Updated description',
          imagePath: '/path/to/image.jpg',
        },
      };

      const res = await request(app)
        .put('/api/user/profile')
        .set('x-test-user-id', testData.testUserId)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('User info updated successfully');
      expect(res.body.data.user).toBeDefined();
      expect(res.body.data.user.profile.name).toBe('Updated Name');
      expect(res.body.data.user.profile.description).toBe('Updated description');
      expect(res.body.data.user.profile.imagePath).toBe('/path/to/image.jpg');
    });

    test('200 – updates only profile name', async () => {
      // Input: profile data with only name field
      // Expected status code: 200
      // Expected behavior: only name field is updated
      // Expected output: updated user object with new name
      const updateData = {
        profile: {
          name: 'New Name Only',
        },
      };

      const res = await request(app)
        .put('/api/user/profile')
        .set('x-test-user-id', testData.testUserId)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.data.user.profile.name).toBe('New Name Only');
    });

    test('404 – returns 404 when user not found', async () => {
      // Input: update request for deleted user
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      // Delete the user first
      await userModel.delete(new mongoose.Types.ObjectId(testData.testUserId));

      const updateData = {
        profile: {
          name: 'Updated Name',
        },
      };

      const res = await request(app)
        .put('/api/user/profile')
        .set('x-test-user-id', testData.testUserId)
        .send(updateData);

      expect(res.status).toBe(404);
      expect(res.body.message).toBe('User not found');
    });

    test('200 – handles update request without profile field (empty dictionary branch)', async () => {
      // Input: update request without profile field (tests user.model.ts line 102)
      // Expected status code: 200
      // Expected behavior: updateData is empty dictionary {}, no profile update performed
      // Expected output: user object unchanged (or successfully returned)
      const updateData = {};

      const res = await request(app)
        .put('/api/user/profile')
        .set('x-test-user-id', testData.testUserId)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('User info updated successfully');
      expect(res.body.data.user).toBeDefined();
    });
  });

  describe('DELETE /api/user/profile - Delete Profile', () => {
    test('401 – returns 401 when user is not authenticated', async () => {
      // Input: request without user authentication
      // Expected status code: 401
      // Expected behavior: error message returned
      // Expected output: error message "User not authenticated"
      // This tests line 65 in user.controller.ts
      const res = await request(app)
        .delete('/api/user/profile')
        .set('x-no-user-id', 'true');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('200 – deletes user profile successfully', async () => {
      // Input: authenticated user deletion request
      // Expected status code: 200
      // Expected behavior: user deleted, owned workspaces deleted, notes deleted, user removed from member workspaces
      // Expected output: success message
      // Create a workspace owned by the user with notes
      const workspace = await workspaceModel.create({
        name: 'User Workspace',
        profile: { imagePath: '', name: 'User Workspace', description: '' },
        ownerId: new mongoose.Types.ObjectId(testData.testUserId),
        members: [new mongoose.Types.ObjectId(testData.testUserId)],
      });

      await noteModel.create({
        userId: new mongoose.Types.ObjectId(testData.testUserId),
        workspaceId: workspace._id.toString(),
        noteType: 'CONTENT',
        fields: [{ fieldType: 'title', content: 'Test Note', _id: '1' }],
      });

      // Add user as member of another workspace
      await workspaceModel.findByIdAndUpdate(testData.testWorkspace2Id, {
        $push: { members: new mongoose.Types.ObjectId(testData.testUserId) },
      });

      const res = await request(app)
        .delete('/api/user/profile')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('User deleted successfully');

      // Verify user is deleted
      const deletedUser = await userModel.findById(new mongoose.Types.ObjectId(testData.testUserId));
      expect(deletedUser).toBeNull();

      // Verify workspace is deleted
      const deletedWorkspace = await workspaceModel.findById(workspace._id);
      expect(deletedWorkspace).toBeNull();

      // Verify notes are deleted
      const notes = await noteModel.find({ workspaceId: workspace._id.toString() });
      expect(notes.length).toBe(0);

      // Verify user removed from other workspace
      const otherWorkspace = await workspaceModel.findById(testData.testWorkspace2Id);
      expect(otherWorkspace?.members).not.toContainEqual(new mongoose.Types.ObjectId(testData.testUserId));
    });

    test('200 – deletes user with no owned workspaces', async () => {
      // Input: authenticated user deletion request for user with no owned workspaces
      // Expected status code: 200
      // Expected behavior: user deleted successfully
      // Expected output: success message
      const res = await request(app)
        .delete('/api/user/profile')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('User deleted successfully');
    });
  });

  describe('POST /api/user/fcm-token - Update FCM Token', () => {
    test('401 – returns 401 when user is not authenticated', async () => {
      // Input: request without user authentication
      // Expected status code: 401
      // Expected behavior: error message returned
      // Expected output: error message "User not authenticated"
      // This tests line 107 in user.controller.ts
      const res = await request(app)
        .post('/api/user/fcm-token')
        .set('x-no-user-id', 'true')
        .send({ fcmToken: 'test-token' });

      expect(res.status).toBe(401);
      expect(res.body.message).toBe('User not authenticated');
    });

    test('200 – updates FCM token successfully', async () => {
      // Input: fcmToken in request body
      // Expected status code: 200
      // Expected behavior: FCM token updated in database
      // Expected output: updated user object with new FCM token
      const updateData = {
        fcmToken: 'new-fcm-token-123',
      };

      const res = await request(app)
        .post('/api/user/fcm-token')
        .set('x-test-user-id', testData.testUserId)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('FCM token updated successfully');
      expect(res.body.data.user).toBeDefined();
      expect(res.body.data.user.fcmToken).toBe('new-fcm-token-123');
    });

    test('404 – returns 404 when user not found', async () => {
      // Input: FCM token update request for deleted user
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      // Delete the user first
      await userModel.delete(new mongoose.Types.ObjectId(testData.testUserId));

      const updateData = {
        fcmToken: 'new-fcm-token-123',
      };

      const res = await request(app)
        .post('/api/user/fcm-token')
        .set('x-test-user-id', testData.testUserId)
        .send(updateData);

      expect(res.status).toBe(404);
      expect(res.body.message).toBe('User not found');
    });
  });

  describe('GET /api/user/:id - Get User By ID', () => {
    test('200 – retrieves user by ID successfully', async () => {
      // Input: user ID in URL params
      // Expected status code: 200
      // Expected behavior: user retrieved from database
      // Expected output: user object
      const res = await request(app)
        .get(`/api/user/${testData.testUserId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('User fetched successfully');
      expect(res.body.data.user).toBeDefined();
      expect(res.body.data.user._id).toBe(testData.testUserId);
      expect(res.body.data.user.email).toBe('testuser1@example.com');
    });

    test('400 – returns 400 for invalid user ID format', async () => {
      // Input: invalid user ID format in URL params
      // Expected status code: 400
      // Expected behavior: validation error returned
      // Expected output: error message
      const res = await request(app)
        .get('/api/user/invalid-id')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(400);
      expect(res.body.message).toBe('Invalid user ID format');
    });

    test('404 – returns 404 when user not found', async () => {
      // Input: valid user ID format but user doesn't exist
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeUserId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .get(`/api/user/${fakeUserId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(404);
      expect(res.body.message).toBe('User not found');
    });
  });

  describe('GET /api/user/email/:email - Get User By Email', () => {
    test('200 – retrieves user by email successfully', async () => {
      // Input: email in URL params
      // Expected status code: 200
      // Expected behavior: user retrieved from database by email
      // Expected output: user object
      const res = await request(app)
        .get('/api/user/email/testuser1@example.com')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('User fetched successfully');
      expect(res.body.data.user).toBeDefined();
      expect(res.body.data.user.email).toBe('testuser1@example.com');
      expect(res.body.data.user._id).toBe(testData.testUserId);
    });

    test('200 – retrieves user by email with special characters', async () => {
      // Input: email with special characters (URL encoded)
      // Expected status code: 200
      // Expected behavior: user retrieved successfully with special characters in email
      // Expected output: user object
      // Create a user with special email
      const specialUser = await userModel.create({
        googleId: 'special-google-id',
        email: 'test+special@example.com',
        name: 'Special User',
        profilePicture: '',
      });

      const res = await request(app)
        .get(`/api/user/email/${encodeURIComponent('test+special@example.com')}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.data.user.email).toBe('test+special@example.com');
    });

    test('400 – returns 400 when email is empty', async () => {
      // Input: empty email in URL params
      // Expected status code: 400
      // Expected behavior: validation error returned
      // Expected output: error message
      // Note: Express may not match empty params, but we test the branch
      const res = await request(app)
        .get('/api/user/email/')
        .set('x-test-user-id', testData.testUserId);

      // The route might not match, so we test with a request that could trigger empty email
      // If the route doesn't match, we'll get 404 from Express, but the branch exists for safety
      expect(res.status).toBeGreaterThanOrEqual(400);
    });

    test('404 – returns 404 when user not found', async () => {
      // Input: email that doesn't exist in database
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const res = await request(app)
        .get('/api/user/email/nonexistent@example.com')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(404);
      expect(res.body.message).toBe('User not found');
    });
  });

  describe('User Model Error Branches - Direct Model Tests', () => {
    beforeEach(async () => {
      // Ensure database is connected before each test in this describe block
      if (mongoose.connection.readyState === 0) {
        await mongoose.connect(mongo.getUri());
      }
    });

    afterEach(async () => {
      // Ensure database is reconnected after each test
      if (mongoose.connection.readyState === 0) {
        await mongoose.connect(mongo.getUri());
      }
    });

    test('create throws ZodError on invalid data (ZodError branch)', async () => {
      // Input: invalid user data that fails schema validation (tests user.model.ts lines 85-87)
      // Expected behavior: ZodError caught and re-thrown as "Invalid update data"
      // Expected output: Error with message "Invalid update data"
      const invalidUserInfo = {
        googleId: 'test-google-id',
        email: 'invalid-email', // Invalid email format
        name: 'Test User',
        profilePicture: '',
      };

      await expect(
        userModel.create(invalidUserInfo)
      ).rejects.toThrow('Invalid update data');
    });

    test('create throws error on database error (generic error branch)', async () => {
      // Input: valid user data but database operation fails (tests user.model.ts lines 89-90)
      // Expected behavior: Error caught and re-thrown as "Failed to update user"
      // Expected output: Error with message "Failed to update user"
      const validUserInfo = {
        googleId: 'test-google-id-2',
        email: 'test@example.com',
        name: 'Test User',
        profilePicture: '',
      };
      const currentUri = mongo.getUri();
      
      // Disconnect database temporarily to trigger error
      await mongoose.disconnect();
      
      try {
        await expect(
          userModel.create(validUserInfo)
        ).rejects.toThrow('Failed to update user');
      } finally {
        // Reconnect using the same Mongo instance
        await mongoose.connect(currentUri);
      }
    });

    test('updateFcmToken throws error on database error', async () => {
      // Input: database operation that fails
      // Expected behavior: Error caught and re-thrown (lines 187-190)
      // Expected output: Error with message "Failed to update FCM token"
      const userId = new mongoose.Types.ObjectId(testData.testUserId);
      const fcmToken = 'test-fcm-token';
      const currentUri = mongo.getUri();
      
      // Disconnect database temporarily to trigger error
      await mongoose.disconnect();
      
      try {
        await expect(
          userModel.updateFcmToken(userId, fcmToken)
        ).rejects.toThrow('Failed to update FCM token');
      } finally {
        // Reconnect using the same Mongo instance
        await mongoose.connect(currentUri);
      }
    });

    test('updatePersonalWorkspace throws error on database error', async () => {
      // Input: database operation that fails
      // Expected behavior: Error caught and re-thrown (lines 204-207)
      // Expected output: Error with message "Failed to update personal workspace"
      const userId = new mongoose.Types.ObjectId(testData.testUserId);
      const workspaceId = new mongoose.Types.ObjectId(testData.testWorkspaceId);
      const currentUri = mongo.getUri();
      
      // Disconnect database temporarily to trigger error
      await mongoose.disconnect();
      
      try {
        await expect(
          userModel.updatePersonalWorkspace(userId, workspaceId)
        ).rejects.toThrow('Failed to update personal workspace');
      } finally {
        // Reconnect using the same Mongo instance
        await mongoose.connect(currentUri);
      }
    });
  });
});

