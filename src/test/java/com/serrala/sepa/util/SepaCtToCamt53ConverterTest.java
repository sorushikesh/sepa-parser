package com.serrala.sepa.util;

import java.io.File;

import junit.framework.TestCase;

/**
 * Tests for {@link SepaCtToCamt53Converter}.
 */
public class SepaCtToCamt53ConverterTest extends TestCase {

    /**
     * Converts a sample pain.001 file and verifies CAMT053 outputs are generated.
     */
    public void testConvertPain001() throws Exception {
        File xml = new File(getClass().getClassLoader().getResource("Bayer_HealthTech.xml").toURI());
        SepaCtToCamt53Converter.ConversionResult result = SepaCtToCamt53Converter.convert(xml);
        assertNotNull(result);
        assertNotNull(result.getCamt53V3Xml());
        assertNotNull(result.getCamt53V8Xml());
        assertTrue(result.getCamt53V3Xml().contains("urn:iso:std:iso:20022:tech:xsd:camt.053.001.03"));
        assertTrue(result.getCamt53V8Xml().contains("urn:iso:std:iso:20022:tech:xsd:camt.053.001.08"));

        // verify mapping of message id and creditor account
        assertTrue(result.getCamt53V8Xml().contains("<MsgId>MSG1090-STMT</MsgId>"));
        assertTrue(result.getCamt53V8Xml().contains("<IBAN>DE10500105179668777364</IBAN>"));
    }
}
