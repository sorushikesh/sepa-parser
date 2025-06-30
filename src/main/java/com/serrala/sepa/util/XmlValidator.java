package com.serrala.sepa.util;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * Utility to validate XML content against an XSD schema available on the classpath.
 */
public class XmlValidator {
    /**
     * Validate the given XML string against an XSD resource.
     *
     * @param xmlContent   XML content to validate
     * @param xsdResource  XSD resource path (relative to classpath)
     * @return true if valid, false otherwise
     */
    public static boolean validate(String xmlContent, String xsdResource) {
        try (InputStream xsd = XmlValidator.class.getClassLoader().getResourceAsStream(xsdResource)) {
            if (xsd == null) {
                throw new IllegalArgumentException("XSD resource not found: " + xsdResource);
            }
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsd));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlContent)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
