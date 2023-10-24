package br.com.av1.sumo.model.mobility;

import java.util.Objects;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Trip {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String depart;

    @XmlAttribute
    private String from;

    @XmlAttribute
    private String to;

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Trip trip = (Trip) obj;
        return Objects.equals(id, trip.id) &&
                Objects.equals(from, trip.from) &&
                Objects.equals(to, trip.to);
    }
}