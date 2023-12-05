package br.com.av1.sumo.model.mobility.testdrive;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Route {
    @XmlAttribute(name = "edges")
    public String edges;
}
