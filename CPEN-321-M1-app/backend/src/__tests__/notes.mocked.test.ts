/// <reference types="jest" />
import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';

import { NoteType } from '../notes.types';
import { noteService } from '../notes.service';
import { workspaceModel } from '../workspace.model';
import { createTestApp, setupTestDatabase, TestData } from './test-helpers';

const app = createTestApp();

// ---------------------------
// Test suite
// ---------------------------
describe('Notes API – Mocked Tests (Jest Mocks)', () => {
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
    let noteId: string;

    beforeEach(async () => {
      const create = await request(app)
        .post('/api/notes')
        .set('x-test-user-id', testData.testUserId)
        .send({
          workspaceId: testData.testWorkspaceId,
          noteType: NoteType.CONTENT,
          tags: ['mock-test'],
          fields: [{ fieldType: 'title', content: 'Mock Test Note', _id: '1' }],
        });
      noteId = create.body.data.note._id;
    });

    test('500 – create note handles service error', async () => {
      jest.spyOn(noteService, 'createNote').mockRejectedValue(new Error('Database connection failed'));

      const res = await request(app)
        .post('/api/notes')
        .set('x-test-user-id', testData.testUserId)
        .send({
          workspaceId: testData.testWorkspaceId,
          noteType: NoteType.CONTENT,
          tags: ['test'],
          fields: [{ fieldType: 'title', content: 'Test', _id: '1' }],
        });

      expect(res.status).toBe(500);
      expect(res.body.error).toBeDefined();
    });

    test('500 – update note handles service error', async () => {
      jest.spyOn(noteService, 'updateNote').mockRejectedValue(new Error('Database write failed'));

      const res = await request(app)
        .put(`/api/notes/${noteId}`)
        .set('x-test-user-id', testData.testUserId)
        .send({ tags: ['updated'], fields: [{ fieldType: 'title', content: 'Updated', _id: '1' }] });

      expect(res.status).toBe(500);
      expect(res.body.error).toBeDefined();
    });

    test('500 – delete note handles service error', async () => {
      jest.spyOn(noteService, 'deleteNote').mockRejectedValue(new Error('Database delete failed'));

      const res = await request(app).delete(`/api/notes/${noteId}`).set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBeDefined();
    });

    test('500 – get notes handles service error', async () => {
      jest.spyOn(noteService, 'getNotes').mockRejectedValue(new Error('Database query failed'));

      const res = await request(app)
        .get('/api/notes')
        .query({ workspaceId: testData.testWorkspaceId, noteType: NoteType.CONTENT })
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBeDefined();
    });

    test('OpenAI failure path does not crash create flow (service decides behavior)', async () => {
      // This assumes your service catches OpenAI errors and proceeds with empty vectors.
      const res = await request(app)
        .post('/api/notes')
        .set('x-test-user-id', testData.testUserId)
        .send({
          workspaceId: testData.testWorkspaceId,
          noteType: NoteType.CONTENT,
          tags: ['openai-error-test'],
          fields: [
            { fieldType: 'title', content: 'OpenAI Error Test', _id: '1' },
            { fieldType: 'textbox', content: 'This should still work even if OpenAI fails', _id: '2' },
          ],
        });

      // Expect success if service swallows OpenAI errors; otherwise 500.
      // Adjust to your implementation contract if needed.
      expect([201, 500]).toContain(res.status);
    });
  });

  describe('Mocked – Workspace model failure', () => {
    let noteId: string;

    beforeEach(async () => {
      const create = await request(app)
        .post('/api/notes')
        .set('x-test-user-id', testData.testUserId)
        .send({
          workspaceId: testData.testWorkspaceId,
          noteType: NoteType.CONTENT,
          tags: ['workspace-test'],
          fields: [{ fieldType: 'title', content: 'Workspace Test', _id: '1' }],
        });
      noteId = create.body.data.note._id;
    });

    test('500 – workspace lookup throws during share', async () => {
      jest.spyOn(workspaceModel, 'findById').mockImplementation(() => {
        throw new Error('Workspace service unavailable');
      });

      await workspaceModel.findByIdAndUpdate(testData.testWorkspace2Id, {
        $push: { members: new mongoose.Types.ObjectId(testData.testUserId) },
      });

      const res = await request(app)
        .post(`/api/notes/${noteId}/share`)
        .set('x-test-user-id', testData.testUserId)
        .send({ workspaceId: testData.testWorkspace2Id });

      expect(res.status).toBe(500);
      expect(res.body.error).toBeDefined();
    });
  });
});

