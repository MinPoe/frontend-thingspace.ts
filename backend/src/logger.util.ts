import { sanitizeArgs, sanitizeInput } from './sanitizeInput.util';

const logger = {
  info: (message: string, ...args: unknown[]) => {
    // Input is already sanitized via sanitizeInput and sanitizeArgs
    // eslint-disable-next-line security/detect-crlf, no-console, security-node/detect-crlf
    console.log('[INFO]', sanitizeInput(message), ...sanitizeArgs(args));
  },
  error: (message: string, ...args: unknown[]) => {
    console.error('[ERROR]', sanitizeInput(message), ...sanitizeArgs(args));
  },
};

export default logger;
