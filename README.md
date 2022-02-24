# myWSJTEncoder

Experimental WSPR / FT4 / FT8 encoder and audio generator written in Java.  

This encoder produces a Tone, WSPR, FT4 or FT8 message on the selected audio port. 

![Alt text](/screenshots/Encoder.jpg)

For WSPR, the encoding is faithful to the WSPR protocol specification.  
For FT4 / FT8 however, the encoding is limited to a "Free Text" message type (Type 0.0). 


## Some implementation details 

As for the WSJTX application, the sampling rate is 12000 Hz. 

NR_OF_SAMPLES_PER_SYMBOL_WSPR = 8192  
NR_OF_SAMPLES_PER_SYMBOL_FT4 = 576   
NR_OF_SAMPLES_PER_SYMBOL_FT8 = 1920  

The symbol duration is calculated as NR_OF_SAMPLES_PER_SYMBOL / 12000 and the tone separation is 1 / symbol duration.

So, for each mode we have :

| Mode | NR_OF_SAMPLES_PER_SYMBOL | symbol duration | tone separation  
| ---- | ---- | ---- | ---- |
| WSPR | 8192 | 682,66 msec     | 1,465 Hz  |
| FT4 | 576 | 48 msec         | 20,833 Hz  |
| FT8 | 1920 | 160 msec        | 6,25 Hz  |
| Tone | 8192 => a 1 minute continuous sine wave  


## Gaussian smoothing (FT4/FT8)

As per the protocol definition, all modes use a continuous phase frequency shift keying.

FT4/FT8 frequency deviations are smoothed with a Gaussian filter. 
A single Gaussian smoothed frequency deviation pulse is created according to equation (3) in [1] and then superposed on each symbol. 
The length of the pulse is limited to a 3 symbol window and is superposed on the previous, current and next symbol.
The frequency deviation, calculated as a phase angle, is calculated per sample with a raised-cosine ramp applied to the first and last symbol.

The Gaussian-smoothed frequency deviation pulse has the following shape for FT4 (BT=1), FT8 (BT=2) and an almost rectangular frequency deviation (BT=99) :

![Alt text](/screenshots/Pulse.jpg)

The effect of the FT4 frequency deviation smoothing (BT=1) can be clearly observed, compared to a pure rectangular frequency deviation :

![Alt text](/screenshots/FT4_frequency_deviation.jpg)

The above diagram shows frequency deviations for symbols : 0, 1, 3, 2, 1, 0, 3, 1, 3, 0

As for FT8, a milder smoothing is applied (BT=2) :

![Alt text](/screenshots/FT8_frequency_deviation.jpg)

The above diagram shows frequency deviations for symbols :  0, 1, 2, 2, 3, 4, 5, 7, 0, 6 

## ft8code utility

For FT8, the source-encoded message, 14-bit CRC, parity bits and channel symbols can be verified with the excellent FT8code.exe tool, packaged with the WSJTX app :

C:\WSJT\wsjtx\bin>ft8code "FREE FRE FREE"

Some comments in the code, refer to the output of this utility for a free text message with contents "FREE FRE FREE"

## Java environment

This java application runs on all recent Java versions and was tested on Java 1.8, 15 and 17.
The app is developed with NetBeans 12.6 and the project properties can be found in the nbproject folder.

For all audio processing, the app uses the native javax library. So no external libraries, dll's .. are required.

The user interface is developed with JavaFX version 15. The GUI layout is defined in the Main.fxml file and can be edited by hand, or better, with the JavaFX SceneBuilder.

In your IDE, make sure that the following jar files are on the project classpath :  
javafx-swt.jar  
javafx.base.jar  
javafx.controls.jar  
javafx.fxml.jar  
javafx.graphics.jar  
javafx.media.jar  
javafx.swing.jar  
javafx.web.jar  
as well as charm-glisten-6.0.6.jar  

The Java app can be started up as follows :
java --module-path "{your path to Java FX}\openjfx-15.0.1_windows-x64_bin-sdk\javafx-sdk-15.0.1\lib" --add-modules javafx.controls,javafx.fxml -Djava.util.logging.config.file=console_logging.properties -jar "dist\myWSJTEncoder.jar"

Two more Java apps are provided to display the Gaussian smoothed pulse and the Frequency deviation per mode.

## Some further useful reading :

[1] The FT4 and FT8 Communication Protocols - QEX July / August 2020 : https://physics.princeton.edu/pulsar/k1jt/FT4_FT8_QEX.pdf   
[2] Encoding process, by Andy G4JNT : http://www.g4jnt.com/WSPR_Coding_Process.pdf and http://www.g4jnt.com/WSJT-X_LdpcModesCodingProcess.pdf  
[3] Synchronisation in FT8 : http://www.sportscliche.com/wb2fko/FT8sync.pdf  
[4] Costas Arrays : http://www.sportscliche.com/wb2fko/TechFest_2019_WB2FKO_revised.pdf  
[5] FT8 - costas arrays - video : https://www.youtube.com/watch?v=rjLhTN59Bg4  

## Copyright notice and credits

The algorithms, source code, look-and-feel of WSJT-X and related programs, and protocol specifications for the modes FSK441, FT4, FT8, JT4, JT6M, JT9, JT65, JTMS, QRA64, ISCAT, and MSK144 
are Copyright © 2001-2020 by one or more of the following authors: Joseph Taylor, K1JT; Bill Somerville, G4WJS; Steven Franke, K9AN; Nico Palermo, IV3NWV; Greg Beam, KI7MT; Michael Black, W9MDB; 
Edson Pereira, PY2SDR; Philip Karn, KA9Q; and other members of the WSJT Development Group.

All credits to K1JT, Joe Taylor, and the WSJTX development team. Without their work, we would even not dream about weak signals and their processing !


Give it a try and 73's  
Erik  
ON4PB  
runningerik@gmail.com  