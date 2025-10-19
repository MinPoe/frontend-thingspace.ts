import { Request, Response } from 'express';
import { CreateNoteRequest, UpdateNoteRequest } from './notes.types';
import { noteService } from './notes.service';

export class NotesController {
  async createNote(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }

      const noteData = req.body as CreateNoteRequest;

      // NOTE: We probably need to add some verification thing checking if the author id has access to the workspace
      const newNote = await noteService.createNote(userId, noteData);

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
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }

      const noteId = req.params.id;
      const updateData = req.body as UpdateNoteRequest;

      const updatedNote = await noteService.updateNote(noteId, userId, updateData);

      res.status(200).json({
        message: 'Note successfully updated',
        note: updatedNote,
      });
    } catch (error) {
      console.error('Error updating note:', error);
      res.status(500).json({ error: 'Failed to update note' });
    }
  }

  async deleteNote(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }
      const noteId = req.params.id;
      const deletedNote = await noteService.deleteNote(noteId, userId);

      res.status(200).json({
        message: 'Note successfully deleted',
        note: deletedNote,
      });
    } catch (error) {
      console.error('Error deleting note:', error);
      res.status(500).json({ error: 'Failed to delete note' });
    }
  }

  async getNote(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }
      const noteId = req.params.id;
      const note = await noteService.getNote(noteId, userId);

      if (!note) {
        res.status(404).json({ error: 'Note not found' });
        return;
      }

      res.status(200).json({
        message: 'Note successfully retrieved',
        note: note,
      });
    } catch (error) {
      console.error('Error retrieving note:', error);
      res.status(500).json({ error: 'Failed to retrieve note' });
    }
  }

  async getAuthors(req: Request, res: Response): Promise<void> {
    try {
      const noteId = req.params.id;
      const authors = await noteService.getAuthors(noteId);

      res.status(200).json({
        message: 'Authors retrieved successfully',
        authors: authors,
      });
    } catch (error) {
      console.error('Error retrieving authors:', error);
      res.status(500).json({ error: 'Failed to retrieve authors' });
    }
  }

  async shareNoteToWorkspace(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }

      const noteId = req.params.id;
      const { workspaceId } = req.body;

      if (!workspaceId) {
        res.status(400).json({ error: 'workspaceId is required' });
        return;
      }

      const sharedNote = await noteService.shareNoteToWorkspace(noteId, userId, workspaceId);

      res.status(200).json({
        message: 'Note shared to workspace successfully',
        note: sharedNote,
      });
    } catch (error) {
      console.error('Error sharing note:', error);
      res.status(500).json({ error: 'Failed to share note' });
    }
  }

  async getWorkspacesForNote(req: Request, res: Response): Promise<void> {
    try {
      const noteId = req.params.id;
      const workspaceId = await noteService.getWorkspacesForNote(noteId);

      res.status(200).json({
        message: 'Workspace retrieved successfully',
        workspaceId: workspaceId,
      });
    } catch (error) {
      console.error('Error retrieving workspace:', error);
      res.status(500).json({ error: 'Failed to retrieve workspace' });
    }
  }

  async findNotes(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }

      // TODO: Implement filtering by workspaceId, noteType, tags, searchQuery, pagination
      const notes = await noteService.getNotesByUserId(userId);

      res.status(200).json({
        message: 'Notes retrieved successfully',
        notes: notes,
      });
    } catch (error) {
      console.error('Error retrieving notes:', error);
      res.status(500).json({ error: 'Failed to retrieve notes' });
    }
  }

}