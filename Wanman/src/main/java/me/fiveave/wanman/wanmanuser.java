package me.fiveave.wanman;

import org.bukkit.entity.Player;

import static me.fiveave.wanman.main.wmuser;

public class wanmanuser {
    private int totaldist;
    private boolean incart;
    private boolean measuring;
    private double lastx;
    private double lastz;
    private double measuretotaldist;
    private int measuretotaltime;

    wanmanuser(Player p) {
        this.measuring = false;
        this.measuretotaldist = 0;
        this.measuretotaltime = 0;
        this.totaldist = 0;
    }

    static void initWanmanuser(Player p) {
        wanmanuser newuser = new wanmanuser(p);
        wmuser.putIfAbsent(p, newuser);
    }

    public int getTotaldist() {
        return totaldist;
    }

    public void setTotaldist(int totaldist) {
        this.totaldist = totaldist;
    }

    public boolean isIncart() {
        return incart;
    }

    public void setIncart(boolean incart) {
        this.incart = incart;
    }

    public boolean isMeasuring() {
        return measuring;
    }

    public void setMeasuring(boolean measuring) {
        this.measuring = measuring;
    }

    public double getLastx() {
        return lastx;
    }

    public void setLastx(double lastx) {
        this.lastx = lastx;
    }

    public double getLastz() {
        return lastz;
    }

    public void setLastz(double lastz) {
        this.lastz = lastz;
    }

    public double getMeasuretotaldist() {
        return measuretotaldist;
    }

    public void setMeasuretotaldist(double measuretotaldist) {
        this.measuretotaldist = measuretotaldist;
    }

    public int getMeasuretotaltime() {
        return measuretotaltime;
    }

    public void setMeasuretotaltime(int measuretotaltime) {
        this.measuretotaltime = measuretotaltime;
    }
}
