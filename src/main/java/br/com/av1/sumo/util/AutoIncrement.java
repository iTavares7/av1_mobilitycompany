package br.com.av1.sumo.util;

public class AutoIncrement {
    
    private static int nextId = 0;
    public synchronized static Integer getNextId(){
        return ++nextId;
    };
}
