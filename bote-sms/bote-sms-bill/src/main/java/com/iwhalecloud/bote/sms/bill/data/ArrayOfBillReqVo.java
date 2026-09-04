
package com.iwhalecloud.bote.sms.bill.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>ArrayOfBillReqVo complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * <pre>
 * &lt;complexType name="ArrayOfBillReqVo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="BillReqVo" type="{http://vo.intf.smpin.tydic.com}BillReqVo" maxOccurs="unbounded" minOccurs="0" form="qualified"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfBillReqVo", propOrder = "billReqVo")
public class ArrayOfBillReqVo {

    @XmlElement(name = "BillReqVo")
    protected List<BillReqVo> billReqVo;

    /**
     * Gets the value of the billReqVo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the billReqVo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBillReqVo().add(newItem);
     * </pre>
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BillReqVo }
     */
    public List<BillReqVo> getBillReqVo() {
        if (billReqVo == null) {
            billReqVo = new ArrayList<BillReqVo>();
        }
        return this.billReqVo;
    }

}
