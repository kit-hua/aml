package clause;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for formatted XQuery clauses
 * @author Yingbing Hua, IAR-IPR, KIT
 * @email yingbing.hua@kit.edu, yingbing.hua@gmail.com
 */
public abstract class AbstractFormattedXQueryClause implements IXQueryClause{

	public String getTabs (int nrTabs) {
		String tabs = "";
		for(int i = 0; i < nrTabs; i++)
			tabs += "\t";
		
		return tabs;
	}

	public String format (String xquery) {
		String formatted = "";
		String[] lines = xquery.split("\n");
		
		int nrTabs = 0;
		for(String line : lines) {			
			//TODO: for now, remove tabs first
			line = line.replaceAll("\t", "");
			if(line.equals("(") || line.equals("{")) {											
				formatted += getTabs(nrTabs) + line + "\n";
				nrTabs ++;
			}
			else if (line.equals(")") || line.equals("}")) {								
				nrTabs --;
				formatted += getTabs(nrTabs) + line + "\n";
			}
			// put a lonely "," to the end of the previous line
			else if(line.replaceAll("\t", "").replaceAll(" ", "").replaceAll("\n", "").equals(",")) {
				formatted = formatted.substring(0, formatted.length()-1);
				formatted += ",\n";
			}
			else {				
				formatted += getTabs(nrTabs) + line + "\n";
			}
		}
		
		return formatted;
	}
	
	public String toString () {
		return format(this.getXQuery());
	}
}
