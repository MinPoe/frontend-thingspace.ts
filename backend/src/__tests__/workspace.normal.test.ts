/// <reference types="jest" />
import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';

import { workspaceModel } from '../workspace.model';
import { userModel } from '../user.model';
import { noteModel } from '../note.model';
import { NoteType } from '../notes.types';
import { notificationService } from '../notification.service';
import { createTestApp, setupTestDatabase, TestData } from './test-helpers';

const app = createTestApp();

// ---------------------------
// Test suite
// ---------------------------
describe('Workspace API – Normal Tests (No Mocking)', () => {
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

  describe('POST /api/workspaces - Create Workspace', () => {
    test('201 – creates a workspace', async () => {
      // Input: workspaceData with name, optional description and profilePicture
      // Expected status code: 201
      // Expected behavior: workspace is created in database
      // Expected output: workspaceId of the created workspace
      const workspaceData = {
        name: 'My New Workspace',
        description: 'A test workspace',
        profilePicture: 'https://example.com/image.jpg',
      };

      const res = await request(app)
        .post('/api/workspaces')
        .set('x-test-user-id', testData.testUserId)
        .send(workspaceData);

      expect(res.status).toBe(201);
      expect(res.body.message).toBe('Workspace created successfully');
      expect(res.body.data.workspaceId).toBeDefined();
    });

    test('201 – creates workspace with minimal data', async () => {
      // Input: workspaceData with only name
      // Expected status code: 201
      // Expected behavior: workspace is created with default values
      // Expected output: workspaceId of the created workspace
      const workspaceData = {
        name: 'Minimal Workspace',
      };

      const res = await request(app)
        .post('/api/workspaces')
        .set('x-test-user-id', testData.testUserId)
        .send(workspaceData);

      expect(res.status).toBe(201);
      expect(res.body.message).toBe('Workspace created successfully');
      expect(res.body.data.workspaceId).toBeDefined();
    });

    test('409 – workspace name already in use', async () => {
      // Input: workspaceData with name that already exists for this user
      // Expected status code: 409
      // Expected behavior: error message returned
      // Expected output: error message
      const workspaceData = {
        name: 'Test Workspace', // Same name as testData.testWorkspaceId
      };

      const res = await request(app)
        .post('/api/workspaces')
        .set('x-test-user-id', testData.testUserId)
        .send(workspaceData);

      expect(res.status).toBe(409);
      expect(res.body.error).toBe('Workspace name already in use');
    });
  });

  describe('GET /api/workspaces/personal - Get Personal Workspace', () => {
    test('200 – retrieves personal workspace successfully', async () => {
      // Input: userId with personal workspace
      // Expected status code: 200
      // Expected behavior: workspace is retrieved
      // Expected output: workspace data
      
      // Create a personal workspace for the test user
      const personalWorkspace = await workspaceModel.create({
        name: 'Personal Workspace',
        profile: { imagePath: '', name: 'Personal Workspace', description: 'My personal workspace' },
        ownerId: new mongoose.Types.ObjectId(testData.testUserId),
        members: [new mongoose.Types.ObjectId(testData.testUserId)],
      });
      
      // Update user to have this personal workspace
      await userModel.updatePersonalWorkspace(
        new mongoose.Types.ObjectId(testData.testUserId),
        personalWorkspace._id
      );

      const res = await request(app)
        .get('/api/workspaces/personal')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Personal workspace retrieved successfully');
      expect(res.body.data.workspace).toBeDefined();
      expect(res.body.data.workspace._id).toBe(personalWorkspace._id.toString());
      expect(res.body.data.workspace.name).toBe('Personal Workspace');
      expect(res.body.data.workspace.ownerId).toBe(testData.testUserId);
    });

    test('404 – personal workspace ID exists but workspace not found', async () => {
      // Input: userId with personalWorkspaceId pointing to non-existent workspace
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: "Personal workspace not found" error
      const fakeWorkspaceId = new mongoose.Types.ObjectId();
      
      // Set user's personalWorkspaceId to a non-existent workspace
      await userModel.updatePersonalWorkspace(
        new mongoose.Types.ObjectId(testData.testUserId),
        fakeWorkspaceId
      );

      const res = await request(app)
        .get('/api/workspaces/personal')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(404);
      expect(res.body.error).toContain('Personal workspace not found');
    });

    test('404 – user does not have personal workspace', async () => {
      // Input: userId without personal workspace
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const res = await request(app)
        .get('/api/workspaces/personal')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(404);
      expect(res.body.error).toContain('personal workspace');
    });

    test('404 – user not found', async () => {
      // Input: fake userId
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeUserId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .get('/api/workspaces/personal')
        .set('x-test-user-id', fakeUserId);

      expect(res.status).toBe(404);
      expect(res.body.error).toBeDefined();
    });
  });

  describe('GET /api/workspaces/user - Get Workspaces For User', () => {
    test('200 – retrieves all workspaces for user', async () => {
      // Input: userId in header
      // Expected status code: 200
      // Expected behavior: list of workspaces retrieved
      // Expected output: array of workspaces
      const res = await request(app)
        .get('/api/workspaces/user')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Workspaces retrieved successfully');
      expect(res.body.data.workspaces).toBeDefined();
      expect(Array.isArray(res.body.data.workspaces)).toBe(true);
    });

    test('200 – excludes personal workspace from results', async () => {
      // Input: userId with personal workspace
      // Expected status code: 200
      // Expected behavior: personal workspace excluded from results
      // Expected output: array of workspaces without personal workspace
      
      // Create a personal workspace
      const personalWorkspace = await workspaceModel.create({
        name: 'Personal Workspace',
        profile: { imagePath: '', name: 'Personal Workspace', description: 'My personal workspace' },
        ownerId: new mongoose.Types.ObjectId(testData.testUserId),
        members: [new mongoose.Types.ObjectId(testData.testUserId)],
      });
      
      // Update user to have this personal workspace
      await userModel.updatePersonalWorkspace(
        new mongoose.Types.ObjectId(testData.testUserId),
        personalWorkspace._id
      );

      const res = await request(app)
        .get('/api/workspaces/user')
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.data.workspaces).toBeDefined();
      // Personal workspace should be excluded
      const workspaceIds = res.body.data.workspaces.map((w: any) => w._id);
      expect(workspaceIds).not.toContain(personalWorkspace._id.toString());
    });
  });

  describe('GET /api/workspaces/:id - Get Workspace', () => {
    test('200 – retrieves workspace when user is a member', async () => {
      // Input: workspaceId in URL
      // Expected status code: 200
      // Expected behavior: workspace details retrieved
      // Expected output: workspace object
      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspaceId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Workspace retrieved successfully');
      expect(res.body.data.workspace).toBeDefined();
      expect(res.body.data.workspace._id).toBe(testData.testWorkspaceId);
    });

    test('403 – cannot access workspace when not a member', async () => {
      // Input: workspaceId of workspace user is not a member of
      // Expected status code: 403
      // Expected behavior: error message returned
      // Expected output: error message
      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspace2Id}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Access denied');
    });

    test('404 – workspace not found', async () => {
      // Input: fake workspaceId
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeWorkspaceId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .get(`/api/workspaces/${fakeWorkspaceId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Workspace not found');
    });
  });

  describe('GET /api/workspaces/:id/members - Get Workspace Members', () => {
    test('200 – retrieves workspace members', async () => {
      // Input: workspaceId in URL
      // Expected status code: 200
      // Expected behavior: list of members retrieved
      // Expected output: array of user objects
      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Members retrieved successfully');
      expect(res.body.data.members).toBeDefined();
      expect(Array.isArray(res.body.data.members)).toBe(true);
    });

    test('404 – workspace not found', async () => {
      // Input: fake workspaceId
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeWorkspaceId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .get(`/api/workspaces/${fakeWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Workspace not found');
    });
  });

  describe('GET /api/workspaces/:id/tags - Get All Tags', () => {
    beforeEach(async () => {
      // Create notes with tags
      await noteModel.create({
        userId: new mongoose.Types.ObjectId(testData.testUserId),
        workspaceId: testData.testWorkspaceId,
        noteType: NoteType.CONTENT,
        tags: ['tag1', 'tag2'],
        fields: [{ fieldType: 'title', content: 'Note 1', _id: '1' }],
      });

      await noteModel.create({
        userId: new mongoose.Types.ObjectId(testData.testUserId),
        workspaceId: testData.testWorkspaceId,
        noteType: NoteType.CONTENT,
        tags: ['tag2', 'tag3'],
        fields: [{ fieldType: 'title', content: 'Note 2', _id: '2' }],
      });
    });

    test('200 – retrieves all unique tags in workspace', async () => {
      // Input: workspaceId in URL
      // Expected status code: 200
      // Expected behavior: list of unique tags retrieved
      // Expected output: array of unique tag strings
      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspaceId}/tags`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Tags retrieved successfully');
      expect(res.body.data.tags).toBeDefined();
      expect(Array.isArray(res.body.data.tags)).toBe(true);
      expect(res.body.data.tags.length).toBeGreaterThanOrEqual(3); // tag1, tag2, tag3
      // Verify flatMap is executed by checking tags are extracted from multiple notes
      expect(res.body.data.tags).toContain('tag1');
      expect(res.body.data.tags).toContain('tag2');
      expect(res.body.data.tags).toContain('tag3');
    });



    test('403 – cannot access tags when not a member', async () => {
      // Input: workspaceId of workspace user is not a member of
      // Expected status code: 403
      // Expected behavior: error message returned
      // Expected output: error message
      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspace2Id}/tags`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Access denied');
    });

    test('404 – workspace not found', async () => {
      // Input: fake workspaceId
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeWorkspaceId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .get(`/api/workspaces/${fakeWorkspaceId}/tags`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Workspace not found');
    });
  });

  describe('GET /api/workspaces/:id/membership/:userId - Get Membership Status', () => {
    test('200 – returns OWNER status', async () => {
      // Input: workspaceId and userId where user is owner
      // Expected status code: 200
      // Expected behavior: membership status retrieved
      // Expected output: status object with OWNER
      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspaceId}/membership/${testData.testUserId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Membership status retrieved successfully');
      expect(res.body.data.status).toBe('OWNER');
    });

    test('200 – returns MEMBER status', async () => {
      // Input: workspaceId and userId where user is a member (not owner)
      // Expected status code: 200
      // Expected behavior: membership status retrieved
      // Expected output: status object with MEMBER
      
      // Add testUserId as a member to testWorkspace2 (owned by testUser2Id)
      await workspaceModel.findByIdAndUpdate(
        testData.testWorkspace2Id,
        { $addToSet: { members: new mongoose.Types.ObjectId(testData.testUserId) } }
      );

      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspace2Id}/membership/${testData.testUserId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.data.status).toBe('MEMBER');
    });

    test('200 – returns BANNED status', async () => {
      // Input: workspaceId and userId where user is banned
      // Expected status code: 200
      // Expected behavior: membership status retrieved
      // Expected output: status object with BANNED
      
      // Ban testUser2Id from testWorkspaceId
      await workspaceModel.findByIdAndUpdate(
        testData.testWorkspaceId,
        { $addToSet: { bannedMembers: new mongoose.Types.ObjectId(testData.testUser2Id) } }
      );

      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspaceId}/membership/${testData.testUser2Id}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.data.status).toBe('BANNED');
    });

    test('200 – returns NOT_MEMBER status', async () => {
      // Input: workspaceId and userId where user is not a member
      // Expected status code: 200
      // Expected behavior: membership status retrieved
      // Expected output: status object with NOT_MEMBER
      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspace2Id}/membership/${testData.testUserId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.data.status).toBe('NOT_MEMBER');
    });

    test('404 – workspace not found', async () => {
      // Input: fake workspaceId
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeWorkspaceId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .get(`/api/workspaces/${fakeWorkspaceId}/membership/${testData.testUserId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Workspace not found');
    });
  });

  describe('POST /api/workspaces/:id/members - Invite Member', () => {
    test('200 – adds member to workspace', async () => {
      // Input: workspaceId in URL, userId in body
      // Expected status code: 200
      // Expected behavior: member added to workspace
      // Expected output: updated workspace object
      const res = await request(app)
        .post(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Member added successfully');
      expect(res.body.data.workspace).toBeDefined();
      expect(res.body.data.workspace.members).toContain(testData.testUser2Id);
    });

    test('400 – missing userId', async () => {
      // Input: workspaceId in URL, no userId in body
      // Expected status code: 400
      // Expected behavior: validation error
      // Expected output: error message
      const res = await request(app)
        .post(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId)
        .send({});

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('userId is required');
    });

    test('400 – user already a member', async () => {
      // Input: workspaceId and userId where user is already a member
      // Expected status code: 400
      // Expected behavior: error message returned
      // Expected output: error message
      const res = await request(app)
        .post(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId)
        .send({ userId: testData.testUserId }); // Adding owner again

      expect(res.status).toBe(400);
      expect(res.body.error).toContain('already a member');
    });

    test('404 – workspace not found', async () => {
      // Input: fake workspaceId
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeWorkspaceId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .post(`/api/workspaces/${fakeWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Workspace not found');
    });

    test('404 – user to add not found', async () => {
      // Input: workspaceId and fake userId
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeUserId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .post(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId)
        .send({ userId: fakeUserId });

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('User to add not found');
    });

    test('403 – cannot invite when not a member', async () => {
      // Input: workspaceId where requesting user is not a member
      // Expected status code: 403
      // Expected behavior: error message returned
      // Expected output: error message
      
      // testUserId is not a member of testWorkspace2Id
      const res = await request(app)
        .post(`/api/workspaces/${testData.testWorkspace2Id}/members`)
        .set('x-test-user-id', testData.testUserId)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Access denied');
      expect(res.body.error).toContain('not a member of this workspace');
    });

    test('403 – cannot invite members to personal workspace', async () => {
      // Input: workspaceId of a personal workspace
      // Expected status code: 403
      // Expected behavior: error message returned
      // Expected output: error message
      
      // Create a personal workspace
      const personalWorkspace = await workspaceModel.create({
        name: 'Personal Workspace',
        profile: { imagePath: '', name: 'Personal Workspace', description: '' },
        ownerId: new mongoose.Types.ObjectId(testData.testUserId),
        members: [new mongoose.Types.ObjectId(testData.testUserId)],
      });
      
      // Set it as the user's personal workspace
      await userModel.updatePersonalWorkspace(
        new mongoose.Types.ObjectId(testData.testUserId),
        personalWorkspace._id
      );

      const res = await request(app)
        .post(`/api/workspaces/${personalWorkspace._id.toString()}/members`)
        .set('x-test-user-id', testData.testUserId)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Cannot invite members to personal workspace');
    });

    test('403 – cannot invite banned user', async () => {
      // Input: workspaceId and userId of a banned user
      // Expected status code: 403
      // Expected behavior: error message returned
      // Expected output: error message
      // First ban the user
      await workspaceModel.findByIdAndUpdate(testData.testWorkspaceId, {
        $push: { bannedMembers: new mongoose.Types.ObjectId(testData.testUser2Id) },
      });

      const res = await request(app)
        .post(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('banned from this workspace');
    });

    test('200 – sends notification when user has fcmToken', async () => {
      // Input: workspaceId, userId with fcmToken set
      // Expected status code: 200
      // Expected behavior: member added, notification attempted (covers notification sending branch)
      // Expected output: updated workspace object
      
      // Set fcmToken for testUser2Id
      await userModel.updateFcmToken(
        new mongoose.Types.ObjectId(testData.testUser2Id),
        'test-fcm-token-123'
      );

      const res = await request(app)
        .post(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Member added successfully');
      // Note: Notification may succeed or fail depending on Firebase setup, but the code path is covered
    });

    test('200 – skips notification when user has no fcmToken', async () => {
      // Input: workspaceId, userId without fcmToken
      // Expected status code: 200
      // Expected behavior: member added, notification skipped (covers else branch)
      // Expected output: updated workspace object
      
      // Ensure testUser2Id has no fcmToken (should be null/undefined by default)
      const user = await userModel.findById(new mongoose.Types.ObjectId(testData.testUser2Id));
      expect(user?.fcmToken).toBeUndefined();

      const res = await request(app)
        .post(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Member added successfully');
      // This covers the else branch where fcmToken is missing
    });

    test('200 – handles notification error gracefully (covers catch block)', async () => {
      // Input: workspaceId, userId with fcmToken, but notification service throws
      // Expected status code: 200
      // Expected behavior: member added despite notification failure (covers catch block)
      // Expected output: updated workspace object
      
      // Set fcmToken for testUser2Id
      await userModel.updateFcmToken(
        new mongoose.Types.ObjectId(testData.testUser2Id),
        'test-fcm-token-456'
      );

      // Mock notification service to throw an error to trigger the catch block
      const sendNotificationSpy = jest.spyOn(notificationService, 'sendNotification')
        .mockRejectedValueOnce(new Error('Notification service error'));

      const res = await request(app)
        .post(`/api/workspaces/${testData.testWorkspaceId}/members`)
        .set('x-test-user-id', testData.testUserId)
        .send({ userId: testData.testUser2Id });

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Member added successfully');
      expect(res.body.data.workspace.members).toContain(testData.testUser2Id);
      // Verify notification was attempted (covers the catch block)
      expect(sendNotificationSpy).toHaveBeenCalledTimes(1);

      // Restore the spy
      sendNotificationSpy.mockRestore();
    });
  });

  describe('POST /api/workspaces/:id/leave - Leave Workspace', () => {
    beforeEach(async () => {
      // Add testUser2 as a member first
      await workspaceModel.findByIdAndUpdate(testData.testWorkspaceId, {
        $push: { members: new mongoose.Types.ObjectId(testData.testUser2Id) },
      });
    });

    test('200 – user leaves workspace', async () => {
      // Input: workspaceId in URL
      // Expected status code: 200
      // Expected behavior: user removed from workspace members
      // Expected output: updated workspace object
      const res = await request(app)
        .post(`/api/workspaces/${testData.testWorkspaceId}/leave`)
        .set('x-test-user-id', testData.testUser2Id);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Successfully left the workspace');
      expect(res.body.data.workspace).toBeDefined();
      expect(res.body.data.workspace.members).not.toContain(testData.testUser2Id);
    });

    test('403 – cannot leave personal workspace', async () => {
      // Input: workspaceId of a personal workspace
      // Expected status code: 403
      // Expected behavior: error message returned
      // Expected output: error message
      
      // Create a personal workspace
      const personalWorkspace = await workspaceModel.create({
        name: 'Personal Workspace',
        profile: { imagePath: '', name: 'Personal Workspace', description: '' },
        ownerId: new mongoose.Types.ObjectId(testData.testUserId),
        members: [new mongoose.Types.ObjectId(testData.testUserId)],
      });
      
      // Set it as the user's personal workspace
      await userModel.updatePersonalWorkspace(
        new mongoose.Types.ObjectId(testData.testUserId),
        personalWorkspace._id
      );
      
      // Add testUser2 as a member so they can try to leave
      await workspaceModel.findByIdAndUpdate(
        personalWorkspace._id,
        { $push: { members: new mongoose.Types.ObjectId(testData.testUser2Id) } }
      );

      const res = await request(app)
        .post(`/api/workspaces/${personalWorkspace._id.toString()}/leave`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Cannot leave your personal workspace');
    });

    test('403 – owner cannot leave workspace', async () => {
      // Input: workspaceId where user is owner
      // Expected status code: 403
      // Expected behavior: error message returned
      // Expected output: error message
      const res = await request(app)
        .post(`/api/workspaces/${testData.testWorkspaceId}/leave`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Owner cannot leave');
    });

    test('400 – user not a member', async () => {
      // Input: workspaceId where user is not a member
      // Expected status code: 400
      // Expected behavior: error message returned
      // Expected output: error message
      const res = await request(app)
        .post(`/api/workspaces/${testData.testWorkspace2Id}/leave`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('You are not a member of this workspace');
    });

    test('404 – workspace not found', async () => {
      // Input: fake workspaceId
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeWorkspaceId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .post(`/api/workspaces/${fakeWorkspaceId}/leave`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Workspace not found');
    });
  });

  describe('PUT /api/workspaces/:id - Update Workspace Profile', () => {
    test('200 – updates workspace profile', async () => {
      // Input: workspaceId in URL, updateData in body
      // Expected status code: 200
      // Expected behavior: workspace profile updated
      // Expected output: updated workspace object
      const updateData = {
        name: 'Updated Workspace Name',
        description: 'Updated description',
      };

      const res = await request(app)
        .put(`/api/workspaces/${testData.testWorkspaceId}`)
        .set('x-test-user-id', testData.testUserId)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Workspace profile updated successfully');
      expect(res.body.data.workspace.name).toBe('Updated Workspace Name');
      expect(res.body.data.workspace.profile.description).toBe('Updated description');
    });

    test('403 – only owner can update profile', async () => {
      // Input: workspaceId and updateData, but user is not owner
      // Expected status code: 403
      // Expected behavior: error message returned
      // Expected output: error message
      // First add user2 as member
      await workspaceModel.findByIdAndUpdate(testData.testWorkspaceId, {
        $push: { members: new mongoose.Types.ObjectId(testData.testUser2Id) },
      });

      const updateData = {
        name: 'Hacked Name',
      };

      const res = await request(app)
        .put(`/api/workspaces/${testData.testWorkspaceId}`)
        .set('x-test-user-id', testData.testUser2Id)
        .send(updateData);

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Only workspace owner');
    });

    test('404 – workspace not found', async () => {
      // Input: fake workspaceId
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeWorkspaceId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .put(`/api/workspaces/${fakeWorkspaceId}`)
        .set('x-test-user-id', testData.testUserId)
        .send({ name: 'New Name' });

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Workspace not found');
    });
  });

  describe('PUT /api/workspaces/:id/picture - Update Workspace Picture', () => {
    test('200 – updates workspace picture', async () => {
      // Input: workspaceId in URL, profilePicture in body
      // Expected status code: 200
      // Expected behavior: workspace picture updated
      // Expected output: updated workspace object
      const updateData = {
        profilePicture: 'https://example.com/new-picture.jpg',
      };

      const res = await request(app)
        .put(`/api/workspaces/${testData.testWorkspaceId}/picture`)
        .set('x-test-user-id', testData.testUserId)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Workspace picture updated successfully');
      expect(res.body.data.workspace.profile.imagePath).toBe('https://example.com/new-picture.jpg');
    });

    test('403 – only owner can update picture', async () => {
      // Input: workspaceId and profilePicture, but user is not owner
      // Expected status code: 403
      // Expected behavior: error message returned
      // Expected output: error message
      await workspaceModel.findByIdAndUpdate(testData.testWorkspaceId, {
        $push: { members: new mongoose.Types.ObjectId(testData.testUser2Id) },
      });

      const updateData = {
        profilePicture: 'https://example.com/hacked.jpg',
      };

      const res = await request(app)
        .put(`/api/workspaces/${testData.testWorkspaceId}/picture`)
        .set('x-test-user-id', testData.testUser2Id)
        .send(updateData);

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Only workspace owner');
    });

    test('404 – workspace not found', async () => {
      // Input: fake workspaceId
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeWorkspaceId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .put(`/api/workspaces/${fakeWorkspaceId}/picture`)
        .set('x-test-user-id', testData.testUserId)
        .send({ profilePicture: 'https://example.com/pic.jpg' });

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Workspace not found');
    });
  });

  describe('DELETE /api/workspaces/:id/members/:userId - Ban Member', () => {
    beforeEach(async () => {
      // Add testUser2 as a member first
      await workspaceModel.findByIdAndUpdate(testData.testWorkspaceId, {
        $push: { members: new mongoose.Types.ObjectId(testData.testUser2Id) },
      });
    });

    test('200 – bans member from workspace', async () => {
      // Input: workspaceId and userId in URL
      // Expected status code: 200
      // Expected behavior: member removed and added to banned list
      // Expected output: updated workspace object
      const res = await request(app)
        .delete(`/api/workspaces/${testData.testWorkspaceId}/members/${testData.testUser2Id}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Member banned successfully');
      expect(res.body.data.workspace).toBeDefined();
      expect(res.body.data.workspace.members).not.toContain(testData.testUser2Id);
    });

    test('200 – bans already banned user (no duplicate)', async () => {
      // Input: workspaceId and userId where user is already banned
      // Expected status code: 200
      // Expected behavior: user remains banned, no duplicate
      // Expected output: updated workspace object
      
      // First ban the user
      await workspaceModel.findByIdAndUpdate(testData.testWorkspaceId, {
        $push: { bannedMembers: new mongoose.Types.ObjectId(testData.testUser2Id) },
        $pull: { members: new mongoose.Types.ObjectId(testData.testUser2Id) },
      });

      // Try to ban again
      const res = await request(app)
        .delete(`/api/workspaces/${testData.testWorkspaceId}/members/${testData.testUser2Id}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Member banned successfully');
      
      // Verify user is still banned (check by verifying workspace)
      const workspace = await workspaceModel.findById(testData.testWorkspaceId);
      const bannedCount = workspace?.bannedMembers.filter(id => id.toString() === testData.testUser2Id).length || 0;
      expect(bannedCount).toBe(1); // Should only appear once, not duplicated
    });

    test('403 – cannot ban members from personal workspace', async () => {
      // Input: workspaceId of a personal workspace
      // Expected status code: 403
      // Expected behavior: error message returned
      // Expected output: error message
      
      // Create a personal workspace
      const personalWorkspace = await workspaceModel.create({
        name: 'Personal Workspace',
        profile: { imagePath: '', name: 'Personal Workspace', description: '' },
        ownerId: new mongoose.Types.ObjectId(testData.testUserId),
        members: [new mongoose.Types.ObjectId(testData.testUserId)],
      });
      
      // Set it as the user's personal workspace
      await userModel.updatePersonalWorkspace(
        new mongoose.Types.ObjectId(testData.testUserId),
        personalWorkspace._id
      );
      
      // Add testUser2 as a member
      await workspaceModel.findByIdAndUpdate(
        personalWorkspace._id,
        { $push: { members: new mongoose.Types.ObjectId(testData.testUser2Id) } }
      );

      const res = await request(app)
        .delete(`/api/workspaces/${personalWorkspace._id.toString()}/members/${testData.testUser2Id}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Cannot ban members from personal workspace');
    });

    test('403 – only owner can ban members', async () => {
      // Input: workspaceId and userId, but requesting user is not owner
      // Expected status code: 403
      // Expected behavior: error message returned
      // Expected output: error message
      // Create a third user and add them
      let User = mongoose.models.User;
      if (!User) {
        User = mongoose.model('User', new mongoose.Schema({
          googleId: { type: String, unique: true },
          email: String,
          profile: { name: String, imagePath: String, description: String }
        }, { timestamps: true }));
      }
      const testUser3 = await User.create({
        googleId: 'test-google-id-3',
        email: 'testuser3@example.com',
        profile: { name: 'Test User 3', imagePath: '', description: '' },
      });

      await workspaceModel.findByIdAndUpdate(testData.testWorkspaceId, {
        $push: { members: new mongoose.Types.ObjectId(testUser3._id) },
      });

      const res = await request(app)
        .delete(`/api/workspaces/${testData.testWorkspaceId}/members/${testData.testUser2Id}`)
        .set('x-test-user-id', testUser3._id.toString());

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Only workspace owner');
    });

    test('400 – cannot ban workspace owner', async () => {
      // Input: workspaceId and userId where userId is the owner
      // Expected status code: 400
      // Expected behavior: error message returned
      // Expected output: error message
      const res = await request(app)
        .delete(`/api/workspaces/${testData.testWorkspaceId}/members/${testData.testUserId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(400);
      expect(res.body.error).toContain('Cannot ban the workspace owner');
    });

    test('404 – workspace not found', async () => {
      // Input: fake workspaceId
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeWorkspaceId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .delete(`/api/workspaces/${fakeWorkspaceId}/members/${testData.testUser2Id}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Workspace not found');
    });

    test('404 – user to ban not found', async () => {
      // Input: workspaceId and fake userId
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeUserId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .delete(`/api/workspaces/${testData.testWorkspaceId}/members/${fakeUserId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('User to ban not found');
    });
  });

  describe('DELETE /api/workspaces/:id - Delete Workspace', () => {
    test('200 – deletes workspace and all notes', async () => {
      // Input: workspaceId in URL
      // Expected status code: 200
      // Expected behavior: workspace and all its notes deleted
      // Expected output: deleted workspace object
      // Create some notes first
      await noteModel.create({
        userId: new mongoose.Types.ObjectId(testData.testUserId),
        workspaceId: testData.testWorkspaceId,
        noteType: NoteType.CONTENT,
        tags: ['test'],
        fields: [{ fieldType: 'title', content: 'Test Note', _id: '1' }],
      });

      const res = await request(app)
        .delete(`/api/workspaces/${testData.testWorkspaceId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Workspace and all its notes deleted successfully');
      expect(res.body.data.workspace).toBeDefined();

      // Verify workspace is deleted
      const deletedWorkspace = await workspaceModel.findById(testData.testWorkspaceId);
      expect(deletedWorkspace).toBeNull();

      // Verify notes are deleted
      const notes = await noteModel.find({ workspaceId: testData.testWorkspaceId });
      expect(notes.length).toBe(0);
    });

    test('403 – cannot delete personal workspace', async () => {
      // Input: workspaceId of a personal workspace
      // Expected status code: 403
      // Expected behavior: error message returned
      // Expected output: error message
      
      // Create a personal workspace
      const personalWorkspace = await workspaceModel.create({
        name: 'Personal Workspace',
        profile: { imagePath: '', name: 'Personal Workspace', description: '' },
        ownerId: new mongoose.Types.ObjectId(testData.testUserId),
        members: [new mongoose.Types.ObjectId(testData.testUserId)],
      });
      
      // Set it as the user's personal workspace
      await userModel.updatePersonalWorkspace(
        new mongoose.Types.ObjectId(testData.testUserId),
        personalWorkspace._id
      );

      const res = await request(app)
        .delete(`/api/workspaces/${personalWorkspace._id.toString()}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Cannot delete your personal workspace');
    });

    test('403 – only owner can delete workspace', async () => {
      // Input: workspaceId where user is not owner
      // Expected status code: 403
      // Expected behavior: error message returned
      // Expected output: error message
      await workspaceModel.findByIdAndUpdate(testData.testWorkspaceId, {
        $push: { members: new mongoose.Types.ObjectId(testData.testUser2Id) },
      });

      const res = await request(app)
        .delete(`/api/workspaces/${testData.testWorkspaceId}`)
        .set('x-test-user-id', testData.testUser2Id);

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Only workspace owner');
    });

    test('404 – workspace not found', async () => {
      // Input: fake workspaceId
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeWorkspaceId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .delete(`/api/workspaces/${fakeWorkspaceId}`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Workspace not found');
    });
  });

  describe('GET /api/workspaces/:id/poll - Poll For New Messages', () => {
    test('200 – checks for new messages', async () => {
      // Input: workspaceId in URL
      // Expected status code: 200
      // Expected behavior: polling check completed
      // Expected output: hasNewMessages boolean
      const res = await request(app)
        .get(`/api/workspaces/${testData.testWorkspaceId}/poll`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Polling check completed');
      expect(res.body.data.hasNewMessages).toBeDefined();
      expect(typeof res.body.data.hasNewMessages).toBe('boolean');
    });

    test('404 – workspace not found', async () => {
      // Input: fake workspaceId
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeWorkspaceId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .get(`/api/workspaces/${fakeWorkspaceId}/poll`)
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Workspace not found');
    });
  });
});

