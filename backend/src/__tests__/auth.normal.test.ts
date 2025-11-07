/// <reference types="jest" />
import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';
import jwt from 'jsonwebtoken';

import logger from '../logger.util';
import { userModel } from '../user.model';
import { workspaceModel } from '../workspace.model';
import { authService } from '../auth.service';
import { createTestApp, setupTestDatabase, TestData } from './test-helpers';

// ---------------------------
// Test suite
// ---------------------------
describe('Auth API – Normal Tests (No Mocking)', () => {
  let mongo: MongoMemoryServer;
  let testData: TestData;
  let app: ReturnType<typeof createTestApp>;

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
    
    // Create app after DB connection (uses full production app)
    app = createTestApp();
  }, 60000); // 60 second timeout for MongoDB Memory Server startup

  // Tear down DB
  afterAll(async () => {
    if (mongoose.connection.readyState !== 0) {
      await mongoose.disconnect();
    }
    if (mongo) {
      await mongo.stop();
    }
  });

  // Fresh DB state before each test
  beforeEach(async () => {
    testData = await setupTestDatabase(app);
  });

  describe('POST /api/auth/signup - Sign Up (Validation)', () => {
    test('400 – returns validation error when idToken is missing', async () => {
      // Input: request body without idToken
      // Expected status code: 400
      // Expected behavior: validateBody middleware rejects request
      // Expected output: validation error with details
      const res = await request(app)
        .post('/api/auth/signup')
        .send({});

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Validation error');
      expect(res.body.message).toBe('Invalid input data');
      expect(res.body.details).toBeDefined();
      expect(Array.isArray(res.body.details)).toBe(true);
    });

    test('400 – returns validation error when idToken is empty string', async () => {
      // Input: request body with empty idToken
      // Expected status code: 400
      // Expected behavior: validateBody middleware rejects request (min(1) validation)
      // Expected output: validation error with details
      const res = await request(app)
        .post('/api/auth/signup')
        .send({ idToken: '' });

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Validation error');
      expect(res.body.details).toBeDefined();
      const fieldPaths = res.body.details.map((d: any) => d.field);
      expect(fieldPaths).toContain('idToken');
    });

    test('400 – returns validation error when idToken is wrong type', async () => {
      // Input: request body with non-string idToken
      // Expected status code: 400
      // Expected behavior: validateBody middleware rejects request (type validation)
      // Expected output: validation error with details
      const res = await request(app)
        .post('/api/auth/signup')
        .send({ idToken: 12345 });

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Validation error');
      expect(res.body.details).toBeDefined();
    });
  });

  describe('POST /api/auth/signin - Sign In (Validation)', () => {
    test('400 – returns validation error when idToken is missing', async () => {
      // Input: request body without idToken
      // Expected status code: 400
      // Expected behavior: validateBody middleware rejects request
      // Expected output: validation error with details
      const res = await request(app)
        .post('/api/auth/signin')
        .send({});

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Validation error');
      expect(res.body.message).toBe('Invalid input data');
      expect(res.body.details).toBeDefined();
    });
  });

  describe('POST /api/auth/dev-login - Dev Login', () => {
    test('200 – creates new test user and returns token', async () => {
      // Input: email in request body (optional, defaults to test@example.com)
      // Expected status code: 200
      // Expected behavior: creates new user if doesn't exist, returns token
      // Expected output: success response with token and user data
      const res = await request(app)
        .post('/api/auth/dev-login')
        .send({ email: 'dev-test@example.com' });

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Dev login successful');
      expect(res.body.data).toBeDefined();
      expect(res.body.data.token).toBeDefined();
      expect(res.body.data.user).toBeDefined();
      expect(res.body.data.user.email).toBe('dev-test@example.com');
    });

    test('200 – returns token for existing user', async () => {
      // Input: email of existing user
      // Expected status code: 200
      // Expected behavior: finds existing user, returns token
      // Expected output: success response with token and user data
      // First create a user
      const existingUser = await userModel.create({
        googleId: 'dev-test-existing',
        email: 'existing@example.com',
        name: 'Existing User',
        profilePicture: '',
      });

      const res = await request(app)
        .post('/api/auth/dev-login')
        .send({ email: 'existing@example.com' });

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Dev login successful');
      expect(res.body.data.token).toBeDefined();
      expect(res.body.data.user._id).toBe(existingUser._id.toString());
    });

    test('200 – uses default email when email not provided', async () => {
      // Input: empty request body (no email)
      // Expected status code: 200
      // Expected behavior: uses default email 'test@example.com'
      // Expected output: success response with token and user data
      const res = await request(app)
        .post('/api/auth/dev-login')
        .send({});

      expect(res.status).toBe(200);
      expect(res.body.data.user.email).toBe('test@example.com');
    });
  });

  describe('POST /api/auth/signup - Sign Up', () => {
    test('400 – returns 400 when idToken is missing', async () => {
      // Input: request body without idToken
      // Expected status code: 400
      // Expected behavior: validation error returned
      // Expected output: validation error response
      const res = await request(app)
        .post('/api/auth/signup')
        .send({});

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Validation error');
    });

    test('400 – returns 400 when idToken is empty string', async () => {
      // Input: request body with empty idToken
      // Expected status code: 400
      // Expected behavior: validation error returned
      // Expected output: validation error response
      const res = await request(app)
        .post('/api/auth/signup')
        .send({ idToken: '' });

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Validation error');
    });
  });

  describe('POST /api/auth/signin - Sign In', () => {
    test('400 – returns 400 when idToken is missing', async () => {
      // Input: request body without idToken
      // Expected status code: 400
      // Expected behavior: validation error returned
      // Expected output: validation error response
      const res = await request(app)
        .post('/api/auth/signin')
        .send({});

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Validation error');
    });

    test('400 – returns 400 when idToken is empty string', async () => {
      // Input: request body with empty idToken
      // Expected status code: 400
      // Expected behavior: validation error returned
      // Expected output: validation error response
      const res = await request(app)
        .post('/api/auth/signin')
        .send({ idToken: '' });

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Validation error');
    });
  });

  describe('Auth Service Error Branches - Direct Service Tests', () => {
    beforeEach(async () => {
      // Ensure database is connected before each test in this describe block
      if (mongoose.connection.readyState === 0 && mongo) {
        await mongoose.connect(mongo.getUri());
      }
    });

    afterEach(async () => {
      // Ensure database is reconnected after each test
      if (mongoose.connection.readyState === 0 && mongo) {
        await mongoose.connect(mongo.getUri());
      }
    });

    test('devLogin throws error on database error', async () => {
      // Input: database operation that fails
      // Expected behavior: Error caught and re-thrown
      // Expected output: Error with message
      if (!mongo) {
        throw new Error('MongoDB Memory Server not initialized');
      }
      const currentUri = mongo.getUri();
      
      // Disconnect database temporarily to trigger error
      await mongoose.disconnect();
      
      try {
        await expect(
          authService.devLogin('test@example.com')
        ).rejects.toThrow();
      } finally {
        // Reconnect using the same Mongo instance
        await mongoose.connect(currentUri);
      }
    });
  });

  describe('Async Handler integration via /api/auth/dev-login', () => {
    /**
     * Uses the real /api/auth/dev-login route but swaps the controller method so we can
     * deterministically trigger asyncHandler behaviours. No fake endpoints are introduced.
     */
    const buildAppWithMockedDevLogin = async (
      impl: () => Promise<void>
    ) => {
      jest.resetModules();

      if (!process.env.JWT_SECRET) {
        process.env.JWT_SECRET = 'test-jwt-secret-key-for-testing-only';
      }

      const { AuthController } = await import('../auth.controller.js') as typeof import('../auth.controller');
      jest
        .spyOn(AuthController.prototype, 'devLogin')
        .mockImplementation(async function devLoginMock(req, res, next) {
          await impl();
          return res;
        });

      const helpers = await import('./test-helpers.js') as typeof import('./test-helpers');
      const appInstance = helpers.createTestApp();

      return appInstance;
    };

    afterEach(() => {
      jest.restoreAllMocks();
      jest.resetModules();
    });

    test('500 – surfaces thrown Error through global error handler', async () => {
      // Input: POST /api/auth/dev-login with controller throwing an Error instance
      // Expected status code: 500
      // Expected behavior: error bubbles into asyncHandler and global error middleware
      // Expected output: generic 500 JSON response ({ message: 'Internal server error' })
      const appInstance = await buildAppWithMockedDevLogin(async () => {
        throw new Error('Integration failure');
      });

      const res = await request(appInstance)
        .post('/api/auth/dev-login')
        .send({ email: 'error@example.com' });

      expect(res.status).toBe(500);
      expect(res.body).toEqual({ message: 'Internal server error' });
    });

    test('500 – surfaces non-Error rejections via Express default handler', async () => {
      // Input: POST /api/auth/dev-login with controller throwing a string (non-Error)
      // Expected status code: 500
      // Expected behavior: asyncHandler forwards rejection, Express default handler renders HTML
      // Expected output: HTML error page containing the non-Error value
      const appInstance = await buildAppWithMockedDevLogin(async () => {
        throw 'String error';
      });

      const res = await request(appInstance)
        .post('/api/auth/dev-login')
        .send({ email: 'error@example.com' });

      expect(res.status).toBe(500);
      expect(res.text).toContain('String error');
    });
  });

  describe('Global error handling', () => {
    afterEach(() => {
      jest.restoreAllMocks();
    });

    test('404 – returns structured response for unknown routes', async () => {
      // Input: GET request to unknown /api path with valid auth token
      // Expected status code: 404
      // Expected behaviour: notFoundHandler formats response with route details
      // Expected output: JSON containing error, message, path, method, timestamp
      const res = await request(app)
        .get('/api/route-that-does-not-exist')
        .set('Authorization', `Bearer ${testData.testUserToken}`)
        .expect(404);

      expect(res.body).toMatchObject({
        error: 'Route not found',
        message: 'Cannot GET /api/route-that-does-not-exist',
        path: '/api/route-that-does-not-exist',
        method: 'GET',
      });
      expect(typeof res.body.timestamp).toBe('string');
    });

    test('500 – falls back to generic message when controller throws', async () => {
      // Input: POST /api/auth/signup where service layer throws
      // Expected status code: 500
      // Expected behaviour: global error handler logs and returns generic payload
      // Expected output: { message: 'Internal server error' }
      const loggerSpy = jest.spyOn(logger, 'error').mockImplementation(() => {});
      const signUpSpy = jest
        .spyOn(authService, 'signUpWithGoogle')
        .mockRejectedValue(new Error('Simulated failure'));

      const res = await request(app)
        .post('/api/auth/signup')
        .send({ idToken: 'valid-token' })
        .expect(500);

      expect(res.body).toEqual({ message: 'Internal server error' });
      expect(loggerSpy).toHaveBeenCalledWith('Error:', expect.any(Error));
      expect(signUpSpy).toHaveBeenCalledWith('valid-token');
    });
  });
});

