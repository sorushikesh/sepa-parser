package com.serrala.sepa;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    public void testMt101StatementGeneration() throws Exception {
        java.io.File mt101File = new java.io.File("sample-mt101.rft");
        assertTrue("Sample MT101 file should exist", mt101File.exists());

        String rootNamespace = null;
        boolean isMt101 = false;
        try {
            rootNamespace = extractRootNamespace(mt101File);
        } catch (Exception e) {
            isMt101 = isMt101File(mt101File);
        }
        com.serrala.sepa.parser.StatementParser parser = com.serrala.sepa.parser.ParserFactory.getParser(mt101File, rootNamespace, isMt101);
        com.serrala.sepa.model.SepaStatement statement = parser.parse(mt101File);
        assertNotNull("Statement should not be null", statement);
        assertEquals("Account IBAN should match", "GB12SEPA12341234123412", statement.getAccountIban());
        assertEquals("Account Currency should match", "EUR", statement.getAccountCurrency());
        assertEquals("Should have 2 transactions", 2, statement.getTransactions().size());

        // Generate output files
        com.serrala.sepa.util.StatementOutputWriter.writeAllOutputs(statement);

        // Check that output files exist
        assertTrue(new java.io.File("GB12SEPA12341234123412_EUR_v8.xml").exists());
        assertTrue(new java.io.File("GB12SEPA12341234123412_EUR_camt052.xml").exists());
        assertTrue(new java.io.File("GB12SEPA12341234123412_EUR.sta").exists());
        assertTrue(new java.io.File("GB12SEPA12341234123412_EUR_mt942.sta").exists());

        // Content validation for CAMT053 v8 XML
        String camt053 = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("GB12SEPA12341234123412_EUR_v8.xml")), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(camt053.contains("<IBAN>GB12SEPA12341234123412</IBAN>"));
        assertTrue(camt053.contains("<Cdtr><Nm>JAMES BOND</Nm></Cdtr>"));
        assertTrue(camt053.contains("<Cdtr><Nm>Q BRANCH</Nm></Cdtr>"));
        assertTrue(camt053.contains("<Dbtr><Nm>ORDERING CUST NAME</Nm></Dbtr>"));
        assertTrue(camt053.contains("<RmtInf><Ustrd>SUPPLIER-INV-REF1</Ustrd></RmtInf>"));
        assertTrue(camt053.contains("<RmtInf><Ustrd>SUPPLIER-INV-REF2</Ustrd></RmtInf>"));
        assertTrue(camt053.contains("<Purp><Cd>URGP</Cd></Purp>"));

        // Content validation for MT940
        String mt940 = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("GB12SEPA12341234123412_EUR.sta")), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(mt940.contains(":25:GB12SEPA12341234123412"));
        assertTrue(mt940.contains(":86:SUPPLIER-INV-REF1 DBTR:ORDERING CUST NAME CDTR:JAMES BOND"));
        assertTrue(mt940.contains(":86:SUPPLIER-INV-REF2 DBTR:ORDERING CUST NAME CDTR:Q BRANCH"));
    }

    public void testPain001StatementGeneration() throws Exception {
        java.io.File pain001File = new java.io.File("sample-pain001.xml");
        assertTrue("Sample pain.001 file should exist", pain001File.exists());

        String rootNamespace = null;
        boolean isMt101 = false;
        try {
            rootNamespace = extractRootNamespace(pain001File);
        } catch (Exception e) {
            isMt101 = isMt101File(pain001File);
        }
        com.serrala.sepa.parser.StatementParser parser = com.serrala.sepa.parser.ParserFactory.getParser(pain001File, rootNamespace, isMt101);
        assertTrue("Parser should be StAX implementation", parser instanceof com.serrala.sepa.parser.SepaCtPain001StaxParser);
        com.serrala.sepa.model.SepaStatement statement = parser.parse(pain001File);
        assertNotNull("Statement should not be null", statement);
        assertEquals("Account IBAN should match", "DE41500105177649559137", statement.getAccountIban());
        assertEquals("Account Currency should match", "EUR", statement.getAccountCurrency());
        assertEquals("Should have 2 transactions", 2, statement.getTransactions().size());

        com.serrala.sepa.util.StatementOutputWriter.writeOutputs(statement, true, false, true, false);
        assertTrue(new java.io.File("DE41500105177649559137_EUR_v8.xml").exists());
        assertTrue(new java.io.File("DE41500105177649559137_EUR_camt052.xml").exists());
        assertTrue(new java.io.File("DE41500105177649559137_EUR.sta").exists());
        assertTrue(new java.io.File("DE41500105177649559137_EUR_mt942.sta").exists());

        String camt053 = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("DE41500105177649559137_EUR_v8.xml")), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(camt053.contains("<IBAN>DE41500105177649559137</IBAN>"));
        assertTrue(camt053.contains("<Cdtr><Nm>GreenTech Solutions AG</Nm></Cdtr>"));
        assertTrue(camt053.contains("<Cdtr><Nm>BlueOcean GmbH</Nm></Cdtr>"));
        assertTrue(camt053.contains("<Dbtr><Nm>Bayerland HealthTech GmbH</Nm></Dbtr>"));
        assertTrue(camt053.contains("<RmtInf><Ustrd>Consulting Fee 1</Ustrd></RmtInf>"));
        assertTrue(camt053.contains("<RmtInf><Ustrd>Consulting Fee 2</Ustrd></RmtInf>"));
        assertTrue(camt053.contains("<Purp><Cd>PURP1</Cd></Purp>"));

        String mt940 = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("DE41500105177649559137_EUR.sta")), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(mt940.contains(":25:DE41500105177649559137"));
        assertTrue(mt940.contains(":86:Consulting Fee 1 DBTR:Bayerland HealthTech GmbH CDTR:GreenTech Solutions AG"));
        assertTrue(mt940.contains(":86:Consulting Fee 2 DBTR:Bayerland HealthTech GmbH CDTR:BlueOcean GmbH"));
    }

    public void testPain008StatementGeneration() throws Exception {
        java.io.File pain008File = new java.io.File("sample-pain008.xml");
        assertTrue("Sample pain.008 file should exist", pain008File.exists());

        String rootNamespace = null;
        boolean isMt101 = false;
        try {
            rootNamespace = extractRootNamespace(pain008File);
        } catch (Exception e) {
            isMt101 = isMt101File(pain008File);
        }
        com.serrala.sepa.parser.StatementParser parser = com.serrala.sepa.parser.ParserFactory.getParser(pain008File, rootNamespace, isMt101);
        com.serrala.sepa.model.SepaStatement statement = parser.parse(pain008File);
        assertNotNull("Statement should not be null", statement);
        assertEquals("Account IBAN should match", "DE17500105175626227839", statement.getAccountIban());
        assertEquals("Account Currency should match", "EUR", statement.getAccountCurrency());
        assertEquals("Should have 2 transactions", 2, statement.getTransactions().size());

        com.serrala.sepa.util.StatementOutputWriter.writeAllOutputs(statement);
        assertTrue(new java.io.File("DE17500105175626227839_EUR_v8.xml").exists());
        assertTrue(new java.io.File("DE17500105175626227839_EUR_camt052.xml").exists());
        assertTrue(new java.io.File("DE17500105175626227839_EUR.sta").exists());
        assertTrue(new java.io.File("DE17500105175626227839_EUR_mt942.sta").exists());

        String camt053 = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("DE17500105175626227839_EUR_v8.xml")), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(camt053.contains("<IBAN>DE17500105175626227839</IBAN>"));
        assertTrue(camt053.contains("<Cdtr><Nm>TechnoGmbH Germany-UI</Nm></Cdtr>"));
        assertTrue(camt053.contains("<Dbtr><Nm>Test Partner Automation</Nm></Dbtr>"));
        assertTrue(camt053.contains("<Dbtr><Nm>Another Debtor</Nm></Dbtr>"));
        assertTrue(camt053.contains("<RmtInf><Ustrd>Direct Debit for Invoice 1</Ustrd></RmtInf>"));
        assertTrue(camt053.contains("<RmtInf><Ustrd>Direct Debit for Invoice 2</Ustrd></RmtInf>"));
        assertTrue(camt053.contains("<Purp><Cd>INVC</Cd></Purp>"));

        String mt940 = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("DE17500105175626227839_EUR.sta")), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(mt940.contains(":25:DE17500105175626227839"));
        assertTrue(mt940.contains(":86:Direct Debit for Invoice 1 DBTR:Test Partner Automation CDTR:TechnoGmbH Germany-UI"));
        assertTrue(mt940.contains(":86:Direct Debit for Invoice 2 DBTR:Another Debtor CDTR:TechnoGmbH Germany-UI"));
    }

    /**
     * Verify that the command line application generates a statement file
     * when processing the sample pain.001 message.
     */
    public void testPain001CliCreatesStatementFile() throws Exception {
        java.io.File pain001File = new java.io.File("sample-pain001.xml");
        assertTrue("Sample pain.001 file should exist", pain001File.exists());

        java.io.File statementFile = new java.io.File("DE41500105177649559137_EUR.sta");
        if (statementFile.exists()) {
            statementFile.delete();
        }
        assertFalse("Statement file should not exist before execution", statementFile.exists());

        com.serrala.sepa.App.main(new String[] { pain001File.getPath() });

        assertTrue("Statement file should be created", statementFile.exists());
        String content = new String(java.nio.file.Files.readAllBytes(statementFile.toPath()),
                                    java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(content.contains(":25:DE41500105177649559137"));
    }

    // Copied from App.java for test context
    private static String extractRootNamespace(java.io.File xmlFile) throws Exception {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        org.w3c.dom.Document doc = dbf.newDocumentBuilder().parse(xmlFile);
        return doc.getDocumentElement().getNamespaceURI();
    }
    private static boolean isMt101File(java.io.File file) throws Exception {
        java.util.List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
        for (String line : lines) {
            if (line.startsWith(":20:") || line.startsWith(":25:") || line.startsWith(":21:") || line.startsWith(":32B:")) {
                return true;
            }
        }
        return false;
    }
}
