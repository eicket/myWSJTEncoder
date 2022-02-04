x. FT8code.exe :

C:\WSJT\wsjtx\bin>ft8code "FREE FRE FREE"
    Message                               Decoded                             Err i3.n3
----------------------------------------------------------------------------------------------------
 1. FREE FRE FREE                         FREE FRE FREE                            0.0 Free text

Source-encoded message, 77 bits:
00110110011110001111010110111111101000000001001010100001110110001010101 000000

14-bit CRC:
10110011001000

83 Parity bits:
00100011101100010110001110100011000001010110110100101111110110000011010000100111110

Channel symbols (79 tones):
  Sync               Data               Sync               Data               Sync
3140652 16575246677300336026536301215 3140652 50526534145201344567440230574 3140652
   

x. JSYN

http://www.softsynth.com/jsyn/index.php

Find the max amplitude your wavefile can represent. (I'm guessing either 1, 128, or 256.) Take the 20 log of this, then use that as your 0 dB reference point.
set 0 dB to energy of sine wave with maximum amplitude

x. FT8 in python :

https://github.com/rtmrtmrtmrtm/weakmon/blob/master/notes.txt
https://github.com/rtmrtmrtmrtm/ft8mon and https://github.com/rtmrtmrtmrtm/weakmon
https://github.com/rtmrtmrtmrtm/weakmon/blob/master/ft8.py



FT8 - costas arrays - video :
https://www.youtube.com/watch?v=rjLhTN59Bg4

