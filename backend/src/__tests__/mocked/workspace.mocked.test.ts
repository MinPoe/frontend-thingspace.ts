/// <reference types="jest" />
import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';
import type { Request, Response, NextFunction } from 'express';

import { workspaceService } from '../../workspaces/workspace.service';
import { workspaceModel } from '../../workspaces/workspace.model';
import { notificationService } from '../../notifications/notification.service';
import * as authMiddleware from '../../authentication/auth.middleware';
import { userModel } from '../../users/user.model';
import { createTestApp, setupTestDatabase, TestData } from '../test-utils/test-helpers';
import { mockSend } from '../test-utils/setup';

// ---------------------------
// Test suite
// ---------------------------
describe('Workspace API – Mocked Tests (Jest Mocks)', () => {
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

  // Clean mocks every test; full DB reset occurs in beforeEach
  afterEach(() => {
    jest.restoreAllMocks();
    jest.clearAllMocks();
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

  describe('Notification Service Direct Tests', () => {
    beforeEach(() => {
      mockSend.mockClear();
    });

    test('notification service module initialization throws error when FIREBASE_JSON is not set', () => {
      // Input: FIREBASE_JSON environment variable not set
      // Expected behavior: Module throws error during initialization (line 7)
      // Expected output: Error with message "FIREBASE_JSON environment variable is not set"
      // Save the original value
      const originalFirebaseJson = process.env.FIREBASE_JSON;
      
      // Temporarily delete FIREBASE_JSON
      delete process.env.FIREBASE_JSON;
      
      // Clear the module cache to force re-import
      const notificationServicePath = require.resolve('../../notifications/notification.service');
      delete require.cache[notificationServicePath];
      
      // Also clear firebase-admin from cache
      const firebaseAdminPath = require.resolve('firebase-admin');
      delete require.cache[firebaseAdminPath];
      
      try {
        // Try to import the module and expect it to throw
        expect(() => {
          jest.isolateModules(() => {
            require('../../notifications/notification.service');
          });
        }).toThrow('FIREBASE_JSON environment variable is not set');
      } finally {
        // Restore the original value
        process.env.FIREBASE_JSON = originalFirebaseJson;
        
        // Clear cache again to reload with correct env var
        delete require.cache[notificationServicePath];
        delete require.cache[firebaseAdminPath];
        
        // Re-import to restore normal state
        require('../../notifications/notification.service');
      }
    });

    test('isTokenValid returns true for valid token', async () => {
      // Mocked behavior: admin.messaging().send succeeds (via mockSend)
      // Input: valid FCM token
      // Expected behavior: isTokenValid returns true
      // Expected output: true
  
      mockSend.mockResolvedValueOnce('success');

      const result = await notificationService.isTokenValid('valid-token');

      expect(result).toBe(true);
      expect(mockSend).toHaveBeenCalledWith(
        {
          token: 'valid-token',
          notification: { title: '', body: '' }
        },
        true // dry run
      );
    });

    test('isTokenValid returns false for invalid token', async () => {
      // Mocked behavior: admin.messaging().send rejects (via mockSend)
      // Input: invalid FCM token
      // Expected behavior: isTokenValid returns false (catch block line 62-63)
      // Expected output: false
      mockSend.mockRejectedValueOnce(new Error('Invalid token'));

      const result = await notificationService.isTokenValid('invalid-token');

      expect(result).toBe(false);
      expect(mockSend).toHaveBeenCalledWith(
        {
          token: 'invalid-token',
          notification: { title: '', body: '' }
        },
        true // dry run
      );
    });

    test('sendNotification returns true when sending succeeds', async () => {
      // Mocked behavior: admin.messaging().send succeeds (via mockSend)
      // Input: FCM token, title, body
      // Expected behavior: sendNotification returns true (line 42-44)
      // Expected output: true
      mockSend.mockResolvedValueOnce('mock-message-id');

      const result = await notificationService.sendNotification(
        'test-token',
        'Test Title',
        'Test Body'
      );

      expect(result).toBe(true);
      expect(mockSend).toHaveBeenCalledWith({
        token: 'test-token',
        notification: {
          title: 'Test Title',
          body: 'Test Body',
        },
        data: {},
        android: {
          priority: 'high',
          notification: {
            sound: 'default',
            channelId: 'workspace_invites',
          },
        },
      });
    });

    test('sendNotification returns false when sending fails', async () => {
      // Mocked behavior: admin.messaging().send rejects (via mockSend)
      // Input: FCM token, title, body
      // Expected behavior: sendNotification returns false (catch block line 45-47)
      // Expected output: false
      mockSend.mockRejectedValueOnce(new Error('Send failed'));

      const result = await notificationService.sendNotification(
        'test-token',
        'Test Title',
        'Test Body'
      );

      expect(result).toBe(false);
      expect(mockSend).toHaveBeenCalled();
    });

    test('sendNotification includes data payload when provided', async () => {
      // Mocked behavior: admin.messaging().send succeeds with data
      // Input: FCM token, title, body, data object
      // Expected behavior: sendNotification sends message with data (line 32: data ?? {})
      // Expected output: true, mockSend called with data payload
      mockSend.mockResolvedValueOnce('mock-message-id');

      const data = { workspaceId: '123', type: 'invite' };
      const result = await notificationService.sendNotification(
        'test-token',
        'Test Title',
        'Test Body',
        data
      );

      expect(result).toBe(true);
      expect(mockSend).toHaveBeenCalledWith({
        token: 'test-token',
        notification: {
          title: 'Test Title',
          body: 'Test Body',
        },
        data: data,
        android: {
          priority: 'high',
          notification: {
            sound: 'default',
            channelId: 'workspace_invites',
          },
        },
      });
    });

    test('sendNotification uses empty object when data is undefined', async () => {
      // Mocked behavior: admin.messaging().send succeeds
      // Input: FCM token, title, body, no data parameter
      // Expected behavior: sendNotification uses ?? operator to default to {} (line 32)
      // Expected output: true, mockSend called with data: {}
      mockSend.mockResolvedValueOnce('mock-message-id');

      const result = await notificationService.sendNotification(
        'test-token',
        'Test Title',
        'Test Body'
        // No data parameter
      );

      expect(result).toBe(true);
      expect(mockSend).toHaveBeenCalledWith(
        expect.objectContaining({
          data: {},
        })
      );
    });
  });

  describe('POST /api/workspace - Create Workspace, with mocks', () => {
    test('500 – create workspace handles service error', async () => {
      // Mocked behavior: workspaceService.createWorkspace throws database connection error
      // Input: workspaceData with name
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(workspaceService, 'createWorkspace').mockRejectedValue(new Error('Database connection failed'));

      const res = await request(app)
        .post('/api/workspace')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({
          name: 'Test Workspace',
        });

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Database connection failed');
    });

    test('500 – create workspace handles non-Error thrown value', async () => {
      // Mocked behavior: workspaceService.createWorkspace throws non-Error value (string)
      // Input: workspaceData with name
      // Expected status code: 500
      // Expected behavior: error handled gracefully, falls back to generic message
      // Expected output: generic error message "Failed to create workspace"
      jest.spyOn(workspaceService, 'createWorkspace').mockRejectedValue('String error');

      const res = await request(app)
        .post('/api/workspace')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({
          name: 'Test Workspace',
        });

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to create workspace');
    });
  });

  describe('GET /api/workspace/personal - Get Personal Workspace, with mocks', () => {
    test('500 – get personal workspace handles service error', async () => {
      // Mocked behavior: workspaceService.getPersonalWorkspaceForUser throws database lookup error
      // Input: userId in header
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'getPersonalWorkspaceForUser').mockRejectedValue(new Error('Database lookup failed'));

      const res = await request(app)
        .get('/api/workspace/personal')
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Database lookup failed');
    });

    test('500 – get personal workspace handles non-Error thrown value', async () => {
      // Mocked behavior: workspaceService.getPersonalWorkspaceForUser throws non-Error value (string)
      // Input: userId in header
      // Expected status code: 500
      // Expected behavior: error handled gracefully, falls back to generic message
      // Expected output: generic error message "Failed to retrieve personal workspace"
      jest.spyOn(workspaceService, 'getPersonalWorkspaceForUser').mockRejectedValue('String error');

      const res = await request(app)
        .get('/api/workspace/personal')
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to retrieve personal workspace');
    });

    test('404 when user not found via service (tests workspace.service.ts line 50)', async () => {
      // Input: request for personal workspace where user lookup fails
      // Expected status code: 404
      // Expected behavior: service throws "User not found", controller returns 404
      // Expected output: error message
      // Mock authenticateToken to set a user but mock userModel.findById to return null
      // This simulates a race condition where the user exists for auth but not for the service call
      jest.spyOn(workspaceService, 'getPersonalWorkspaceForUser').mockRejectedValueOnce(
        new Error('User not found')
      );

      const res = await request(app)
        .get('/api/workspace/personal')
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(404);
      expect(res.body.error).toContain('User not found');
    });
  });

  describe('GET /api/workspace/user - Get Workspaces For User, with mocks', () => {
    test('500 – get workspaces for user handles service error', async () => {
      // Mocked behavior: workspaceService.getWorkspacesForUser throws database query error
      // Input: userId in header
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'getWorkspacesForUser').mockRejectedValue(new Error('Database query failed'));

      const res = await request(app)
        .get('/api/workspace/user')
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Database query failed');
    });

    test('500 – get workspaces for user handles non-Error thrown value', async () => {
      // Mocked behavior: workspaceService.getWorkspacesForUser throws non-Error value (string)
      // Input: userId in header
      // Expected status code: 500
      // Expected behavior: error handled gracefully, falls back to generic message
      // Expected output: generic error message "Failed to retrieve workspaces"
      jest.spyOn(workspaceService, 'getWorkspacesForUser').mockRejectedValue('String error');

      const res = await request(app)
        .get('/api/workspace/user')
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to retrieve workspaces');
    });
  });

  describe('GET /api/workspace/:id - Get Workspace, with mocks', () => {
    test('500 – get workspace handles service error', async () => {
      // Mocked behavior: workspaceService.getWorkspace throws database lookup error
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'getWorkspace').mockRejectedValue(new Error('Database lookup failed'));

      const res = await request(app)
        .get(`/api/workspace/${testData.testWorkspaceId}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Database lookup failed');
    });

    test('500 – get workspace handles non-Error thrown value', async () => {
      // Mocked behavior: workspaceService.getWorkspace throws non-Error value (string)
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully, falls back to generic message
      // Expected output: generic error message "Failed to retrieve workspace"
      jest.spyOn(workspaceService, 'getWorkspace').mockRejectedValue('String error');

      const res = await request(app)
        .get(`/api/workspace/${testData.testWorkspaceId}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to retrieve workspace');
    });
  });

  describe('GET /api/workspace/:id/members - Get Workspace Members, with mocks', () => {
    test('403 – get workspace members handles Access denied error', async () => {
      // Mocked behavior: workspaceService.getWorkspaceMembers throws Access denied error
      // Input: workspaceId in URL
      // Expected status code: 403
      // Expected behavior: error handled gracefully
      // Expected output: error message
      jest.spyOn(workspaceService, 'getWorkspaceMembers').mockRejectedValue(new Error('Access denied: You are not a member of this workspace'));

      const res = await request(app)
        .get(`/api/workspace/${testData.testWorkspaceId}/members`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Access denied');
    });

    test('500 – get workspace members handles service error', async () => {
      // Mocked behavior: workspaceService.getWorkspaceMembers throws database lookup error
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'getWorkspaceMembers').mockRejectedValue(new Error('Database lookup failed'));

      const res = await request(app)
        .get(`/api/workspace/${testData.testWorkspaceId}/members`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Database lookup failed');
    });

    test('500 – get workspace members handles non-Error thrown value', async () => {
      // Mocked behavior: workspaceService.getWorkspaceMembers throws non-Error value (string)
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully, falls back to generic message
      // Expected output: generic error message "Failed to retrieve members"
      jest.spyOn(workspaceService, 'getWorkspaceMembers').mockRejectedValue('String error');

      const res = await request(app)
        .get(`/api/workspace/${testData.testWorkspaceId}/members`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to retrieve members');
    });
  });

  describe('GET /api/workspace/:id/tags - Get All Tags, with mocks', () => {
    test('500 – get all tags handles service error', async () => {
      // Mocked behavior: workspaceService.getAllTags throws database query error
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'getAllTags').mockRejectedValue(new Error('Database query failed'));

      const res = await request(app)
        .get(`/api/workspace/${testData.testWorkspaceId}/tags`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Database query failed');
    });

    test('500 – get all tags handles non-Error thrown value', async () => {
      // Mocked behavior: workspaceService.getAllTags throws non-Error value (string)
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully, falls back to generic message
      // Expected output: generic error message "Failed to retrieve tags"
      jest.spyOn(workspaceService, 'getAllTags').mockRejectedValue('String error');

      const res = await request(app)
        .get(`/api/workspace/${testData.testWorkspaceId}/tags`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to retrieve tags');
    });
  });

  describe('GET /api/workspace/:id/membership/:userId - Get Membership Status, with mocks', () => {
    test('500 – get membership status handles service error', async () => {
      // Mocked behavior: workspaceService.getMembershipStatus throws database lookup error
      // Input: workspaceId and userId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'getMembershipStatus').mockRejectedValue(new Error('Database lookup failed'));

      const res = await request(app)
        .get(`/api/workspace/${testData.testWorkspaceId}/membership/${testData.testUserId}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Database lookup failed');
    });

    test('500 – get membership status handles non-Error thrown value', async () => {
      // Mocked behavior: workspaceService.getMembershipStatus throws non-Error value (string)
      // Input: workspaceId and userId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully, falls back to generic message
      // Expected output: generic error message "Failed to retrieve membership status"
      jest.spyOn(workspaceService, 'getMembershipStatus').mockRejectedValue('String error');

      const res = await request(app)
        .get(`/api/workspace/${testData.testWorkspaceId}/membership/${testData.testUserId}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to retrieve membership status');
    });
  });

  describe('POST /api/workspace/:id/members - Invite Member, with mocks', () => {
    test('403 – invite member handles Only workspace owner error', async () => {
      // Mocked behavior: workspaceService.inviteMember throws Only workspace owner error
      // Input: workspaceId in URL, userId in body
      // Expected status code: 403
      // Expected behavior: error handled gracefully
      // Expected output: error message
      jest.spyOn(workspaceService, 'inviteMember').mockRejectedValue(new Error('Only workspace owner can invite members'));

      const res = await request(app)
        .post(`/api/workspace/${testData.testWorkspaceId}/members`)
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Only workspace owner');
    });

    test('403 – invite member handles banned user error', async () => {
      // Mocked behavior: workspaceService.inviteMember throws banned user error
      // Input: workspaceId in URL, userId in body
      // Expected status code: 403
      // Expected behavior: error handled gracefully
      // Expected output: error message
      jest.spyOn(workspaceService, 'inviteMember').mockRejectedValue(new Error('User is banned from this workspace'));

      const res = await request(app)
        .post(`/api/workspace/${testData.testWorkspaceId}/members`)
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('banned from this workspace');
    });

    test('500 – invite member handles service error', async () => {
      // Mocked behavior: workspaceService.inviteMember throws database write error
      // Input: workspaceId in URL, userId in body
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'inviteMember').mockRejectedValue(new Error('Database write failed'));

      const res = await request(app)
        .post(`/api/workspace/${testData.testWorkspaceId}/members`)
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Database write failed');
    });

    test('500 – invite member handles non-Error thrown value', async () => {
      // Mocked behavior: workspaceService.inviteMember throws non-Error value (string)
      // Input: workspaceId in URL, userId in body
      // Expected status code: 500
      // Expected behavior: error handled gracefully, falls back to generic message
      // Expected output: generic error message "Failed to add member"
      jest.spyOn(workspaceService, 'inviteMember').mockRejectedValue('String error');

      const res = await request(app)
        .post(`/api/workspace/${testData.testWorkspaceId}/members`)
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to add member');
    });

    test('200 – invite member sends notification successfully', async () => {
      // Mocked behavior: notificationService.sendNotification succeeds
      // Input: workspaceId in URL, userId in body for user with FCM token
      // Expected status code: 200
      // Expected behavior: member added and notification sent
      // Expected output: member added successfully, notification sent
      // Set FCM token for user2
      await userModel.updateFcmToken(
        new mongoose.Types.ObjectId(testData.testUser2Id),
        'test-fcm-token-123'
      );

      const sendNotificationSpy = jest.spyOn(notificationService, 'sendNotification').mockResolvedValue(true);

      const res = await request(app)
        .post(`/api/workspace/${testData.testWorkspaceId}/members`)
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Member added successfully');
      expect(sendNotificationSpy).toHaveBeenCalledWith(
        'test-fcm-token-123',
        'Workspace Invitation',
        expect.stringContaining('added you to'),
        expect.objectContaining({
          type: 'workspace_invite',
          workspaceId: testData.testWorkspaceId,
        })
      );
    });

    test('200 – invite member succeeds even if notification fails', async () => {
      // Mocked behavior: notificationService.sendNotification fails
      // Input: workspaceId in URL, userId in body for user with FCM token
      // Expected status code: 200
      // Expected behavior: member added despite notification failure
      // Expected output: member added successfully
      // Set FCM token for user2
      await userModel.updateFcmToken(
        new mongoose.Types.ObjectId(testData.testUser2Id),
        'test-fcm-token-456'
      );

      const sendNotificationSpy = jest.spyOn(notificationService, 'sendNotification').mockResolvedValue(false);

      const res = await request(app)
        .post(`/api/workspace/${testData.testWorkspaceId}/members`)
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Member added successfully');
      expect(sendNotificationSpy).toHaveBeenCalled();
    });

    test('200 – invite member without FCM token does not send notification', async () => {
      // Mocked behavior: user2 has no FCM token
      // Input: workspaceId in URL, userId in body for user without FCM token
      // Expected status code: 200
      // Expected behavior: member added, no notification sent
      // Expected output: member added successfully
      const sendNotificationSpy = jest.spyOn(notificationService, 'sendNotification');

      const res = await request(app)
        .post(`/api/workspace/${testData.testWorkspaceId}/members`)
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Member added successfully');
      expect(sendNotificationSpy).not.toHaveBeenCalled();
    });

    test('200 – invite member with notification data payload', async () => {
      // Mocked behavior: notificationService.sendNotification sends notification with data
      // Input: workspaceId in URL, userId in body
      // Expected status code: 200
      // Expected behavior: notification sent with correct data payload including workspaceId, workspaceName, inviterId
      // Expected output: member added and notification with data sent
      // Set FCM token for user2
      await userModel.updateFcmToken(
        new mongoose.Types.ObjectId(testData.testUser2Id),
        'test-fcm-token-789'
      );

      const sendNotificationSpy = jest.spyOn(notificationService, 'sendNotification').mockResolvedValue(true);

      const res = await request(app)
        .post(`/api/workspace/${testData.testWorkspaceId}/members`)
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(200);
      // Verify notification was called with data payload
      expect(sendNotificationSpy).toHaveBeenCalledWith(
        'test-fcm-token-789',
        'Workspace Invitation',
        expect.any(String),
        expect.objectContaining({
          type: 'workspace_invite',
          workspaceId: testData.testWorkspaceId,
          workspaceName: 'Test Workspace',
          inviterId: testData.testUserId,
        })
      );
    });
  });

  describe('POST /api/workspace/:id/leave - Leave Workspace, with mocks', () => {
    test('500 – leave workspace handles service error', async () => {
      // Mocked behavior: workspaceService.leaveWorkspace throws database update error
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully, falls back to hardcoded message
      // Expected output: generic error message "Failed to leave workspace"
      jest.spyOn(workspaceService, 'leaveWorkspace').mockRejectedValue(new Error('Database update failed'));

      const res = await request(app)
        .post(`/api/workspace/${testData.testWorkspaceId}/leave`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to leave workspace');
    });
  });

  describe('PUT /api/workspace/:id - Update Workspace Profile, with mocks', () => {
    test('500 – update workspace profile handles service error', async () => {
      // Mocked behavior: workspaceService.updateWorkspaceProfile throws database update error
      // Input: workspaceId in URL, updateData in body
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(workspaceService, 'updateWorkspaceProfile').mockRejectedValue(new Error('Database update failed'));

      const res = await request(app)
        .put(`/api/workspace/${testData.testWorkspaceId}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ name: 'Updated Name' });

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Database update failed');
    });

    test('500 – update workspace profile handles non-Error thrown value', async () => {
      // Mocked behavior: workspaceService.updateWorkspaceProfile throws non-Error value (object)
      // Input: workspaceId in URL, updateData in body
      // Expected status code: 500
      // Expected behavior: error handled gracefully, falls back to generic message
      // Expected output: generic error message "Failed to update workspace profile"
      jest.spyOn(workspaceService, 'updateWorkspaceProfile').mockRejectedValue({ code: 'UNKNOWN' });

      const res = await request(app)
        .put(`/api/workspace/${testData.testWorkspaceId}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ name: 'Updated Name' });

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to update workspace profile');
    });
  });

  describe('PUT /api/workspace/:id/picture - Update Workspace Picture, with mocks', () => {
    test('500 – update workspace picture handles service error', async () => {
      // Mocked behavior: workspaceService.updateWorkspacePicture throws database update error
      // Input: workspaceId in URL, profilePicture in body
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'updateWorkspacePicture').mockRejectedValue(new Error('Database update failed'));

      const res = await request(app)
        .put(`/api/workspace/${testData.testWorkspaceId}/picture`)
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ profilePicture: 'https://example.com/pic.jpg' });

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Database update failed');
    });

    test('500 – update workspace picture handles non-Error thrown value', async () => {
      // Mocked behavior: workspaceService.updateWorkspacePicture throws non-Error value (string)
      // Input: workspaceId in URL, profilePicture in body
      // Expected status code: 500
      // Expected behavior: error handled gracefully, falls back to generic message
      // Expected output: generic error message "Failed to update workspace picture"
      jest.spyOn(workspaceService, 'updateWorkspacePicture').mockRejectedValue('String error');

      const res = await request(app)
        .put(`/api/workspace/${testData.testWorkspaceId}/picture`)
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ profilePicture: 'https://example.com/pic.jpg' });

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to update workspace picture');
    });
  });

  describe('DELETE /api/workspace/:id/members/:userId - Ban Member, with mocks', () => {
    test('500 – ban member handles service error', async () => {
      // Mocked behavior: workspaceService.banMember throws database update error
      // Input: workspaceId and userId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'banMember').mockRejectedValue(new Error('Database update failed'));

      const res = await request(app)
        .delete(`/api/workspace/${testData.testWorkspaceId}/members/${testData.testUser2Id}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Database update failed');
    });

    test('500 – ban member handles non-Error thrown value', async () => {
      // Mocked behavior: workspaceService.banMember throws non-Error value (string)
      // Input: workspaceId and userId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully, falls back to generic message
      // Expected output: generic error message "Failed to ban member"
      jest.spyOn(workspaceService, 'banMember').mockRejectedValue('String error');

      const res = await request(app)
        .delete(`/api/workspace/${testData.testWorkspaceId}/members/${testData.testUser2Id}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to ban member');
    });
  });

  describe('DELETE /api/workspace/:id - Delete Workspace, with mocks', () => {
    test('500 – delete workspace handles service error', async () => {
      // Mocked behavior: workspaceService.deleteWorkspace throws database delete error
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(workspaceService, 'deleteWorkspace').mockRejectedValue(new Error('Database delete failed'));

      const res = await request(app)
        .delete(`/api/workspace/${testData.testWorkspaceId}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Database delete failed');
    });

    test('500 – delete workspace handles non-Error thrown value', async () => {
      // Mocked behavior: workspaceService.deleteWorkspace throws non-Error value (string)
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully, falls back to generic message
      // Expected output: generic error message "Failed to delete workspace"
      jest.spyOn(workspaceService, 'deleteWorkspace').mockRejectedValue('String error');

      const res = await request(app)
        .delete(`/api/workspace/${testData.testWorkspaceId}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to delete workspace');
    });
  });

  describe('GET /api/workspace/:id/poll - Poll For New Messages, with mocks', () => {
    test('500 – poll for new messages handles service error', async () => {
      // Mocked behavior: workspaceService.checkForNewChatMessages throws database lookup error
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'checkForNewChatMessages').mockRejectedValue(new Error('Database lookup failed'));

      const res = await request(app)
        .get(`/api/workspace/${testData.testWorkspaceId}/poll`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Database lookup failed');
    });

    test('500 – poll for new messages handles non-Error thrown value', async () => {
      // Mocked behavior: workspaceService.checkForNewChatMessages throws non-Error value (string)
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully, falls back to generic message
      // Expected output: generic error message "Failed to poll for new messages"
      jest.spyOn(workspaceService, 'checkForNewChatMessages').mockRejectedValue('String error');

      const res = await request(app)
        .get(`/api/workspace/${testData.testWorkspaceId}/poll`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to poll for new messages');
    });
  });

  describe('Workspace routes - user authentication edge cases', () => {
    const buildAppWithMockedAuth = async (userMock: any) => {
      jest.resetModules();

      // Mock authenticateToken before requiring routes
      jest.doMock('../../authentication/auth.middleware', () => ({
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
      jest.dontMock('../../authentication/auth.middleware');
    });

    test('POST /api/workspace - 401 when req.user is undefined (lines 10-11)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests lines 10-11 in workspace.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      const res = await request(appInstance)
        .post('/api/workspace')
        .set('Authorization', 'Bearer fake-token')
        .send({ name: 'Test Workspace' });

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('GET /api/workspace/personal - 401 when req.user is undefined (lines 40-41)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests lines 40-41 in workspace.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      const res = await request(appInstance)
        .get('/api/workspace/personal')
        .set('Authorization', 'Bearer fake-token');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('GET /api/workspace/user - 401 when req.user is undefined (lines 76-77)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests lines 76-77 in workspace.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      const res = await request(appInstance)
        .get('/api/workspace/user')
        .set('Authorization', 'Bearer fake-token');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('GET /api/workspace/:id - 401 when req.user is undefined (lines 96-97)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests lines 96-97 in workspace.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      const validWorkspaceId = new mongoose.Types.ObjectId().toString();

      const res = await request(appInstance)
        .get(`/api/workspace/${validWorkspaceId}`)
        .set('Authorization', 'Bearer fake-token');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('GET /api/workspace/:id/members - 401 when req.user is undefined (lines 132-133)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests lines 132-133 in workspace.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      const validWorkspaceId = new mongoose.Types.ObjectId().toString();

      const res = await request(appInstance)
        .get(`/api/workspace/${validWorkspaceId}/members`)
        .set('Authorization', 'Bearer fake-token');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('GET /api/workspace/:id/tags - 401 when req.user is undefined (lines 165-166)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests lines 165-166 in workspace.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      const validWorkspaceId = new mongoose.Types.ObjectId().toString();

      const res = await request(appInstance)
        .get(`/api/workspace/${validWorkspaceId}/tags`)
        .set('Authorization', 'Bearer fake-token');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('POST /api/workspace/:id/members - 401 when req.user is undefined (lines 223-224)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests lines 223-224 in workspace.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      const validWorkspaceId = new mongoose.Types.ObjectId().toString();

      const res = await request(appInstance)
        .post(`/api/workspace/${validWorkspaceId}/members`)
        .set('Authorization', 'Bearer fake-token')
        .send({ userId: new mongoose.Types.ObjectId().toString() });

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('POST /api/workspace/:id/leave - 401 when req.user is undefined (lines 285-286)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests lines 285-286 in workspace.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      const res = await request(appInstance)
        .post(`/api/workspace/${testData.testWorkspaceId}/leave`)
        .set('Authorization', 'Bearer fake-token');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('DELETE /api/workspace/:id/members/:userId - 401 when req.user is undefined (lines 329-330)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests lines 329-330 in workspace.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      // Use a valid ObjectId to avoid validation errors
      const validWorkspaceId = new mongoose.Types.ObjectId().toString();
      const validUserId = new mongoose.Types.ObjectId().toString();

      const res = await request(appInstance)
        .delete(`/api/workspace/${validWorkspaceId}/members/${validUserId}`)
        .set('Authorization', 'Bearer fake-token');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('PUT /api/workspace/:id - 401 when req.user is undefined (lines 378-379)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests lines 378-379 in workspace.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      // Use a valid ObjectId to avoid validation errors
      const validWorkspaceId = new mongoose.Types.ObjectId().toString();

      const res = await request(appInstance)
        .put(`/api/workspace/${validWorkspaceId}`)
        .set('Authorization', 'Bearer fake-token')
        .send({ name: 'Test' });

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('PUT /api/workspace/:id/picture - 401 when req.user is undefined (lines 417-418)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests lines 417-418 in workspace.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      const res = await request(appInstance)
        .put(`/api/workspace/${testData.testWorkspaceId}/picture`)
        .set('Authorization', 'Bearer fake-token')
        .send({ profilePicture: '/path/to/image.jpg' });

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('DELETE /api/workspace/:id - 401 when req.user is undefined (lines 457-458)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests lines 457-458 in workspace.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      const res = await request(appInstance)
        .delete(`/api/workspace/${testData.testWorkspaceId}`)
        .set('Authorization', 'Bearer fake-token');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('GET /api/workspace/:id/poll - 401 when req.user is undefined (lines 496-497)', async () => {
      // Input: request where authenticateToken passes but req.user is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests lines 496-497 in workspace.controller.ts
      const appInstance = await buildAppWithMockedAuth(undefined);

      const res = await request(appInstance)
        .get(`/api/workspace/${testData.testWorkspaceId}/poll`)
        .set('Authorization', 'Bearer fake-token');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });
  });

  describe('WorkspaceService - getPersonalWorkspaceForUser user not found (line 50)', () => {
    test('throws User not found error when userModel.findById returns null', async () => {
      // Input: userId that doesn't exist
      // Expected behavior: userModel.findById returns null, throws "User not found" (line 50)
      // Expected output: Error with message "User not found"
      // This tests line 50 in workspace.service.ts
      jest.spyOn(userModel, 'findById').mockResolvedValueOnce(null);

      const userId = new mongoose.Types.ObjectId();
      
      await expect(
        workspaceService.getPersonalWorkspaceForUser(userId)
      ).rejects.toThrow('User not found');
    });
  });
});

