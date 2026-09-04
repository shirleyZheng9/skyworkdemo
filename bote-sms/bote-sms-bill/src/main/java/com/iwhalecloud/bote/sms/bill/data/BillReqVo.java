
package com.iwhalecloud.bote.sms.bill.data;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>BillReqVo complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="BillReqVo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="channelType" type="{http://www.w3.org/2001/XMLSchema}int" form="qualified"/&gt;
 *         &lt;element name="endDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="planId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="params" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="beginDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="pushUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="createStaff" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="endHour" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="latnId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="productId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="productType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="toTel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="isOntime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="beginHour" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="batch" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="flowCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="sentContent" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="createDespart" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="businessId" type="{http://www.w3.org/2001/XMLSchema}long" form="qualified"/&gt;
 *         &lt;element name="sentType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="sysCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BillReqVo", propOrder = {
    "channelType",
    "endDate",
    "planId",
    "params",
    "beginDate",
    "pushUrl",
    "createStaff",
    "endHour",
    "latnId",
    "productId",
    "productType",
    "id",
    "toTel",
    "isOntime",
    "beginHour",
    "batch",
    "flowCode",
    "sentContent",
    "createDespart",
    "businessId",
    "sentType",
    "sysCode"
})
public class BillReqVo {

    protected int channelType;
    protected String endDate;
    @XmlElementRef(name = "planId", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> planId;
    @XmlElementRef(name = "params", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> params;
    protected String beginDate;
    @XmlElementRef(name = "pushUrl", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> pushUrl;
    @XmlElementRef(name = "createStaff", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> createStaff;
    @XmlElementRef(name = "endHour", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> endHour;
    @XmlElementRef(name = "latnId", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> latnId;
    @XmlElementRef(name = "productId", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> productId;
    @XmlElementRef(name = "productType", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> productType;
    @XmlElementRef(name = "id", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> id;
    @XmlElementRef(name = "toTel", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> toTel;
    @XmlElementRef(name = "isOntime", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> isOntime;
    @XmlElementRef(name = "beginHour", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> beginHour;
    @XmlElementRef(name = "batch", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> batch;
    @XmlElementRef(name = "flowCode", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> flowCode;
    @XmlElementRef(name = "sentContent", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> sentContent;
    @XmlElementRef(name = "createDespart", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> createDespart;
    protected long businessId;
    @XmlElementRef(name = "sentType", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> sentType;
    @XmlElementRef(name = "sysCode", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> sysCode;

    /**
     * 获取channelType属性的值。
     * 
     */
    public int getChannelType() {
        return channelType;
    }

    /**
     * 设置channelType属性的值。
     * 
     */
    public void setChannelType(int value) {
        this.channelType = value;
    }

    /**
     * 获取endDate属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * 设置endDate属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndDate(String value) {
        this.endDate = value;
    }

    /**
     * 获取planId属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPlanId() {
        return planId;
    }

    /**
     * 设置planId属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPlanId(JAXBElement<String> value) {
        this.planId = value;
    }

    /**
     * 获取params属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getParams() {
        return params;
    }

    /**
     * 设置params属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setParams(JAXBElement<String> value) {
        this.params = value;
    }

    /**
     * 获取beginDate属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBeginDate() {
        return beginDate;
    }

    /**
     * 设置beginDate属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBeginDate(String value) {
        this.beginDate = value;
    }

    /**
     * 获取pushUrl属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPushUrl() {
        return pushUrl;
    }

    /**
     * 设置pushUrl属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPushUrl(JAXBElement<String> value) {
        this.pushUrl = value;
    }

    /**
     * 获取createStaff属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCreateStaff() {
        return createStaff;
    }

    /**
     * 设置createStaff属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCreateStaff(JAXBElement<String> value) {
        this.createStaff = value;
    }

    /**
     * 获取endHour属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getEndHour() {
        return endHour;
    }

    /**
     * 设置endHour属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setEndHour(JAXBElement<String> value) {
        this.endHour = value;
    }

    /**
     * 获取latnId属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getLatnId() {
        return latnId;
    }

    /**
     * 设置latnId属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setLatnId(JAXBElement<String> value) {
        this.latnId = value;
    }

    /**
     * 获取productId属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getProductId() {
        return productId;
    }

    /**
     * 设置productId属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setProductId(JAXBElement<String> value) {
        this.productId = value;
    }

    /**
     * 获取productType属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getProductType() {
        return productType;
    }

    /**
     * 设置productType属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setProductType(JAXBElement<String> value) {
        this.productType = value;
    }

    /**
     * 获取id属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getId() {
        return id;
    }

    /**
     * 设置id属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setId(JAXBElement<String> value) {
        this.id = value;
    }

    /**
     * 获取toTel属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getToTel() {
        return toTel;
    }

    /**
     * 设置toTel属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setToTel(JAXBElement<String> value) {
        this.toTel = value;
    }

    /**
     * 获取isOntime属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getIsOntime() {
        return isOntime;
    }

    /**
     * 设置isOntime属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setIsOntime(JAXBElement<String> value) {
        this.isOntime = value;
    }

    /**
     * 获取beginHour属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getBeginHour() {
        return beginHour;
    }

    /**
     * 设置beginHour属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setBeginHour(JAXBElement<String> value) {
        this.beginHour = value;
    }

    /**
     * 获取batch属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getBatch() {
        return batch;
    }

    /**
     * 设置batch属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setBatch(JAXBElement<String> value) {
        this.batch = value;
    }

    /**
     * 获取flowCode属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getFlowCode() {
        return flowCode;
    }

    /**
     * 设置flowCode属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setFlowCode(JAXBElement<String> value) {
        this.flowCode = value;
    }

    /**
     * 获取sentContent属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSentContent() {
        return sentContent;
    }

    /**
     * 设置sentContent属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSentContent(JAXBElement<String> value) {
        this.sentContent = value;
    }

    /**
     * 获取createDespart属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCreateDespart() {
        return createDespart;
    }

    /**
     * 设置createDespart属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCreateDespart(JAXBElement<String> value) {
        this.createDespart = value;
    }

    /**
     * 获取businessId属性的值。
     * 
     */
    public long getBusinessId() {
        return businessId;
    }

    /**
     * 设置businessId属性的值。
     * 
     */
    public void setBusinessId(long value) {
        this.businessId = value;
    }

    /**
     * 获取sentType属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSentType() {
        return sentType;
    }

    /**
     * 设置sentType属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSentType(JAXBElement<String> value) {
        this.sentType = value;
    }

    /**
     * 获取sysCode属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSysCode() {
        return sysCode;
    }

    /**
     * 设置sysCode属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSysCode(JAXBElement<String> value) {
        this.sysCode = value;
    }

}
