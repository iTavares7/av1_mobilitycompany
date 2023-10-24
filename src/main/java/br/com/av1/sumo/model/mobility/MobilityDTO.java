package br.com.av1.sumo.model.mobility;

import br.com.av1.sumo.enumerators.MobilityActions;

public class MobilityDTO<T> {
    private MobilityActions action;
    private T payload;

    public MobilityDTO(MobilityActions action, T payload) {
        this.action = action;
        this.payload = payload;
    }

    public T getPayload() {
        return payload;
    }

    public MobilityActions getAction() {
        return action;
    }
}