package de.soutier.fixtures.yaml

import org.junit.runner.RunWith
import org.specs._
import org.specs.runner.{ JUnitSuiteRunner, JUnit }
import org.yaml.snakeyaml.Yaml
import com.webobjects.foundation.NSTimestamp
import java.util.GregorianCalendar
import java.util.Calendar


class TimestampConstructorSpec extends SpecificationWithJUnit {
  "Importing a timestamp" should {
    "allow to specify today's date" in {
      val result = new Yaml(new TimestampConstructor).load("!now").asInstanceOf[NSTimestamp]
      val now = new NSTimestamp
      now.getHours mustEqual result.getHours
      now.getMinutes mustEqual result.getMinutes
      now.getDay mustEqual result.getDay
      now.getMonth mustEqual result.getMonth
    }
    
    "correctly add and subtract days" in {
      val result = new Yaml(new TimestampConstructor).load("!ts -3 days").asInstanceOf[NSTimestamp]
      val resultCal = new GregorianCalendar
	  resultCal.setTime(result)
      val calendar = new GregorianCalendar
      calendar.setTime(new NSTimestamp)
	  calendar.add(Calendar.DAY_OF_YEAR, -3)
	  resultCal.get(Calendar.DAY_OF_MONTH) mustEqual calendar.get(Calendar.DAY_OF_MONTH)
    }
    
    "correctly add and subtract weeks" in {
      val result = new Yaml(new TimestampConstructor).load("!ts -1 weeks").asInstanceOf[NSTimestamp]
      val resultCal = new GregorianCalendar
	  resultCal.setTime(result)
      val calendar = new GregorianCalendar
      calendar.setTime(new NSTimestamp)
	  calendar.add(Calendar.WEEK_OF_YEAR, -1)
	  resultCal.get(Calendar.DAY_OF_YEAR) mustEqual calendar.get(Calendar.DAY_OF_YEAR)

    }
    
    "correctly add and subtracts months" in {
      val result = new Yaml(new TimestampConstructor).load("!ts -3 months").asInstanceOf[NSTimestamp]
      val resultCal = new GregorianCalendar
	  resultCal.setTime(result)
      val calendar = new GregorianCalendar
      calendar.setTime(new NSTimestamp)
	  calendar.add(Calendar.MONTH, -3)
	  resultCal.get(Calendar.DAY_OF_YEAR) mustEqual calendar.get(Calendar.DAY_OF_YEAR)
	  resultCal.get(Calendar.MONTH) mustEqual calendar.get(Calendar.MONTH)
    }
  }
}
