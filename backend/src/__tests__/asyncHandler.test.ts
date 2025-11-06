/// <reference types="jest" />
import express, { Request, Response, NextFunction } from 'express';
import request from 'supertest';
import { asyncHandler } from '../asyncHandler.util';

describe('asyncHandler Utility', () => {
  test('should handle async function that resolves successfully', async () => {
    const app = express();
    app.use(express.json());

    const asyncRoute = asyncHandler(async (req: Request, res: Response, next: NextFunction) => {
      await Promise.resolve();
      res.status(200).json({ message: 'Success' });
    });

    app.get('/test', asyncRoute);

    const res = await request(app)
      .get('/test')
      .expect(200);

    expect(res.body.message).toBe('Success');
  });

  test('should catch errors and pass to next', async () => {
    const app = express();
    app.use(express.json());

    const asyncRoute = asyncHandler(async (req: Request, res: Response, next: NextFunction) => {
      throw new Error('Test error');
    });

    const errorHandler = (err: unknown, req: Request, res: Response, next: NextFunction) => {
      res.status(500).json({ error: err instanceof Error ? err.message : 'Unknown error' });
    };

    app.get('/test', asyncRoute);
    app.use(errorHandler);

    const res = await request(app)
      .get('/test')
      .expect(500);

    expect(res.body.error).toBe('Test error');
  });

  test('should handle non-Error values in catch', async () => {
    const app = express();
    app.use(express.json());

    const asyncRoute = asyncHandler(async (req: Request, res: Response, next: NextFunction) => {
      throw 'String error';
    });

    const errorHandler = (err: unknown, req: Request, res: Response, next: NextFunction) => {
      res.status(500).json({ error: typeof err === 'string' ? err : 'Unknown error' });
    };

    app.get('/test', asyncRoute);
    app.use(errorHandler);

    const res = await request(app)
      .get('/test')
      .expect(500);

    expect(res.body.error).toBe('String error');
  });
});

