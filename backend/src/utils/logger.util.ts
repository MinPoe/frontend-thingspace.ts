import { sanitizeArgs, sanitizeInput } from './sanitizeInput.util';

const logger = {
  info: (message: string, ...args: unknown[]) => {
    // Input is already sanitized via sanitizeInput and sanitizeArgs
    // Using process.stdout.write with literal prefix to satisfy security linter
    process.stdout.write('[INFO] ' + sanitizeInput(message) + (args.length > 0 ? ' ' + sanitizeArgs(args).join(' ') : '') + '\n');
  },
  error: (message: string, ...args: unknown[]) => {
    // Using process.stderr.write with literal prefix to satisfy security linter
    process.stderr.write('[ERROR] ' + sanitizeInput(message) + (args.length > 0 ? ' ' + sanitizeArgs(args).join(' ') : '') + '\n');
  },
};

export default logger;

