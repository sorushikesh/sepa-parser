package com.serrala.sepa.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serrala.sepa.model.SepaStatement;
import com.serrala.sepa.util.Camt053V8Generator;
import com.serrala.sepa.util.Camt053V3Generator;
import com.serrala.sepa.util.SepaStatementUtils;
import com.serrala.sepa.util.XmlValidator;

public class StatementOutputWriter {
    private static final Logger logger = LoggerFactory.getLogger(StatementOutputWriter.class);
    public static void writeAllOutputs(SepaStatement statement) throws Exception {
        writeOutputs(statement, true, true, true, true);
        writeCamt053V3(statement);
    }

    public static void writeOutputs(SepaStatement statement, boolean camt053, boolean camt052,
                                    boolean mt940, boolean mt942) throws Exception {
        if (camt053) writeCamt053(statement);
        if (camt052) writeCamt052(statement);
        if (mt940) writeMt940(statement);
        if (mt942) writeMt942(statement);
    }

    private static void writeCamt053(SepaStatement statement) throws Exception {
        SepaStatementUtils.ensureMandatoryFields(statement);
        String fileName = sanitize(statement.getAccountIban() + "_" + statement.getAccountCurrency() + "_v8.xml");
        Path outputPath = Paths.get(fileName);
        String content = Camt053V8Generator.generate(statement);

        // Validate against the bundled XSD and retry once with filled fields
        String xsd = "xsd/camt.053.001.08.xsd";
        if (!XmlValidator.validate(content, xsd)) {
            logger.warn("CAMT053 validation failed - adding random data for missing fields");
            SepaStatementUtils.ensureMandatoryFields(statement);
            content = Camt053V8Generator.generate(statement);
        }

        try (java.io.OutputStream out = Files.newOutputStream(outputPath)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
        logger.info("CAMT053 v8 statement generated: {}", fileName);
    }

    private static void writeCamt053V3(SepaStatement statement) throws Exception {
        SepaStatementUtils.ensureMandatoryFields(statement);
        String fileName = sanitize(statement.getAccountIban() + "_" + statement.getAccountCurrency() + "_v3.xml");
        Path outputPath = Paths.get(fileName);
        String content = Camt053V3Generator.generate(statement);

        // Validate against the bundled XSD and retry once with filled fields
        String xsd = "xsd/camt.053.001.03.xsd";
        if (!XmlValidator.validate(content, xsd)) {
            logger.warn("CAMT053 v3 validation failed - adding random data for missing fields");
            SepaStatementUtils.ensureMandatoryFields(statement);
            content = Camt053V3Generator.generate(statement);
        }

        try (java.io.OutputStream out = Files.newOutputStream(outputPath)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
        logger.info("CAMT053 v3 statement generated: {}", fileName);
    }

    private static void writeCamt052(SepaStatement statement) throws Exception {
        String fileName = sanitize(statement.getAccountIban() + "_" + statement.getAccountCurrency() + "_camt052.xml");
        Path outputPath = Paths.get(fileName);
        String content = com.serrala.sepa.util.Camt052V8Generator.generate(statement);
        try (java.io.OutputStream out = Files.newOutputStream(outputPath)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
        logger.info("CAMT052 v8 statement generated: {}", fileName);
    }

    private static void writeMt940(SepaStatement statement) throws Exception {
        String fileName = sanitize(statement.getAccountIban() + "_" + statement.getAccountCurrency() + ".sta");
        Path outputPath = Paths.get(fileName);
        String content = com.serrala.sepa.util.Mt940Generator.generate(statement);
        try (java.io.OutputStream out = Files.newOutputStream(outputPath)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
        logger.info("MT940 statement generated: {}", fileName);
    }

    private static void writeMt942(SepaStatement statement) throws Exception {
        String fileName = sanitize(statement.getAccountIban() + "_" + statement.getAccountCurrency() + "_mt942.sta");
        Path outputPath = Paths.get(fileName);
        String content = com.serrala.sepa.util.Mt942Generator.generate(statement);
        try (java.io.OutputStream out = Files.newOutputStream(outputPath)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
        logger.info("MT942 statement generated: {}", fileName);
    }

    private static String sanitize(String fileName) {
        return fileName.replaceAll("[^A-Za-z0-9_\\-:.]", "_");
    }
}
