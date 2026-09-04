
package com.iwhalecloud.bote.sms.bill.data;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;

import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.iwhalecloud.bote.sms.bill.data package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private static final QName _BillInfo_QNAME = new QName("http://intf.smpin.tydic.com", "BillInfo");
    private static final QName _BillInfoResponse_QNAME = new QName("http://intf.smpin.tydic.com", "BillInfoResponse");
    private static final QName _BillResVoStateDesc_QNAME = new QName("http://vo.intf.smpin.tydic.com", "stateDesc");
    private static final QName _BillResVoState_QNAME = new QName("http://vo.intf.smpin.tydic.com", "state");
    private static final QName _BillReqVoPlanId_QNAME = new QName("http://vo.intf.smpin.tydic.com", "planId");
    private static final QName _BillReqVoParams_QNAME = new QName("http://vo.intf.smpin.tydic.com", "params");
    private static final QName _BillReqVoPushUrl_QNAME = new QName("http://vo.intf.smpin.tydic.com", "pushUrl");
    private static final QName _BillReqVoCreateStaff_QNAME = new QName("http://vo.intf.smpin.tydic.com", "createStaff");
    private static final QName _BillReqVoEndHour_QNAME = new QName("http://vo.intf.smpin.tydic.com", "endHour");
    private static final QName _BillReqVoLatnId_QNAME = new QName("http://vo.intf.smpin.tydic.com", "latnId");
    private static final QName _BillReqVoProductId_QNAME = new QName("http://vo.intf.smpin.tydic.com", "productId");
    private static final QName _BillReqVoProductType_QNAME = new QName("http://vo.intf.smpin.tydic.com", "productType");
    private static final QName _BillReqVoId_QNAME = new QName("http://vo.intf.smpin.tydic.com", "id");
    private static final QName _BillReqVoToTel_QNAME = new QName("http://vo.intf.smpin.tydic.com", "toTel");
    private static final QName _BillReqVoIsOntime_QNAME = new QName("http://vo.intf.smpin.tydic.com", "isOntime");
    private static final QName _BillReqVoBeginHour_QNAME = new QName("http://vo.intf.smpin.tydic.com", "beginHour");
    private static final QName _BillReqVoBatch_QNAME = new QName("http://vo.intf.smpin.tydic.com", "batch");
    private static final QName _BillReqVoFlowCode_QNAME = new QName("http://vo.intf.smpin.tydic.com", "flowCode");
    private static final QName _BillReqVoSentContent_QNAME = new QName("http://vo.intf.smpin.tydic.com", "sentContent");
    private static final QName _BillReqVoCreateDespart_QNAME = new QName("http://vo.intf.smpin.tydic.com", "createDespart");
    private static final QName _BillReqVoSentType_QNAME = new QName("http://vo.intf.smpin.tydic.com", "sentType");
    private static final QName _BillReqVoSysCode_QNAME = new QName("http://vo.intf.smpin.tydic.com", "sysCode");
    private static final QName _UserVoUserName_QNAME = new QName("http://vo.intf.smpin.tydic.com", "userName");
    private static final QName _UserVoPassWord_QNAME = new QName("http://vo.intf.smpin.tydic.com", "passWord");

    /**
     * Create an instance of {@link UserVo }
     * 
     */
    public UserVo createUserVo() {
        return new UserVo();
    }

    /**
     * Create an instance of {@link ArrayOfBillReqVo }
     * 
     */
    public ArrayOfBillReqVo createArrayOfBillReqVo() {
        return new ArrayOfBillReqVo();
    }

    /**
     * Create an instance of {@link BillReqVo }
     * 
     */
    public BillReqVo createBillReqVo() {
        return new BillReqVo();
    }

    /**
     * Create an instance of {@link BillResVo }
     * 
     */
    public BillResVo createBillResVo() {
        return new BillResVo();
    }

    /**
     * Create an instance of {@link BillInfo }
     * 
     */
    public BillInfo createBillInfo() {
        return new BillInfo();
    }

    /**
     * Create an instance of {@link BillInfoResponse }
     * 
     */
    public BillInfoResponse createBillInfoResponse() {
        return new BillInfoResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BillInfo }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BillInfo }{@code >}
     */
    @XmlElementDecl(namespace = "http://intf.smpin.tydic.com", name = "BillInfo")
    public JAXBElement<BillInfo> createBillInfo(BillInfo value) {
        return new JAXBElement<BillInfo>(_BillInfo_QNAME, BillInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BillInfoResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BillInfoResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://intf.smpin.tydic.com", name = "BillInfoResponse")
    public JAXBElement<BillInfoResponse> createBillInfoResponse(BillInfoResponse value) {
        return new JAXBElement<BillInfoResponse>(_BillInfoResponse_QNAME, BillInfoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "stateDesc", scope = BillResVo.class)
    public JAXBElement<String> createBillResVoStateDesc(String value) {
        return new JAXBElement<String>(_BillResVoStateDesc_QNAME, String.class, BillResVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "state", scope = BillResVo.class)
    public JAXBElement<String> createBillResVoState(String value) {
        return new JAXBElement<String>(_BillResVoState_QNAME, String.class, BillResVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "planId", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoPlanId(String value) {
        return new JAXBElement<String>(_BillReqVoPlanId_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "params", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoParams(String value) {
        return new JAXBElement<String>(_BillReqVoParams_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "pushUrl", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoPushUrl(String value) {
        return new JAXBElement<String>(_BillReqVoPushUrl_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "createStaff", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoCreateStaff(String value) {
        return new JAXBElement<String>(_BillReqVoCreateStaff_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "endHour", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoEndHour(String value) {
        return new JAXBElement<String>(_BillReqVoEndHour_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "latnId", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoLatnId(String value) {
        return new JAXBElement<String>(_BillReqVoLatnId_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "productId", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoProductId(String value) {
        return new JAXBElement<String>(_BillReqVoProductId_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "productType", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoProductType(String value) {
        return new JAXBElement<String>(_BillReqVoProductType_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "id", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoId(String value) {
        return new JAXBElement<String>(_BillReqVoId_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "toTel", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoToTel(String value) {
        return new JAXBElement<String>(_BillReqVoToTel_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "isOntime", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoIsOntime(String value) {
        return new JAXBElement<String>(_BillReqVoIsOntime_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "beginHour", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoBeginHour(String value) {
        return new JAXBElement<String>(_BillReqVoBeginHour_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "batch", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoBatch(String value) {
        return new JAXBElement<String>(_BillReqVoBatch_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "flowCode", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoFlowCode(String value) {
        return new JAXBElement<String>(_BillReqVoFlowCode_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "sentContent", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoSentContent(String value) {
        return new JAXBElement<String>(_BillReqVoSentContent_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "createDespart", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoCreateDespart(String value) {
        return new JAXBElement<String>(_BillReqVoCreateDespart_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "sentType", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoSentType(String value) {
        return new JAXBElement<String>(_BillReqVoSentType_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "sysCode", scope = BillReqVo.class)
    public JAXBElement<String> createBillReqVoSysCode(String value) {
        return new JAXBElement<String>(_BillReqVoSysCode_QNAME, String.class, BillReqVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "userName", scope = UserVo.class)
    public JAXBElement<String> createUserVoUserName(String value) {
        return new JAXBElement<String>(_UserVoUserName_QNAME, String.class, UserVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "passWord", scope = UserVo.class)
    public JAXBElement<String> createUserVoPassWord(String value) {
        return new JAXBElement<String>(_UserVoPassWord_QNAME, String.class, UserVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "sysCode", scope = UserVo.class)
    public JAXBElement<String> createUserVoSysCode(String value) {
        return new JAXBElement<String>(_BillReqVoSysCode_QNAME, String.class, UserVo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://vo.intf.smpin.tydic.com", name = "productId", scope = UserVo.class)
    public JAXBElement<String> createUserVoProductId(String value) {
        return new JAXBElement<String>(_BillReqVoProductId_QNAME, String.class, UserVo.class, value);
    }

}
