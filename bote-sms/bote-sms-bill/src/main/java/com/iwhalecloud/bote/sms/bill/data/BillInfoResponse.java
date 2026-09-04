
package com.iwhalecloud.bote.sms.bill.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>BillInfoResponse complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="BillInfoResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="BillResVo" type="{http://vo.intf.smpin.tydic.com}BillResVo" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BillInfoResponse", namespace = "http://intf.smpin.tydic.com", propOrder = "billResVo")
public class BillInfoResponse {

    @XmlElement(name = "BillResVo", namespace = "")
    protected BillResVo billResVo;

    /**
     * 获取billResVo属性的值。
     * 
     * @return possible object is {@link BillResVo }
     *     
     */
    public BillResVo getBillResVo() {
        return billResVo;
    }

    /**
     * 设置billResVo属性的值。
     * 
     * @param value allowed object is {@link BillResVo }
     *     
     */
    public void setBillResVo(BillResVo value) {
        this.billResVo = value;
    }

}
