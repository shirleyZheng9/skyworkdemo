
package com.iwhalecloud.bote.sms.bill.data;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>BillResVo complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="BillResVo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="stateDesc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="errorFlowCode" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0" form="qualified"/&gt;
 *         &lt;element name="state" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BillResVo", propOrder = {
    "stateDesc",
    "errorFlowCode",
    "state"
})
public class BillResVo {

    @XmlElementRef(name = "stateDesc", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> stateDesc;
    @XmlElement(nillable = true)
    protected List<String> errorFlowCode;
    @XmlElementRef(name = "state", namespace = "http://vo.intf.smpin.tydic.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> state;

    /**
     * 获取stateDesc属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStateDesc() {
        return stateDesc;
    }

    /**
     * 设置stateDesc属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStateDesc(JAXBElement<String> value) {
        this.stateDesc = value;
    }

    /**
     * Gets the value of the errorFlowCode property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the errorFlowCode property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getErrorFlowCode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getErrorFlowCode() {
        if (errorFlowCode == null) {
            errorFlowCode = new ArrayList<String>();
        }
        return this.errorFlowCode;
    }

    /**
     * 获取state属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getState() {
        return state;
    }

    /**
     * 设置state属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setState(JAXBElement<String> value) {
        this.state = value;
    }

}
