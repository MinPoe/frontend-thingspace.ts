/// <reference types="jest" />
import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';

import { messageModel } from '../message.model';
import { workspaceModel } from '../workspace.model';
import { createTestApp, setupTestDatabase, TestData } from './test-helpers';

// ---------------------------
// Test suite
// ---------------------------
describe('Message API – Mocked Tests (Jest Mocks)', () => {
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

  // Clean mocks every test
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
    testData = await setupTestDatabase(app);
  });

  describe('GET /api/messages/workspace/:workspaceId - Get Messages, with mocks', () => {
    test('500 – returns 500 when messageModel.find throws error', async () => {
      // Mocked behavior: messageModel.find throws database error
      // Input: workspaceId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message "Failed to fetch messages"
      jest.spyOn(messageModel, 'find').mockReturnValue({
        sort: jest.fn().mockReturnValue({
          limit: jest.fn().mockReturnValue({
            lean: jest.fn().mockRejectedValue(new Error('Database error')),
          }),
        }),
      } as any);

      const res = await request(app)
        .get(`/api/messages/workspace/${testData.testWorkspaceId}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to fetch messages');
    });
  });

  describe('POST /api/messages/workspace/:workspaceId - Create Message, with mocks', () => {
    test('500 – returns 500 when messageModel.create throws error', async () => {
      // Mocked behavior: messageModel.create throws database error
      // Input: workspaceId in URL, content in body
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message "Failed to create message"
      jest.spyOn(messageModel, 'create').mockRejectedValue(new Error('Database error'));

      const res = await request(app)
        .post(`/api/messages/workspace/${testData.testWorkspaceId}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ content: 'Test message' });

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to create message');
    });

    test('500 – returns 500 when workspaceModel.findByIdAndUpdate throws error', async () => {
      // Mocked behavior: workspaceModel.findByIdAndUpdate throws error
      // Input: workspaceId in URL, content in body
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message "Failed to create message"
      jest.spyOn(messageModel, 'create').mockResolvedValue({
        _id: new mongoose.Types.ObjectId(),
        workspaceId: new mongoose.Types.ObjectId(testData.testWorkspaceId),
        authorId: new mongoose.Types.ObjectId(testData.testUserId),
        content: 'Test',
        createdAt: new Date(),
      } as any);
      jest.spyOn(workspaceModel, 'findByIdAndUpdate').mockRejectedValue(new Error('Update error'));

      const res = await request(app)
        .post(`/api/messages/workspace/${testData.testWorkspaceId}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .send({ content: 'Test message' });

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to create message');
    });
  });

  describe('DELETE /api/messages/:messageId - Delete Message, with mocks', () => {
    test('500 – returns 500 when messageModel.findById throws error', async () => {
      // Mocked behavior: messageModel.findById throws database error
      // Input: messageId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message "Failed to delete message"
      jest.spyOn(messageModel, 'findById').mockRejectedValue(new Error('Database error'));

      const messageId = new mongoose.Types.ObjectId();
      const res = await request(app)
        .delete(`/api/messages/${messageId}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to delete message');
    });

    test('500 – returns 500 when messageModel.findByIdAndDelete throws error', async () => {
      // Mocked behavior: messageModel.findByIdAndDelete throws database error
      // Input: messageId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message "Failed to delete message"
      // Create a message first
      const message = await messageModel.create({
        workspaceId: new mongoose.Types.ObjectId(testData.testWorkspaceId),
        authorId: new mongoose.Types.ObjectId(testData.testUserId),
        content: 'Test',
      });

      jest.spyOn(messageModel, 'findByIdAndDelete').mockRejectedValue(new Error('Delete error'));

      const res = await request(app)
        .delete(`/api/messages/${message._id}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to delete message');
    });

    test('500 – returns 500 when workspaceModel.findById throws error', async () => {
      // Mocked behavior: workspaceModel.findById throws database error
      // Input: messageId in URL
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message "Failed to delete message"
      // Create a message first
      const message = await messageModel.create({
        workspaceId: new mongoose.Types.ObjectId(testData.testWorkspaceId),
        authorId: new mongoose.Types.ObjectId(testData.testUserId),
        content: 'Test',
      });

      jest.spyOn(workspaceModel, 'findById').mockRejectedValue(new Error('Database error'));

      const res = await request(app)
        .delete(`/api/messages/${message._id}`)
        .set('Authorization', `Bearer ${testData.testUserToken}`);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Failed to delete message');
    });
  });
});

