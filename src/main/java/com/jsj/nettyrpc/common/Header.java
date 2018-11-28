package com.jsj.nettyrpc.common;

public class Header {
    int length;
    boolean heartBeat;

    public Header(int length, boolean heartBeat) {
        this.length = length;
        this.heartBeat = heartBeat;
    }

    public Header(boolean heartBeat) {
        this.length = 1;
        this.heartBeat = heartBeat;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isHeartBeat() {
        return heartBeat;
    }

    public void setHeartBeat(boolean heartBeat) {
        this.heartBeat = heartBeat;
    }
}
