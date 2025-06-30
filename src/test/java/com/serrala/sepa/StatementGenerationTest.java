package com.serrala.sepa;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import com.serrala.sepa.App;
import com.serrala.sepa.model.SepaStatement;
import com.serrala.sepa.parser.ParserFactory;
import com.serrala.sepa.parser.StatementParser;
import com.serrala.sepa.util.StatementOutputWriter;

import junit.framework.TestCase;

public class StatementGenerationTest extends TestCase {
    private Path tempDir;
    private String originalUserDir;

    @Override
    protected void setUp() throws Exception {
        tempDir = Files.createTempDirectory("stmtTest");
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
    }

    @Override
    protected void tearDown() throws Exception {
        System.setProperty("user.dir", originalUserDir);
        Files.walk(tempDir)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }

    public void testGenerateStatementsForResources() throws Exception {
        String[] files = {"Bayer_HealthTech.xml", "Bayer_Holding.xml"};
        for (String name : files) {
            File xml = new File(getClass().getClassLoader().getResource(name).toURI());
            String ns = App.extractRootNamespace(xml);
            StatementParser parser = ParserFactory.getParser(xml, ns, false);
            SepaStatement statement = parser.parse(xml);
            StatementOutputWriter.writeAllOutputs(statement);

            String base = statement.getAccountIban() + "_" + statement.getAccountCurrency();
            assertTrue(new File(tempDir.toFile(), base + "_v8.xml").exists());
            assertTrue(new File(tempDir.toFile(), base + "_camt052.xml").exists());
            assertTrue(new File(tempDir.toFile(), base + ".sta").exists());
            assertTrue(new File(tempDir.toFile(), base + "_mt942.sta").exists());
        }
    }
}
