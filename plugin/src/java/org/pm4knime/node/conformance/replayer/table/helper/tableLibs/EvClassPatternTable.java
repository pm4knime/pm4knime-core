package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.ArrayList;



public class EvClassPatternTable extends ArrayList<String> {
	private static final long serialVersionUID = 1605098098617975968L;

	public EvClassPatternTable() {
		super();
	}

	public EvClassPatternTable(int capacity) {
		super(capacity);
	}

	public String toString() {
		if (size() == 0) {
			return "Empty Pattern";
		} else {
			String outString = ""; // result
			String lifecycle = ""; // identified lifecycle
			String className = ""; // class name
			String prevClassName = ""; // previous event class

			String lifecycleSeparator = ""; // lifecycle separator

			// collect similar event classes
			for (String ec : this) {
				int plusPos = ec.lastIndexOf(";");
				if ((plusPos > 0) && (plusPos < ec.length())) {
					// there is a semicolom
					className = ec.substring(0, plusPos);
					lifecycle = ec.substring(plusPos + 1);
				} else {
					className = ec;
					lifecycle = "";
				}

				// insert to mapping
				if (!prevClassName.equals(className)){
					// close previous and print new event class name
					if (prevClassName.equals("")){
						// first case
						outString += className + "(";
					} else {
						// next case
						outString += ")," + className + "(";
					}
					prevClassName = className;
					lifecycleSeparator = "";
				} 

				outString += lifecycleSeparator;
				if (lifecycle.equals("")){
					outString += ";";
				} else {
					outString += lifecycle; 
				}
				lifecycleSeparator = ",";

			}
			outString += ")";
			return outString;
		}
	}

}
