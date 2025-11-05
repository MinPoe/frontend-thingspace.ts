/// <reference types="jest" />
import { sanitizeInput, sanitizeArgs } from '../sanitizeInput.util';

describe('sanitizeInput.util', () => {
  describe('sanitizeInput', () => {
    test('returns input string when no CRLF injection detected', () => {
      const input = 'normal string';
      const result = sanitizeInput(input);
      expect(result).toBe(input);
    });

    test('returns input string with valid characters', () => {
      const input = 'Hello World 123 !@#$%^&*()';
      const result = sanitizeInput(input);
      expect(result).toBe(input);
    });

    test('throws error when CRLF injection detected (carriage return)', () => {
      const input = 'test\rstring';
      expect(() => sanitizeInput(input)).toThrow('CRLF injection attempt detected');
    });

    test('throws error when CRLF injection detected (line feed)', () => {
      const input = 'test\nstring';
      expect(() => sanitizeInput(input)).toThrow('CRLF injection attempt detected');
    });

    test('throws error when CRLF injection detected (both)', () => {
      const input = 'test\r\nstring';
      expect(() => sanitizeInput(input)).toThrow('CRLF injection attempt detected');
    });
  });

  describe('sanitizeArgs', () => {
    test('sanitizes array of arguments', () => {
      const args = ['arg1', 'arg2', 'arg3'];
      const result = sanitizeArgs(args);
      expect(result).toEqual(['arg1', 'arg2', 'arg3']);
    });

    test('sanitizes mixed type arguments', () => {
      const args = ['string', 123, true, null];
      const result = sanitizeArgs(args);
      expect(result).toEqual(['string', '123', 'true', 'null']);
    });

    test('throws error when argument contains CRLF injection', () => {
      const args = ['normal', 'test\rstring'];
      expect(() => sanitizeArgs(args)).toThrow('CRLF injection attempt detected');
    });
  });
});

