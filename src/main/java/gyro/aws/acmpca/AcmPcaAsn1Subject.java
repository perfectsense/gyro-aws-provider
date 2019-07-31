package gyro.aws.acmpca;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.acmpca.model.ASN1Subject;

public class AcmPcaAsn1Subject extends Diffable implements Copyable<ASN1Subject> {
    private String commonName;
    private String country;
    private String distinguishedNameQualifier;
    private String generationQualifier;
    private String givenName;
    private String initials;
    private String locality;
    private String organization;
    private String organizationalUnit;
    private String pseudonym;
    private String serialNumber;
    private String state;
    private String surname;
    private String title;

    /**
     * Fully qualified domain name (FQDN) associated with the certificate subject.
     */
    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * Two-digit code that specifies the country in which the certificate subject located.
     */
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Disambiguating information for the certificate subject.
     */
    public String getDistinguishedNameQualifier() {
        return distinguishedNameQualifier;
    }

    public void setDistinguishedNameQualifier(String distinguishedNameQualifier) {
        this.distinguishedNameQualifier = distinguishedNameQualifier;
    }

    /**
     * Typically a qualifier appended to the name of an individual. Examples include Jr. for junior, Sr. for senior, and III for third.
     */
    public String getGenerationQualifier() {
        return generationQualifier;
    }

    public void setGenerationQualifier(String generationQualifier) {
        this.generationQualifier = generationQualifier;
    }

    /**
     * First name.
     */
    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    /**
     * Concatenation that typically contains the first letter of the GivenName, the first letter of the middle name if one exists, and the first letter of the SurName.
     */
    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    /**
     * The locality (such as a city or town) in which the certificate subject is located.
     */
    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    /**
     * Legal name of the organization with which the certificate subject is affiliated.
     */
    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * A subdivision or unit of the organization with which the certificate subject is affiliated.
     */
    public String getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(String organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

    /**
     * Typically a shortened version of a longer GivenName.
     */
    public String getPseudonym() {
        return pseudonym;
    }

    public void setPseudonym(String pseudonym) {
        this.pseudonym = pseudonym;
    }

    /**
     * The certificate serial number.
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * State in which the subject of the certificate is located.
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * Family name.
     */
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * A title such as Mr. or Ms., which is pre-pended to the name to refer formally to the certificate subject.
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void copyFrom(ASN1Subject asn1Subject) {
        setCommonName(asn1Subject.commonName());
        setCountry(asn1Subject.country());
        setDistinguishedNameQualifier(asn1Subject.distinguishedNameQualifier());
        setGenerationQualifier(asn1Subject.generationQualifier());
        setGivenName(asn1Subject.givenName());
        setInitials(asn1Subject.initials());
        setLocality(asn1Subject.locality());
        setOrganization(asn1Subject.organization());
        setOrganizationalUnit(asn1Subject.organizationalUnit());
        setPseudonym(asn1Subject.pseudonym());
        setSerialNumber(asn1Subject.serialNumber());
        setState(asn1Subject.state());
        setSurname(asn1Subject.surname());
        setTitle(asn1Subject.title());
    }

    @Override
    public String primaryKey() {
        return "subject";
    }

    ASN1Subject toAsn1Subject() {
        return ASN1Subject.builder()
            .commonName(getCommonName())
            .country(getCountry())
            .distinguishedNameQualifier(getDistinguishedNameQualifier())
            .generationQualifier(getGenerationQualifier())
            .givenName(getGivenName())
            .initials(getInitials())
            .locality(getLocality())
            .organization(getOrganization())
            .organizationalUnit(getOrganizationalUnit())
            .pseudonym(getPseudonym())
            .serialNumber(getSerialNumber())
            .state(getState())
            .surname(getSurname())
            .title(getTitle())
            .build();
    }
}
