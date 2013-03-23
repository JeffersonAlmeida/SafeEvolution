package br.edu.ufcg.dsc.gui;

import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import br.cin.ufpe.br.fileProperties.FilePropertiesObject;
import br.cin.ufpe.br.fileProperties.FilePropertiesReader;
import br.edu.ufcg.dsc.Approach;
import br.edu.ufcg.dsc.Lines;
import br.edu.ufcg.dsc.ToolCommandLine;
import br.edu.ufcg.dsc.am.AMFormat;
import br.edu.ufcg.dsc.builders.ProductGenerator;
import br.edu.ufcg.dsc.ck.CKFormat;
import br.edu.ufcg.dsc.evaluation.Analyzer;
import br.edu.ufcg.dsc.evaluation.SPLOutcomes;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;
import br.edu.ufcg.saferefactor.core.Criteria;
import edu.mit.csail.sdg.alloy4.Err;

public class AppWindow extends ApplicationWindow {

	private Text campoSource;
	private Text campoTarget;

	private Button botaoBrowseSource;
	private Button botaoBrowseTarget;
	private Button botaoCheck;
	private Combo comboApproaches;
	private Button sourceCKSimpleOptionRadioButton;
	private Button sourceCKHeaphestusOptionRadioButton;

	public AppWindow(String title) {
		super(null);
		addMenuBar();
		addStatusLine();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite compositePrincipal = SWTFactory.createComposite(parent, 1, true);

		Composite formats = SWTFactory.createComposite(compositePrincipal, 2, false);
		formats.setLayoutData(new GridData(GridData.CENTER, GridData.FILL, true, false));
		SWTFactory.createLabel(formats, "Format:");

		Composite ckRadioGroup = SWTFactory.createComposite(formats, 2, true);

		this.sourceCKSimpleOptionRadioButton = new Button(ckRadioGroup, SWT.RADIO);
		this.sourceCKSimpleOptionRadioButton.setText("Simple");

		this.sourceCKHeaphestusOptionRadioButton = new Button(ckRadioGroup, SWT.RADIO);
		this.sourceCKHeaphestusOptionRadioButton.setText("Hephaestus");
		this.sourceCKHeaphestusOptionRadioButton.setSelection(true);

		Composite sourceComposite = SWTFactory.createComposite(compositePrincipal, 2, false);
		this.campoSource = SWTFactory.createTextWithLabel(sourceComposite, "Source");
		this.botaoBrowseSource = SWTFactory.createButton(sourceComposite, "Browse");

		Composite targetComposite = SWTFactory.createComposite(compositePrincipal, 2, false);
		this.campoTarget = SWTFactory.createTextWithLabel(targetComposite, "Target");
		this.botaoBrowseTarget = SWTFactory.createButton(targetComposite, "Browse");

		this.comboApproaches = SWTFactory.createComboBoxWithLabel(compositePrincipal, "Aproach", Approach.APP.toString(),
				Approach.IC.toString(), Approach.IP.toString());

		Composite compositeCheck = SWTFactory.createComposite(compositePrincipal, 1, true);
		compositeCheck.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, true, false));

		this.botaoCheck = SWTFactory.createButton(compositeCheck, "Check");

		this.createButtonActions();

		return compositePrincipal;
	}

	private void createButtonActions() {
		this.botaoBrowseSource.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				String sourcePath = SWTFactory.getDirectoryPathUsingFileChooser(getShell());

				if (sourcePath != null) {
					campoSource.setText(sourcePath);
				}
			}
		});

		this.botaoBrowseTarget.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				String targetPath = SWTFactory.getDirectoryPathUsingFileChooser(getShell());

				if (targetPath != null) {
					campoTarget.setText(targetPath);
				}
			}
		});

		this.botaoCheck.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				String sourcePath = AppWindow.this.campoSource.getText();
				String targetPath = AppWindow.this.campoTarget.getText();

				String approach = AppWindow.this.comboApproaches.getItem(AppWindow.this.comboApproaches.getSelectionIndex());

				ToolCommandLine toolCommandLine = new ToolCommandLine(Lines.TARGET);
				FilePropertiesReader reader = new FilePropertiesReader();
				FilePropertiesObject propertiesObject = reader.getPropertiesObject();
				
				ProductGenerator.MAX_TENTATIVAS = 5000;

				try {

					SPLOutcomes resultado = null;

					if (AppWindow.this.sourceCKSimpleOptionRadioButton.getSelection()) {
						try {
							resultado = Analyzer.getInstance().verifyLine(toolCommandLine, propertiesObject);
						} catch (DirectoryException e1) {
							e1.printStackTrace();
						}
					} else if (AppWindow.this.sourceCKHeaphestusOptionRadioButton.getSelection()) {
						try {
							resultado = Analyzer.getInstance().verifyLine(toolCommandLine, propertiesObject);
						} catch (DirectoryException e1) {
							e1.printStackTrace();
						}
					}

					String message = "";

					if (resultado.isRefinement()) {
						message = "A LPS FOI refinanada\n";
					} else {
						message = "A LPS NAO foi refinada\n";
						message += resultado.toString();

					}

					MessageDialog.openInformation(getShell(), "Resultado", message);

				} catch (Err e1) {
					MessageDialog.openError(getShell(), "Error", e1.getMessage());
				} catch (IOException e1) {
					MessageDialog.openError(getShell(), "Error", e1.getMessage());
				} catch (AssetNotFoundException e1) {
					MessageDialog.openError(getShell(), "Error", e1.getMessage());
				}
			}
		});
	}

	//		this.botaoCadastrar.addSelectionListener(new SelectionListener(){
	//			@Override
	//			public void widgetDefaultSelected(SelectionEvent e) {
	//			}
	//
	//			@Override
	//			public void widgetSelected(SelectionEvent e) {
	////				AdicionarTipoContratoDialog dialog = new AdicionarTipoContratoDialog(shell);
	////
	////				int code = dialog.open();
	////
	////				if (code == Window.OK) {
	////					//TODO: Implementar a��o aqui.
	////
	////					MessageDialog.openInformation(shell,Properties.getProperty("title_information"),Properties.getProperty("success_operation"));
	////				}
	//			}
	//
	//		});
}
