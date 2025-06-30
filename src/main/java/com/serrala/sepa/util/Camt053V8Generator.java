package com.serrala.sepa.util;

import java.io.StringWriter;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import com.serrala.sepa.model.SepaStatement;
import com.serrala.sepa.model.SepaTransaction;

public class Camt053V8Generator {
    public static String generate(SepaStatement statement) throws Exception {
        if (statement.getStatementId() == null || statement.getStatementId().isEmpty()) {
            statement.setStatementId(SepaStatement.generateRandomStatementId());
        }
        long elctrncSeqNb = (long) (Math.random() * 1_000_000_000_000_000_000L);
        long lglSeqNb = (long) (Math.random() * 1_000_000_000_000_000_000L);

        StringWriter sw = new StringWriter();
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter w = xof.createXMLStreamWriter(sw);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("Document");
        w.writeDefaultNamespace("urn:iso:std:iso:20022:tech:xsd:camt.053.001.08");
        w.writeStartElement("BkToCstmrStmt");

        w.writeStartElement("GrpHdr");
        w.writeStartElement("MsgId");
        w.writeCharacters(statement.getStatementId());
        w.writeEndElement(); // MsgId
        w.writeStartElement("CreDtTm");
        w.writeCharacters(statement.getCreationDateTime() != null ? statement.getCreationDateTime()
                : new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new java.util.Date()));
        w.writeEndElement();
        w.writeEndElement(); // GrpHdr

