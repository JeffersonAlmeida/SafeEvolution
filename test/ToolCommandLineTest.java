import org.junit.Test;

import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.Lines;
import br.edu.ufcg.dsc.ToolCommandLine;

public class ToolCommandLineTest {
	
	@Test
	public void testIsSameAssets() {
		ToolCommandLine t = new ToolCommandLine(Lines.DEFAULT);
		String source = Constants.PLUGIN_PATH + "/../spl_example_source/";
		String target = Constants.PLUGIN_PATH + "/../spl_example_target1/";
	//	assertTrue(t.getChangedAssets(source,target).size() == 0);
	}

}
