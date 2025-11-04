/// <reference types="jest" />
import express, { Request, Response } from 'express';
import request from 'supertest';
import { z } from 'zod';
import { validateBody } from '../validation.middleware';

// Create a test app with validation middleware
function createValidationTestApp(schema: z.ZodSchema) {
  const app = express();
  app.use(express.json());

  // Use the validation middleware
  app.post('/api/test', validateBody(schema), (req: Request, res: Response) => {
    res.status(200).json({ message: 'Validated', data: req.body });
  });

  return app;
}

// ---------------------------
// Test suite
// ---------------------------
describe('Validation Middleware Tests', () => {
  const testSchema = z.object({
    name: z.string().min(1),
    email: z.string().email(),
    age: z.number().optional(),
  });

  describe('validateBody middleware', () => {
    test('200 – allows valid request body', async () => {
      // Input: valid request body matching schema
      // Expected status code: 200
      // Expected behavior: validation passes, request proceeds
      // Expected output: success response with validated data
      const app = createValidationTestApp(testSchema);

      const res = await request(app)
        .post('/api/test')
        .send({
          name: 'Test User',
          email: 'test@example.com',
          age: 25,
        });

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Validated');
      expect(res.body.data.name).toBe('Test User');
      expect(res.body.data.email).toBe('test@example.com');
    });

    test('400 – returns 400 when validation fails (ZodError)', async () => {
      // Input: invalid request body that doesn't match schema
      // Expected status code: 400
      // Expected behavior: validation error returned
      // Expected output: error response with validation details
      const app = createValidationTestApp(testSchema);

      const res = await request(app)
        .post('/api/test')
        .send({
          name: '', // Empty string fails min(1) validation
          email: 'invalid-email', // Invalid email format
        });

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Validation error');
      expect(res.body.message).toBe('Invalid input data');
      expect(res.body.details).toBeDefined();
      expect(Array.isArray(res.body.details)).toBe(true);
    });

    test('400 – handles missing required fields', async () => {
      // Input: request body missing required fields
      // Expected status code: 400
      // Expected behavior: validation error returned
      // Expected output: error response with validation details
      const app = createValidationTestApp(testSchema);

      const res = await request(app)
        .post('/api/test')
        .send({
          // Missing name and email
        });

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Validation error');
      expect(res.body.details).toBeDefined();
    });

    test('400 – handles wrong data types', async () => {
      // Input: request body with wrong data types
      // Expected status code: 400
      // Expected behavior: validation error returned
      // Expected output: error response with validation details
      const app = createValidationTestApp(testSchema);

      const res = await request(app)
        .post('/api/test')
        .send({
          name: 123, // Should be string
          email: 'test@example.com',
          age: 'not-a-number', // Should be number
        });

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Validation error');
      expect(res.body.details).toBeDefined();
    });

    test('500 – handles non-ZodError exceptions (covers catch branch)', async () => {
      // Input: request that triggers non-ZodError in validation
      // Expected status code: 500
      // Expected behavior: internal server error returned
      // Expected output: error response
      // Mock schema.parse to throw a non-ZodError
      const mockSchema = {
        parse: jest.fn(() => {
          throw new Error('Unexpected error');
        }),
      } as unknown as z.ZodSchema;

      const app = express();
      app.use(express.json());
      app.post('/api/test', validateBody(mockSchema), (req: Request, res: Response) => {
        res.status(200).json({ message: 'Validated' });
      });

      const res = await request(app)
        .post('/api/test')
        .send({ name: 'Test' });

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Internal server error');
      expect(res.body.message).toBe('Validation processing failed');
    });

    test('200 – validates nested field paths correctly', async () => {
      // Input: request body with nested validation errors
      // Expected status code: 400
      // Expected behavior: validation error includes nested field paths
      // Expected output: error response with nested field details
      const nestedSchema = z.object({
        user: z.object({
          name: z.string().min(1),
          profile: z.object({
            age: z.number().min(0),
          }),
        }),
      });

      const app = createValidationTestApp(nestedSchema);

      const res = await request(app)
        .post('/api/test')
        .send({
          user: {
            name: '', // Invalid
            profile: {
              age: -5, // Invalid
            },
          },
        });

      expect(res.status).toBe(400);
      expect(res.body.details).toBeDefined();
      // Check that field paths include nested structure
      const fieldPaths = res.body.details.map((d: any) => d.field);
      expect(fieldPaths.some((path: string) => path.includes('user.name'))).toBe(true);
      expect(fieldPaths.some((path: string) => path.includes('user.profile.age'))).toBe(true);
    });
  });
});

