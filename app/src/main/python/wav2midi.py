import numpy as np
import librosa
from mido import MidiFile,MidiTrack,Message,MetaMessage
import mido

def wav2midi(file_in,file_out):
    y, sr = librosa.load(file_in)

    if np.all(y==0) == True:
        return "Didn't record anything"

    bpm, beats = librosa.beat.beat_track(y=y, sr=sr)
    f0, voiced_flag, voiced_prob = librosa.pyin(y=y, fmin=librosa.note_to_hz("A0"), fmax=librosa.note_to_hz("C8"))
    times = librosa.times_like(f0)
    mid = MidiFile()
    track = MidiTrack()

    mid.tracks.append(track)

    track.append(Message('program_change', program=0, channel=0, time=0))
    track.append(MetaMessage('set_tempo', tempo=mido.bpm2tempo(bpm), time=0))
    prev_index = 0
    time = 0
    for i in range(1, len(f0)):
        if np.isnan(f0[i]) != True and voiced_flag[i] == True:

            if i != 0:
                prev = f0[i - 1]
            else:
                prev = np.nan
            if np.isnan(prev) != True:
                prev = round(librosa.hz_to_midi(prev))

            if i + 1 != len(f0):
                next = f0[i + 1]
            else:
                next = np.nan
            if np.isnan(next) != True:
                next = round(librosa.hz_to_midi(next))

            now = round(librosa.hz_to_midi(f0[i]))

            if i == 0 or prev != now:
                time = time + (times[i] - times[i - 1]) * 1000
            if prev == now:
                time = time + (times[i] - times[i - 1]) * 1000
            if i == len(f0) - 1 or now != next:
                if (time > 100):
                    track.append(Message('note_on', note=int(round(librosa.hz_to_midi(f0[i]))),
                                         time=0, velocity=64))
                    track.append(Message('note_off', note=int(round(librosa.hz_to_midi(f0[i]))),
                                         time=int(time), velocity=64))
                    # print('note = ' + librosa.midi_to_note((librosa.hz_to_midi(f0[i]))) + " time = " + str(
                    #     int(time)) + "\n")
                else:
                    track.append(Message('control_change', channel=0, time=int(time)))
                time = 0
        # else:
        #     track.append(Message('control_change',channel = 0, time = int((times[i] - times[i-1])*1000)))

    track.append(MetaMessage('end_of_track'))

    track2 = MidiTrack()
    mid.tracks.append(track2)
    track2.append(Message('program_change', program=0, channel=0, time=0))
    track2.append(MetaMessage('set_tempo', tempo=mido.bpm2tempo(bpm), time=0))
    track.append(MetaMessage('end_of_track'))

    mid.save(file_out)
    return "Success"



