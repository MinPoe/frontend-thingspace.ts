/// <reference types="jest" />
// Set JWT_SECRET before importing middleware (since it's captured at module load time)
if (!process.env.JWT_SECRET) {
  process.env.JWT_SECRET = 'test-jwt-secret-key-for-testing-only';
}

import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';
import jwt from 'jsonwebtoken';
import express, { Request, Response, NextFunction } from 'express';

import { authenticateToken, authMiddleware } from '../auth.middleware';
import { userModel } from '../user.model';
import { createTestApp, setupTestDatabase, TestData } from './test-helpers';

// Create a test app that uses the REAL auth middleware
function createAuthTestApp() {
  const app = express();
  app.use(express.json());

  // Use the REAL authenticateToken middleware
  app.use('/api/protected', authenticateToken);

  // Test endpoint that requires auth
  app.get('/api/protected/test', (req: Request, res: Response) => {
    res.status(200).json({ message: 'Authenticated', user: req.user });
  });

  return app;
}

// Create a test app that uses the REAL authMiddleware (alternative middleware)
function createAuthMiddlewareTestApp() {
  const app = express();
  app.use(express.json());

  // Use the REAL authMiddleware
  app.use('/api/protected2', authMiddleware);

  // Test endpoint that requires auth
  app.get('/api/protected2/test', (req: Request, res: Response) => {
    res.status(200).json({ message: 'Authenticated', user: req.user });
  });

  // Error handler for tests
  app.use((err: any, req: Request, res: Response, next: NextFunction) => {
    res.status(err.status || 500).json({
      error: err.message || 'Internal server error',
    });
  });

  return app;
}

