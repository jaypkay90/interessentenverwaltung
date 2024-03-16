import java.util.Map;

import javax.swing.JTextField;
import javax.swing.RowFilter;

public class MyRowFilter extends RowFilter {
	private String searchText;
	private int colCount;
	//private Map<Integer, String> colsToFilter;
	
	MyRowFilter () {
	
	}
	
	MyRowFilter (String searchText, int colCount) {
		this.searchText = searchText;
		this.colCount = colCount;
	}
	

	@Override
	// Daten kommen aus einem JTable --> ein Entry entspricht einer Reihe in der Tabelle
	// Diese Methode gibt true zurück, wenn eine Reihe angezeigt werden soll und false, wenn nicht
	public boolean include(Entry entry) {
		// Ist der Suchtext innnerhalb der aktuellen Zeile in der col, für die die Suche spezifiziert wurde, enthalten?
		// Gehe durch alle Reihen im Table und schaue, ob der Suchtext drin ist
		for (int i = 0; i < colCount; i++) {
			if (entry.getStringValue(i).indexOf(searchText) >= 0) {
				return true;
			}
		}
		return false;
	}
	
	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public int getColNum() {
		return colNum;
	}

	public void setColNum(int colNum) {
		this.colNum = colNum;
	}

	public void appendColToFilter(int colNum, String searchText) {
		colsToFilter.put(colNum, searchText);
	}

}
