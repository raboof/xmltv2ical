package net.bzzt.xmltv2ical;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import java.text.SimpleDateFormat;

import scala.collection.mutable.HashMap;

import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.util.UidGenerator;
import net.fortuna.ical4j.util.Dates;

/**
 * Simple XMLTV to ICalendar converter.
 * 
 * Properly converts all dates into UTC.
 *
 */
object XmlTvToIcal {
  
  /**
   * 
   */
  def main(args: Array[String])
  {
    if (args.length < 1)
    {
      printUsage();
    }
    else
    {
      val xmltv = loadXmlTvFile(args.first);
      val fileperchannel = args.length > 1;
      writeChannels(xmltv, fileperchannel);
    }
  }
  
  /**
   * write each channel into its own .ics file
   */
  def writeChannels(xmltv : scala.xml.Elem, fileperchannel : boolean)
  {
	val ug = new UidGenerator("1");
 
    val channelNames = getChannelNames(xmltv);
    
    if (fileperchannel)
    {
	    for (val channel <- xmltv \ "channel")
	    {
	      val calendarid = channel.attribute("id").get.text;
	
	      val ical = new Calendar();
	      
	      ical.getProperties().add(new ProdId("-//bzzt.net//xmltv2ical"));
		  ical.getProperties().add(Version.VERSION_2_0);
		  ical.getProperties().add(CalScale.GREGORIAN);
		  
		  if (! (channel \ "display-name" isEmpty))
	      {
	        ical.getProperties().add(new XProperty("X-WR-CALNAME", channel \ "display-name" text));
	      }
	      val icon = channel \ "icon";
		  if (! (icon.isEmpty))
	      {
	        ical.getProperties().add(new XProperty("X-ICON-URL", icon.first.attribute("src").get.text));
	      }
		    
		    for (val programme <- xmltv \ "programme")
		    {
		      if (calendarid.equals(programme.attribute("channel").get.text))
	          {
	            addEvent(ical, ug, programme, Some (channel \ "display-name" text));
	          }
		    }
		    
		    val outputter = new CalendarOutputter();
		    outputter.output(ical, new FileOutputStream(calendarid + ".ics"));
	    }
     }
    else
      {
	      val ical = new Calendar();
	      
	      ical.getProperties().add(new ProdId("-//bzzt.net//xmltv2ical"));
		  ical.getProperties().add(Version.VERSION_2_0);
		  ical.getProperties().add(CalScale.GREGORIAN);
  	      
          for (val programme <- xmltv \ "programme")
            {
              val channelName = channelNames.get(programme.attribute("channel").get.text);
              addEvent(ical, ug, programme, channelName);
            }
    
          val outputter = new CalendarOutputter();
	      outputter.output(ical, System.out);
        
      }
  }
  
  def getChannelNames(xmltv : scala.xml.Elem) : HashMap[String, String] = {
    val retval = new HashMap[String, String]
    for (val channel <- xmltv \ "channel")
      {
        retval += channel.attribute("id").get.text -> (channel \ "display-name" text) 
      }
    retval;
  }
  
  def addEvent(ical : Calendar, ug : UidGenerator , programme : scala.xml.Node, location : Option[String]) = {
      val title = programme \ "title" text;
      val event = new VEvent(convertDate(programme.attribute("start").get.text), 
                             convertDate(programme.attribute("stop").get.text), title);
      event.getProperties().add(ug.generateUid());
      val desc = programme \ "desc";
      if (desc.length > 0) {
        event.getProperties().add(new Description(desc.text));
      }
      val cat = programme \ "category";
      if (cat.length > 0)
      {
        event.getProperties().add(new Categories(cat.text));  
      }
      if (location.isDefined)
      {
        event.getProperties().add(new Location(location.get))
      }
      ical.getComponents().add(event);
    
  }
  
  val df = new SimpleDateFormat("yyyyMMddHHmmss Z");
  
  /**
   * parse a date from the XML file into a DateTime structure
   */
  def convertDate(date : String) : DateTime = 
    {
     val result = new DateTime(df.parse(date));
       result.setUtc(true);
       result;
     }
  
  def printUsage() =
    {
      System.out.println("Usage: java -jar xmltv2ical.jar infile.xml [-fileperchannel]");
    }
  
  /**
   * write /xmltv.dtd from the classpath onto @param file
   */
  def writeDtd(file : File)
  {
    System.err.println("Generating missing xmltv.dtd...");
    val fos = new FileOutputStream(file);
    val is = XmlTvToIcal.getClass.getResourceAsStream("/xmltv.dtd");
    
    var byte = is.read();
    while(byte >= 0)
    {
      fos.write(byte);
      byte = is.read();
    }
    fos.close();
    is.close();
  }

  /**
   * parse the file into a scala.xml element.
   * 
   * this would fail when xmltv.dtd is not present. In that case, it is generated
   * before the parser is ran.
   * 
   * @param inputFilename the file to load
   */
  def loadXmlTvFile(inputFilename : String) : scala.xml.Elem = 
    {
      val dtd = new File("xmltv.dtd");
      if (!dtd.exists)
        {
          writeDtd(dtd);
        }
      scala.xml.XML.loadFile(new File(inputFilename));
    }
    
}

