import { Request, Response } from 'express';
import { Note, CreateNoteRequest, UpdateNoteRequest } from './notes.types';
import { userModel } from './user.model';

export class NotesController {
  async createNote(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?.userId;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }

      const noteData = req.body;
      
      if (!noteData.title || !noteData.content) {
        res.status(400).json({ error: 'Title and content are required' });
        return;
      }

      const { title, content, tags, workspaceId } = noteData;
      
      // Okay this part idk what the obvject will look like
      const noteObject = {
        title,
        content,
        userId,
        tags: tags || [],
        workspaceId,
        createdAt: new Date(),
        updatedAt: new Date(),
      };


      const user = await userModel.findById(userId);
      if (!user) {
        res.status(404).json({ error: 'User not found' });
        return;
      }

      if (!user.notes) {
        user.notes = [];
      }

      const newNote = {
        _id: new Date().toISOString(), 
        ...noteObject,
      };

      user.notes.push(newNote);
      await user.save();

      res.status(201).json({
        message: 'Note created successfully',
        note: newNote,
      });
    } catch (error) {
      console.error('Error creating note:', error);
      res.status(500).json({ error: 'Failed to create note' });
    }
  }

  async updateNote(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?.userId;
      const noteId = req.params.id;

      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }

      const user = await userModel.findById(userId);
      if (!user || !user.notes) {
        res.status(404).json({ error: 'User or notes not found' });
        return;
      }

      const noteIndex = user.notes.findIndex(
        (note: any) => note._id === noteId && note.userId === userId
      );

      if (noteIndex === -1) {
        res.status(404).json({ error: 'Note not found' });
        return;
      }

      const updateData: UpdateNoteRequest = req.body;
      const updatedNote = {
        ...user.notes[noteIndex],
        ...updateData,
        updatedAt: new Date(),
      };

      user.notes[noteIndex] = updatedNote;
      await user.save();

      res.json({
        message: 'Note updated successfully',
        note: updatedNote,
      });
    } catch (error) {
      console.error('Error updating note:', error);
      res.status(500).json({ error: 'Failed to update note' });
    }
  }

  async deleteNote(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?.userId;
      const noteId = req.params.id;

      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }

      const user = await userModel.findById(userId);
      if (!user || !user.notes) {
        res.status(404).json({ error: 'User or notes not found' });
        return;
      }

      const noteIndex = user.notes.findIndex(
        (note: any) => note._id === noteId && note.userId === userId
      );

      if (noteIndex === -1) {
        res.status(404).json({ error: 'Note not found' });
        return;
      }

      user.notes.splice(noteIndex, 1);
      await user.save();

      res.json({ message: 'Note deleted successfully' });
    } catch (error) {
      console.error('Error deleting note:', error);
      res.status(500).json({ error: 'Failed to delete note' });
    }
  }

  async getNotes(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?.userId;

      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }

      const user = await userModel.findById(userId);
      if (!user || !user.notes) {
        res.json({ notes: [] });
        return;
      }

      res.json({ notes: user.notes });
    } catch (error) {
      console.error('Error fetching notes:', error);
      res.status(500).json({ error: 'Failed to fetch notes' });
    }
  }
}
