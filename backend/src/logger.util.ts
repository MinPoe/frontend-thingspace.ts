import { sanitizeArgs, sanitizeInput } from './sanitizeInput.util';

const logger = {
  info: (message: string, ...args: unknown[]) => {
    // Input is already sanitized via sanitizeInput and sanitizeArgs
    const sanitizedArgs = sanitizeArgs(args);
    const logMessage = ['[INFO]', sanitizeInput(message), ...sanitizedArgs].join(' ');
    console.log(logMessage);
  },
  error: (message: string, ...args: unknown[]) => {
    const sanitizedArgs = sanitizeArgs(args);
    const logMessage = ['[ERROR]', sanitizeInput(message), ...sanitizedArgs].join(' ');
    console.error(logMessage);
  },
};

export default logger;
