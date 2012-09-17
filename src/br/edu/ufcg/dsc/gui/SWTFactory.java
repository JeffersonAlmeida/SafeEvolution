package br.edu.ufcg.dsc.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class SWTFactory {

	public static enum TextPostion{
		LEFT, UP
	}

	public static Button createButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.NULL);
		button.setText(text);

		return button;
	}

	public static Group createGroup(Composite parent, int quantidadeDeColunas, boolean colunasDeMesmoComprimento, String text) {
		GridLayout groupLayout = new GridLayout(quantidadeDeColunas,colunasDeMesmoComprimento);
		groupLayout.verticalSpacing = 1;

		Group group = new Group(parent, SWT.NULL);
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		group.setText(text);

		return group;
	}

	public static Composite createComposite(Composite parent, int quantidadeDeColunas, boolean colunasDeMesmoTamanho) {
		Composite composite = new Composite(parent,SWT.NULL);
		GridLayout layout = new GridLayout(quantidadeDeColunas,colunasDeMesmoTamanho);

		composite.setLayout(layout);
		layout.verticalSpacing = 9;
		composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		return composite;
	}

	public static ScrolledComposite createScrolledComposite(Composite parent, int quantidadeDeColunas, boolean colunasDeMesmoTamanho) {
		ScrolledComposite composite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL);
		GridLayout layout = new GridLayout(quantidadeDeColunas,colunasDeMesmoTamanho);

		composite.setLayout(layout);
		layout.verticalSpacing = 9;
		composite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 1, 1));

		return composite;
	}

	public static Combo createComboBox(Composite parent) {
		Combo campo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		campo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		return campo;
	}

	public static Combo createComboBoxWithLabel(Composite parent, String text) {
		Composite parentComboAndLabel = createComposite(parent, 2, false);

		createLabel(parentComboAndLabel,text);

		return createComboBox(parentComboAndLabel);
	}

	public static Combo createComboBoxWithLabel(Composite parent, String text, String... options) {
		Combo result = null;

		Composite parentComboAndLabel = createComposite(parent, 2, false);

		createLabel(parentComboAndLabel,text);

		result = createComboBox(parentComboAndLabel);

		for(String option : options){
			result.add(option);
		}

		return result;
	}

	public static Text createText(Composite parent) {
		Text campo = new Text(parent, SWT.BORDER);
		campo.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, false));

		return campo;
	}

	public static Text createMultilineText(Composite parent) {
		Text campo = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);

		campo.setLayoutData(new GridData(GridData.FILL_BOTH));
		return campo;
	}

	public static Label createLabel(Composite parent, String text, int horizontalAllignment) {
		Label label = new Label(parent, SWT.NULL);
		label.setText(text);
		label.setLayoutData(new GridData(horizontalAllignment, GridData.CENTER, false, false));

		return label;
	}

	public static Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NULL);
		label.setText(text);
		label.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		return label;
	}

	public static void createTableColumn(Table table, int style, String title, int width) {
		TableColumn tc = new TableColumn(table, style);

		tc.setText(title);
		tc.setResizable(true);
		tc.setWidth(width);
	}

	public static void createRow(Table table,String text, int column) {
		TableItem newItem = new TableItem(table, SWT.NONE);

		newItem.setText(column, text);
	}

	public static void createRow(Table table, String... strings) {
		TableItem newItem = new TableItem(table, SWT.NONE);

		for(int i = 0; i < strings.length; i++){
			newItem.setText(i, strings[i]);
		}
	}

	public static Table createTable(Composite grupo, int alturaPreferencial) {
		Table vendedoresTable = new Table(grupo, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL);
		vendedoresTable.setHeaderVisible(true);
		vendedoresTable.setLinesVisible(true);

		GridData gridData = new GridData(GridData.FILL, GridData.FILL,
				true, true);
		gridData.horizontalSpan = 3;
		gridData.heightHint = alturaPreferencial;

		vendedoresTable.setLayoutData(gridData);

		return vendedoresTable;
	}

	public static Text createTextWithLabel(Composite parent, String text, TextPostion txtPosition) {
		if(txtPosition == TextPostion.LEFT){
			return SWTFactory.createTextWithLabel(parent,text);
		}
		else if(txtPosition == TextPostion.UP){
			Composite parentTextAndLabel = createComposite(parent, 1, false);

			createLabel(parentTextAndLabel,text,GridData.BEGINNING);

			return createText(parentTextAndLabel);
		}
		return null;
	}

	public static Text createTextWithLabel(Composite parent, String text) {
		Composite parentTextAndLabel = createComposite(parent, 2, false);

		createLabel(parentTextAndLabel,text);

		return createText(parentTextAndLabel);
	}

	public static String getFilePathUsingFileChooser(String[] filterExtentions, String[] filterNames, Shell shell){
		String result = "";

		FileDialog dialog = new FileDialog(shell, SWT.OPEN);

		dialog.setFilterExtensions(filterExtentions);
		dialog.setFilterNames(filterNames);

		result = dialog.open();

		return result;
	}
	
	public static String getDirectoryPathUsingFileChooser(Shell shell){
		String result = "";

		DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);

		result = dialog.open();

		return result;
	}
}
