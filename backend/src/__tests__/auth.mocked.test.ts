/// <reference types="jest" />
import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';
import jwt from 'jsonwebtoken';

import { authService, AuthService } from '../auth.service';
import { workspaceService } from '../workspace.service';
import { userModel } from '../user.model';
import { setupTestDatabase, TestData } from './test-helpers';

// Helper to create auth test app
function createAuthTestApp() {
  const express = require('express');
  const app = express();
  app.use(express.json());

  const authRoutes = require('../auth.routes').default;
  app.use('/api/auth', authRoutes);

  // Error handler for tests
  app.use((err: any, req: any, res: any, next: any) => {
    res.status(err.status || 500).json({
      message: err.message || 'Internal server error',
    });
  });

  return app;
}

// ---------------------------
// Test suite
// ---------------------------
describe('Auth API – Mocked Tests (Jest Mocks)', () => {
  let mongo: MongoMemoryServer;
  let testData: TestData;

  // Spin up in-memory Mongo
  beforeAll(async () => {
    mongo = await MongoMemoryServer.create();
    const uri = mongo.getUri();
    await mongoose.connect(uri);
    console.log('✅ Connected to in-memory MongoDB');

    // Ensure JWT_SECRET is set
    if (!process.env.JWT_SECRET) {
      process.env.JWT_SECRET = 'test-jwt-secret-key-for-testing-only';
    }
    if (!process.env.GOOGLE_CLIENT_ID) {
      process.env.GOOGLE_CLIENT_ID = 'test-google-client-id';
    }
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

  let app: ReturnType<typeof createAuthTestApp>;

  // Fresh DB state before each test
  beforeEach(async () => {
    testData = await setupTestDatabase();
    // Clear module cache and recreate app to ensure fresh imports
    delete require.cache[require.resolve('../auth.routes')];
    delete require.cache[require.resolve('../auth.controller')];
    delete require.cache[require.resolve('../auth.service')];
    app = createAuthTestApp();
  });

  describe('POST /api/auth/signup - Sign Up, with mocks', () => {
    test('401 – returns 401 when Google token is invalid', async () => {
      // Mocked behavior: authService.signUpWithGoogle throws Invalid Google token error
      // Input: invalid Google idToken
      // Expected status code: 401
      // Expected behavior: error handled gracefully
      // Expected output: error message "Invalid Google token"
      jest.spyOn(authService, 'signUpWithGoogle').mockRejectedValueOnce(new Error('Invalid Google token'));

      const res = await request(app)
        .post('/api/auth/signup')
        .send({ idToken: 'invalid-token' });

      expect(res.status).toBe(401);
      expect(res.body.message).toBe('Invalid Google token');
    });

    test('409 – returns 409 when user already exists', async () => {
      // Mocked behavior: authService.signUpWithGoogle throws User already exists error
      // Input: valid Google idToken for existing user
      // Expected status code: 409
      // Expected behavior: error handled gracefully
      // Expected output: error message "User already exists"
      jest.spyOn(authService, 'signUpWithGoogle').mockRejectedValueOnce(new Error('User already exists'));

      const res = await request(app)
        .post('/api/auth/signup')
        .send({ idToken: 'valid-token' });

      expect(res.status).toBe(409);
      expect(res.body.message).toContain('already exists');
    });

    test('500 – returns 500 when service throws Failed to process user', async () => {
      // Mocked behavior: authService.signUpWithGoogle throws Failed to process user error
      // Input: valid Google idToken for new user
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message
      jest.spyOn(authService, 'signUpWithGoogle').mockRejectedValueOnce(new Error('Failed to process user'));

      const res = await request(app)
        .post('/api/auth/signup')
        .send({ idToken: 'valid-token' });

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Failed to process user');
    });

    test('500 – returns 500 when service throws generic error', async () => {
      // Mocked behavior: authService.signUpWithGoogle throws generic error
      // Input: valid Google idToken
      // Expected status code: 500
      // Expected behavior: error passed to next()
      // Expected output: error handled by error handler
      jest.spyOn(authService, 'signUpWithGoogle').mockRejectedValueOnce(new Error('Unexpected error'));

      const res = await request(app)
        .post('/api/auth/signup')
        .send({ idToken: 'valid-token' });

      expect(res.status).toBeGreaterThanOrEqual(500);
    });

    test('500 – handles non-Error thrown value', async () => {
      // Mocked behavior: authService.signUpWithGoogle throws non-Error value
      // Input: valid Google idToken
      // Expected status code: 500 or handled by error handler
      // Expected behavior: next(error) called
      // Expected output: error handled by error handler
      jest.spyOn(authService, 'signUpWithGoogle').mockRejectedValueOnce('String error');

      const res = await request(app)
        .post('/api/auth/signup')
        .send({ idToken: 'valid-token' });

      expect(res.status).toBeGreaterThanOrEqual(500);
    });

    test('201 – returns 201 when signup succeeds and personal workspace is created', async () => {
      // Mocked behavior: authService.signUpWithGoogle succeeds, workspaceService.createWorkspace succeeds, userModel.updatePersonalWorkspace succeeds
      // Input: valid Google idToken
      // Expected status code: 201
      // Expected behavior: user created, personal workspace created, user updated with workspace ID
      // Expected output: success response with token and user data (tests auth.controller.ts lines 28-38)
      const mockUser = {
        _id: new mongoose.Types.ObjectId(),
        googleId: 'test-google-id',
        email: 'test@example.com',
        name: 'Test User',
        profile: {
          imagePath: 'https://example.com/image.jpg',
          name: 'Test User',
          description: '',
        },
        profilePicture: 'https://example.com/image.jpg',
        fcmToken: undefined,
        personalWorkspaceId: undefined,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      const mockWorkspace = {
        _id: new mongoose.Types.ObjectId(),
        name: "Test User's Personal Workspace",
        profile: {
          imagePath: 'https://example.com/image.jpg',
          name: "Test User's Personal Workspace",
          description: 'Your personal workspace for all your personal notes',
        },
        ownerId: mockUser._id,
        members: [mockUser._id],
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      jest.spyOn(authService, 'signUpWithGoogle').mockResolvedValueOnce({
        token: 'mock-token',
        user: mockUser as any,
      });
      jest.spyOn(workspaceService, 'createWorkspace').mockResolvedValueOnce(mockWorkspace as any);
      jest.spyOn(userModel, 'updatePersonalWorkspace').mockResolvedValueOnce(mockUser as any);

      const res = await request(app)
        .post('/api/auth/signup')
        .send({ idToken: 'valid-token' });

      expect(res.status).toBe(201);
      expect(res.body.message).toBe('User signed up successfully');
      expect(res.body.data).toBeDefined();
      expect(res.body.data.token).toBe('mock-token');
      expect(res.body.data.user).toBeDefined();
      
      // Verify workspaceService.createWorkspace was called with correct parameters
      expect(workspaceService.createWorkspace).toHaveBeenCalledWith(
        mockUser._id,
        {
          name: "Test User's Personal Workspace",
          profilePicture: 'https://example.com/image.jpg',
          description: 'Your personal workspace for all your personal notes',
        }
      );
      
      // Verify userModel.updatePersonalWorkspace was called with correct parameters
      expect(userModel.updatePersonalWorkspace).toHaveBeenCalledWith(
        mockUser._id,
        new mongoose.Types.ObjectId(mockWorkspace._id)
      );
    });

    test('201 – returns 201 when signup succeeds with empty profile imagePath (fallback to empty string)', async () => {
      // Mocked behavior: authService.signUpWithGoogle succeeds with user that has no imagePath
      // Input: valid Google idToken
      // Expected status code: 201
      // Expected behavior: workspace created with empty string for profilePicture (tests auth.controller.ts line 25 fallback)
      // Expected output: success response (tests auth.controller.ts line 25 || '' branch)
      const mockUserNoImage = {
        _id: new mongoose.Types.ObjectId(),
        googleId: 'test-google-id-2',
        email: 'test2@example.com',
        name: 'Test User 2',
        profile: {
          imagePath: undefined, // No image path
          name: 'Test User 2',
          description: '',
        },
        profilePicture: undefined,
        fcmToken: undefined,
        personalWorkspaceId: undefined,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      const mockWorkspaceNoImage = {
        _id: new mongoose.Types.ObjectId(),
        name: "Test User 2's Personal Workspace",
        profile: {
          imagePath: '',
          name: "Test User 2's Personal Workspace",
          description: 'Your personal workspace for all your personal notes',
        },
        ownerId: mockUserNoImage._id,
        members: [mockUserNoImage._id],
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      jest.spyOn(authService, 'signUpWithGoogle').mockResolvedValueOnce({
        token: 'mock-token-2',
        user: mockUserNoImage as any,
      });
      jest.spyOn(workspaceService, 'createWorkspace').mockResolvedValueOnce(mockWorkspaceNoImage as any);
      jest.spyOn(userModel, 'updatePersonalWorkspace').mockResolvedValueOnce(mockUserNoImage as any);

      const res = await request(app)
        .post('/api/auth/signup')
        .send({ idToken: 'valid-token-2' });

      expect(res.status).toBe(201);
      expect(res.body.message).toBe('User signed up successfully');
      
      // Verify workspaceService.createWorkspace was called with empty string for profilePicture (fallback)
      expect(workspaceService.createWorkspace).toHaveBeenCalledWith(
        mockUserNoImage._id,
        {
          name: "Test User 2's Personal Workspace",
          profilePicture: '', // Should be empty string when imagePath is undefined
          description: 'Your personal workspace for all your personal notes',
        }
      );
    });
  });

  describe('POST /api/auth/signin - Sign In, with mocks', () => {
    test('401 – returns 401 when Google token is invalid', async () => {
      // Mocked behavior: authService.signInWithGoogle throws Invalid Google token error
      // Input: invalid Google idToken
      // Expected status code: 401
      // Expected behavior: error handled gracefully
      // Expected output: error message "Invalid Google token"
      jest.spyOn(authService, 'signInWithGoogle').mockRejectedValueOnce(new Error('Invalid Google token'));

      const res = await request(app)
        .post('/api/auth/signin')
        .send({ idToken: 'invalid-token' });

      expect(res.status).toBe(401);
      expect(res.body.message).toBe('Invalid Google token');
    });

    test('404 – returns 404 when user not found', async () => {
      // Mocked behavior: authService.signInWithGoogle throws User not found error
      // Input: valid Google idToken for non-existent user
      // Expected status code: 404
      // Expected behavior: error handled gracefully
      // Expected output: error message "User not found"
      jest.spyOn(authService, 'signInWithGoogle').mockRejectedValueOnce(new Error('User not found'));

      const res = await request(app)
        .post('/api/auth/signin')
        .send({ idToken: 'valid-token' });

      expect(res.status).toBe(404);
      expect(res.body.message).toContain('not found');
    });

    test('500 – returns 500 when service throws Failed to process user', async () => {
      // Mocked behavior: authService.signInWithGoogle throws Failed to process user error
      // Input: valid Google idToken
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message
      jest.spyOn(authService, 'signInWithGoogle').mockRejectedValueOnce(new Error('Failed to process user'));

      const res = await request(app)
        .post('/api/auth/signin')
        .send({ idToken: 'valid-token' });

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Failed to process user');
    });

    test('500 – handles non-Error thrown value', async () => {
      // Mocked behavior: authService.signInWithGoogle throws non-Error value
      // Input: valid Google idToken
      // Expected status code: 500 or handled by error handler
      // Expected behavior: next(error) called
      // Expected output: error handled by error handler
      jest.spyOn(authService, 'signInWithGoogle').mockRejectedValueOnce('String error');

      const res = await request(app)
        .post('/api/auth/signin')
        .send({ idToken: 'valid-token' });

      expect(res.status).toBeGreaterThanOrEqual(500);
    });

    test('200 – returns 200 when signin succeeds', async () => {
      // Mocked behavior: authService.signInWithGoogle succeeds
      // Input: valid Google idToken
      // Expected status code: 200
      // Expected behavior: user signed in successfully
      // Expected output: success response with token and user data (tests auth.controller.ts line 79)
      const mockUser = {
        _id: new mongoose.Types.ObjectId(),
        googleId: 'test-google-id',
        email: 'test@example.com',
        name: 'Test User',
        profile: {
          imagePath: 'https://example.com/image.jpg',
          name: 'Test User',
          description: '',
        },
        profilePicture: 'https://example.com/image.jpg',
        fcmToken: undefined,
        personalWorkspaceId: new mongoose.Types.ObjectId(),
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      jest.spyOn(authService, 'signInWithGoogle').mockResolvedValueOnce({
        token: 'mock-token',
        user: mockUser as any,
      });

      const res = await request(app)
        .post('/api/auth/signin')
        .send({ idToken: 'valid-token' });

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('User signed in successfully');
      expect(res.body.data).toBeDefined();
      expect(res.body.data.token).toBe('mock-token');
      expect(res.body.data.user).toBeDefined();
    });
  });

  describe('POST /api/auth/dev-login - Dev Login, with mocks', () => {
    test('500 – returns 500 when service throws error', async () => {
      // Mocked behavior: authService.devLogin throws error
      // Input: email for new user
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message
      jest.spyOn(authService, 'devLogin').mockRejectedValueOnce(new Error('Database error'));

      const res = await request(app)
        .post('/api/auth/dev-login')
        .send({ email: 'dev-error@example.com' });

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Database error');
    });

    test('500 – handles non-Error thrown value', async () => {
      // Mocked behavior: authService.devLogin throws non-Error value
      // Input: email
      // Expected status code: 500
      // Expected behavior: error handled gracefully
      // Expected output: error message
      jest.spyOn(authService, 'devLogin').mockRejectedValueOnce('String error');

      const res = await request(app)
        .post('/api/auth/dev-login')
        .send({ email: 'dev-error@example.com' });

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Dev login failed');
    });

    test('500 – handles Error with empty message (covers error.message || fallback)', async () => {
      // Mocked behavior: authService.devLogin throws Error with empty message
      // Input: email
      // Expected status code: 500
      // Expected behavior: error.message is empty, so fallback to 'Dev login failed'
      // Expected output: 'Dev login failed' (tests auth.controller.ts line 126)
      const errorWithEmptyMessage = new Error('');
      errorWithEmptyMessage.message = ''; // Explicitly set empty message
      jest.spyOn(authService, 'devLogin').mockRejectedValueOnce(errorWithEmptyMessage);

      const res = await request(app)
        .post('/api/auth/dev-login')
        .send({ email: 'dev-error@example.com' });

      expect(res.status).toBe(500);
      expect(res.body.message).toBe('Dev login failed');
    });
  });

  describe('Auth Service - Direct Service Tests with Mocks', () => {
    test('signUpWithGoogle throws error when user already exists', async () => {
      // Input: valid Google token for existing user
      // Expected behavior: Throws "User already exists" error
      // Expected output: Error thrown
      
      // Create a user first
      const existingUser = await userModel.create({
        googleId: 'test-google-id-123',
        email: 'existing@example.com',
        name: 'Existing User',
        profilePicture: '',
      });

      // Mock Google verification to return user info matching existing user
      const mockTicket = {
        getPayload: jest.fn().mockReturnValue({
          sub: existingUser.googleId,
          email: 'different@example.com',
          name: 'Different Name',
          picture: '',
        }),
      };

      const mockVerifyIdToken = jest.fn().mockResolvedValue(mockTicket);
      const mockGoogleClient = {
        verifyIdToken: mockVerifyIdToken,
      } as any;

      // Create a new service instance with mocked Google client
      const serviceInstance = new AuthService();
      (serviceInstance as any).googleClient = mockGoogleClient;

      await expect(
        serviceInstance.signUpWithGoogle('valid-token')
      ).rejects.toThrow('User already exists');
    });

    test('signInWithGoogle throws error when user not found', async () => {
      // Input: valid Google token for non-existent user
      // Expected behavior: Throws "User not found" error
      // Expected output: Error thrown
      
      // Mock Google verification to return user info for non-existent user
      const mockTicket = {
        getPayload: jest.fn().mockReturnValue({
          sub: 'non-existent-google-id',
          email: 'nonexistent@example.com',
          name: 'Non Existent',
          picture: '',
        }),
      };

      const mockVerifyIdToken = jest.fn().mockResolvedValue(mockTicket);
      const mockGoogleClient = {
        verifyIdToken: mockVerifyIdToken,
      } as any;

      // Create a new service instance with mocked Google client
      const serviceInstance = new AuthService();
      (serviceInstance as any).googleClient = mockGoogleClient;

      await expect(
        serviceInstance.signInWithGoogle('valid-token')
      ).rejects.toThrow('User not found');
    });

    test('signUpWithGoogle throws error when Google verification fails', async () => {
      // Input: invalid Google token
      // Expected behavior: Throws "Invalid Google token" error
      // Expected output: Error thrown
      
      const mockVerifyIdToken = jest.fn().mockRejectedValue(new Error('Google verification failed'));
      const mockGoogleClient = {
        verifyIdToken: mockVerifyIdToken,
      } as any;

      // Create a new service instance with mocked Google client
      const serviceInstance = new AuthService();
      (serviceInstance as any).googleClient = mockGoogleClient;

      await expect(
        serviceInstance.signUpWithGoogle('invalid-token')
      ).rejects.toThrow('Invalid Google token');
    });

    test('signUpWithGoogle throws error when payload is null', async () => {
      // Input: Google token that returns null payload
      // Expected behavior: Throws "Invalid Google token" error
      // Expected output: Error thrown
      
      const mockTicket = {
        getPayload: jest.fn().mockReturnValue(null),
      };

      const mockVerifyIdToken = jest.fn().mockResolvedValue(mockTicket);
      const mockGoogleClient = {
        verifyIdToken: mockVerifyIdToken,
      } as any;

      // Create a new service instance with mocked Google client
      const serviceInstance = new AuthService();
      (serviceInstance as any).googleClient = mockGoogleClient;

      await expect(
        serviceInstance.signUpWithGoogle('token-with-null-payload')
      ).rejects.toThrow('Invalid Google token');
    });

    test('signUpWithGoogle throws error when payload missing email', async () => {
      // Input: Google token with payload missing email
      // Expected behavior: Throws "Invalid Google token" error
      // Expected output: Error thrown
      
      const mockTicket = {
        getPayload: jest.fn().mockReturnValue({
          sub: 'test-google-id',
          name: 'Test User',
          picture: '',
          // email is missing
        }),
      };

      const mockVerifyIdToken = jest.fn().mockResolvedValue(mockTicket);
      const mockGoogleClient = {
        verifyIdToken: mockVerifyIdToken,
      } as any;

      // Create a new service instance with mocked Google client
      const serviceInstance = new AuthService();
      (serviceInstance as any).googleClient = mockGoogleClient;

      await expect(
        serviceInstance.signUpWithGoogle('token-missing-email')
      ).rejects.toThrow('Invalid Google token');
    });

    test('signUpWithGoogle throws error when payload missing name', async () => {
      // Input: Google token with payload missing name
      // Expected behavior: Throws "Invalid Google token" error
      // Expected output: Error thrown
      
      const mockTicket = {
        getPayload: jest.fn().mockReturnValue({
          sub: 'test-google-id',
          email: 'test@example.com',
          picture: '',
          // name is missing
        }),
      };

      const mockVerifyIdToken = jest.fn().mockResolvedValue(mockTicket);
      const mockGoogleClient = {
        verifyIdToken: mockVerifyIdToken,
      } as any;

      // Create a new service instance with mocked Google client
      const serviceInstance = new AuthService();
      (serviceInstance as any).googleClient = mockGoogleClient;

      await expect(
        serviceInstance.signUpWithGoogle('token-missing-name')
      ).rejects.toThrow('Invalid Google token');
    });

    test('signInWithGoogle throws error when Google verification fails', async () => {
      // Input: invalid Google token
      // Expected behavior: Throws "Invalid Google token" error
      // Expected output: Error thrown
      
      const mockVerifyIdToken = jest.fn().mockRejectedValue(new Error('Google verification failed'));
      const mockGoogleClient = {
        verifyIdToken: mockVerifyIdToken,
      } as any;

      // Create a new service instance with mocked Google client
      const serviceInstance = new AuthService();
      (serviceInstance as any).googleClient = mockGoogleClient;

      await expect(
        serviceInstance.signInWithGoogle('invalid-token')
      ).rejects.toThrow('Invalid Google token');
    });

    test('signUpWithGoogle successfully creates user and generates token', async () => {
      // Input: valid Google token for new user
      // Expected behavior: User created, token generated (tests auth.service.ts lines 71-74)
      // Expected output: AuthResult with token and user
      const mockTicket = {
        getPayload: jest.fn().mockReturnValue({
          sub: 'new-google-id',
          email: 'newuser@example.com',
          name: 'New User',
          picture: 'https://example.com/pic.jpg',
        }),
      };

      const mockVerifyIdToken = jest.fn().mockResolvedValue(mockTicket);
      const mockGoogleClient = {
        verifyIdToken: mockVerifyIdToken,
      } as any;

      // Create a new service instance with mocked Google client
      const serviceInstance = new AuthService();
      (serviceInstance as any).googleClient = mockGoogleClient;

      const result = await serviceInstance.signUpWithGoogle('valid-token');

      expect(result).toBeDefined();
      expect(result.token).toBeDefined();
      expect(result.user).toBeDefined();
      expect(result.user.email).toBe('newuser@example.com');
    });

    test('signInWithGoogle successfully generates token for existing user', async () => {
      // Input: valid Google token for existing user
      // Expected behavior: Token generated (tests auth.service.ts lines 91-93)
      // Expected output: AuthResult with token and user
      // Create a user first
      const existingUser = await userModel.create({
        googleId: 'existing-google-id',
        email: 'existing@example.com',
        name: 'Existing User',
        profilePicture: '',
      });

      const mockTicket = {
        getPayload: jest.fn().mockReturnValue({
          sub: existingUser.googleId,
          email: 'existing@example.com',
          name: 'Existing User',
          picture: '',
        }),
      };

      const mockVerifyIdToken = jest.fn().mockResolvedValue(mockTicket);
      const mockGoogleClient = {
        verifyIdToken: mockVerifyIdToken,
      } as any;

      // Create a new service instance with mocked Google client
      const serviceInstance = new AuthService();
      (serviceInstance as any).googleClient = mockGoogleClient;

      const result = await serviceInstance.signInWithGoogle('valid-token');

      expect(result).toBeDefined();
      expect(result.token).toBeDefined();
      expect(result.user).toBeDefined();
      expect(result.user._id.toString()).toBe(existingUser._id.toString());
    });

    test('generateAccessToken throws error when JWT_SECRET not configured', async () => {
      // Input: JWT_SECRET not set
      // Expected behavior: Throws "JWT_SECRET not configured" error (tests auth.service.ts line 47)
      // Expected output: Error thrown
      const originalSecret = process.env.JWT_SECRET;
      delete process.env.JWT_SECRET;

      // Use a unique googleId that doesn't exist
      const uniqueGoogleId = `test-google-id-jwt-secret-${Date.now()}`;

      // Clear module cache and re-import to get fresh JWT_SECRET value
      jest.resetModules();
      // eslint-disable-next-line @typescript-eslint/no-require-imports
      const { AuthService: FreshAuthService } = require('../auth.service');
      // eslint-disable-next-line @typescript-eslint/no-require-imports
      const { userModel: freshUserModel } = require('../user.model');
      const serviceInstance = new FreshAuthService();

      // Mock Google client to avoid verification issues
      const mockTicket = {
        getPayload: jest.fn().mockReturnValue({
          sub: uniqueGoogleId,
          email: 'test-jwt-secret@example.com',
          name: 'Test User JWT',
          picture: '',
        }),
      };
      const mockVerifyIdToken = jest.fn().mockResolvedValue(mockTicket);
      (serviceInstance as any).googleClient = {
        verifyIdToken: mockVerifyIdToken,
      };

      // Mock findByGoogleId to return null (user doesn't exist)
      jest.spyOn(freshUserModel, 'findByGoogleId').mockResolvedValueOnce(null);
      // Mock create to return a user
      const mockUser = {
        _id: new mongoose.Types.ObjectId(),
        googleId: uniqueGoogleId,
        email: 'test-jwt-secret@example.com',
        name: 'Test User JWT',
        profilePicture: '',
      };
      jest.spyOn(freshUserModel, 'create').mockResolvedValueOnce(mockUser as any);

      await expect(
        serviceInstance.signUpWithGoogle('valid-token')
      ).rejects.toThrow('JWT_SECRET not configured');

      // Restore JWT_SECRET
      if (originalSecret) {
        process.env.JWT_SECRET = originalSecret;
      }
      jest.resetModules();
      // Re-import to restore
      // eslint-disable-next-line @typescript-eslint/no-require-imports
      require('../auth.service');
      // eslint-disable-next-line @typescript-eslint/no-require-imports
      require('../user.model');
    });

    test('generateAccessToken handles token generation failure', async () => {
      // Input: jwt.sign returns non-string (tests auth.service.ts line 53)
      // Expected behavior: Throws "Failed to generate token" error
      // Expected output: Error thrown
      // Use a unique googleId that doesn't exist
      const uniqueGoogleId = `test-google-id-token-fail-${Date.now()}`;

      // Mock Google client
      const mockTicket = {
        getPayload: jest.fn().mockReturnValue({
          sub: uniqueGoogleId,
          email: `test-token-fail-${Date.now()}@example.com`,
          name: 'Test User Token Fail',
          picture: '',
        }),
      };
      const mockVerifyIdToken = jest.fn().mockResolvedValue(mockTicket);
      const mockGoogleClient = {
        verifyIdToken: mockVerifyIdToken,
      } as any;

      const serviceInstance = new AuthService();
      (serviceInstance as any).googleClient = mockGoogleClient;

      // Mock findByGoogleId to return null (user doesn't exist)
      jest.spyOn(userModel, 'findByGoogleId').mockResolvedValueOnce(null);
      // Mock create to return a user
      const mockUser = {
        _id: new mongoose.Types.ObjectId(),
        googleId: uniqueGoogleId,
        email: `test-token-fail-${Date.now()}@example.com`,
        name: 'Test User Token Fail',
        profilePicture: '',
      };
      jest.spyOn(userModel, 'create').mockResolvedValueOnce(mockUser as any);

      // Mock jwt.sign to return null - spy on the same module that auth.service uses
      const signSpy = jest.spyOn(jwt, 'sign').mockReturnValue(null as any);

      await expect(
        serviceInstance.signUpWithGoogle('valid-token')
      ).rejects.toThrow('Failed to generate token');

      // Verify jwt.sign was called
      expect(signSpy).toHaveBeenCalled();

      // Restore
      signSpy.mockRestore();
      jest.restoreAllMocks();
    });
  });
});

