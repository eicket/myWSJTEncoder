// Erik Icket, ON4PB - 2022

package common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class PropertiesWrapper
{

    static final Logger logger = Logger.getLogger(PropertiesWrapper.class.getName());
    Properties prop;

    public PropertiesWrapper()
    {

        // load the properties
        prop = new Properties();

        try
        {
            FileInputStream fis = new FileInputStream("DSP.properties");
            prop.load(fis);
        }
        catch (IOException ex)
        {
            reset();
        }

        logger.fine("properties : " + prop.toString());
    }

    public void reset()
    {
        prop.clear();        
        setProperty("ReceivedAudioOut", "SMS23A350H (High Definition Audio Device)");      
    }

    public void setProperty(String name, String value)
    {
        prop.setProperty(name, value);
        try
        {
            //save properties to project root folder
            prop.store(new FileOutputStream("DSP.properties"), null);
        }
        catch (IOException ex)
        {
            logger.severe("Property file can not be saved");
            return;
        }
    }

    public String getStringProperty(String name)
    {
        return prop.getProperty(name);
    }

    public int getIntProperty(String name)
    {
        int i;
        try
        {
            i = Integer.parseInt(prop.getProperty(name));

        }
        catch (NumberFormatException e2)
        {
            return -1;
        }
        return i;
    }
}
