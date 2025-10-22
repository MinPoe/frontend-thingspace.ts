import { z } from 'zod';

export interface BaseField {
  _id: string;
  fieldType: 'title' | 'textbox' | 'datetime'; 
}

export interface TitleField extends BaseField {
  fieldType: 'title';
  content: string;
}

export interface TextBoxField extends BaseField {
  fieldType: 'textbox';
  content: string;
}

export interface DateTimeField extends BaseField {
  fieldType: 'datetime';
  dateTime: Date; 
}

export type Field = TitleField | TextBoxField | DateTimeField;

export enum NoteType {
  CONTENT = "CONTENT",
  CHAT = "CHAT",
  TEMPLATE = "TEMPLATE"
}

export interface Note {
  _id: string;
  userId: string;
  fields: Field[];
  noteType: NoteType;
  tags?: string[];
  workspaceId?: string;
  vectorData: number[];
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateNoteRequest {
  tags?: string[];
  fields: Field[];
  noteType?: NoteType;
  workspaceId: string;
}

export interface UpdateNoteRequest {
  tags?: string[];
  fields?: Field[];
  noteType?: NoteType;
  workspaceId?: string;
}

export interface GetNoteRequest {
  authorId: string;
  tags?: string[];
  fields: Field[];
  noteType?: NoteType;
  workspaceId: string;
}

export const createNoteSchema = z.object({
  fields: z.array(z.any()),
  workspaceId: z.string().min(1, 'workspaceId is required'),
  tags: z.array(z.string()).optional(),
  noteType: z.enum([NoteType.CONTENT, NoteType.CHAT, NoteType.TEMPLATE]).optional(),
}).strict();

export const updateNoteSchema = z.object({
  tags: z.array(z.string()).optional(),
  fields: z.array(z.any()).optional(),
  noteType: z.enum([NoteType.CONTENT, NoteType.CHAT, NoteType.TEMPLATE]).optional(),
}).strict();
