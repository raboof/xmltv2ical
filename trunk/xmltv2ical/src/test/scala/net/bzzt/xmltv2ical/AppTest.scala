package net.bzzt.xmltv2ical;

import junit.framework._;
import Assert._;
import java.io.File;

object AppTest {
    def suite: Test = {
        val suite = new TestSuite(classOf[AppTest]);
        suite
    }

    def main(args : Array[String]) {
        junit.textui.TestRunner.run(suite);
    }
}

/**
 * Unit test for simple App.
 */
class AppTest extends TestCase("app") {

    /**
     * Rigourous Tests :-)
     */
    def testOK() = assertTrue(true);
    
    def testFoo()
    {
      System.out.println("1234");
      
      val xmltv = scala.xml.XML.loadFile(new File("/home/arnouten/dev/tvgids/tmp/tvgidsdata.xml"));
      for (val programme <- xmltv \ "programme")
      {
        //System.out.println("xx");
      }
    }
    
    //def testKO() = assertTrue(false);
    

}