// ---------------------------
// Test suite
// ---------------------------
describe('Auth Middleware – Real Middleware Tests', () => {
  let mongo: MongoMemoryServer;
  let testData: TestData;
  let jwtSecret: string | undefined;
  let app: express.Application;
  let app2: express.Application;

  // Spin up in-memory Mongo
  beforeAll(async () => {
    mongo = await MongoMemoryServer.create();
    const uri = mongo.getUri();
    await mongoose.connect(uri);
    console.log('✅ Connected to in-memory MongoDB');

    // Ensure JWT_SECRET is set
    jwtSecret = process.env.JWT_SECRET;
    if (!jwtSecret) {
      process.env.JWT_SECRET = 'test-jwt-secret-key-for-testing-only';
      jwtSecret = process.env.JWT_SECRET;
    }

    // Create apps after JWT_SECRET is set
    app = createAuthTestApp();
    app2 = createAuthMiddlewareTestApp();
  });

  // Tear down DB
  afterAll(async () => {
    await mongoose.disconnect();
    await mongo.stop();
    // Restore JWT_SECRET if it wasn't set originally
    if (!jwtSecret) {
      delete process.env.JWT_SECRET;
    }
  });

  // Fresh DB state before each test
  beforeEach(async () => {
    testData = await setupTestDatabase();
  });

  describe('authenticateToken middleware', () => {
    test('401 – returns 401 when no token provided', async () => {
      // Input: request without authorization header
      // Expected status code: 401
      // Expected behavior: error message returned
      // Expected output: error message
      const res = await request(app)
        .get('/api/protected/test');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('Access denied');
      expect(res.body.message).toBe('No token provided');
    });

    test('401 – returns 401 when authorization header is missing', async () => {
      // Input: request without Bearer token format
      // Expected status code: 401
      // Expected behavior: error message returned
      // Expected output: error message
      const res = await request(app)
        .get('/api/protected/test')
        .set('authorization', '');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('Access denied');
      expect(res.body.message).toBe('No token provided');
    });

    test('401 – returns 401 when token is invalid (malformed)', async () => {
      // Input: invalid/malformed JWT token
      // Expected status code: 401
      // Expected behavior: error message returned
      // Expected output: error message
      const res = await request(app)
        .get('/api/protected/test')
        .set('authorization', 'Bearer invalid-token-123');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('Invalid token');
      expect(res.body.message).toBe('Token is malformed or expired');
    });

    test('401 – returns 401 when token is expired', async () => {
      // Input: expired JWT token
      // Expected status code: 401
      // Expected behavior: error message returned
      // Expected output: error message
      const expiredToken = jwt.sign(
        { id: testData.testUserId },
        process.env.JWT_SECRET!,
        { expiresIn: '-1h' } // Expired 1 hour ago
      );

      const res = await request(app)
        .get('/api/protected/test')
        .set('authorization', `Bearer ${expiredToken}`);

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('Token expired');
      expect(res.body.message).toBe('Please login again');
    });

    test('401 – returns 401 when decoded token has no id', async () => {
      // Input: valid JWT token but without id field
      // Expected status code: 401
      // Expected behavior: error message returned
      // Expected output: error message
      const tokenWithoutId = jwt.sign(
        { someOtherField: 'value' },
        process.env.JWT_SECRET!
      );

      const res = await request(app)
        .get('/api/protected/test')
        .set('authorization', `Bearer ${tokenWithoutId}`);

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('Invalid token');
      expect(res.body.message).toBe('Token verification failed');
    });

    test('401 – returns 401 when user not found (token valid but user deleted)', async () => {
      // Input: valid JWT token but user doesn't exist
      // Expected status code: 401
      // Expected behavior: error message returned
      // Expected output: error message
      const fakeUserId = new mongoose.Types.ObjectId();
      const token = jwt.sign(
        { id: fakeUserId.toString() },
        process.env.JWT_SECRET!
      );

      const res = await request(app)
        .get('/api/protected/test')
        .set('authorization', `Bearer ${token}`);

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('User not found');
      expect(res.body.message).toBe('Token is valid but user no longer exists');
    });

    test('200 – allows request when token is valid and user exists', async () => {
      // Input: valid JWT token with valid user ID
      // Expected status code: 200
      // Expected behavior: request proceeds to handler
      // Expected output: success response with user data
      const token = jwt.sign(
        { id: testData.testUserId },
        process.env.JWT_SECRET!
      );

      const res = await request(app)
        .get('/api/protected/test')
        .set('authorization', `Bearer ${token}`);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Authenticated');
      expect(res.body.user).toBeDefined();
      expect(res.body.user._id).toBe(testData.testUserId);
    });

    test('200 – handles token with Bearer prefix', async () => {
      // Input: valid JWT token with "Bearer " prefix
      // Expected status code: 200
      // Expected behavior: token extracted correctly
      // Expected output: success response
      const token = jwt.sign(
        { id: testData.testUserId },
        process.env.JWT_SECRET!
      );

      const res = await request(app)
        .get('/api/protected/test')
        .set('authorization', `Bearer ${token}`);

      expect(res.status).toBe(200);
      expect(res.body.user).toBeDefined();
    });

    test('401 – handles authorization header without Bearer prefix', async () => {
      // Input: token without "Bearer " prefix
      // Expected status code: 401
      // Expected behavior: token extraction fails (token is undefined)
      // Expected output: error message "Access denied"
      const token = jwt.sign(
        { id: testData.testUserId },
        process.env.JWT_SECRET!
      );

      const res = await request(app)
        .get('/api/protected/test')
        .set('authorization', token); // No "Bearer " prefix

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('Access denied');
      expect(res.body.message).toBe('No token provided');
    });

    test('500 – handles non-JWT errors (covers next(error) branch)', async () => {
      // Input: request that triggers a non-JWT error in middleware
      // Expected status code: depends on error handler, but error should be passed to next()
      // Expected behavior: next(error) called for non-JWT errors
      // Expected output: error handled by error handler
      // Mock userModel.findById to throw a non-JWT error
      const originalFindById = userModel.findById;
      jest.spyOn(userModel, 'findById').mockImplementationOnce(() => {
        throw new Error('Database connection failed');
      });

      const token = jwt.sign(
        { id: testData.testUserId },
        process.env.JWT_SECRET!
      );

      const res = await request(app)
        .get('/api/protected/test')
        .set('authorization', `Bearer ${token}`);

      // The error should be passed to next(error), which may be handled by Express error handler
      expect(res.status).toBeGreaterThanOrEqual(500);

      // Restore original method
      jest.restoreAllMocks();
    });

    test('401 – handles decoded as null (covers !decoded branch)', async () => {
      // Input: token that decodes to null (edge case)
      // Expected status code: 401
      // Expected behavior: error message returned
      // Expected output: error message
      // This is tricky to test since jwt.verify will throw if invalid
      // But we can test the decoded.id check
      const token = jwt.sign(
        { notId: 'something' }, // Token without 'id' field
        process.env.JWT_SECRET!
      );

      const res = await request(app)
        .get('/api/protected/test')
        .set('authorization', `Bearer ${token}`);

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('Invalid token');
      expect(res.body.message).toBe('Token verification failed');
    });
  });

  describe('authMiddleware (alternative middleware)', () => {
    test('401 – returns 401 when no token provided', async () => {
      // Input: request without authorization header
      // Expected status code: 401
      // Expected behavior: error message returned
      // Expected output: error message
      const res = await request(app2)
        .get('/api/protected2/test');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('No token provided');
    });

    test('401 – returns 401 when token is invalid', async () => {
      // Input: invalid JWT token
      // Expected status code: 401
      // Expected behavior: error message returned
      // Expected output: error message
      const res = await request(app2)
        .get('/api/protected2/test')
        .set('authorization', 'Bearer invalid-token-123');

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('Invalid token');
    });

    test('401 – returns 401 when token is expired (TokenExpiredError branch)', async () => {
      // Input: expired JWT token (tests auth.middleware.ts line 99)
      // Expected status code: 401
      // Expected behavior: TokenExpiredError caught and returns 401 with "Invalid token"
      // Expected output: error message "Invalid token"
      const expiredToken = jwt.sign(
        { id: testData.testUserId },
        process.env.JWT_SECRET!,
        { expiresIn: '-1h' } // Expired 1 hour ago
      );

      const res = await request(app2)
        .get('/api/protected2/test')
        .set('authorization', `Bearer ${expiredToken}`);

      expect(res.status).toBe(401);
      expect(res.body.error).toBe('Invalid token');
    });

    test('200 – allows request when token is valid', async () => {
      // Input: valid JWT token
      // Expected status code: 200
      // Expected behavior: request proceeds to handler
      // Expected output: success response
      const token = jwt.sign(
        { id: testData.testUserId },
        process.env.JWT_SECRET!
      );

      const res = await request(app2)
        .get('/api/protected2/test')
        .set('authorization', `Bearer ${token}`);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Authenticated');
    });

    test('500 – handles missing JWT_SECRET (covers !JWT_SECRET branch)', async () => {
      // Input: JWT_SECRET not set in environment
      // Expected status code: 500
      // Expected behavior: error thrown
      // Expected output: error handled
      const originalSecret = process.env.JWT_SECRET;
      delete process.env.JWT_SECRET;

      // Clear module cache and re-import to get fresh JWT_SECRET value
      jest.resetModules();
      // eslint-disable-next-line @typescript-eslint/no-require-imports
      const { authMiddleware: freshAuthMiddleware } = require('../auth.middleware');
      
      // Create a new app with the fresh middleware
      const testApp = express();
      testApp.use(express.json());
      testApp.use('/api/protected2', freshAuthMiddleware);
      testApp.get('/api/protected2/test', (req: Request, res: Response) => {
        res.status(200).json({ message: 'Authenticated', user: req.user });
      });
      testApp.use((err: any, req: Request, res: Response, next: NextFunction) => {
        res.status(err.status || 500).json({
          error: err.message || 'Internal server error',
        });
      });

      const token = jwt.sign(
        { id: testData.testUserId },
        'some-secret'
      );

      const res = await request(testApp)
        .get('/api/protected2/test')
        .set('authorization', `Bearer ${token}`);

      // Should throw error about JWT_SECRET not configured
      expect(res.status).toBeGreaterThanOrEqual(500);

      // Restore JWT_SECRET and re-import original module
      if (originalSecret) {
        process.env.JWT_SECRET = originalSecret;
      }
      jest.resetModules();
      // Re-import to restore the original middleware
      // eslint-disable-next-line @typescript-eslint/no-require-imports
      require('../auth.middleware');
    });
  });
});

