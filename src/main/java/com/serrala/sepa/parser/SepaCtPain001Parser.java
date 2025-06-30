package com.serrala.sepa.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.serrala.sepa.model.SepaStatement;
import com.serrala.sepa.model.SepaTransaction;

public class SepaCtPain001Parser implements StatementParser {
    private static final String NS = "urn:iso:std:iso:20022:tech:xsd:pain.001.001.09";

    @Override
    public SepaStatement parse(File xmlFile) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xmlFile);
        doc.getDocumentElement().normalize();

        SepaStatement statement = new SepaStatement();
        List<SepaTransaction> transactions = new ArrayList<>();

        // Extract account info
        NodeList dbtrAcctList = doc.getElementsByTagNameNS(NS, "DbtrAcct");
        if (dbtrAcctList.getLength() > 0) {
            Element dbtrAcct = (Element) dbtrAcctList.item(0);
            statement.setAccountIban(getElementTextNS(dbtrAcct, "IBAN"));
            statement.setAccountCurrency(getElementTextNS(dbtrAcct, "Ccy"));
        }
        // Extract transactions
        NodeList txList = doc.getElementsByTagNameNS(NS, "CdtTrfTxInf");
        for (int i = 0; i < txList.getLength(); i++) {
            Element txElem = (Element) txList.item(i);
            SepaTransaction tx = new SepaTransaction();
            // Debtor
            Element dbtr = getChildElementNS(txElem, "Dbtr");
            if (dbtr != null && getElementTextNS(dbtr, "Nm") != null) {
                tx.setDebtorName(getElementTextNS(dbtr, "Nm"));
            } else {
                Node parent = txElem.getParentNode();
                if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
                    Element pmtInf = (Element) parent;
                    Element fallbackDbtr = getChildElementNS(pmtInf, "Dbtr");
                    if (fallbackDbtr != null && getElementTextNS(fallbackDbtr, "Nm") != null) {
                        tx.setDebtorName(getElementTextNS(fallbackDbtr, "Nm"));
                    }
                }
            }
            // Debtor IBAN
            Element dbtrAcct = getChildElementNS(txElem, "DbtrAcct");
            if (dbtrAcct != null && getElementTextNS(dbtrAcct, "IBAN") != null) {
                tx.setDebtorIban(getElementTextNS(dbtrAcct, "IBAN"));
            } else {
                Node parent = txElem.getParentNode();
                if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
                    Element pmtInf = (Element) parent;
                    Element fallbackDbtrAcct = getChildElementNS(pmtInf, "DbtrAcct");
                    if (fallbackDbtrAcct != null && getElementTextNS(fallbackDbtrAcct, "IBAN") != null) {
                        tx.setDebtorIban(getElementTextNS(fallbackDbtrAcct, "IBAN"));
                    }
                }
            }
            // Remittance info
            String ustrd = getElementTextNS(txElem, "Ustrd");
            if (ustrd == null) {
                Element rmtInf = getChildElementNS(txElem, "RmtInf");
                if (rmtInf != null) {
                    Element strd = getChildElementNS(rmtInf, "Strd");
                    if (strd != null) {
                        ustrd = getElementTextNS(strd, "Ustrd");
                        if (ustrd == null) {
                            ustrd = strd.getTextContent();
                        }
                    }
                }
            }
            tx.setRemittanceInfo(ustrd);
            // Booking/Value dates from ReqdExctnDt of parent PmtInf
            Node parentNode = txElem.getParentNode();
            while (parentNode != null && parentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element parentElem = (Element) parentNode;
                if ("PmtInf".equals(parentElem.getLocalName())) {
                    String exctnDt = getElementTextNS(parentElem, "ReqdExctnDt");
                    tx.setBookingDate(exctnDt);
                    tx.setValueDate(exctnDt);
                    break;
                }
                parentNode = parentNode.getParentNode();
            }
            tx.setAmount(getElementTextNS(txElem, "InstdAmt"));
            tx.setCurrency(getElementAttributeNS(txElem, "InstdAmt", "Ccy"));
            tx.setEndToEndId(getElementTextNS(txElem, "EndToEndId"));
            tx.setPurposeCode(getElementTextNS(txElem, "Cd"));
            // Creditor
            Element cdtr = getChildElementNS(txElem, "Cdtr");
            if (cdtr != null) tx.setCreditorName(getElementTextNS(cdtr, "Nm"));
            Element cdtrAcct = getChildElementNS(txElem, "CdtrAcct");
            if (cdtrAcct != null) tx.setCreditorIban(getElementTextNS(cdtrAcct, "IBAN"));
            transactions.add(tx);
        }
        // Fallback: if accountIban or accountCurrency is null, use from first transaction
        if ((statement.getAccountIban() == null || statement.getAccountIban().isEmpty()) && !transactions.isEmpty()) {
            statement.setAccountIban(transactions.get(0).getDebtorIban());
        }
        if ((statement.getAccountCurrency() == null || statement.getAccountCurrency().isEmpty()) && !transactions.isEmpty()) {
            statement.setAccountCurrency(transactions.get(0).getCurrency());
        }
        statement.setTransactions(transactions);
        return statement;
    }

    private String getElementTextNS(Element parent, String tag) {
        NodeList list = parent.getElementsByTagNameNS(NS, tag);
        if (list.getLength() > 0) return list.item(0).getTextContent();
        return null;
    }

    private String getElementAttributeNS(Element parent, String tag, String attr) {
        NodeList list = parent.getElementsByTagNameNS(NS, tag);
        if (list.getLength() > 0) {
            Element elem = (Element) list.item(0);
            return elem.getAttribute(attr);
        }
        return null;
    }

    private Element getChildElementNS(Element parent, String tag) {
        NodeList list = parent.getElementsByTagNameNS(NS, tag);
        if (list.getLength() > 0) return (Element) list.item(0);
        return null;
    }
}
