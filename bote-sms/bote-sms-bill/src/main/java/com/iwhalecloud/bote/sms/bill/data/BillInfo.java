
package com.iwhalecloud.bote.sms.bill.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>BillInfo complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="BillInfo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="UserVo" type="{http://vo.intf.smpin.tydic.com}UserVo" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="ArrayOfBillReqVo" type="{http://vo.intf.smpin.tydic.com}ArrayOfBillReqVo" minOccurs="0" form="qualified"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BillInfo", namespace = "http://intf.smpin.tydic.com", propOrder = {
    "userVo",
    "arrayOfBillReqVo"
})
public class BillInfo {

    @XmlElement(name = "UserVo")
    protected UserVo userVo;
    @XmlElement(name = "ArrayOfBillReqVo")
    protected ArrayOfBillReqVo arrayOfBillReqVo;

    /**
     * 获取userVo属性的值。
     * 
     * @return
     *     possible object is
     *     {@link UserVo }
     *     
     */
    public UserVo getUserVo() {
        return userVo;
    }

    /**
     * 设置userVo属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link UserVo }
     *     
     */
    public void setUserVo(UserVo value) {
        this.userVo = value;
    }

    /**
     * 获取arrayOfBillReqVo属性的值。
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfBillReqVo }
     *     
     */
    public ArrayOfBillReqVo getArrayOfBillReqVo() {
        return arrayOfBillReqVo;
    }

    /**
     * 设置arrayOfBillReqVo属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfBillReqVo }
     *     
     */
    public void setArrayOfBillReqVo(ArrayOfBillReqVo value) {
        this.arrayOfBillReqVo = value;
    }

}
