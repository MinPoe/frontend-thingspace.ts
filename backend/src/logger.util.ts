import { sanitizeArgs, sanitizeInput } from './sanitizeInput.util';

const logger = {
  info: (message: string, ...args: unknown[]) => {
    // Input is already sanitized via sanitizeInput and sanitizeArgs
    console.log('[INFO]', sanitizeInput(message), ...sanitizeArgs(args));
  },
  error: (message: string, ...args: unknown[]) => {
    console.error('[ERROR]', sanitizeInput(message), ...sanitizeArgs(args));
  },
};

export default logger;
