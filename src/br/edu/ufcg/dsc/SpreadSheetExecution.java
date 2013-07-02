package br.edu.ufcg.dsc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.odftoolkit.odfdom.OdfFileDom;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.office.OdfOfficeAutomaticStyles;
import org.odftoolkit.odfdom.doc.office.OdfOfficeSpreadsheet;
import org.odftoolkit.odfdom.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.doc.style.OdfStyle;
import org.odftoolkit.odfdom.doc.style.OdfStyleParagraphProperties;
import org.odftoolkit.odfdom.doc.style.OdfStyleTableColumnProperties;
import org.odftoolkit.odfdom.doc.style.OdfStyleTableRowProperties;
import org.odftoolkit.odfdom.doc.style.OdfStyleTextProperties;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.doc.table.OdfTableColumn;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.odftoolkit.odfdom.doc.text.OdfTextParagraph;
import org.odftoolkit.odfdom.dom.attribute.office.OfficeValueTypeAttribute;
import org.odftoolkit.odfdom.dom.element.OdfStyleBase;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.w3c.dom.Node;


/**
 *
 * @author Jefferson R. Almeida.
 */
public class SpreadSheetExecution {

    String inputFileName;
    String outputFileName;
    OdfSpreadsheetDocument outputDocument;
    OdfFileDom contentDom;	// the document object model for content.xml
    OdfFileDom stylesDom;	// the document object model for styles.xml
    // the office:automatic-styles element in content.xml
    OdfOfficeAutomaticStyles contentAutoStyles;
    // the office:styles element in styles.xml
    OdfOfficeStyles stylesOfficeStyles;
    // Save the automatically generated style names
    String columnStyleName;
    String rowStyleName;
    String headingStyleName;
    String lineStyleName;
    
    String stringStyleCell;
    String noaaDateStyleName;
    String noaaTempStyleName;
    // the office:spreadsheet element in the content.xml file
    
    OdfTable table;
    OdfTableRow row;
    OdfTableColumn column;
    OdfTableCell cell;
    
    OdfOfficeSpreadsheet officeSpreadsheet;
    
    Map <String, String > approaches;
    
	private Properties properties = new Properties();

    public SpreadSheetExecution(String in, String out) throws IOException {
		super();
		this.inputFileName = in;
		this.outputFileName = out;
		this.run();
	}
    
    private void loadConsumeData(String filepath) throws IOException {
		InputStream is = new FileInputStream(filepath);
		properties.load(is);
		is.close();
	}


	public void run() throws IOException {
       /* File f = new File(outputFileName);
        if(f.exists()){
        	f.delete();
        }*/
        setupOutputDocument();
        if (outputDocument != null) {
            cleanOutDocument();
            addAutomaticStyles();
            readPropertiesFiles();
            saveOutputDocument();
        }
    }

    private void readPropertiesFiles() throws IOException {
    	 File f = new File(inputFileName);
    	 File[] files = f.listFiles();
    	 createHeader();
    	 for(File file: files){
    		 loadConsumeData(file.getAbsolutePath());
    		 processInputDocument();
    	 }
	}

	private void createHeader() {
	    table = getTable();
        row = new OdfTableRow(contentDom);
        row.setTableStyleNameAttribute(rowStyleName);
        row.appendCell(createCell(headingStyleName, "Branch Name"));
        
        row.appendCell(createCell(headingStyleName, "IC<randoop>"));
        row.appendCell(createCell(headingStyleName, "EIC<randoop>"));
        
        row.appendCell(createCell(headingStyleName, "IC<evosuite>"));
        row.appendCell(createCell(headingStyleName, "EIC<evosuite>"));
        
        row.appendCell(createCell(headingStyleName, "Time"));
        
        table.appendRow(row);
        table.appendRow(new OdfTableRow(contentDom)); // insert a blank row
	}

	void setupOutputDocument() {
        try {
            outputDocument = OdfSpreadsheetDocument.newSpreadsheetDocument();
            contentDom = outputDocument.getContentDom();
            stylesDom = outputDocument.getStylesDom();
            contentAutoStyles = contentDom.getOrCreateAutomaticStyles();
            stylesOfficeStyles = outputDocument.getOrCreateDocumentStyles();
            this.officeSpreadsheet = outputDocument.getContentRoot();
        } catch (Exception e) {
            System.err.println("Unable to create output file.");
            System.err.println(e.getMessage());
            outputDocument = null;
        }
    }

    /**
     * The default document has some content in it already (in the case
     * of a text document, a <text:p>.  Clean out all the old stuff.
     */
    void cleanOutDocument() {
        Node childNode;
        childNode = officeSpreadsheet.getFirstChild();
        while (childNode != null) {
            officeSpreadsheet.removeChild(childNode);
            childNode = officeSpreadsheet.getFirstChild();
        }
    }

