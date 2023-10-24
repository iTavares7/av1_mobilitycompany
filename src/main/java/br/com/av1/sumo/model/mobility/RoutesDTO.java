package br.com.av1.sumo.model.mobility;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "routes")
@XmlAccessorType(XmlAccessType.FIELD)
public class RoutesDTO {

    @XmlElement(name = "trip")
    private List<Trip> trips;

    public Trip getRandomTrip() {
        return trips.get((int) (Math.random() * trips.size()));
    }

    public Trip geTrip(int index){
        return trips.get(index);
    }
}

