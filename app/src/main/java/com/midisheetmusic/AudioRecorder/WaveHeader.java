package com.midisheetmusic.AudioRecorder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by ZhouMeng on 2018/8/31.
 * wav文件頭
 */
public class WaveHeader {
//    public final char fileID[] = {'R', 'I', 'F', 'F'};
//    public int fileLength;
//    public char wavTag[] = {'W', 'A', 'V', 'E'};
//    public char FmtHdrID[] = {'f', 'm', 't', ' '};
//    public int FmtHdrLeth;
//    public short FormatTag;
//    public short Channels;
//    public int SamplesPerSec;
//    public int AvgBytesPerSec;
//    public short BlockAlign;
//    public short BitsPerSample;
//    public char DataHdrID[] = {'d','a','t','a'};
//    public int DataHdrLeth;
    public long totalAudioLen = 0;
    public long totalDataLen = totalAudioLen + 36;
    public long longSampleRate;
    public int channels;
    public long byteRate;

    public byte[] getHeader() throws IOException {
        byte [] header = new byte [ 44 ];
        header[ 0 ] = 'R' ; // RIFF/WAVE header
        header[ 1 ] = 'I' ;
        header[ 2 ] = 'F' ;
        header[ 3 ] = 'F' ;
        header[ 4 ] = ( byte ) (totalDataLen & 0xff );
        header[ 5 ] = ( byte ) ((totalDataLen >> 8 ) & 0xff );
        header[ 6 ] = ( byte ) ((totalDataLen >> 16 ) & 0xff );
        header[ 7 ] = ( byte ) ((totalDataLen >> 24 ) & 0xff );
        header[ 8 ] = 'W' ;
        header[ 9 ] = 'A' ;
        header[ 10 ] = 'V' ;
        header[ 11 ] = 'E' ;
        header[ 12 ] = 'f' ; // 'fmt ' chunk
        header[ 13 ] = 'm' ;
        header[ 14 ] = 't' ;
        header[ 15 ] = ' ' ;
        header[ 16 ] = 16 ; // 4 bytes: size of 'fmt ' chunk
        header[ 17 ] = 0 ;
        header[ 18 ] = 0 ;
        header[ 19 ] = 0 ;
        header[ 20 ] = 1 ; // format = 1
        header[ 21 ] = 0 ;
        header[ 22 ] = ( byte ) channels;
        header[ 23 ] = 0 ;
        header[ 24 ] = ( byte ) (longSampleRate & 0xff );
        header[ 25 ] = ( byte ) ((longSampleRate >> 8 ) & 0xff );
        header[ 26 ] = ( byte ) ((longSampleRate >> 16 ) & 0xff );
        header[ 27 ] = ( byte ) ((longSampleRate >> 24 ) & 0xff );
        header[ 28 ] = ( byte ) (byteRate & 0xff );
        header[ 29 ] = ( byte ) ((byteRate >> 8 ) & 0xff );
        header[ 30 ] = ( byte ) ((byteRate >> 16 ) & 0xff );
        header[ 31 ] = ( byte ) ((byteRate >> 24 ) & 0xff );
        header[ 32 ] = ( byte ) ( 2 * 16 / 8 ); // block align
        header[ 33 ] = 0 ;
        header[ 34 ] = 16 ; // bits per sample
        header[ 35 ] = 0 ;
        header[ 36 ] = 'd' ;
        header[ 37 ] = 'a' ;
        header[ 38 ] = 't' ;
        header[ 39 ] = 'a' ;
        header[ 40 ] = ( byte ) (totalAudioLen & 0xff );
        header[ 41 ] = ( byte ) ((totalAudioLen >> 8 ) & 0xff );
        header[ 42 ] = ( byte ) ((totalAudioLen >> 16 ) & 0xff );
        header[ 43 ] = ( byte ) ((totalAudioLen >> 24 ) & 0xff );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header, 0 ,44);
        bos.flush();
        byte[] h = bos.toByteArray();
        bos.close();
        return h;
    }

    private void WriteShort(ByteArrayOutputStream bos, int s) throws IOException {
        byte[] myByte = new byte[2];
        myByte[1] =(byte)( (s << 16) >> 24 );
        myByte[0] =(byte)( (s << 24) >> 24 );
        bos.write(myByte);
    }

    private void WriteInt(ByteArrayOutputStream bos, int n) throws IOException {
        byte[] buf = new byte[4];
        buf[3] =(byte)( n >> 24 );
        buf[2] =(byte)( (n << 8) >> 24 );
        buf[1] =(byte)( (n << 16) >> 24 );
        buf[0] =(byte)( (n << 24) >> 24 );
        bos.write(buf);
    }

    private void WriteChar(ByteArrayOutputStream bos, char[] id) {
        for (char c : id) {
            bos.write(c);
        }
    }
}