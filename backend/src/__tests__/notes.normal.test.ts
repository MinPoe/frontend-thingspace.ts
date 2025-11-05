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
      // Input: noteData with workspaceId, noteType CONTENT, tags, and fields array
      // Expected status code: 201
      // Expected behavior: note is added to the database
      // Expected output: id of the created note, note details with fields
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
      // Input: noteData missing workspaceId
      // Expected status code: 500
      // Expected behavior: database error, as validation is mocked out
      // Expected output: None
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
      // Input: noteData with workspaceId, noteType CHAT, empty tags, field content
      // Expected status code: 201
      // Expected behavior: CHAT note is added to database, workspace timestamp updated
      // Expected output: id of the created CHAT note
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

    test('201 – creates note with default noteType when noteType is missing', async () => {
      // Input: noteData with workspaceId, missing noteType, tags, and fields
      // Expected status code: 201
      // Expected behavior: note is created with default noteType (CONTENT)
      // Expected output: note with noteType set to CONTENT
      const noteData = {
        workspaceId: testData.testWorkspaceId,
        tags: ['tag1'],
        fields: [{ fieldType: 'title', content: 'Default Note Type Test', _id: '1' }],
      };

      const res = await request(app).post('/api/notes').set('x-test-user-id', testData.testUserId).send(noteData);

      expect(res.status).toBe(201);
      expect(res.body.message).toBe('Note created successfully');
      expect(res.body.data.note.noteType).toBe(NoteType.CONTENT);
    });

    test('201 – creates note with default empty tags when tags is missing', async () => {
      // Input: noteData with workspaceId, noteType, missing tags, and fields
      // Expected status code: 201
      // Expected behavior: note is created with default empty tags array
      // Expected output: note with tags set to []
      const noteData = {
        workspaceId: testData.testWorkspaceId,
        noteType: NoteType.CONTENT,
        fields: [{ fieldType: 'title', content: 'Default Tags Test', _id: '1' }],
      };

      const res = await request(app).post('/api/notes').set('x-test-user-id', testData.testUserId).send(noteData);

      expect(res.status).toBe(201);
      expect(res.body.message).toBe('Note created successfully');
      expect(res.body.data.note.tags).toEqual([]);
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
      // Input: noteId in URL, updated tags and fields in body
      // Expected status code: 200
      // Expected behavior: note is updated in database
      // Expected output: updated note details
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
      // Input: fake noteId that doesn't exist, updated data in body
      // Expected status code: 500
      // Expected behavior: database error
      // Expected output: None
      const fakeId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .put(`/api/notes/${fakeId}`)
        .set('x-test-user-id', testData.testUserId)
        .send({ tags: ['new-tag'], fields: [{ fieldType: 'title', content: 'Updated', _id: '1' }] });

      expect(res.status).toBe(500);
      expect(res.body.error).toBeDefined();
    });

    test("500 – cannot update another user's note (Note not found)", async () => {
      // Input: noteId of another user's note, updated data in body, different userId
      // Expected status code: 500
      // Expected behavior: database error due to ownership check
      // Expected output: None
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
      // Input: noteId in URL
      // Expected status code: 200
      // Expected behavior: note is deleted from database
      // Expected output: success message
      const res = await request(app).delete(`/api/notes/${noteId}`).set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Note successfully deleted');
    });

    test("500 – deleting a non-existent note", async () => {
      // Input: fake noteId that doesn't exist
      // Expected status code: 500
      // Expected behavior: database error
      // Expected output: None
      const fakeId = new mongoose.Types.ObjectId().toString();
      const res = await request(app).delete(`/api/notes/${fakeId}`).set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBeDefined();
    });

    test("500 – cannot delete another user's note (Note not found)", async () => {
      // Input: noteId of another user's note, different userId
      // Expected status code: 500
      // Expected behavior: database error due to ownership check
      // Expected output: None
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
      // Input: noteId in URL
      // Expected status code: 200
      // Expected behavior: note is retrieved from database
      // Expected output: note details with fields
      const res = await request(app).get(`/api/notes/${noteId}`).set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Note successfully retrieved');
      expect(res.body.data.note._id).toBe(noteId);
      expect(res.body.data.note.fields[0].content).toBe('Fetch This');
    });

    test("404 – note doesn't exist", async () => {
      // Input: fake noteId that doesn't exist
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeId = new mongoose.Types.ObjectId().toString();
      const res = await request(app).get(`/api/notes/${fakeId}`).set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Note not found');
    });

    test("404 – accessing another user's note", async () => {
      // Input: noteId of another user's note, different userId
      // Expected status code: 404
      // Expected behavior: error message due to ownership check
      // Expected output: error message
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
      // Input: workspaceId and noteType in query params
      // Expected status code: 200
      // Expected behavior: notes retrieved from database
      // Expected output: array of notes
      const res = await request(app)
        .get('/api/notes')
        .query({ workspaceId: testData.testWorkspaceId, noteType: NoteType.CONTENT })
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Notes retrieved successfully');
      expect(res.body.data.notes.length).toBeGreaterThanOrEqual(3);
    });

    test('200 – filters by tag', async () => {
      // Input: workspaceId, noteType, and tag in query params
      // Expected status code: 200
      // Expected behavior: notes filtered by tag retrieved from database
      // Expected output: array of notes with matching tag
      const res = await request(app)
        .get('/api/notes')
        .query({ workspaceId: testData.testWorkspaceId, noteType: NoteType.CONTENT, tags: 'urgent' })
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      const urgentNotes = res.body.data.notes.filter((note: any) => note.tags.includes('urgent'));
      expect(urgentNotes.length).toBeGreaterThan(0);
    });

    test('400 – missing workspaceId', async () => {
      // Input: noteType only in query params, missing workspaceId
      // Expected status code: 400
      // Expected behavior: validation error
      // Expected output: error message
      const res = await request(app)
        .get('/api/notes')
        .query({ noteType: NoteType.CONTENT })
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('workspaceId is required');
    });

    test('400 – missing noteType', async () => {
      // Input: workspaceId only in query params, missing noteType
      // Expected status code: 400
      // Expected behavior: validation error
      // Expected output: error message
      const res = await request(app)
        .get('/api/notes')
        .query({ workspaceId: testData.testWorkspaceId })
        .set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('noteType is required');
    });

    test('500 – user not a member of workspace', async () => {
      // Input: workspaceId and noteType for workspace user doesn't belong to
      // Expected status code: 500
      // Expected behavior: database error due to access denied
      // Expected output: None
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
      // Input: noteId in URL, workspaceId in body
      // Expected status code: 200
      // Expected behavior: note is shared to target workspace
      // Expected output: shared note details
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
      // Input: noteId in URL, fake workspaceId in body
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeWorkspaceId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .post(`/api/notes/${noteId}/share`)
        .set('x-test-user-id', testData.testUserId)
        .send({ workspaceId: fakeWorkspaceId });

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Workspace not found');
    });

    test('403 – user not a member of target workspace', async () => {
      // Input: noteId in URL, workspaceId user doesn't have access to
      // Expected status code: 403
      // Expected behavior: error message returned
      // Expected output: error message
      const res = await request(app)
        .post(`/api/notes/${noteId}/share`)
        .set('x-test-user-id', testData.testUserId)
        .send({ workspaceId: testData.testWorkspace2Id });

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Access denied');
    });

    test('404 – source note not found', async () => {
      // Input: fake noteId in URL, workspaceId in body
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeNoteId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .post(`/api/notes/${fakeNoteId}/share`)
        .set('x-test-user-id', testData.testUserId)
        .send({ workspaceId: testData.testWorkspace2Id });

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Note not found');
    });

    test('403 – cannot share another user\'s note (not owner)', async () => {
      // Input: noteId of another user's note, workspaceId, different userId
      // Expected status code: 403
      // Expected behavior: error message returned due to ownership check
      // Expected output: error message
      const res = await request(app)
        .post(`/api/notes/${noteId}/share`)
        .set('x-test-user-id', testData.testUser2Id)
        .send({ workspaceId: testData.testWorkspaceId });

      expect(res.status).toBe(403);
      expect(res.body.error).toBe('Access denied: Only the note owner can share');
    });

    test('400 – missing workspaceId', async () => {
      // Input: noteId in URL, no workspaceId in body
      // Expected status code: 400
      // Expected behavior: validation error
      // Expected output: error message
      const res = await request(app)
        .post(`/api/notes/${noteId}/share`)
        .set('x-test-user-id', testData.testUserId)
        .send({});

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('workspaceId is required');
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
      // Input: noteId in URL, workspaceId in body
      // Expected status code: 201
      // Expected behavior: note is copied to target workspace with new ID
      // Expected output: copied note details with new ID
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
      // Input: noteId in URL, fake workspaceId in body
      // Expected status code: 404
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeWorkspaceId = new mongoose.Types.ObjectId().toString();
      const res = await request(app)
        .post(`/api/notes/${noteId}/copy`)
        .set('x-test-user-id', testData.testUserId)
        .send({ workspaceId: fakeWorkspaceId });

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Workspace not found');
    });

    test('403 – user not a member of target workspace', async () => {
      // Input: noteId in URL, workspaceId user doesn't have access to
      // Expected status code: 403
      // Expected behavior: error message returned
      // Expected output: error message
      const res = await request(app)
        .post(`/api/notes/${noteId}/copy`)
        .set('x-test-user-id', testData.testUserId)
        .send({ workspaceId: testData.testWorkspace2Id });

      expect(res.status).toBe(403);
      expect(res.body.error).toContain('Access denied');
    });

    test('403 – cannot copy another user\'s note (not owner)', async () => {
      // Input: noteId of another user's note, workspaceId, different userId
      // Expected status code: 403
      // Expected behavior: error message returned due to ownership check
      // Expected output: error message
      const res = await request(app)
        .post(`/api/notes/${noteId}/copy`)
        .set('x-test-user-id', testData.testUser2Id)
        .send({ workspaceId: testData.testWorkspaceId });

      expect(res.status).toBe(403);
      expect(res.body.error).toBe('Access denied: Only the note owner can copy');
    });

    test('400 – missing workspaceId', async () => {
      // Input: noteId in URL, no workspaceId in body
      // Expected status code: 400
      // Expected behavior: validation error
      // Expected output: error message
      const res = await request(app)
        .post(`/api/notes/${noteId}/copy`)
        .set('x-test-user-id', testData.testUserId)
        .send({});

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('workspaceId is required');
    });
  });

  describe('GET /api/notes/:id/workspaces - Get Workspace for Note', () => {
    let noteId: string;

    beforeEach(async () => {
      const create = await request(app)
        .post('/api/notes')
        .set('x-test-user-id', testData.testUserId)
        .send({
          workspaceId: testData.testWorkspaceId,
          noteType: NoteType.CONTENT,
          tags: ['workspace-test'],
          fields: [{ fieldType: 'title', content: 'Get Workspace', _id: '1' }],
        });
      noteId = create.body.data.note._id;
    });

    test('200 – retrieves workspace ID for a note', async () => {
      // Input: noteId in URL
      // Expected status code: 200
      // Expected behavior: workspace ID retrieved from database
      // Expected output: workspace ID
      const res = await request(app).get(`/api/notes/${noteId}/workspaces`).set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Workspace retrieved successfully');
      expect(res.body.data.workspaceId).toBe(testData.testWorkspaceId);
    });

    test('500 – note does not exist', async () => {
      // Input: fake noteId that doesn't exist
      // Expected status code: 500
      // Expected behavior: database error
      // Expected output: None
      const fakeNoteId = new mongoose.Types.ObjectId().toString();
      const res = await request(app).get(`/api/notes/${fakeNoteId}/workspaces`).set('x-test-user-id', testData.testUserId);

      expect(res.status).toBe(500);
      expect(res.body.error).toBeDefined();
    });
  });
});

