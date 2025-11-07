/// <reference types="jest" />
import express, { Request, Response } from 'express';
import request from 'supertest';
import { errorHandler, notFoundHandler } from '../errorHandler.middleware';

describe('Error Handler Middleware', () => {
  describe('notFoundHandler', () => {
    test('404 – returns 404 with route not found message for non-existent route', async () => {
      // Input: request to non-existent route
      // Expected status code: 404
      // Expected behavior: returns error response with route information
      // Expected output: JSON with error, message, timestamp, path, and method
      const app = express();
      app.use(express.json());
      app.get('/api/existing', (req, res) => res.json({ success: true }));
      app.use('*', notFoundHandler);

      const res = await request(app)
        .get('/api/non-existent-route')
        .expect(404);

      expect(res.body).toMatchObject({
        error: 'Route not found',
        message: 'Cannot GET /api/non-existent-route',
        path: '/api/non-existent-route',
        method: 'GET',
      });
      expect(res.body.timestamp).toBeDefined();
      expect(typeof res.body.timestamp).toBe('string');
    });

    test('404 – returns 404 with correct method in message', async () => {
      // Input: POST request to non-existent route
      // Expected status code: 404
      // Expected behavior: message includes correct HTTP method
      const app = express();
      app.use(express.json());
      app.use('*', notFoundHandler);

      const res = await request(app)
        .post('/api/unknown')
        .send({ data: 'test' })
        .expect(404);

      expect(res.body.message).toBe('Cannot POST /api/unknown');
      expect(res.body.method).toBe('POST');
    });

    test('404 – returns 404 with correct path for nested routes', async () => {
      // Input: request to nested non-existent route
      // Expected status code: 404
      // Expected behavior: path includes full original URL
      const app = express();
      app.use(express.json());
      app.use('*', notFoundHandler);

      const res = await request(app)
        .get('/api/workspace/123/members/456/nonexistent')
        .expect(404);

      expect(res.body.path).toBe('/api/workspace/123/members/456/nonexistent');
      expect(res.body.message).toBe('Cannot GET /api/workspace/123/members/456/nonexistent');
    });
  });

  describe('errorHandler', () => {
    test('500 – returns 500 with internal server error message when error occurs', async () => {
      // Input: route handler that throws an error
      // Expected status code: 500
      // Expected behavior: error is logged and formatted error response is returned
      // Expected output: JSON with generic error message
      const app = express();
      app.use(express.json());
      
      // Mock logger to verify error is logged
      const logger = require('../logger.util').default;
      const loggerErrorSpy = jest.spyOn(logger, 'error').mockImplementation(() => {});

      app.get('/api/test-error', (req, res, next) => {
        next(new Error('Test error'));
      });
      app.use(errorHandler);

      const res = await request(app)
        .get('/api/test-error')
        .expect(500);

      expect(res.body).toEqual({
        message: 'Internal server error',
      });

      // Verify error was logged
      expect(loggerErrorSpy).toHaveBeenCalledWith('Error:', expect.any(Error));
      
      loggerErrorSpy.mockRestore();
    });

    test('500 – returns 500 for different error types', async () => {
      // Input: route handler that throws different error types
      // Expected status code: 500
      // Expected behavior: all errors are handled consistently
      const app = express();
      app.use(express.json());

      const logger = require('../logger.util').default;
      const loggerErrorSpy = jest.spyOn(logger, 'error').mockImplementation(() => {});

      app.get('/api/type-error', (req, res, next) => {
        next(new TypeError('Type error'));
      });
      app.use(errorHandler);

      const res = await request(app)
        .get('/api/type-error')
        .expect(500);

      expect(res.body).toEqual({
        message: 'Internal server error',
      });

      expect(loggerErrorSpy).toHaveBeenCalledWith('Error:', expect.any(TypeError));
      
      loggerErrorSpy.mockRestore();
    });

    test('500 – handles errors without exposing internal details', async () => {
      // Input: route handler that throws error with sensitive information
      // Expected status code: 500
      // Expected behavior: generic error message returned, sensitive info not exposed
      const app = express();
      app.use(express.json());

      const logger = require('../logger.util').default;
      const loggerErrorSpy = jest.spyOn(logger, 'error').mockImplementation(() => {});

      app.get('/api/sensitive-error', (req, res, next) => {
        next(new Error('Database password: secret123'));
      });
      app.use(errorHandler);

      const res = await request(app)
        .get('/api/sensitive-error')
        .expect(500);

      // Should return generic message, not the actual error message
      expect(res.body).toEqual({
        message: 'Internal server error',
      });
      expect(res.body.message).not.toContain('secret123');

      // But error should still be logged with full details
      expect(loggerErrorSpy).toHaveBeenCalledWith('Error:', expect.any(Error));
      
      loggerErrorSpy.mockRestore();
    });

    test('500 – calls next when error is not an instance of Error', async () => {
      // Input: route handler that passes non-Error value to next
      // Expected status code: handled by subsequent error handler
      // Expected behavior: error is logged and next is called with the non-Error value
      const app = express();
      app.use(express.json());

      const logger = require('../logger.util').default;
      const loggerErrorSpy = jest.spyOn(logger, 'error').mockImplementation(() => {});

      // Track if next error handler was called
      let nextErrorHandlerCalled = false;
      let nextErrorHandlerValue: unknown = null;

      app.get('/api/non-error', (req, res, next) => {
        next('String error');
      });
      // errorHandler should be called first
      app.use(errorHandler);
      // This error handler should catch the non-Error case after errorHandler calls next
      app.use((error: unknown, req: express.Request, res: express.Response, next: express.NextFunction) => {
        nextErrorHandlerCalled = true;
        nextErrorHandlerValue = error;
        // Handle the non-Error case by returning a response
        return res.status(500).json({
          message: 'Internal server error',
        });
      });

      const res = await request(app)
        .get('/api/non-error')
        .expect(500);

      // Verify error was logged by errorHandler
      expect(loggerErrorSpy).toHaveBeenCalledWith('Error:', 'String error');
      
      // Verify next error handler was called with the non-Error value
      expect(nextErrorHandlerCalled).toBe(true);
      expect(nextErrorHandlerValue).toBe('String error');

      expect(res.body).toEqual({
        message: 'Internal server error',
      });
      
      loggerErrorSpy.mockRestore();
    });
  });
});

