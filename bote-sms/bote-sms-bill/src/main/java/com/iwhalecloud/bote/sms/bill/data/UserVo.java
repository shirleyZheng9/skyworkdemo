
package com.iwhalecloud.bote.sms.bill.data;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>UserVo complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="UserVo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="userName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="passWord" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="sysCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="productId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserVo", propOrder = {
    "userName",
    "passWord",
    "sysCode",
    "productId"
})
public class UserVo {

    @XmlElementRef(name = "userName", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> userName;
    @XmlElementRef(name = "passWord", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> passWord;
    @XmlElementRef(name = "sysCode", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> sysCode;
    @XmlElementRef(name = "productId", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> productId;

    /**
     * 获取userName属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserName() {
        return userName;
    }

    /**
     * 设置userName属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserName(JAXBElement<String> value) {
        this.userName = value;
    }

    /**
     * 获取passWord属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPassWord() {
        return passWord;
    }

    /**
     * 设置passWord属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPassWord(JAXBElement<String> value) {
        this.passWord = value;
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

}
