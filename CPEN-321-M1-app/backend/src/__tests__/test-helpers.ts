/// <reference types="jest" />
import mongoose from 'mongoose';
import express, { Request, Response, NextFunction, RequestHandler } from 'express';
import { NotesController } from '../notes.controller';
import { workspaceModel } from '../workspace.model';

// ---------------------------
// Express test app bootstrap
// ---------------------------
export function createTestApp() {
  const app = express();
  app.use(express.json());

  // Mock authentication middleware
  const mockAuthMiddleware: RequestHandler = async (req: Request, res: Response, next: NextFunction) => {
    const mockUserId = req.headers['x-test-user-id'] as string;
    if (mockUserId) {
      // Attach a minimal mock user onto the request (typecast for test)
      (req as any).user = {
        _id: new mongoose.Types.ObjectId(mockUserId),
        googleId: 'test-google-id',
        email: 'test@example.com',
        profile: { name: 'Test User', imagePath: '', description: '' },
        createdAt: new Date(),
        updatedAt: new Date(),
      };
      return next();
    }
    return res.status(401).json({ error: 'No test user ID provided' });
  };

  // Pass-through validation middleware (validation tested elsewhere)
  const mockValidateBody = (_req: Request, _res: Response, next: NextFunction) => next();

  // Controller
  const notesController = new NotesController();

  // ---------------------------
  // Route mounting (ORDER MATTERS)
  // ---------------------------
  // Put the more specific route BEFORE the generic :id route.
  app.post('/api/notes', mockAuthMiddleware, mockValidateBody, notesController.createNote.bind(notesController));
  app.put('/api/notes/:id', mockAuthMiddleware, mockValidateBody, notesController.updateNote.bind(notesController));
  app.delete('/api/notes/:id', mockAuthMiddleware, notesController.deleteNote.bind(notesController));
  app.get('/api/notes/:id/workspaces', mockAuthMiddleware, notesController.getWorkspacesForNote.bind(notesController));
  app.get('/api/notes/:id', mockAuthMiddleware, notesController.getNote.bind(notesController));
  app.get('/api/notes', mockAuthMiddleware, notesController.findNotes.bind(notesController));
  app.post('/api/notes/:id/share', mockAuthMiddleware, notesController.shareNoteToWorkspace.bind(notesController));
  app.post('/api/notes/:id/copy', mockAuthMiddleware, notesController.copyNoteToWorkspace.bind(notesController));

  return app;
}

// ---------------------------
// Test setup/teardown utilities
// ---------------------------
export interface TestData {
  testUserId: string;
  testWorkspaceId: string;
  testUser2Id: string;
  testWorkspace2Id: string;
}

export async function setupTestDatabase(): Promise<TestData> {
  // Fresh DB state
  if (mongoose.connection.db) {
    await mongoose.connection.db.dropDatabase();
  }

  // Create test users - get existing model or create new one
  let User = mongoose.models.User;
  if (!User) {
    User = mongoose.model('User', new mongoose.Schema({
      googleId: { type: String, unique: true },
      email: String,
      profile: {
        name: String,
        imagePath: String,
        description: String
      }
    }, { timestamps: true }));
  }

  const testUser = await User.create({
    googleId: 'test-google-id-1',
    email: 'testuser1@example.com',
    profile: { name: 'Test User 1', imagePath: '', description: '' },
  });
  const testUserId = testUser._id.toString();

  const testUser2 = await User.create({
    googleId: 'test-google-id-2',
    email: 'testuser2@example.com',
    profile: { name: 'Test User 2', imagePath: '', description: '' },
  });
  const testUser2Id = testUser2._id.toString();

  // Workspaces
  const testWorkspace = await workspaceModel.create({
    name: 'Test Workspace',
    profile: { imagePath: '', name: 'Test Workspace', description: 'Test workspace description' },
    ownerId: new mongoose.Types.ObjectId(testUserId),
    members: [new mongoose.Types.ObjectId(testUserId)],
  });
  const testWorkspaceId = testWorkspace._id.toString();

  const testWorkspace2 = await workspaceModel.create({
    name: 'Test Workspace 2',
    profile: { imagePath: '', name: 'Test Workspace 2', description: 'Test workspace 2 description' },
    ownerId: new mongoose.Types.ObjectId(testUser2Id),
    members: [new mongoose.Types.ObjectId(testUser2Id)],
  });
  const testWorkspace2Id = testWorkspace2._id.toString();

  return { testUserId, testWorkspaceId, testUser2Id, testWorkspace2Id };
}

