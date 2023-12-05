package br.com.av1.sumo.model.mobility.testdrive;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "routes")
public class Routes {

    @XmlElement(name = "vehicle")
    public List<TestVehicle> vehicles;
}
