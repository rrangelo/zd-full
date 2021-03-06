/*
 * 
 */

package com.zimbra.soap.account.type;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class Prop
{
    @XmlAttribute private String zimlet;
    @XmlAttribute private String name; 
    @XmlValue private String value;
    public String getZimlet() {
        return zimlet;
    }
    public void setZimlet(String zimlet) {
        this.zimlet = zimlet;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    
    private static final String SEPARATOR = ":";
    private String serialization;
    
    public Prop() {
        //required for jaxb binding
    }
    
    public Prop(String zimlet, String name, String value) {
            this.zimlet = zimlet;
            this.name = name;
            this.value = value;
            this.serialization = makeSerialization();
    }
    
    public Prop(String serialization) throws IllegalArgumentException {
        this.serialization = serialization;
        int sep1 = serialization.indexOf(SEPARATOR);
        int sep2 = serialization.indexOf(SEPARATOR, sep1+1);
        if (sep1 < 0 || sep2 < 0) {
            throw new IllegalArgumentException(serialization);
        }
        zimlet = serialization.substring(0, sep1);
        name = serialization.substring(sep1+1, sep2);
        value = serialization.substring(sep2+1);
    }
    
    private String makeSerialization() {
        return zimlet + SEPARATOR + name + SEPARATOR + value;
    }
    
    public String getSerialization() {
        if (serialization == null) {
            serialization = makeSerialization(); 
        }
        return serialization;
    }
    
    public boolean matches(Prop other) {
        return (zimlet.equals(other.zimlet) && name.equals(other.name));
    }
    public void replace(Prop other) {
        this.zimlet = other.zimlet;
        this.name = other.name;
        this.value = other.value;
        this.serialization = other.serialization;
    }
    
    public static Multimap<String, String> toMultimap(List<Prop> props, String userPropKey) {
        Multimap<String, String> map = ArrayListMultimap.create();
        for (Prop p : props) {
            map.put(userPropKey, p.getSerialization());
        }
        return map;
    }
}
