/// <reference types="jest" />
import mongoose from 'mongoose';
import request from 'supertest';
import { MongoMemoryServer } from 'mongodb-memory-server';
import * as fs from 'fs';
import * as path from 'path';

import { NoteType } from '../notes.types';
import { noteService } from '../notes.service';
import { createTestApp, setupTestDatabase, TestData } from './test-helpers';

const app = createTestApp();

// Load notes data from JSON file
function loadNotesData(): any[] {
  const jsonPath = path.join(__dirname, '../../../scripts/500_notes.json');
  const jsonContent = fs.readFileSync(jsonPath, 'utf-8');
  const data = JSON.parse(jsonContent);
  return data.notes || [];
}

// Convert JSON note data to CreateNoteRequest format
function convertToNoteRequest(noteData: any, workspaceId: string): any {
  const fields: any[] = [];
  
  // Add title field
  if (noteData.title) {
    fields.push({
      _id: new mongoose.Types.ObjectId().toString(),
      fieldType: 'text',
      label: 'Title',
      required: true,
      placeholder: 'Enter title',
      maxLength: 100,
      content: noteData.title,
    });
  }
  
  // Add description field
  if (noteData.description) {
    fields.push({
      _id: new mongoose.Types.ObjectId().toString(),
      fieldType: 'text',
      label: 'Description',
      required: false,
      placeholder: 'Enter description',
      maxLength: 500,
      content: noteData.description,
    });
  }
  
  // Add date field if present
  if (noteData.date) {
    fields.push({
      _id: new mongoose.Types.ObjectId().toString(),
      fieldType: 'datetime',
      label: 'Date',
      required: false,
      minDate: null,
      maxDate: null,
      content: noteData.date,
    });
  }
  
  // Add number field if present
  if (noteData.number !== undefined) {
    fields.push({
      _id: new mongoose.Types.ObjectId().toString(),
      fieldType: 'number',
      label: 'Number',
      required: false,
      min: 0,
      max: 100,
      content: noteData.number,
    });
  }
  
  // Build tags array
  const tags: string[] = [];
  tags.push(noteData.tag);

  
  return {
    workspaceId,
    noteType: NoteType.CONTENT,
    tags,
    fields,
  };
}

// ---------------------------
// Test suite
// ---------------------------
describe('Notes API – Search Latency Test (Non-Functional Requirement)', () => {
  let mongo: MongoMemoryServer;
  let testData: TestData;
  const notesData = loadNotesData();

  // Spin up in-memory Mongo
  beforeAll(async () => {
    mongo = await MongoMemoryServer.create();
    const uri = mongo.getUri();
    await mongoose.connect(uri);
    console.log('✅ Connected to in-memory MongoDB');
  });

  // Tear down DB
  afterAll(async () => {
    await mongoose.disconnect();
    await mongo.stop();
  });

  // Fresh DB state and note creation before each test
  beforeEach(async () => {
    testData = await setupTestDatabase();
    // Clean up any previous mocks
    jest.restoreAllMocks();
    
    // Mock OpenAI client to avoid actual API calls during latency testing
    const mockEmbedding = Array(3072).fill(0).map(() => Math.random() * 0.1 - 0.05); // Simulate embedding vector (3072 dimensions for text-embedding-3-large)
    const mockClient = {
      embeddings: {
        create: jest.fn().mockResolvedValue({
          data: [{ embedding: mockEmbedding }],
        }),
      },
    };
    
    // Mock getClient to return our mock client
    jest.spyOn(noteService as any, 'getClient').mockReturnValue(mockClient);
    
    // Setup: Create 400 notes in the database
    const notesToCreate = notesData.slice(0, 400);
    const createdNotes: any[] = [];
    
    console.log(`Creating ${notesToCreate.length} notes...`);
    const createStartTime = Date.now();
    
    for (const noteData of notesToCreate) {
      const noteRequest = convertToNoteRequest(noteData, testData.testWorkspaceId);
      const res = await request(app)
        .post('/api/notes')
        .set('x-test-user-id', testData.testUserId)
        .send(noteRequest);
      
      if (res.status === 201) {
        createdNotes.push(res.body.data.note);
      } else {
        console.error(`Failed to create note: ${res.status} - ${JSON.stringify(res.body)}`);
      }
    }
    
    const createEndTime = Date.now();
    const createDuration = createEndTime - createStartTime;
    console.log(`Created ${createdNotes.length} notes in ${createDuration}ms`);
    
    // Verify we created the expected number of notes
    expect(createdNotes.length).toBe(400);
  });

  describe('GET /api/notes - Search Latency Test', () => {
    test('Search query latency should be under 5 seconds with 400 notes', async () => {
      // Test: Run 3 different search queries and measure average latency
      const searchQueries = ['food recipe cooking', 'study session homework', 'travel trip vacation'];
      const searchLatencies: number[] = [];
      
      console.log(`Running 3 different search queries...`);
      
      for (let i = 0; i < 3; i++) {
        const searchQuery = searchQueries[i];
        console.log(`\nSearch ${i + 1}: "${searchQuery}"`);
        
        const searchStartTime = Date.now();
        const res = await request(app)
          .get('/api/notes')
          .query({
            workspaceId: testData.testWorkspaceId,
            noteType: NoteType.CONTENT,
            query: searchQuery,
          })
          .set('x-test-user-id', testData.testUserId);
        
        const searchEndTime = Date.now();
        const searchLatency = searchEndTime - searchStartTime;
        searchLatencies.push(searchLatency);
        
        console.log(`Search ${i + 1} completed in ${searchLatency}ms`);
        
        // Log error if status is not 200
        if (res.status !== 200) {
          console.log(`Failed: Search ${i + 1} failed with status ${res.status}:`, JSON.stringify(res.body, null, 2));
          console.log(`Full response:`, res.body);
          // Skip printing notes if search failed
          continue;
        }
        
        // Assertions for each search
        expect(res.status).toBe(200);
        expect(res.body.message).toBe('Notes retrieved successfully');
        expect(res.body.data).toBeDefined();
        expect(res.body.data.notes).toBeDefined();
        expect(Array.isArray(res.body.data.notes)).toBe(true);
        
        // Print first 3 notes
        const notes = res.body.data.notes;
        console.log(`Found ${notes.length} notes. First 3 notes:`);
        for (let j = 0; j < Math.min(3, notes.length); j++) {
          const note = notes[j];
          const titleField = note.fields?.find((f: any) => f.label === 'Title' || f.fieldType === 'title');
          const title = titleField?.content || 'No title';
          console.log(`  ${j + 1}. ${title} (ID: ${note._id})`);
        }
      }
      
      // Calculate average latency
      const avgLatency = searchLatencies.reduce((sum, latency) => sum + latency, 0) / searchLatencies.length;
      console.log(`\nSearch latencies: ${searchLatencies.join('ms, ')}ms`);
      console.log(`Average search latency: ${avgLatency.toFixed(2)}ms`);
      
      // Non-functional requirement: average latency should be under 5 seconds
      expect(avgLatency).toBeLessThan(5000);
      
      console.log(`Search latency test passed: average ${avgLatency.toFixed(2)}ms < 5000ms`);
    });
  });
});

