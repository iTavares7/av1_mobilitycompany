package br.com.av1.sumo.model.mobility.testdrive;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class TestVehicle {

    @XmlAttribute
    public String id;

    @XmlAttribute
    public String depart;

    @XmlElement(name = "route")
    public Route route;
}
