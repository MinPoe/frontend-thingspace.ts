/// <reference types="jest" />
import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';
import type { Request, Response, NextFunction } from 'express';

import { messageModel } from '../message.model';
import { workspaceModel } from '../workspace.model';
import * as authMiddleware from '../auth.middleware';
import logger from '../logger.util';
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
    await mongo.stop({ doCleanup: true, force: true });
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

  describe('Message routes - user authentication edge cases', () => {
    const buildAppWithMockedAuth = async (userMock: any) => {
      jest.resetModules();

      // Mock authenticateToken before requiring routes
      jest.doMock('../auth.middleware', () => ({
        authenticateToken: async (req: Request, res: Response, next: NextFunction) => {
          req.user = userMock;
          next();
        },
      }));

      const helpers = await import('./test-helpers.js') as typeof import('./test-helpers');
      return helpers.createTestApp();
    };

    afterEach(() => {
      jest.resetModules();
      jest.dontMock('../auth.middleware');
    });

    test('GET /api/messages/workspace/:workspaceId - 401 when req.user._id is undefined (line 18)', async () => {
      // Input: request where authenticateToken passes but req.user._id is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests line 18 in message.routes.ts
      const appInstance = await buildAppWithMockedAuth({} as any);

      const res = await request(appInstance)
        .get(`/api/messages/workspace/${testData.testWorkspaceId}`)
        .set('Authorization', 'Bearer fake-token');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('POST /api/messages/workspace/:workspaceId - 401 when req.user._id is undefined (line 64)', async () => {
      // Input: request where authenticateToken passes but req.user._id is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests line 64 in message.routes.ts
      const appInstance = await buildAppWithMockedAuth({} as any);

      const res = await request(appInstance)
        .post(`/api/messages/workspace/${testData.testWorkspaceId}`)
        .set('Authorization', 'Bearer fake-token')
        .send({ content: 'Test message' });

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });

    test('DELETE /api/messages/:messageId - 401 when req.user._id is undefined (line 109)', async () => {
      // Input: request where authenticateToken passes but req.user._id is undefined
      // Expected status code: 401
      // Expected behavior: returns "User not authenticated" error
      // Expected output: error message
      // This tests line 109 in message.routes.ts
      // Create a message first
      const message = await messageModel.create({
        workspaceId: new mongoose.Types.ObjectId(testData.testWorkspaceId),
        authorId: new mongoose.Types.ObjectId(testData.testUserId),
        content: 'Test',
      });

      const appInstance = await buildAppWithMockedAuth({} as any);

      const res = await request(appInstance)
        .delete(`/api/messages/${message._id}`)
        .set('Authorization', 'Bearer fake-token');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not authenticated');
    });
  });

  describe('Logger utility tests', () => {
    let stdoutSpy: jest.SpyInstance;
    let stderrSpy: jest.SpyInstance;

    beforeEach(() => {
      stdoutSpy = jest.spyOn(process.stdout, 'write').mockImplementation(() => true);
      stderrSpy = jest.spyOn(process.stderr, 'write').mockImplementation(() => true);
    });

    afterEach(() => {
      stdoutSpy.mockRestore();
      stderrSpy.mockRestore();
    });

    test('logger.info with no additional args (tests line 7 false branch)', () => {
      // Input: message only, no additional args
      // Expected behavior: args.length > 0 evaluates to false
      // Expected output: writes to stdout without additional args
      // This tests line 7 in logger.util.ts (args.length > 0 ? ... : '')
      logger.info('Test message');

      expect(stdoutSpy).toHaveBeenCalledWith(expect.stringContaining('[INFO] Test message'));
      expect(stdoutSpy).toHaveBeenCalledWith(expect.not.stringContaining('  ')); // No extra spaces from args
    });

    test('logger.info with additional args (tests line 7 true branch)', () => {
      // Input: message with additional args
      // Expected behavior: args.length > 0 evaluates to true
      // Expected output: writes to stdout with additional args joined
      // This tests line 7 in logger.util.ts (args.length > 0 ? ... : '')
      logger.info('Test message', 'arg1', 'arg2', 123);

      expect(stdoutSpy).toHaveBeenCalledWith(expect.stringContaining('[INFO] Test message'));
      expect(stdoutSpy).toHaveBeenCalledWith(expect.stringMatching(/arg1.*arg2.*123/));
    });

    test('logger.error with no additional args (tests line 11 false branch)', () => {
      // Input: message only, no additional args
      // Expected behavior: args.length > 0 evaluates to false
      // Expected output: writes to stderr without additional args
      // This tests line 11 in logger.util.ts (args.length > 0 ? ... : '')
      logger.error('Test error');

      expect(stderrSpy).toHaveBeenCalledWith(expect.stringContaining('[ERROR] Test error'));
      expect(stderrSpy).toHaveBeenCalledWith(expect.not.stringContaining('  ')); // No extra spaces from args
    });

    test('logger.error with additional args (tests line 11 true branch)', () => {
      // Input: message with additional args
      // Expected behavior: args.length > 0 evaluates to true
      // Expected output: writes to stderr with additional args joined
      // This tests line 11 in logger.util.ts (args.length > 0 ? ... : '')
      logger.error('Test error', 'error detail', { code: 500 });

      expect(stderrSpy).toHaveBeenCalledWith(expect.stringContaining('[ERROR] Test error'));
      expect(stderrSpy).toHaveBeenCalledWith(expect.stringMatching(/error detail/));
    });
  });
});