    void setFontWeight(OdfStyleBase style, String value) {
        style.setProperty(OdfStyleTextProperties.FontWeight, value);
        style.setProperty(OdfStyleTextProperties.FontWeightAsian, value);
        style.setProperty(OdfStyleTextProperties.FontWeightComplex, value);
    }

    void addAutomaticStyles() {

        OdfStyle style;

         // Column style (all columns same width)
        style = contentAutoStyles.newStyle(OdfStyleFamily.TableColumn);
        columnStyleName = style.getStyleNameAttribute();
        style.setProperty(OdfStyleParagraphProperties.TextAlign, "center");
        style.setProperty(OdfStyleTableColumnProperties.ColumnWidth, "5.1cm");
        
        // Row style
        style = contentAutoStyles.newStyle(OdfStyleFamily.TableRow);
        rowStyleName = style.getStyleNameAttribute();
        style.setProperty(OdfStyleParagraphProperties.TextAlign, "center");
        style.setProperty(OdfStyleTableRowProperties.RowHeight, "0.5cm");

        // bold centered cells (for first row)
        style = contentAutoStyles.newStyle(OdfStyleFamily.TableCell);
        headingStyleName = style.getStyleNameAttribute();
        style.setProperty(OdfStyleParagraphProperties.TextAlign, "center");
        setFontWeight(style, "bold");
        
        style = contentAutoStyles.newStyle(OdfStyleFamily.TableCell);
        lineStyleName = style.getStyleNameAttribute();
        style.setProperty(OdfStyleParagraphProperties.TextAlign, "center");

        // style for string cells - All cells
        style = contentAutoStyles.newStyle(OdfStyleFamily.TableCell);
        stringStyleCell = style.getStyleNameAttribute();
        style.setStyleDataStyleNameAttribute("stringStyleCell");
        style.setProperty(OdfStyleParagraphProperties.TextAlign, "center");
     }
    
    public synchronized OdfTable getTable(){
		if(table==null){
			table = new OdfTable(contentDom);
		}
		return table;
    }

    void processInputDocument() {
      
        table = getTable();
        setColumnStyle();
        
        try {
    			
			String branchName = properties.getProperty("branchName");
			
			String icRandoop = verifyString(properties.getProperty("IC-randoop"));
			String eicRandoop = verifyString(properties.getProperty("EIC-randoop"));
			String icEvosuite = verifyString(properties.getProperty("IC-evosuite"));
			String eicEvosuite = verifyString(properties.getProperty("EIC-evosuite"));
			
			String approachTime = properties.getProperty("approachTime");
			
			
            row = new OdfTableRow(contentDom);
            row.setTableStyleNameAttribute(rowStyleName);
         
            row.appendCell(createCell(lineStyleName, branchName));
            
            row.appendCell(createCell(lineStyleName, icRandoop));
            
            row.appendCell(createCell(lineStyleName, eicRandoop));
            
            row.appendCell(createCell(lineStyleName, icEvosuite));
            
            row.appendCell(createCell(lineStyleName, eicEvosuite));
            
            row.appendCell(createCell(lineStyleName, approachTime));
            
            table.appendRow(row);
	                
    		table.appendRow(new OdfTableRow(contentDom)); // insert a blank row
        	this.officeSpreadsheet.appendChild(table);
            
        } catch (Exception e) {
            System.err.println("Cannot process " + inputFileName);
        }
    }

	private String verifyString(String str) {
		if(str.equals("true")){
			return "Refinement";
		}return "Non-Refinement";
		
	}

	private void setColumnStyle() {
		for(int i=0; i< 5; i++){
        	column = table.addStyledTableColumn(columnStyleName);
            column.setDefaultCellStyle(contentAutoStyles.getStyle(stringStyleCell,OdfStyleFamily.TableCell));	
        }
	}

    private OdfTableCell createCell(String cellStyle, String content) {
        OdfTableCell cell = new OdfTableCell(contentDom);
        OdfTextParagraph paragraph = new OdfTextParagraph(contentDom, null, content);
        cell.setTableStyleNameAttribute(cellStyle);
        cell.setOfficeStringValueAttribute(content);
        cell.setOfficeValueTypeAttribute(OfficeValueTypeAttribute.Value.STRING.toString());
        cell.appendChild(paragraph);
        return cell;
    }

    void saveOutputDocument() {
        try {	
            outputDocument.save(outputFileName);
        } catch (Exception e) {
            System.err.println("Unable to save document.");
            System.err.println(e.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
    	System.out.println("Execution Report!");
    	String in = "/media/jefferson/Expansion Drive/workspace/ferramentaLPSSM/executionReport/"; 
    	String out = "/media/jefferson/Expansion Drive/workspace/ferramentaLPSSM/Output/executionReport.ods";
        new SpreadSheetExecution(in,out);
        System.out.println("finished!");
    }
}
