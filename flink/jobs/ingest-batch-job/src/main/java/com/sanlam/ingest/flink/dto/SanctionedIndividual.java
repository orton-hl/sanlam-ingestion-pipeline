package com.sanlam.ingest.flink.dto;

public class SanctionedIndividual {
    private String individualID;
    private String referenceNumber;
    private String fullName;
    private String listedOn;
    private String comments;
    private String title;
    private String designation;
    private String individualDateOfBirth;
    private String individualPlaceOfBirth;
    private String individualAlias;
    private String nationality;
    private String individualDocument;
    private String individualAddress;
    private String applicationStatus;

    public SanctionedIndividual() {
    }

    public SanctionedIndividual(String individualID, String referenceNumber, String fullName, String listedOn, String comments, String title, String designation, String individualDateOfBirth, String individualPlaceOfBirth, String individualAlias, String nationality, String individualDocument, String individualAddress, String applicationStatus) {
        this.individualID = individualID;
        this.referenceNumber = referenceNumber;
        this.fullName = fullName;
        this.listedOn = listedOn;
        this.comments = comments;
        this.title = title;
        this.designation = designation;
        this.individualDateOfBirth = individualDateOfBirth;
        this.individualPlaceOfBirth = individualPlaceOfBirth;
        this.individualAlias = individualAlias;
        this.nationality = nationality;
        this.individualDocument = individualDocument;
        this.individualAddress = individualAddress;
        this.applicationStatus = applicationStatus;
    }

    public String getIndividualID() {
        return individualID;
    }

    public void setIndividualID(String individualID) {
        this.individualID = individualID;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getListedOn() {
        return listedOn;
    }

    public void setListedOn(String listedOn) {
        this.listedOn = listedOn;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getIndividualDateOfBirth() {
        return individualDateOfBirth;
    }

    public void setIndividualDateOfBirth(String individualDateOfBirth) {
        this.individualDateOfBirth = individualDateOfBirth;
    }

    public String getIndividualPlaceOfBirth() {
        return individualPlaceOfBirth;
    }

    public void setIndividualPlaceOfBirth(String individualPlaceOfBirth) {
        this.individualPlaceOfBirth = individualPlaceOfBirth;
    }

    public String getIndividualAlias() {
        return individualAlias;
    }

    public void setIndividualAlias(String individualAlias) {
        this.individualAlias = individualAlias;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getIndividualDocument() {
        return individualDocument;
    }

    public void setIndividualDocument(String individualDocument) {
        this.individualDocument = individualDocument;
    }

    public String getIndividualAddress() {
        return individualAddress;
    }

    public void setIndividualAddress(String individualAddress) {
        this.individualAddress = individualAddress;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    @Override
    public String toString() {
        return "SanctionedIndividual{" +
                "individualID='" + individualID + '\'' +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", fullName='" + fullName + '\'' +
                ", listedOn='" + listedOn + '\'' +
                ", comments='" + comments + '\'' +
                ", title='" + title + '\'' +
                ", designation='" + designation + '\'' +
                ", individualDateOfBirth='" + individualDateOfBirth + '\'' +
                ", individualPlaceOfBirth='" + individualPlaceOfBirth + '\'' +
                ", individualAlias='" + individualAlias + '\'' +
                ", nationality='" + nationality + '\'' +
                ", individualDocument='" + individualDocument + '\'' +
                ", individualAddress='" + individualAddress + '\'' +
                ", applicationStatus='" + applicationStatus + '\'' +
                '}';
    }
}
