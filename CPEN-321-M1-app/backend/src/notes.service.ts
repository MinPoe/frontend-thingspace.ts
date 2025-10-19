import mongoose from 'mongoose';
import { Note, CreateNoteRequest, NoteType, UpdateNoteRequest } from './notes.types';
import { noteModel } from './note.model';
import OpenAI from 'openai';


export class NoteService {
    private client?: OpenAI;

    private getClient(): OpenAI {
        if (!this.client) {
            this.client = new OpenAI({ apiKey: process.env.OPENAI_API_KEY });
        }
        return this.client;
    }

    async createNote(userId: mongoose.Types.ObjectId, data: CreateNoteRequest): Promise<Note> {
        let vectorInput = "";

        for (const field of data.fields) {
            if ('content' in field) {
                vectorInput += field.content + " ";
            } else if ('dateTime' in field) {
                vectorInput += field.dateTime.toString() + " ";
            }
        }

        let vectorData: number[] = [];
        
        try {
            if (vectorInput.trim().length > 0) {
                const vectorResponse = await this.getClient().embeddings.create({
                    model: "text-embedding-3-small",
                    input: vectorInput.trim(),
                });
                vectorData = vectorResponse.data[0].embedding;
            }
        } catch (error) {
            console.error('Failed to generate embeddings (continuing with empty vector):', error);
            // Continue with empty vector instead of failing
        }

        const newNote = await noteModel.create({
            userId,
            workspaceId: data.workspaceId,
            fields: data.fields,
            noteType: data.noteType || NoteType.CONTENT,
            tags: data.tags || [],
            authors: [userId],
            vectorData: vectorData,
        });
    
        return {
            ...newNote.toObject(),
            _id: newNote._id.toString(),
            userId: newNote.userId.toString(),
            authors: newNote.authors.map(id => id.toString()),
        } as Note;
    }

    // Update a note
    async updateNote(noteId: string, userId: mongoose.Types.ObjectId, updateData: UpdateNoteRequest): Promise<Note> {
        const updatedNote = await noteModel.findOneAndUpdate(
            { _id: noteId, userId },
            { 
                ...updateData,
                updatedAt: new Date()
            },
            { new: true }
        );

        if (!updatedNote) {
            throw new Error('Note not found');
        }

        return {
            ...updatedNote.toObject(),
            _id: updatedNote._id.toString(),
            userId: updatedNote.userId.toString(),
            authors: updatedNote.authors?.map(id => id.toString()),
        } as Note;
    }

    // Delete a note
    async deleteNote(noteId: string, userId: mongoose.Types.ObjectId): Promise<Note> {
        const deletedNote = await noteModel.findOneAndDelete({ _id: noteId, userId });

        if (!deletedNote) {
            throw new Error('Note not found');
        }

        return {
            ...deletedNote.toObject(),
            _id: deletedNote._id.toString(),
            userId: deletedNote.userId.toString(),
            authors: deletedNote.authors?.map(id => id.toString()),
        } as Note;
    }

    async getNote(noteId: string, userId: mongoose.Types.ObjectId): Promise<Note | null> {
        const note = await noteModel.findOne({ _id: noteId, userId });
        return note ? {
            ...note.toObject(),
            _id: note._id.toString(),
            userId: note.userId.toString(),
            authors: note.authors?.map(id => id.toString()),
        } as Note : null;
    }


    async getAuthors(noteId: string): Promise<any[]> {
        const note = await noteModel.findById(noteId).populate('authors', 'name email profilePicture');
        
        if (!note) {
            throw new Error('Note not found');
        }

        return note.authors as any[];
    }

    // Share note to a different workspace
    async shareNoteToWorkspace(noteId: string, userId: mongoose.Types.ObjectId, workspaceId: string): Promise<Note> {
        const updatedNote = await noteModel.findOneAndUpdate(
            { _id: noteId, userId },
            { workspaceId },
            { new: true }
        );

        if (!updatedNote) {
            throw new Error('Note not found');
        }

        return {
            ...updatedNote.toObject(),
            _id: updatedNote._id.toString(),
            userId: updatedNote.userId.toString(),
            authors: updatedNote.authors?.map(id => id.toString()),
        } as Note;
    }

    // Get workspace for a note
    async getWorkspacesForNote(noteId: string): Promise<string | null> {
        const note = await noteModel.findById(noteId);
        
        if (!note) {
            throw new Error('Note not found');
        }

        return note.workspaceId;
    }

    // Get all notes for a user
    async getNotesByUserId(userId: mongoose.Types.ObjectId): Promise<Note[]> {
        const notes = await noteModel.find({ userId }).sort({ createdAt: -1 });
        return notes.map(note => ({
            ...note.toObject(),
            _id: note._id.toString(),
            userId: note.userId.toString(),
            authors: note.authors?.map(id => id.toString()),
        } as Note));
    }

}

export const noteService = new NoteService();