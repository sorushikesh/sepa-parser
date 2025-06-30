package com.serrala.sepa.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class SepaTransaction {
    @XmlElement(name = "debtorName")
    private String debtorName;

    @XmlElement(name = "debtorIban")
    private String debtorIban;

    @XmlElement(name = "creditorName")
    private String creditorName;

    @XmlElement(name = "creditorIban")
    private String creditorIban;

    @XmlElement(name = "amount")
    private String amount;

    @XmlElement(name = "currency")
    private String currency;

    @XmlElement(name = "endToEndId")
    private String endToEndId;

    @XmlElement(name = "remittanceInfo")
    private String remittanceInfo;

    @XmlElement(name = "purposeCode")
    private String purposeCode;

    @XmlElement(name = "bookingDate")
    private String bookingDate;

    @XmlElement(name = "valueDate")
    private String valueDate;

    public String getDebtorName() { return debtorName; }
    public void setDebtorName(String debtorName) { this.debtorName = debtorName; }
    public String getDebtorIban() { return debtorIban; }
    public void setDebtorIban(String debtorIban) { this.debtorIban = debtorIban; }
    public String getCreditorName() { return creditorName; }
    public void setCreditorName(String creditorName) { this.creditorName = creditorName; }
    public String getCreditorIban() { return creditorIban; }
    public void setCreditorIban(String creditorIban) { this.creditorIban = creditorIban; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getEndToEndId() { return endToEndId; }
    public void setEndToEndId(String endToEndId) { this.endToEndId = endToEndId; }
    public String getRemittanceInfo() { return remittanceInfo; }
    public void setRemittanceInfo(String remittanceInfo) { this.remittanceInfo = remittanceInfo; }
    public String getPurposeCode() { return purposeCode; }
    public void setPurposeCode(String purposeCode) { this.purposeCode = purposeCode; }
    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }
    public String getValueDate() { return valueDate; }
    public void setValueDate(String valueDate) { this.valueDate = valueDate; }
}
