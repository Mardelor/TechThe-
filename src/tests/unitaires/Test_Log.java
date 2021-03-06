package unitaires;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pfg.config.Config;
import utils.ConfigData;
import utils.Log;

import java.io.*;

public class Test_Log
{
    /**
     * Stream & Fichier de sortie pour contrôler le résultat des tests
     */
    private PrintStream output;
    private PrintStream systemOut;
    private File file;
    private BufferedReader input;

    @Before
    public void setUp() throws Exception
    {
        file = new File("LogTest");
        output = new PrintStream(file);
        input = new BufferedReader(new FileReader(file));
        systemOut = System.out;
        System.setOut(output);
        Log.activeAllChannels();
        Log.init(new Config(ConfigData.values(), true));
    }

    @After
    public void tearDown() throws Exception
    {
        file.delete();
        file = null;
        output = null;
        input = null;
        System.setOut(systemOut);
        Log.disableAllChannels();
        Log.close();
    }

    @Test
    public void testLogDebug() throws Exception
    {
        Log.COMMUNICATION.debug("TC");
        Log.LOCOMOTION.debug("TL");
        Log.DATA_HANDLER.debug("TD");
        Log.STRATEGY.debug("TS");
        output.flush();

        Assert.assertTrue(input.readLine().endsWith("DEMARRAGE DU SERVICE DE LOG"));
        Assert.assertTrue(input.readLine().endsWith(""));
        Assert.assertTrue(input.readLine().endsWith("TC"));
        Assert.assertTrue(input.readLine().endsWith("TL"));
        Assert.assertTrue(input.readLine().endsWith("TD"));
        Assert.assertTrue(input.readLine().endsWith("TS"));
    }

    @Test
    public void testLogChannel() throws Exception
    {
        Log.COMMUNICATION.setActive(false);
        Log.DATA_HANDLER.setActive(false);

        Log.COMMUNICATION.debug("TC");
        Log.LOCOMOTION.debug("TL");
        Log.DATA_HANDLER.debug("TD");
        Log.STRATEGY.debug("TS");
        output.flush();

        Assert.assertTrue(input.readLine().endsWith("DEMARRAGE DU SERVICE DE LOG"));
        Assert.assertTrue(input.readLine().endsWith(""));
        Assert.assertTrue(input.readLine().endsWith("TL"));
        Assert.assertTrue(input.readLine().endsWith("TS"));
    }

    @Test
    public void testLogCritical() throws Exception
    {
        Log.COMMUNICATION.setActive(false);
        Log.LOCOMOTION.setActive(false);

        Log.COMMUNICATION.critical("SUUS");
        Log.LOCOMOTION.critical("SU");

        Assert.assertTrue(input.readLine().endsWith("DEMARRAGE DU SERVICE DE LOG"));
        Assert.assertTrue(input.readLine().endsWith(""));
        Assert.assertTrue(input.readLine().endsWith("SUUS"));
        Assert.assertTrue(input.readLine().endsWith("SU"));
    }
}
