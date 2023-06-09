/*
 * Copyright (c) 2007-2011 Madhav Vaidyanathan
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 */



package com.midisheetmusic;

import android.util.Log;

import java.util.*;



/** @class MidiTrack
 * The MidiTrack takes as input the raw MidiEvents for the track, and gets:
 * - The list of midi notes in the track.
 * - The first instrument used in the track.
 *
 * For each NoteOn event in the midi file, a new MidiNote is created
 * and added to the track, using the AddNote() method.
 * 
 * The NoteOff() method is called when a NoteOff event is encountered,
 * in order to update the duration of the MidiNote.
 */ 
public class MidiTrack {
    private int tracknum;                 /** The track number */
    private ArrayList<MidiNote> notes;    /** List of Midi notes */
    private int instrument;               /** Instrument for this track */
    private ArrayList<MidiEvent> lyrics;  /** The lyrics in this track */
    private ArrayList<MidiEvent> new_events;/** Used in write **/
    private MidiEvent end_of_track;

    /** Create an empty MidiTrack.  Used by the Clone method */
    public MidiTrack(int tracknum) {
        this.tracknum = tracknum;
        notes = new ArrayList<MidiNote>(20);
        instrument = 0;
    } 

    /** Create a MidiTrack based on the Midi events.  Extract the NoteOn/NoteOff
     *  events to gather the list of MidiNotes.
     */
    public MidiTrack(ArrayList<MidiEvent> events, int tracknum) {
        new_events = new ArrayList<MidiEvent>();
        this.tracknum = tracknum;
        notes = new ArrayList<MidiNote>(events.size());

        instrument = 0;
 
        for (MidiEvent mevent : events) {
            if (mevent.EventFlag == MidiFile.EventNoteOn && mevent.Velocity > 0) {
                MidiNote note = new MidiNote(mevent.StartTime, mevent.Channel, mevent.Notenumber, 0);
                AddNote(note);
            }
            else if (mevent.EventFlag == MidiFile.EventNoteOn && mevent.Velocity == 0) {
                NoteOff(mevent.Channel, mevent.Notenumber, mevent.StartTime);
            }
            else if (mevent.EventFlag == MidiFile.EventNoteOff) {
                NoteOff(mevent.Channel, mevent.Notenumber, mevent.StartTime);
            }
            else if (mevent.EventFlag == MidiFile.EventProgramChange) {
                new_events.add(mevent.Clone());
                instrument = mevent.Instrument;
            }
            else if (mevent.Metaevent == MidiFile.MetaEventLyric) {
                new_events.add(mevent.Clone());
                AddLyric(mevent);
            }else if(mevent.Metaevent == MidiFile.MetaEventEndOfTrack){
                end_of_track = mevent.Clone();
            }else if(mevent.EventFlag != MidiFile.EventControlChange){
                new_events.add(mevent.Clone());
            }
        }
        if (notes.size() > 0 && notes.get(0).getChannel() == 9)  {
            instrument = 128;  /* Percussion */
        }
        for(MidiNote note:notes){
            System.out.println(note);
        }
    }

    public void set_tracknumber(int i){this.tracknum = i;}

    public int trackNumber() { return tracknum; }

    public ArrayList<MidiNote> getNotes() { return notes; }

    public int getInstrument() { return instrument; }
    public void setInstrument(int value) { instrument = value; }

    public ArrayList<MidiEvent> getLyrics() { return lyrics; }
    public void setLyrics(ArrayList<MidiEvent> value) { lyrics = value; }


    public String getInstrumentName() { if (instrument >= 0 && instrument <= 128)
                  return MidiFile.Instruments[instrument];
              else
                  return "";
            }

    /** Add a MidiNote to this track.  This is called for each NoteOn event */
    public void AddNote(MidiNote m) {
        notes.add(m);
    }

    public void AddAtNote(int position, MidiNote m) {
        notes.add(position, m);
    }

    public void Delete(int m) {
        notes.remove(m);
    }

    /** A NoteOff event occured.  Find the MidiNote of the corresponding
     * NoteOn event, and update the duration of the MidiNote.
     */
    public void NoteOff(int channel, int notenumber, int endtime) {
        for (int i = notes.size()-1; i >= 0; i--) {
            MidiNote note = notes.get(i);
            if (note.getChannel() == channel && note.getNumber() == notenumber &&
                note.getDuration() == 0) {
                note.NoteOff(endtime);
                return;
            }
        }
    }

    /** Add a lyric event to this track */
    public void AddLyric(MidiEvent mevent) { 
        if (lyrics == null) {
            lyrics = new ArrayList<MidiEvent>();
        }
        lyrics.add(mevent);
    }

    /** Return a deep copy clone of this MidiTrack. */
    public MidiTrack Clone() {
        MidiTrack track = new MidiTrack(trackNumber());
        track.instrument = instrument;
        for (MidiNote note : notes) {
            track.notes.add( note.Clone() );
        }
        if (lyrics != null) {
            track.lyrics = new ArrayList<MidiEvent>();
            track.lyrics.addAll(lyrics);
        }
        return track;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(
                "Track number=" + tracknum + " instrument=" + instrument + "\n");
        for (MidiNote n : notes) {
           result.append(n).append("\n");
        }
        result.append("End Track\n");
        return result.toString();
    }

    public ArrayList<MidiEvent> Covert2Event(){
        ArrayList<MidiEvent> note_events = new ArrayList<>();

        for(int i=0;i<notes.size();i++){
            MidiEvent note_on = notes.get(i).Note2Event()[0];
            MidiEvent note_off = notes.get(i).Note2Event()[1];
            note_events.add(note_on);
            note_events.add(note_off);
        }

        Collections.sort(note_events, (x, y) -> {
            if (x.StartTime == y.StartTime) {
                if (x.EventFlag == y.EventFlag) {
                    return x.Notenumber - y.Notenumber;
                }
                else {
                    return x.EventFlag - y.EventFlag;
                }
            }
            else {
                return x.StartTime - y.StartTime;
            }
        });

        new_events.addAll(note_events);

        for(int i=0;i<new_events.size();i++){
            if(new_events.get(i).EventFlag == MidiFile.EventNoteOn){
                new_events.get(i).DeltaTime = new_events.get(i).StartTime -
                        new_events.get(i-1).StartTime;
            }
        }

        end_of_track.StartTime = new_events.get(new_events.size()-1).StartTime;
        new_events.add(end_of_track.Clone());

        return new_events;
    }
}


