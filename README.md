# myWSJTEncoder

Experimental WSPR / FT4 / FT8 encoder and audio generator written in Java.  

This encoder produces a Tone, WSPR, FT4 or FT8 message on the selected audio port. 

![Alt text](/Screenshot.jpg)

For WSPR, the encoding is faithful to the WSPR protocol specification.  
For FT4 / FT8 however, two limitations exist :
1. The encoding is limited to free text
2. No gaussian filtering is done after the audio symbols are generated (TBD)

## Some implementation details 

As for the WSJTX application, the sampling rate is 12000 Hz. 

NR_OF_SAMPLES_PER_SYMBOL_WSPR = 8192  
NR_OF_SAMPLES_PER_SYMBOL_FT4 = 576   
NR_OF_SAMPLES_PER_SYMBOL_FT8 = 1920  

The symbol duration is calculated as NR_OF_SAMPLES_PER_SYMBOL / 12000 and the tone separation is 1 / symbol duration.

So, for each mode we have :

Mode           | NR_OF_SAMPLES_PER_SYMBOL | symbol duration | tone separation  
WSPR           | 8192                     | 682,66 msec     | 1,465 Hz  
FT4            | 576                      | 48 msec         | 20,833 Hz  
FT8            | 1920                     | 160 msec        | 6,25 Hz  
Tone           | 8192 => a 1 minute continuous sine wave  


## ft8code utility

For FT8, the source-encoded message, 14-bit CRC, parity bits and channel symbols can te verified with the excellent FT8code.exe tool, packaged with the WSJTX app :

C:\WSJT\wsjtx\bin>ft8code "FREE FRE FREE"

Some comments in my code, refer to the output of this utility for a free text message with contents "FREE FRE FREE"

## Java environment

This java application runs on all recent Java versions and was tested on Java 1.8, 15 and 17.

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

And finally, the app can be started up as follows  
java --module-path "{your path to Java FX}\openjfx-15.0.1_windows-x64_bin-sdk\javafx-sdk-15.0.1\lib" --add-modules javafx.controls,javafx.fxml -Djava.util.logging.config.file=console_logging.properties

## Some further useful reading :

Encoding process, by Andy G4JNT : http://www.g4jnt.com/WSPR_Coding_Process.pdf and http://www.g4jnt.com/WSJT-X_LdpcModesCodingProcess.pdf used as a companion guide to produce this application. Thanks to Andy for this.  
The FT4 and FT8 Communication Protocols - QEX July / August 2020 : https://physics.princeton.edu/pulsar/k1jt/FT4_FT8_QEX.pdf   
Synchronisation in FT8 : http://www.sportscliche.com/wb2fko/FT8sync.pdf  
Costas Arrays : http://www.sportscliche.com/wb2fko/TechFest_2019_WB2FKO_revised.pdf  
FT8 - costas arrays - video : https://www.youtube.com/watch?v=rjLhTN59Bg4  

## Credits

And finally, all credits for bringing the WSJT modes to the ham radio community goes to K1JT, Joe Taylor, and the WSJTX development team. Without their work, we would even not dream about weak signals and their processing.

Give it a try and 73's  
Erik  
ON4PB  
runningerik@gmail.com  