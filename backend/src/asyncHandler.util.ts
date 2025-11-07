import { Request, Response, NextFunction } from 'express';

/**
 * Wraps async route handlers to properly handle promises and errors.
 * Express expects route handlers to return void, not Promise<void>.
 * This wrapper ensures async functions are properly handled and errors are passed to Express's error handler.
 */
export function asyncHandler<T extends Request = Request, U extends Response = Response>(
  fn: (req: T, res: U, next: NextFunction) => Promise<unknown>
): (req: T, res: U, next: NextFunction) => void {
  return (req: T, res: U, next: NextFunction): void => {
    Promise.resolve(fn(req, res, next)).catch((error: unknown) => { next(error); });
  };
}

