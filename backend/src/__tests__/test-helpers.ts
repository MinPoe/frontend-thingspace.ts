/// <reference types="jest" />
import mongoose from 'mongoose';
import express, { Request, Response, NextFunction, RequestHandler } from 'express';
import { NotesController } from '../notes.controller';
import { WorkspaceController } from '../workspace.controller';
import { UserController } from '../user.controller';
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
      // Try to fetch the actual user from database to get real data (e.g. personalWorkspaceId)
      let User = mongoose.models.User;
      if (!User) {
        User = mongoose.model('User', new mongoose.Schema({
          googleId: { type: String, unique: true },
          email: String,
          profile: {
            name: String,
            imagePath: String,
            description: String
          },
          personalWorkspaceId: { type: mongoose.Schema.Types.ObjectId, ref: 'Workspace' }
        }, { timestamps: true }));
      }
      
      const dbUser = await User.findById(mockUserId);
      
      if (dbUser) {
        // Use real user data if it exists in DB
        (req as any).user = {
          _id: dbUser._id,
          googleId: dbUser.googleId,
          email: dbUser.email,
          profile: dbUser.profile,
          personalWorkspaceId: dbUser.personalWorkspaceId || null,
          createdAt: dbUser.createdAt,
          updatedAt: dbUser.updatedAt,
        };
      } else {
        // Fallback to mock user if not in DB
        (req as any).user = {
          _id: new mongoose.Types.ObjectId(mockUserId),
          googleId: 'test-google-id',
          email: 'test@example.com',
          profile: { name: 'Test User', imagePath: '', description: '' },
          personalWorkspaceId: null,
          createdAt: new Date(),
          updatedAt: new Date(),
        };
      }
      return next();
    }
    return res.status(401).json({ error: 'No test user ID provided' });
  };

  // Pass-through validation middleware (validation tested elsewhere)
  const mockValidateBody = (_req: Request, _res: Response, next: NextFunction) => next();

  // Controllers
  const notesController = new NotesController();
  const workspaceController = new WorkspaceController();
  const userController = new UserController();

  // ---------------------------
  // Route mounting (ORDER MATTERS)
  // ---------------------------
  // Workspace routes FIRST - Put the more specific routes BEFORE the generic :id route.
  // This prevents workspace routes from being matched by notes routes
  app.get('/api/workspaces/user', mockAuthMiddleware, workspaceController.getWorkspacesForUser.bind(workspaceController));
  app.get('/api/workspaces/personal', mockAuthMiddleware, workspaceController.getPersonalWorkspace.bind(workspaceController));
  app.get('/api/workspaces/:id/members', mockAuthMiddleware, workspaceController.getWorkspaceMembers.bind(workspaceController));
  app.get('/api/workspaces/:id/tags', mockAuthMiddleware, workspaceController.getAllTags.bind(workspaceController));
  app.get('/api/workspaces/:id/membership/:userId', mockAuthMiddleware, workspaceController.getMembershipStatus.bind(workspaceController));
  app.get('/api/workspaces/:id/poll', mockAuthMiddleware, workspaceController.pollForNewMessages.bind(workspaceController));
  app.post('/api/workspaces', mockAuthMiddleware, mockValidateBody, workspaceController.createWorkspace.bind(workspaceController));
  app.post('/api/workspaces/:id/members', mockAuthMiddleware, workspaceController.inviteMember.bind(workspaceController));
  app.post('/api/workspaces/:id/leave', mockAuthMiddleware, workspaceController.leaveWorkspace.bind(workspaceController));
  app.put('/api/workspaces/:id/picture', mockAuthMiddleware, mockValidateBody, workspaceController.updateWorkspacePicture.bind(workspaceController));
  app.put('/api/workspaces/:id', mockAuthMiddleware, mockValidateBody, workspaceController.updateWorkspaceProfile.bind(workspaceController));
  app.delete('/api/workspaces/:id/members/:userId', mockAuthMiddleware, workspaceController.banMember.bind(workspaceController));
  app.delete('/api/workspaces/:id', mockAuthMiddleware, workspaceController.deleteWorkspace.bind(workspaceController));
  app.get('/api/workspaces/:id', mockAuthMiddleware, workspaceController.getWorkspace.bind(workspaceController));

  // Notes routes - Put the more specific route BEFORE the generic :id route.
  app.post('/api/notes', mockAuthMiddleware, mockValidateBody, notesController.createNote.bind(notesController));
  app.get('/api/notes', mockAuthMiddleware, notesController.findNotes.bind(notesController));
  app.get('/api/notes/:id/workspaces', mockAuthMiddleware, notesController.getWorkspacesForNote.bind(notesController));
  app.post('/api/notes/:id/share', mockAuthMiddleware, notesController.shareNoteToWorkspace.bind(notesController));
  app.post('/api/notes/:id/copy', mockAuthMiddleware, notesController.copyNoteToWorkspace.bind(notesController));
  app.put('/api/notes/:id', mockAuthMiddleware, mockValidateBody, notesController.updateNote.bind(notesController));
  app.delete('/api/notes/:id', mockAuthMiddleware, notesController.deleteNote.bind(notesController));
  app.get('/api/notes/:id', mockAuthMiddleware, notesController.getNote.bind(notesController));

  // User routes - Put specific routes before generic :id route
  app.get('/api/users/profile', mockAuthMiddleware, userController.getProfile.bind(userController));
  app.put('/api/users/profile', mockAuthMiddleware, mockValidateBody, userController.updateProfile.bind(userController));
  app.delete('/api/users/profile', mockAuthMiddleware, userController.deleteProfile.bind(userController));
  app.post('/api/users/fcm-token', mockAuthMiddleware, mockValidateBody, userController.updateFcmToken.bind(userController));
  app.get('/api/users/email/:email', mockAuthMiddleware, userController.getUserByEmail.bind(userController));
  app.get('/api/users/:id', mockAuthMiddleware, userController.getUserById.bind(userController));

  // Message routes - messageRouter uses authenticateToken, so we apply mockAuthMiddleware 
  // instead of the router's authenticateToken. The router's authenticateToken would fail 
  // because it requires a real JWT token. We apply mockAuthMiddleware before the router.
  // Note: The router still has authenticateToken in its route definitions, so we need to
  // mock it at module level. This should be done in the test files that use message routes.
  const { messageRouter } = require('../message.routes');
  app.use('/api/messages', mockAuthMiddleware, messageRouter);

  // Media routes
  const MediaController = require('../media.controller').MediaController;
  const mediaController = new MediaController();
  const { upload } = require('../storage');
  app.post('/api/media/upload', mockAuthMiddleware, upload.single('media'), mediaController.uploadImage.bind(mediaController));

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

