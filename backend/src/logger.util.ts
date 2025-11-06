import { sanitizeArgs, sanitizeInput } from './sanitizeInput.util';

const logger = {
  info: (message: string, ...args: unknown[]) => {
    // Input is already sanitized via sanitizeInput and sanitizeArgs
    /* eslint-disable-next-line security/detect-crlf, no-console */
    console.log('[INFO]', sanitizeInput(message), ...sanitizeArgs(args));
  },
  error: (message: string, ...args: unknown[]) => {
    console.error('[ERROR]', sanitizeInput(message), ...sanitizeArgs(args));
  },
  warn: (message: string, ...args: unknown[]) => {
    console.warn('[WARN]', sanitizeInput(message), ...sanitizeArgs(args));
  },
  debug: (message: string, ...args: unknown[]) => {
    console.debug('[DEBUG]', sanitizeInput(message), ...sanitizeArgs(args));
  },
};

export default logger;
