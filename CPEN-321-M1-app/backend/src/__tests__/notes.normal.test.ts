/// <reference types="jest" />
import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';

import { NoteType } from '../notes.types';
import { workspaceModel } from '../workspace.model';
import { createTestApp, setupTestDatabase, TestData } from './test-helpers';

const app = createTestApp();

// ---------------------------
// Test suite
// ---------------------------
describe('Notes API – Normal Tests (No Mocking)', () => {
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

  describe('POST /api/notes - Create Note', () => {
    test('201 – creates a CONTENT note', async () => {
      const noteData = {
        workspaceId: testData.testWorkspaceId,
        noteType: NoteType.CONTENT,
        tags: ['tag1', 'tag2'],
        fields: [
          { fieldType: 'title', content: 'Test Note', _id: '1' },
          { fieldType: 'textbox', content: 'This is test content', _id: '2' },
        ],
      };

      const res = await request(app).post('/api/notes').set('x-test-user-id', testData.testUserId).send(noteData);

      expect(res.status).toBe(201);
      expect(res.body.message).toBe('Note created successfully');
      expect(res.body.data.note).toBeDefined();
      expect(res.body.data.note.noteType).toBe(NoteType.CONTENT);
      expect(res.body.data.note.workspaceId).toBe(testData.testWorkspaceId);
      expect(res.body.data.note.fields).toHaveLength(2);
    });

    test('500 – missing workspaceId (validation mocked out)', async () => {
      const noteData = {
        noteType: NoteType.CONTENT,
        tags: ['tag1'],
        fields: [{ fieldType: 'title', content: 'Test', _id: '1' }],
      };

      const res = await request(app).post('/api/notes').set('x-test-user-id', testData.testUserId).send(noteData);
      expect(res.status).toBe(500);
      expect(res.body.error).toBeDefined();
    });

    test('201 – creates a CHAT note (and updates workspace timestamp)', async () => {
      const noteData = {
        workspaceId: testData.testWorkspaceId,
        noteType: NoteType.CHAT,
        tags: [],
        fields: [{ fieldType: 'textbox', content: 'Chat message', _id: '1' }],
      };

      const res = await request(app).post('/api/notes').set('x-test-user-id', testData.testUserId).send(noteData);

      expect(res.status).toBe(201);
      expect(res.body.data.note.noteType).toBe(NoteType.CHAT);
    });
  });

  describe('PUT /api/notes/:id - Update Note', () => {
    let noteId: string;

    beforeEach(async () => {
      const create = await request(app)
        .post('/api/notes')
        .set('x-test-user-id', testData.testUserId)
        .send({
          workspaceId: testData.testWorkspaceId,
          noteType: NoteType.CONTENT,
          tags: ['old-tag'],
          fields: [{ fieldType: 'title', content: 'Original Title', _id: '1' }],
        });
      noteId = create.body.data.note._id;
    });

    test('200 – updates an existing note', async () => {
      const res = await request(app)
        .put(`/api/notes/${noteId}`)
        .set('x-test-user-id', testData.testUserId)
        .send({ tags: ['new-tag'], fields: [{ fieldType: 'title', content: 'Updated Title', _id: '1' }] });

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Note successfully updated');
      expect(res.body.data.note.tags).toEqual(['new-tag']);
      expect(res.body.data.note.fields[0].content).toBe('Updated Title');
    });

    test("500 – note doesn't exist", async () => {
      const fakeId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .put(`/api/notes/${fakeId}`)
        .set('x-test-user-id', testData.testUserId)
        .send({ tags: ['new-tag'], fields: [{ fieldType: 'title', content: 'Updated', _id: '1' }] });

      expect(res.status).toBe(500);
      expect(res.body.error).toBeDefined();
    });

    test("500 – cannot update another user's note (Note not found)", async () => {
      const res = await request(app)
        .put(`/api/notes/${noteId}`)
        .set('x-test-user-id', testData.testUser2Id)
        .send({ tags: ['new-tag'], fields: [{ fieldType: 'title', content: 'Hacked!', _id: '1' }] });

      expect(res.status).toBe(500);
      expect(res.body.error).toBeDefined();
    });
  });

  describe('DELETE /api/notes/:id - Delete Note', () => {
    let noteId: string;

    beforeEach(async () => {
      const create = await request(app)
        .post('/api/notes')
        .set('x-test-user-id', testData.testUserId)
        .send({
          workspaceId: testData.testWorkspaceId,
          noteType: NoteType.CONTENT,
          tags: ['delete-me'],
          fields: [{ fieldType: 'title', content: 'Delete This', _id: '1' }],
        });
      noteId = create.body.data.note._id;
    });

    test('200 – deletes an existing note', async () => {
      const res = await request(app).delete(`/api/notes/${noteId}`).set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Note successfully deleted');
    });

    test("500 – deleting a non-existent note", async () => {
      const fakeId = new mongoose.Types.ObjectId().toString();
      const res = await request(app).delete(`/api/notes/${fakeId}`).set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBeDefined();
    });

    test("500 – cannot delete another user's note (Note not found)", async () => {
      const res = await request(app).delete(`/api/notes/${noteId}`).set('x-test-user-id', testData.testUser2Id);

      expect(res.status).toBe(500);
      expect(res.body.error).toBeDefined();
    });
  });

  describe('GET /api/notes/:id - Get Single Note', () => {
    let noteId: string;

    beforeEach(async () => {
      const create = await request(app)
        .post('/api/notes')
        .set('x-test-user-id', testData.testUserId)
        .send({
          workspaceId: testData.testWorkspaceId,
          noteType: NoteType.CONTENT,
          tags: ['get-me'],
          fields: [{ fieldType: 'title', content: 'Fetch This', _id: '1' }],
        });
      noteId = create.body.data.note._id;
    });

    test('200 – fetches a note', async () => {
      const res = await request(app).get(`/api/notes/${noteId}`).set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Note successfully retrieved');
      expect(res.body.data.note._id).toBe(noteId);
      expect(res.body.data.note.fields[0].content).toBe('Fetch This');
    });

    test("404 – note doesn't exist", async () => {
      const fakeId = new mongoose.Types.ObjectId().toString();
      const res = await request(app).get(`/api/notes/${fakeId}`).set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Note not found');
    });

    test("404 – accessing another user's note", async () => {
      const res = await request(app).get(`/api/notes/${noteId}`).set('x-test-user-id', testData.testUser2Id);

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Note not found');
    });
  });

  describe('GET /api/notes - Find Notes', () => {
    beforeEach(async () => {
      const notes = [
        {
          workspaceId: testData.testWorkspaceId,
          noteType: NoteType.CONTENT,
          tags: ['important'],
          fields: [{ fieldType: 'title', content: 'Important Note', _id: '1' }],
        },
        {
          workspaceId: testData.testWorkspaceId,
          noteType: NoteType.CONTENT,
          tags: ['important', 'urgent'],
          fields: [{ fieldType: 'title', content: 'Urgent Note', _id: '1' }],
        },
        {
          workspaceId: testData.testWorkspaceId,
          noteType: NoteType.CONTENT,
          tags: ['normal'],
          fields: [{ fieldType: 'title', content: 'Normal Note', _id: '1' }],
        },
      ];

      for (const n of notes) {
        await request(app).post('/api/notes').set('x-test-user-id', testData.testUserId).send(n);
      }
    });

    test('200 – finds all notes in workspace', async () => {
      const res = await request(app)
        .get('/api/notes')
        .query({ workspaceId: testData.testWorkspaceId, noteType: NoteType.CONTENT })
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Notes retrieved successfully');
      expect(res.body.data.notes.length).toBeGreaterThanOrEqual(3);
    });

    test('200 – filters by tag', async () => {
      const res = await request(app)
        .get('/api/notes')
        .query({ workspaceId: testData.testWorkspaceId, noteType: NoteType.CONTENT, tags: 'urgent' })
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      const urgentNotes = res.body.data.notes.filter((note: any) => note.tags.includes('urgent'));
      expect(urgentNotes.length).toBeGreaterThan(0);
    });

    test('400 – missing workspaceId', async () => {
      const res = await request(app)
        .get('/api/notes')
        .query({ noteType: NoteType.CONTENT })
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('workspaceId is required');
    });

    test('400 – missing noteType', async () => {
      const res = await request(app)
        .get('/api/notes')
        .query({ workspaceId: testData.testWorkspaceId })
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('noteType is required');
    });

    test('500 – user not a member of workspace', async () => {
      const res = await request(app)
        .get('/api/notes')
        .query({ workspaceId: testData.testWorkspaceId, noteType: NoteType.CONTENT })
        .set('x-test-user-id', testData.testUser2Id);

      expect(res.status).toBe(500);
      expect(res.body.error).toBeDefined();
    });
  });

  describe('POST /api/notes/:id/share - Share Note to Workspace', () => {
    let noteId: string;

    beforeEach(async () => {
      const create = await request(app)
        .post('/api/notes')
        .set('x-test-user-id', testData.testUserId)
        .send({
          workspaceId: testData.testWorkspaceId,
          noteType: NoteType.CONTENT,
          tags: ['share-me'],
          fields: [{ fieldType: 'title', content: 'Share This', _id: '1' }],
        });
      noteId = create.body.data.note._id;
    });

    test('200 – shares to another workspace when user is a member', async () => {
      await workspaceModel.findByIdAndUpdate(testData.testWorkspace2Id, {
        $push: { members: new mongoose.Types.ObjectId(testData.testUserId) },
      });

      const res = await request(app)
        .post(`/api/notes/${noteId}/share`)
        .set('x-test-user-id', testData.testUserId)
        .send({ workspaceId: testData.testWorkspace2Id });

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Note shared to workspace successfully');
      expect(res.body.data.note.workspaceId).toBe(testData.testWorkspace2Id);
    });

    test('404 – target workspace not found', async () => {
      const fakeWorkspaceId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .post(`/api/notes/${noteId}/share`)
        .set('x-test-user-id', testData.testUserId)
        .send({ workspaceId: fakeWorkspaceId });

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Workspace not found');
    });

    test('403 – user not a member of target workspace', async () => {
      const res = await request(app)
        .post(`/api/notes/${noteId}/share`)
        .set('x-test-user-id', testData.testUserId)
        .send({ workspaceId: testData.testWorkspace2Id });

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Access denied');
    });

    test('404 – source note not found', async () => {
      const fakeNoteId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .post(`/api/notes/${fakeNoteId}/share`)
        .set('x-test-user-id', testData.testUserId)
        .send({ workspaceId: testData.testWorkspace2Id });

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Note not found');
    });
  });

  describe('POST /api/notes/:id/copy - Copy Note to Workspace', () => {
    let noteId: string;

    beforeEach(async () => {
      const create = await request(app)
        .post('/api/notes')
        .set('x-test-user-id', testData.testUserId)
        .send({
          workspaceId: testData.testWorkspaceId,
          noteType: NoteType.CONTENT,
          tags: ['copy-me'],
          fields: [{ fieldType: 'title', content: 'Copy This', _id: '1' }],
        });
      noteId = create.body.data.note._id;
    });

    test('201 – copies to another workspace when user is a member', async () => {
      await workspaceModel.findByIdAndUpdate(testData.testWorkspace2Id, {
        $push: { members: new mongoose.Types.ObjectId(testData.testUserId) },
      });

      const res = await request(app)
        .post(`/api/notes/${noteId}/copy`)
        .set('x-test-user-id', testData.testUserId)
        .send({ workspaceId: testData.testWorkspace2Id });

      expect(res.status).toBe(201);
      expect(res.body.message).toBe('Note copied to workspace successfully');
      expect(res.body.data.note.workspaceId).toBe(testData.testWorkspace2Id);
      expect(res.body.data.note._id).not.toBe(noteId);
      expect(res.body.data.note.fields[0].content).toBe('Copy This');
    });

    test('404 – target workspace not found', async () => {
      const fakeWorkspaceId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .post(`/api/notes/${noteId}/copy`)
        .set('x-test-user-id', testData.testUserId)
        .send({ workspaceId: fakeWorkspaceId });

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Workspace not found');
    });

    test('403 – user not a member of target workspace', async () => {
      const res = await request(app)
        .post(`/api/notes/${noteId}/copy`)
        .set('x-test-user-id', testData.testUserId)
        .send({ workspaceId: testData.testWorkspace2Id });

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Access denied');
    });
  });
});