        w.writeStartElement("Stmt");
        w.writeStartElement("Id");
        w.writeCharacters(statement.getStatementId());
        w.writeEndElement();
        w.writeStartElement("ElctrncSeqNb");
        w.writeCharacters(Long.toString(elctrncSeqNb));
        w.writeEndElement();
        w.writeStartElement("LglSeqNb");
        w.writeCharacters(Long.toString(lglSeqNb));
        w.writeEndElement();
        w.writeStartElement("CreDtTm");
        w.writeCharacters(statement.getCreationDateTime() != null ? statement.getCreationDateTime()
                : new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new java.util.Date()));
        w.writeEndElement();

        if (statement.getPeriodFrom() != null || statement.getPeriodTo() != null) {
            w.writeStartElement("FrToDt");
            if (statement.getPeriodFrom() != null) {
                w.writeStartElement("FrDt");
                w.writeCharacters(statement.getPeriodFrom());
                w.writeEndElement();
            }
            if (statement.getPeriodTo() != null) {
                w.writeStartElement("ToDt");
                w.writeCharacters(statement.getPeriodTo());
                w.writeEndElement();
            }
            w.writeEndElement();
        }

        // Account information is mandatory before any balances
        w.writeStartElement("Acct");
        w.writeStartElement("Id");
        w.writeStartElement("IBAN");
        w.writeCharacters(statement.getAccountIban() == null || statement.getAccountIban().trim().isEmpty()
                ? "UNKNOWN" : statement.getAccountIban());
        w.writeEndElement(); // IBAN
        w.writeEndElement(); // Id
        if (statement.getAccountCurrency() != null && !statement.getAccountCurrency().isEmpty()) {
            w.writeStartElement("Ccy");
            w.writeCharacters(statement.getAccountCurrency());
            w.writeEndElement();
        }
        w.writeEndElement(); // Acct

        w.writeStartElement("Bal");
        w.writeStartElement("Tp");
        w.writeStartElement("CdOrPrtry");
        w.writeStartElement("Cd");
        w.writeCharacters("OPBD");
        w.writeEndElement(); // Cd
        w.writeEndElement(); // CdOrPrtry
        w.writeEndElement(); // Tp
        w.writeStartElement("Amt");
        w.writeAttribute("Ccy", statement.getAccountCurrency());
        w.writeCharacters("0.00");
        w.writeEndElement(); // Amt
        w.writeStartElement("CdtDbtInd");
        w.writeCharacters("CRDT");
        w.writeEndElement();
        w.writeStartElement("Dt");
        w.writeStartElement("Dt");
        w.writeCharacters(statement.getPeriodFrom() != null ? statement.getPeriodFrom()
                : new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        w.writeEndElement(); // Dt
        w.writeEndElement(); // DateAndDateTime2Choice
        w.writeEndElement(); // Bal

        // Closing balance must appear before any entries according to the XSD
        w.writeStartElement("Bal");
        w.writeStartElement("Tp");
        w.writeStartElement("CdOrPrtry");
        w.writeStartElement("Cd");
        w.writeCharacters("CLBD");
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndElement();
        w.writeStartElement("Amt");
        w.writeAttribute("Ccy", statement.getAccountCurrency());
        w.writeCharacters("0.00");
        w.writeEndElement();
        w.writeStartElement("CdtDbtInd");
        w.writeCharacters("CRDT");
        w.writeEndElement();
        w.writeStartElement("Dt");
        w.writeStartElement("Dt");
        w.writeCharacters(statement.getPeriodTo() != null ? statement.getPeriodTo()
                : new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        w.writeEndElement(); // Dt
        w.writeEndElement();
        w.writeEndElement(); // Bal

        List<SepaTransaction> txs = statement.getTransactions();
        for (SepaTransaction tx : txs) {
            w.writeStartElement("Ntry");
            w.writeStartElement("Amt");
            w.writeAttribute("Ccy", tx.getCurrency());
            w.writeCharacters(tx.getAmount());
            w.writeEndElement();

            w.writeStartElement("CdtDbtInd");
            w.writeCharacters(tx.getAmount().startsWith("-") ? "DBIT" : "CRDT");
            w.writeEndElement();

            w.writeStartElement("Sts");
            w.writeCharacters("BOOK");
            w.writeEndElement(); // Sts

            // Minimal bank transaction code
            w.writeStartElement("BkTxCd");
            w.writeStartElement("Domn");
            w.writeStartElement("Cd");
            w.writeCharacters("PMNT");
            w.writeEndElement();
            w.writeStartElement("Fmly");
            w.writeStartElement("Cd");
            w.writeCharacters("RCDT");
            w.writeEndElement();
            w.writeStartElement("SubFmlyCd");
            w.writeCharacters("GDSV");
            w.writeEndElement();
            w.writeEndElement(); // Fmly
            w.writeEndElement(); // Domn
            w.writeEndElement(); // BkTxCd

            w.writeStartElement("NtryDtls");
            w.writeStartElement("TxDtls");
            w.writeStartElement("Refs");
            w.writeStartElement("EndToEndId");
            w.writeCharacters(tx.getEndToEndId());
            w.writeEndElement(); // EndToEndId
            w.writeEndElement(); // Refs

            w.writeStartElement("Amt");
            w.writeAttribute("Ccy", tx.getCurrency());
            w.writeCharacters(tx.getAmount());
            w.writeEndElement();

            w.writeStartElement("CdtDbtInd");
            w.writeCharacters(tx.getAmount().startsWith("-") ? "DBIT" : "CRDT");
            w.writeEndElement();

            w.writeStartElement("RltdPties");
            String debtorName = tx.getDebtorName();
            String debtorIban = tx.getDebtorIban();
            if ((debtorName == null || debtorName.trim().isEmpty() || debtorName.equalsIgnoreCase("null"))
                    && tx.getCreditorName() != null && !tx.getCreditorName().trim().isEmpty()
                    && !tx.getCreditorName().equalsIgnoreCase("null")) {
                debtorName = tx.getCreditorName();
            }
            if ((debtorIban == null || debtorIban.trim().isEmpty() || debtorIban.equalsIgnoreCase("null"))
                    && tx.getCreditorIban() != null && !tx.getCreditorIban().trim().isEmpty()
                    && !tx.getCreditorIban().equalsIgnoreCase("null")) {
                debtorIban = tx.getCreditorIban();
            }
            w.writeStartElement("Dbtr");
            w.writeStartElement("Pty");
            w.writeStartElement("Nm");
            w.writeCharacters(debtorName == null || debtorName.trim().isEmpty() || debtorName.equalsIgnoreCase("null")
                    ? "UNKNOWN" : debtorName);
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndElement(); // Dbtr
            w.writeStartElement("DbtrAcct");
            w.writeStartElement("Id");
            w.writeStartElement("IBAN");
            w.writeCharacters(debtorIban == null || debtorIban.trim().isEmpty() || debtorIban.equalsIgnoreCase("null")
                    ? "UNKNOWN" : debtorIban);
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndElement(); // DbtrAcct
            w.writeStartElement("Cdtr");
            w.writeStartElement("Pty");
            w.writeStartElement("Nm");
            w.writeCharacters(tx.getCreditorName() == null || tx.getCreditorName().trim().isEmpty()
                    ? "UNKNOWN" : tx.getCreditorName());
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndElement(); // Cdtr
            w.writeStartElement("CdtrAcct");
            w.writeStartElement("Id");
            w.writeStartElement("IBAN");
            w.writeCharacters(tx.getCreditorIban());
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndElement(); // CdtrAcct
            w.writeEndElement(); // RltdPties

            w.writeStartElement("Purp");
            w.writeStartElement("Cd");
            w.writeCharacters(tx.getPurposeCode() == null || "null".equalsIgnoreCase(tx.getPurposeCode())
                    || tx.getPurposeCode().trim().isEmpty() ? "OTHR" : tx.getPurposeCode());
            w.writeEndElement();
            w.writeEndElement();

            w.writeStartElement("RmtInf");
            w.writeStartElement("Ustrd");
            w.writeCharacters(tx.getRemittanceInfo() == null || "null".equalsIgnoreCase(tx.getRemittanceInfo())
                    || tx.getRemittanceInfo().trim().isEmpty() ? "UNKNOWN" : tx.getRemittanceInfo());
            w.writeEndElement();
            w.writeEndElement();

            w.writeEndElement(); // TxDtls
            w.writeEndElement(); // NtryDtls
            w.writeEndElement(); // Ntry
        }
        w.writeEndElement(); // Stmt
        w.writeEndElement(); // BkToCstmrStmt
        w.writeEndElement(); // Document
        w.writeEndDocument();
        w.flush();
        w.close();
        return sw.toString();
    }
}
