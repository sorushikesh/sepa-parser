package com.serrala.sepa.model;

import java.util.List;

public class SepaStatement {
    private String accountIban;
    private String accountCurrency;
    private String statementId;
    private String creationDateTime;
    private String periodFrom;
    private String periodTo;
    private List<SepaTransaction> transactions;

    public String getAccountIban() { return accountIban; }
    public void setAccountIban(String accountIban) { this.accountIban = accountIban; }
    public String getAccountCurrency() { return accountCurrency; }
    public void setAccountCurrency(String accountCurrency) { this.accountCurrency = accountCurrency; }
    public String getStatementId() { return statementId; }
    public void setStatementId(String statementId) { this.statementId = statementId; }
    public String getCreationDateTime() { return creationDateTime; }
    public void setCreationDateTime(String creationDateTime) { this.creationDateTime = creationDateTime; }
    public String getPeriodFrom() { return periodFrom; }
    public void setPeriodFrom(String periodFrom) { this.periodFrom = periodFrom; }
    public String getPeriodTo() { return periodTo; }
    public void setPeriodTo(String periodTo) { this.periodTo = periodTo; }
    public List<SepaTransaction> getTransactions() { return transactions; }
    public void setTransactions(List<SepaTransaction> transactions) { this.transactions = transactions; }

    public static String generateRandomStatementId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 35; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
