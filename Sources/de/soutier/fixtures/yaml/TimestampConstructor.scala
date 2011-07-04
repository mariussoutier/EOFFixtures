package de.soutier.fixtures.yaml

import org.yaml.snakeyaml.constructor.AbstractConstruct
import com.webobjects.foundation.NSTimestamp
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.Tag
import java.util.GregorianCalendar
import java.util.Calendar

/** Adds YAML tags to easily create timestamps.
 * !now is a shortcut for !!com.webobjects.foundation.NSTimestamp.
 * !ts allows you to create a timestamp that adds or substracts days, weeks or months from the current date. 
 * For example: !ts +3 months or !ts -2 days. 
*/
class TimestampConstructor extends Constructor {
  yamlConstructors.put(new Tag("!ts"), new ConstructTimestap)
  yamlConstructors.put(new Tag("!now"), new AbstractConstruct {def construct(node: Node): java.lang.Object = new NSTimestamp})
}

private class ConstructTimestap extends AbstractConstruct {
  def addTimeToTimestamp(time: Int, calendarTimeConstant: Int) = {
    val calendar = new GregorianCalendar
    calendar.setTime(new NSTimestamp)
	calendar.add(calendarTimeConstant, time)
	new NSTimestamp(calendar.getTime())
  }
  
  def addDaysToTimestamp(days: Int) = addTimeToTimestamp(days, Calendar.DAY_OF_YEAR)
  def addWeeksToTimestamp(weeks: Int) = addTimeToTimestamp(weeks, Calendar.WEEK_OF_YEAR)
  def addMonthsToTimestamp(months: Int) = addTimeToTimestamp(months, Calendar.MONTH)
    
  def construct(node: Node): java.lang.Object = {
    val scalarNode = node.asInstanceOf[org.yaml.snakeyaml.nodes.ScalarNode]
    val extractor = "(\\-|\\+)([0-9]+)\\s+(days|weeks|months)".r
    
    scalarNode.getValue match {
      case extractor(sign, number, unit) =>
        val parsedNumber = Integer.parseInt((if (sign == "-") sign else "") + number)
        unit match {
          case "days" => addDaysToTimestamp(parsedNumber)
          case "weeks" => addWeeksToTimestamp(parsedNumber)
          case "months" => addMonthsToTimestamp(parsedNumber)
        }
    }
  }
}
