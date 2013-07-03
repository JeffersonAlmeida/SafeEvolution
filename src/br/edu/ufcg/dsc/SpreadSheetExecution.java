package br.edu.ufcg.dsc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.w3c.dom.NodeList;


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
    OdfTable secondTable;
    OdfTableColumn column;
    OdfTableCell cell;
    
    OdfOfficeSpreadsheet officeSpreadsheet;
    
    Map <String, String > approaches;
    
	private Properties properties = new Properties();

    public SpreadSheetExecution(String in, String out) throws IOException {
		super();
		this.inputFileName = in;
		this.outputFileName = out;
	}
    
    private void loadConsumeData(String filepath) throws IOException {
		InputStream is = new FileInputStream(filepath);
		this.properties.load(is);
		is.close();
	}

	public void run() throws IOException {
        setupOutputDocument();
        if (this.outputDocument != null) {
            readPropertyFile();
            processInputDocument();
            saveOutputDocument();
        }
    }

    private void readPropertyFile() throws IOException {
    	 File file = new File(inputFileName);
   		 loadConsumeData(file.getAbsolutePath());
	}

	private void createHeader() {
	    table = getTable();
	    secondTable = getSecondTable();
	    OdfTableRow secondRow = new OdfTableRow(contentDom);
	    OdfTableRow firstRow = new OdfTableRow(contentDom);
        firstRow.setTableStyleNameAttribute(rowStyleName);
        firstRow.appendCell(createCell(headingStyleName, "Pair Id"));
        firstRow.appendCell(createCell(headingStyleName, "IC<randoop>"));
        firstRow.appendCell(createCell(headingStyleName, "EIC<randoop>"));
        firstRow.appendCell(createCell(headingStyleName, "IC<evosuite>"));
        firstRow.appendCell(createCell(headingStyleName, "EIC<evosuite>"));
        firstRow.appendCell(createCell(headingStyleName, "Expected ?"));
        secondRow.setTableStyleNameAttribute(rowStyleName);
        secondRow.appendCell(createCell(headingStyleName, "Pair Id"));
        secondRow.appendCell(createCell(headingStyleName, "IC<randoop>"));
        secondRow.appendCell(createCell(headingStyleName, "EIC<randoop>"));
        secondRow.appendCell(createCell(headingStyleName, "IC<evosuite>"));
        secondRow.appendCell(createCell(headingStyleName, "EIC<evosuite>"));
        table.appendRow(firstRow);
        secondTable.appendRow(new OdfTableRow(contentDom));
        secondTable.appendRow(secondRow);
	}

	void setupOutputDocument() {
        try {
        	File file = new File(this.outputFileName);
        	if(file.exists()){
    			outputDocument =  (OdfSpreadsheetDocument) OdfSpreadsheetDocument.loadDocument(this.outputFileName);
        		configureVariables();
        		addAutomaticStyles();
        	}else{
    			outputDocument = OdfSpreadsheetDocument.newSpreadsheetDocument();
    			configureVariables();
    			this.officeSpreadsheet.appendChild(new OdfTable(contentDom)); // In this case, second sheet does not exist yet.
                addAutomaticStyles();
                createHeader();
    		}
        } catch (Exception e) {
            System.err.println("Unable to create output file.");
            System.err.println(e.getMessage());
            outputDocument = null;
        }
    }

	private void configureVariables() throws Exception{
		contentDom = outputDocument.getContentDom();
        stylesDom = outputDocument.getStylesDom();
        contentAutoStyles = contentDom.getOrCreateAutomaticStyles();
        stylesOfficeStyles = outputDocument.getOrCreateDocumentStyles();
        this.officeSpreadsheet = outputDocument.getContentRoot();
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
			table = (OdfTable) this.outputDocument.getOfficeBody().getChildNodes().item(0).getChildNodes().item(0);
		}
		return table;
    }
    
    public synchronized OdfTable getSecondTable(){
		if(secondTable==null){
			secondTable = (OdfTable) this.outputDocument.getOfficeBody().getChildNodes().item(0).getChildNodes().item(1);
		}
		return secondTable;
    }

    void processInputDocument() {
        table = getTable();
        secondTable = getSecondTable();
        setColumnStyle();
        try {
			String pairId = properties.getProperty("pairId");
			String[] icRandoopAndTime = properties.getProperty("IC-randoop").split(",");
			String[] eicRandoopAndTime = properties.getProperty("EIC-randoop").split(",");
			String[] icEvosuiteAndTime = properties.getProperty("IC-evosuite").split(",");
			String[] eicEvosuiteAndTime = properties.getProperty("EIC-evosuite").split(",");
			
			String icRandoop = icRandoopAndTime[0];
			String icRandoopTime = icRandoopAndTime[1];
			String eicRandoop = eicRandoopAndTime[0];
			String eicRandoopTime = eicRandoopAndTime[1];
			String icEvosuite = icEvosuiteAndTime[0];
			String icEvosuiteTime = icEvosuiteAndTime[1];
			String eicEvosuite = eicEvosuiteAndTime[0];
			String eicEvosuiteTime = eicEvosuiteAndTime[1];	
			
			 OdfTableRow firstRow = new OdfTableRow(contentDom);
			
			firstRow.setTableStyleNameAttribute(rowStyleName);
			firstRow.appendCell(createCell(lineStyleName, pairId));
			firstRow.appendCell(createCell(lineStyleName, icRandoop));
			firstRow.appendCell(createCell(lineStyleName, eicRandoop));
			firstRow.appendCell(createCell(lineStyleName, icEvosuite));
			firstRow.appendCell(createCell(lineStyleName, eicEvosuite));
            
            OdfTableRow secondRow = new OdfTableRow(contentDom);
            secondRow.setTableStyleNameAttribute(rowStyleName);
            secondRow.appendCell(createCell(lineStyleName, pairId));
            secondRow.appendCell(createCell(lineStyleName, icRandoopTime));
            secondRow.appendCell(createCell(lineStyleName, eicRandoopTime));
            secondRow.appendCell(createCell(lineStyleName, icEvosuiteTime));
            secondRow.appendCell(createCell(lineStyleName, eicEvosuiteTime));
            
            //if(!spreadSheetContainsThisPair(pairId, firstRow))
            table.appendRow(firstRow);
        	
            //Replace the First Sheet
        	this.officeSpreadsheet.replaceChild(table, this.outputDocument.getOfficeBody().getChildNodes().item(0).getChildNodes().item(0));

        	//Replace the Second Sheet
        	secondTable.appendRow(secondRow);
        	this.officeSpreadsheet.replaceChild(secondTable, this.outputDocument.getOfficeBody().getChildNodes().item(0).getChildNodes().item(1));
        } catch (Exception e) {
            System.err.println("Cannot process " + inputFileName);
        }
    }

	private boolean spreadSheetContainsThisPair(String pairId, OdfTableRow row) {
        NodeList nl = (NodeList) this.outputDocument.getOfficeBody().getChildNodes().item(0).getChildNodes().item(0);
        int size = nl.getLength(); 
        for(int i=0; i< size; i++){
        	Node node = nl.item(i);
        	System.out.println(" nodeName: " + node.getNodeName() + " i = " + i);
        	if(node.getNodeName().equals("table:table-row")){
        		if(node.toString().contains(pairId)){
        			this.outputDocument.getOfficeBody().getChildNodes().item(0).getChildNodes().item(0).replaceChild(row, node);
        			return true;
            	}
        	}
        }return false;
	}

	private void setColumnStyle() {
		for(int i=0; i< 5; i++){
        	column = table.addStyledTableColumn(columnStyleName);
            column.setDefaultCellStyle(contentAutoStyles.getStyle(stringStyleCell,OdfStyleFamily.TableCell));
            column = secondTable.addStyledTableColumn(columnStyleName);
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
    
    public String getInputFileName() {
		return inputFileName;
	}
    public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}
	public String getOutputFileName() {
		return outputFileName;
	}
	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

    /**
     * @param args the command line arguments
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
    	System.out.println("Generating SpreadSheetReport ... ");
    	String in = "/media/jefferson/Expansion Drive/workspace/ferramentaLPSSM/executionReport/template.properties"; 
    	String out = "/media/jefferson/Expansion Drive/workspace/ferramentaLPSSM/Output/report.ods";
    	SpreadSheetExecution sheet = new SpreadSheetExecution(in,out);
        sheet.run();
        System.out.println("finished!");
    }
}
