package com.serrala.sepa.util;

import java.util.List;

import com.serrala.sepa.model.SepaStatement;
import com.serrala.sepa.model.SepaTransaction;

public class Camt053V8Generator {
    public static String generate(SepaStatement statement) {
        if (statement.getStatementId() == null || statement.getStatementId().isEmpty()) {
            statement.setStatementId(SepaStatement.generateRandomStatementId());
        }
        long elctrncSeqNb = (long)(Math.random() * 1_000_000_000_000_000_000L);
        long lglSeqNb = (long)(Math.random() * 1_000_000_000_000_000_000L);
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.053.001.08\">\n");
        sb.append("  <BkToCstmrStmt>\n");
        sb.append("    <GrpHdr>\n");
        sb.append("      <MsgId>").append(statement.getStatementId() != null ? statement.getStatementId() : "MSGID-001").append("</MsgId>\n");
        sb.append("      <CreDtTm>").append(statement.getCreationDateTime() != null ? statement.getCreationDateTime() : new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new java.util.Date())).append("</CreDtTm>\n");
        sb.append("    </GrpHdr>\n");
        sb.append("    <Stmt>\n");
        sb.append("      <Id>").append(statement.getStatementId() != null ? statement.getStatementId() : "STMT-001").append("</Id>\n");
        sb.append("      <ElctrncSeqNb>").append(elctrncSeqNb).append("</ElctrncSeqNb>\n");
        sb.append("      <LglSeqNb>").append(lglSeqNb).append("</LglSeqNb>\n");
        sb.append("      <CreDtTm>").append(statement.getCreationDateTime() != null ? statement.getCreationDateTime() : new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new java.util.Date())).append("</CreDtTm>\n");
        if (statement.getPeriodFrom() != null || statement.getPeriodTo() != null) {
            sb.append("      <FrToDt>\n");
            if (statement.getPeriodFrom() != null) sb.append("        <FrDt>").append(statement.getPeriodFrom()).append("</FrDt>\n");
            if (statement.getPeriodTo() != null) sb.append("        <ToDt>").append(statement.getPeriodTo()).append("</ToDt>\n");
            sb.append("      </FrToDt>\n");
        }
        // Opening balance
        sb.append("      <Bal>\n");
        sb.append("        <Tp><CdOrPrtry><Cd>OPBD</Cd></CdOrPrtry></Tp>\n");
        sb.append("        <Amt Ccy=\"").append(statement.getAccountCurrency()).append("\">0.00</Amt>\n");
        sb.append("        <CdtDbtInd>CRDT</CdtDbtInd>\n");
        sb.append("        <Dt>").append(statement.getPeriodFrom() != null ? statement.getPeriodFrom() : new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())).append("</Dt>\n");
        sb.append("      </Bal>\n");
        sb.append("      <Ntry>\n");
        List<SepaTransaction> txs = statement.getTransactions();
        for (SepaTransaction tx : txs) {
            sb.append("        <Amt Ccy=\"").append(tx.getCurrency()).append("\">").append(tx.getAmount()).append("</Amt>\n");
            sb.append("        <NtryDtls>\n");
            sb.append("          <TxDtls>\n");
            sb.append("            <Refs><EndToEndId>").append(tx.getEndToEndId()).append("</EndToEndId></Refs>\n");
            sb.append("            <RltdPties>\n");
            // Use debtor fields as-is if present, do not fallback to creditor if debtor is present
            String debtorName = tx.getDebtorName();
            String debtorIban = tx.getDebtorIban();
            // Only fallback if debtorName/IBAN is null, empty, or 'null' (not if present)
            if ((debtorName == null || debtorName.trim().isEmpty() || debtorName.equalsIgnoreCase("null")) && tx.getCreditorName() != null && !tx.getCreditorName().trim().isEmpty() && !tx.getCreditorName().equalsIgnoreCase("null")) {
                debtorName = tx.getCreditorName();
            }
            if ((debtorIban == null || debtorIban.trim().isEmpty() || debtorIban.equalsIgnoreCase("null")) && tx.getCreditorIban() != null && !tx.getCreditorIban().trim().isEmpty() && !tx.getCreditorIban().equalsIgnoreCase("null")) {
                debtorIban = tx.getCreditorIban();
            }
            sb.append("              <Dbtr><Nm>").append(debtorName == null || debtorName.trim().isEmpty() || debtorName.equalsIgnoreCase("null") ? "UNKNOWN" : debtorName).append("</Nm></Dbtr>\n");
            sb.append("              <DbtrAcct><Id><IBAN>").append(debtorIban == null || debtorIban.trim().isEmpty() || debtorIban.equalsIgnoreCase("null") ? "UNKNOWN" : debtorIban).append("</IBAN></Id></DbtrAcct>\n");
            sb.append("              <Cdtr><Nm>").append(tx.getCreditorName()).append("</Nm></Cdtr>\n");
            sb.append("              <CdtrAcct><Id><IBAN>").append(tx.getCreditorIban()).append("</IBAN></Id></CdtrAcct>\n");
            sb.append("            </RltdPties>\n");
            sb.append("            <RmtInf><Ustrd>")
              .append(tx.getRemittanceInfo() == null || "null".equalsIgnoreCase(tx.getRemittanceInfo()) ? "" : tx.getRemittanceInfo())
              .append("</Ustrd></RmtInf>\n");
            sb.append("            <Purp><Cd>")
              .append(tx.getPurposeCode() == null || "null".equalsIgnoreCase(tx.getPurposeCode()) ? "" : tx.getPurposeCode())
              .append("</Cd></Purp>\n");
            sb.append("          </TxDtls>\n");
            sb.append("        </NtryDtls>\n");
        }
        sb.append("      </Ntry>\n");
        // Closing balance
        sb.append("      <Bal>\n");
        sb.append("        <Tp><CdOrPrtry><Cd>CLBD</Cd></CdOrPrtry></Tp>\n");
        sb.append("        <Amt Ccy=\"").append(statement.getAccountCurrency()).append("\">0.00</Amt>\n");
        sb.append("        <CdtDbtInd>CRDT</CdtDbtInd>\n");
        sb.append("        <Dt>").append(statement.getPeriodTo() != null ? statement.getPeriodTo() : new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())).append("</Dt>\n");
        sb.append("      </Bal>\n");
        sb.append("    </Stmt>\n");
        sb.append("  </BkToCstmrStmt>\n");
        sb.append("</Document>\n");
        return sb.toString();
    }
}
