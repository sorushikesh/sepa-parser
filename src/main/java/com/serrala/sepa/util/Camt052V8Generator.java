package com.serrala.sepa.util;

import java.util.List;

import com.serrala.sepa.model.SepaStatement;
import com.serrala.sepa.model.SepaTransaction;

public class Camt052V8Generator {
    public static String generate(SepaStatement statement) {
        if (statement.getStatementId() == null || statement.getStatementId().isEmpty()) {
            statement.setStatementId(com.serrala.sepa.model.SepaStatement.generateRandomStatementId());
        }
        long elctrncSeqNb = (long)(Math.random() * 1_000_000_000_000_000_000L);
        long lglSeqNb = (long)(Math.random() * 1_000_000_000_000_000_000L);
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.052.001.08\">\n");
        sb.append("  <BkToCstmrAcctRpt>\n");
        sb.append("    <GrpHdr>\n");
        sb.append("      <MsgId>").append(statement.getStatementId() != null ? statement.getStatementId() : "MSGID-001").append("</MsgId>\n");
        sb.append("      <CreDtTm>").append(statement.getCreationDateTime() != null ? statement.getCreationDateTime() : new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new java.util.Date())).append("</CreDtTm>\n");
        sb.append("    </GrpHdr>\n");
        sb.append("    <Rpt>\n");
        sb.append("      <Id>").append(statement.getStatementId() != null ? statement.getStatementId() : "RPT-001").append("</Id>\n");
        sb.append("      <ElctrncSeqNb>").append(elctrncSeqNb).append("</ElctrncSeqNb>\n");
        sb.append("      <LglSeqNb>").append(lglSeqNb).append("</LglSeqNb>\n");
        sb.append("      <CreDtTm>").append(statement.getCreationDateTime() != null ? statement.getCreationDateTime() : new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new java.util.Date())).append("</CreDtTm>\n");
        if (statement.getPeriodFrom() != null || statement.getPeriodTo() != null) {
            sb.append("      <FrToDt>\n");
            if (statement.getPeriodFrom() != null) sb.append("        <FrDt>").append(statement.getPeriodFrom()).append("</FrDt>\n");
            if (statement.getPeriodTo() != null) sb.append("        <ToDt>").append(statement.getPeriodTo()).append("</ToDt>\n");
            sb.append("      </FrToDt>\n");
        }
        sb.append("      <Acct>\n");
        sb.append("        <Id><IBAN>").append(statement.getAccountIban()).append("</IBAN></Id>\n");
        sb.append("        <Ccy>").append(statement.getAccountCurrency()).append("</Ccy>\n");
        sb.append("      </Acct>\n");
        // Remove opening and closing balances for CAMT052
        // Entries
        sb.append("      <Ntry>\n");
        List<SepaTransaction> txs = statement.getTransactions();
        for (SepaTransaction tx : txs) {
            sb.append("        <Amt Ccy=\"").append(tx.getCurrency()).append("\">").append(tx.getAmount()).append("</Amt>\n");
            sb.append("        <NtryDtls>\n");
            sb.append("          <TxDtls>\n");
            sb.append("            <Refs><EndToEndId>").append(tx.getEndToEndId()).append("</EndToEndId></Refs>\n");
            sb.append("            <RltdPties>\n");
            sb.append("              <Dbtr><Nm>").append(tx.getDebtorName()).append("</Nm></Dbtr>\n");
            sb.append("              <DbtrAcct><Id><IBAN>").append(tx.getDebtorIban()).append("</IBAN></Id></DbtrAcct>\n");
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
        sb.append("    </Rpt>\n");
        sb.append("  </BkToCstmrAcctRpt>\n");
        sb.append("</Document>\n");
        return sb.toString();
    }
}
