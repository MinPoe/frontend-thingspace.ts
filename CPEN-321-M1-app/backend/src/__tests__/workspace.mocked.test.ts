/// <reference types="jest" />
import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';

import { workspaceService } from '../workspace.service';
import { workspaceModel } from '../workspace.model';
import { createTestApp, setupTestDatabase, TestData } from './test-helpers';

const app = createTestApp();

// ---------------------------
// Test suite
// ---------------------------
describe('Workspace API – Mocked Tests (Jest Mocks)', () => {
  let mongo: MongoMemoryServer;
  let testData: TestData;

  // Spin up in-memory Mongo
  beforeAll(async () => {
    mongo = await MongoMemoryServer.create();
    const uri = mongo.getUri();
    await mongoose.connect(uri);
    console.log('✅ Connected to in-memory MongoDB');
  });

  // Clean mocks every test; full DB reset occurs in beforeEach
  afterEach(() => {
    jest.restoreAllMocks();
    jest.clearAllMocks();
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

  describe('Mocked – Database/Service failures', () => {
    test('500 – create workspace handles service error', async () => {
      // Mocked behavior: workspaceService.createWorkspace throws database connection error
      // Input: workspaceData with name
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(workspaceService, 'createWorkspace').mockRejectedValue(new Error('Database connection failed'));

      const res = await request(app)
        .post('/api/workspaces')
        .set('x-test-user-id', testData.testUserId)
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
        .post('/api/workspaces')
        .set('x-test-user-id', testData.testUserId)
        .send({
          name: 'Test Workspace',
        });

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to create workspace');
    });

    test('500 – get personal workspace handles service error', async () => {
      // Mocked behavior: workspaceService.getPersonalWorkspaceForUser throws database lookup error
      // Input: userId in header
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'getPersonalWorkspaceForUser').mockRejectedValue(new Error('Database lookup failed'));

      const res = await request(app)
        .get('/api/workspaces/personal')
        .set('x-test-user-id', testData.testUserId);

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
        .get('/api/workspaces/personal')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to retrieve personal workspace');
    });

    test('500 – get workspaces for user handles service error', async () => {
      // Mocked behavior: workspaceService.getWorkspacesForUser throws database query error
      // Input: userId in header
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'getWorkspacesForUser').mockRejectedValue(new Error('Database query failed'));

      const res = await request(app)
        .get('/api/workspaces/user')
        .set('x-test-user-id', testData.testUserId);

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
        .get('/api/workspaces/user')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to retrieve workspaces');
    });

    test('500 – get workspace handles service error', async () => {
      // Mocked behavior: workspaceService.getWorkspace throws database lookup error
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'getWorkspace').mockRejectedValue(new Error('Database lookup failed'));

      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspaceId}`)
        .set('x-test-user-id', testData.testUserId);

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
        .get(`/api/workspaces/${testData.testWorkspaceId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to retrieve workspace');
    });

    test('403 – get workspace members handles Access denied error', async () => {
      // Mocked behavior: workspaceService.getWorkspaceMembers throws Access denied error
      // Input: workspaceId in URL
      // Expected status code: 403
      // Expected behavior: error handled gracefully
      // Expected output: error message
      jest.spyOn(workspaceService, 'getWorkspaceMembers').mockRejectedValue(new Error('Access denied: You are not a member of this workspace'));

      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId);

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
        .get(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId);

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
        .get(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to retrieve members');
    });

    test('500 – get all tags handles service error', async () => {
      // Mocked behavior: workspaceService.getAllTags throws database query error
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'getAllTags').mockRejectedValue(new Error('Database query failed'));

      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspaceId}/tags`)
        .set('x-test-user-id', testData.testUserId);

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
        .get(`/api/workspaces/${testData.testWorkspaceId}/tags`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to retrieve tags');
    });

    test('500 – get membership status handles service error', async () => {
      // Mocked behavior: workspaceService.getMembershipStatus throws database lookup error
      // Input: workspaceId and userId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'getMembershipStatus').mockRejectedValue(new Error('Database lookup failed'));

      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspaceId}/membership/${testData.testUserId}`)
        .set('x-test-user-id', testData.testUserId);

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
        .get(`/api/workspaces/${testData.testWorkspaceId}/membership/${testData.testUserId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to retrieve membership status');
    });

    test('403 – invite member handles Only workspace owner error', async () => {
      // Mocked behavior: workspaceService.inviteMember throws Only workspace owner error
      // Input: workspaceId in URL, userId in body
      // Expected status code: 403
      // Expected behavior: error handled gracefully
      // Expected output: error message
      jest.spyOn(workspaceService, 'inviteMember').mockRejectedValue(new Error('Only workspace owner can invite members'));

      const res = await request(app)
        .post(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId)
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
        .post(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId)
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
        .post(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId)
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
        .post(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to add member');
    });

    test('500 – leave workspace handles service error', async () => {
      // Mocked behavior: workspaceService.leaveWorkspace throws database update error
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully, falls back to hardcoded message
      // Expected output: generic error message "Failed to leave workspace"
      jest.spyOn(workspaceService, 'leaveWorkspace').mockRejectedValue(new Error('Database update failed'));

      const res = await request(app)
        .post(`/api/workspaces/${testData.testWorkspaceId}/leave`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to leave workspace');
    });

    test('500 – update workspace profile handles service error', async () => {
      // Mocked behavior: workspaceService.updateWorkspaceProfile throws database update error
      // Input: workspaceId in URL, updateData in body
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(workspaceService, 'updateWorkspaceProfile').mockRejectedValue(new Error('Database update failed'));

      const res = await request(app)
        .put(`/api/workspaces/${testData.testWorkspaceId}`)
        .set('x-test-user-id', testData.testUserId)
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
        .put(`/api/workspaces/${testData.testWorkspaceId}`)
        .set('x-test-user-id', testData.testUserId)
        .send({ name: 'Updated Name' });

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to update workspace profile');
    });

    test('500 – update workspace picture handles service error', async () => {
      // Mocked behavior: workspaceService.updateWorkspacePicture throws database update error
      // Input: workspaceId in URL, profilePicture in body
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'updateWorkspacePicture').mockRejectedValue(new Error('Database update failed'));

      const res = await request(app)
        .put(`/api/workspaces/${testData.testWorkspaceId}/picture`)
        .set('x-test-user-id', testData.testUserId)
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
        .put(`/api/workspaces/${testData.testWorkspaceId}/picture`)
        .set('x-test-user-id', testData.testUserId)
        .send({ profilePicture: 'https://example.com/pic.jpg' });

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to update workspace picture');
    });

    test('500 – ban member handles service error', async () => {
      // Mocked behavior: workspaceService.banMember throws database update error
      // Input: workspaceId and userId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'banMember').mockRejectedValue(new Error('Database update failed'));

      const res = await request(app)
        .delete(`/api/workspaces/${testData.testWorkspaceId}/members/${testData.testUser2Id}`)
        .set('x-test-user-id', testData.testUserId);

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
        .delete(`/api/workspaces/${testData.testWorkspaceId}/members/${testData.testUser2Id}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to ban member');
    });

    test('500 – delete workspace handles service error', async () => {
      // Mocked behavior: workspaceService.deleteWorkspace throws database delete error
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message from Error
      jest.spyOn(workspaceService, 'deleteWorkspace').mockRejectedValue(new Error('Database delete failed'));

      const res = await request(app)
        .delete(`/api/workspaces/${testData.testWorkspaceId}`)
        .set('x-test-user-id', testData.testUserId);

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
        .delete(`/api/workspaces/${testData.testWorkspaceId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to delete workspace');
    });

    test('500 – poll for new messages handles service error', async () => {
      // Mocked behavior: workspaceService.checkForNewChatMessages throws database lookup error
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: None
      jest.spyOn(workspaceService, 'checkForNewChatMessages').mockRejectedValue(new Error('Database lookup failed'));

      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspaceId}/poll`)
        .set('x-test-user-id', testData.testUserId);

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
        .get(`/api/workspaces/${testData.testWorkspaceId}/poll`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to poll for new messages');
    });
  });
});

