#EzRecord
編號：\
專題名稱：EzRecord\
校名與科系：中央大學資訊管理系\
團隊成員：凃建名、張旭智、黃昭穎、趙秉宏、楊翔安\
Server檔案請見: https://github.com/vony123tony123/django_wav2midi
\
![圖片](https://user-images.githubusercontent.com/43849007/134873328-933ea72b-82c1-410b-a666-6db4ec3c29b3.png)

一、前言\
生活中有多少稍縱即逝的點子，在捕捉前就消失無蹤？為了解決這一創作者普遍存在的問題，我們開發面相新手創作者的應用程式EzRecord，提供簡單方便的功能使創作過程變得更輕鬆，可在合適的環境隨時記錄腦海中浮現的旋律。\
(聲明：本系統以ditek的開源作品MidiSheetMusic-Android為基礎添加原創功能，以下附上網址
https://github.com/ditek/MidiSheetMusic-Android) \
\
二、創意描述\
EzRecord嘗試協助使用者達成兩件事：
1.	隨時使用哼唱方式記錄下一閃即逝的靈感，且系統將自動將錄音轉成五線譜顯示在APP中方便使用者觀看。
2.	提供編輯五線譜功能，使用者可隨時隨地在手機上進行簡易編輯
3.	可將五線譜儲存成Midi通用格式文件，以便使用者回家後可輕鬆匯入電腦內專業音樂編輯軟體。
(Midi格式為現在普遍音樂編輯軟體使用之檔案格式)
4.	在作曲上我們提供沒有靈感的使用者一個靈感曲目庫，裡面有從免費商業Midi網站中挑選出來的各種曲目片段並分成六種不同的風格。使用者可聆聽曲目後找到自己喜歡的片段加入到自己之中或是從中找到靈感。

\
三、系統功能簡介\
1.錄音轉譜\
使用者可利用手機麥克風進行錄音，系統會把錄音檔轉換成五線譜呈現。若不滿意轉檔結果，可使用樂譜編輯功能修改。\
展示影片：https://youtu.be/IQXKHBWFgSg \
2.靈感提示\
系統提供六種依氛圍分類的範例樂句庫，使用者缺乏靈感時可試聽參考範例樂句，也能把喜歡的樂句加入編輯中的樂譜。(樂句來源：Carlo’s MIDI)\
展示影片：https://youtu.be/kDkiVGWaRAQ \
3.樂譜編輯\
可開啟任意midi檔進入樂譜編輯畫面，編輯功能包括音高變更、增刪音符(供編輯的音符選項依長度長至全音符，短至十六分音符)。\
展示影片：https://youtu.be/E5QTawm16bs \
\
四、系統特色\
使用者可利用哼唱錄音，隨時、方便地把想法紀錄成可靠的五線譜格式，待回到平常的創作環境後，可根據五線譜直接把內容輸入至慣用的作曲軟體，或從手機輸出midi檔。\
靈感提示提供不同情緒氛圍的樂句，使用者在認為想法不夠完整時可以參考這些樂句，也能直接以範例樂句為起點開始作曲。\
\
五、系統開發工具與技術\
1.開發環境與使用資源\
開發工具：Android Studio	開發語言：Java、XML、Python\
使用資源一覽：\
a.開源專案－ MidiSheetMusic-Android \
b.靈感樂句庫－ Carlo’s MIDI \
c.錄音功能參考－\
Android實現語音資料實時採集、播放－程式前沿 (codertw.com)\
Android 錄音實現（AudioRecord）－簡書 (jianshu.com)\
d.網路傳輸－\
django REST framework (server)；Retrofit2 (Android)\
e.Python網路架構－ django\
2.轉檔技術:\
a.邏輯\
目標是把錄音的wav格式檔案轉換成mid格式，將音訊切分成時間序列後計算出各幀的基礎頻率當作音高，排除掉靜音、雜音後計算出音高，並將對應的音符資訊放入midi文件之中。\
b.作法(依步驟條列)－ \
i.用Post將錄音wav檔上傳至server中的media資料夾。\
ii.發出Request傳送檔案路徑並啟動轉檔程式。\
iii.開始轉檔，使用Python中的librosa函式庫進行音訊處理將wav檔以採樣的時間序列讀取後：
- beat_track函數計算beats的時間和bpm(此階段處理節奏/速度)。
→以Dynamic programming的方式實現Beat Tracking。
- pYin函數獲取每幀的基礎頻率、voice_flag、voice(是否靜音)機率(此階段處理聲音)。
→先用Yin演算法獲得每幀基礎頻率的候選值和voice機率，再用Viterbi decoding演算法從候選值中選出基礎頻率。\
→根據voice機率判斷voice_flag，此步驟將排除voice_flag為false、基礎頻率為0或音符持續過短的音符。\
→將剩下幀數的音高利用hz_to_midi轉換成代表音符的midi number。
 
iv.最後使用mido函式庫把上述資訊放入midi文件，並發送request下載midi檔案至手機，轉檔完成。\
![專題架構圖](https://user-images.githubusercontent.com/43849007/135796501-abee6c8a-0c12-43e6-9044-d54db4c4ec1d.png)

\
六、系統使用對象\
1.具基本樂譜概念之新手創作者。\
2.想即時以樂譜格式記錄想法之創作者。\
3.想法快要成形，只欠一點靈感刺激之創作者。\
\
七、系統使用環境\
環境音較少、適合錄音且具網路之環境。\
\
八、結語\
無論程式、機械或是藝術，賦予想法形體是漫長而艱難的過程，我們由衷希望這些功能能幫上創作者的忙，降低創作的門檻，也讓他們有好的開始。 

![圖片](https://user-images.githubusercontent.com/43849007/135796911-34ac2a7a-1962-48ab-8f18-e072ce4cfd0b.png)

