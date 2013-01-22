/**
 * 
 */
package toolUnderstanding;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;
import br.edu.ufcg.dsc.util.FilesManager;

/**
 * @author Jefferson Almeida - jra at cin dot ufpe dot br
 */
public class FilesManagerTest {
	
	FilesManager filesManager;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		filesManager = FilesManager.getInstance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		filesManager = null;
	}
	
	@Test
	public void copyFileTest() throws AssetNotFoundException, DirectoryException{
		String sp = "D:/sourcePath/Game.java"; 
		String tp = "D:/targetPath/Game.java";
	    try {
	    	filesManager.copyFile(sp, tp);
		} catch (Exception e) {
			System.out.println("Error Occurred when trying to copy file from " + sp + " to " + tp + "\n\n" + e.getMessage());
		}
	}
	
	@Test
	public void copyFilesTest() throws AssetNotFoundException, DirectoryException{
		String sp = "D:/sourcePath/"; 
		String tp = "D:/targetPath/";
		File fsp = new File(sp); File ftp = new File(tp);
		try {
			filesManager.copyFiles(fsp, fsp, ftp);
		} catch (Exception e) {
			System.out.println("Error Occurred when trying to copy file from " + sp + " to " + tp + "\n\n" + e.getMessage());
		}
		
	}

}
