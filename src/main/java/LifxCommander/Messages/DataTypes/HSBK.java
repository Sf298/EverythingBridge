package LifxCommander.Messages.DataTypes;

import LifxCommander.Values.Kelvin;
import LifxCommander.Values.Levels;

public class HSBK {
    
    public static final HSBK DAYLIGHT = new HSBK(9209, 0, -1, 5000);
    public static final HSBK COOL_WHITE = new HSBK(9209, 0, -1, 4000);
    public static final HSBK ALICE_BLUE = new HSBK(9209, 0, -1, 7500);
    public static final HSBK INCANDESCENT = new HSBK(9209, 0, -1, 2700);
    public static final HSBK CRIMSON = new HSBK(63350, 59577, -1, 7500);
    public static final HSBK MAGENTA = new HSBK(54612, 65535, -1, 7500);
    public static final HSBK INDIGO = new HSBK(49991, 65535, -1, 7500);
    public static final HSBK LIGHT_SKY_BLUE = new HSBK(36946, 30146, -1, 7500);
    public static final HSBK AZURE = new HSBK(38250, 65535, -1, 7500);
    public static final HSBK AQUAMARINE = new HSBK(29098, 32896, -1, 7500);
    public static final HSBK LIGHT_GREEN = new HSBK(21845, 25883, -1, 7500);
    public static final HSBK FOREST_GREEN = new HSBK(21845, 49504, -1, 7500);
    
    private static HSBK[] colorList = new HSBK[] {DAYLIGHT, COOL_WHITE,
        ALICE_BLUE, INCANDESCENT, CRIMSON, MAGENTA, INDIGO, LIGHT_SKY_BLUE,
        AZURE, AQUAMARINE, LIGHT_GREEN, FOREST_GREEN};
    
    private static HSBK getFromName(String name) {
        name = name.toLowerCase().replaceAll("[^a-z]", "");
        switch(name) {
            case "daylight": return DAYLIGHT;
            case "coolwhite": return COOL_WHITE;
            case "aliceblue": return ALICE_BLUE;
            case "incandescent": return INCANDESCENT;
            case "crimson": return CRIMSON;
            case "magenta": return MAGENTA;
            case "indigo": return INDIGO;
            case "lightskyblue": return LIGHT_SKY_BLUE;
            case "azure": return AZURE;
            case "aquamarine": return AQUAMARINE;
            case "lightgreen": return LIGHT_GREEN;
            case "forestgreen": return FOREST_GREEN;
        }
        throw new RuntimeException("Colour not found");
    }
    public static HSBK random() {
        return colorList[(int)(Math.random()*colorList.length)];
    }
    
    /*public static final HSBK IVORY = new HSBK(9209, 0, 30801, 6000);
    public static final HSBK DAYLIGHT = new HSBK(9209, 0, 30801, 5000);
    public static final HSBK COOL_WHITE = new HSBK(9209, 0, 30801, 4000);
    public static final HSBK WARM_WHITE = new HSBK(9209, 0, 30801, 3000);
    public static final HSBK INCANDESCENT = new HSBK(9209, 0, 30801, 2700);
    public static final HSBK CANDLELIGHT = new HSBK(9209, 0, 30801, 2500);
    public static final HSBK SNOW = new HSBK(9209, 0, 30801, 6500);
    public static final HSBK GHOST_WHITE = new HSBK(9209, 0, 30801, 7000);
    public static final HSBK ALICE_BLUE = new HSBK(9209, 0, 30801, 7500);*/
    
    
    public static final double MAX_HUE = Levels.MAX;
    public static final double MAX_SAT = Levels.MAX;
    public static final double MAX_BRI = Levels.MAX;
    public static final double MIN_KEL = Kelvin.WARMEST;
    public static final double MAX_KEL = Kelvin.COOLEST;
    int hue;		// 16-Bits
    int saturation;	// 16-Bits
    int brightness;	// 16-Bits
    int kelvin;		// 16-Bits

    public HSBK() {
        this(0, Levels.MAX, Levels.MAX, Kelvin.MEDIUM);
    }

    public HSBK(int hue, int saturation, int brightness, int kelvin) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.kelvin = kelvin;
    }
    
    public HSBK(HSBK hsbk) {
        this(hsbk.hue, hsbk.saturation, hsbk.brightness, hsbk.kelvin);
    }
    
    public HSBK(String name) {
        this(getFromName(name));
    }

    public boolean hasEmpty() {
        return getHue()==-1 || getSaturation()==-1 || getBrightness()==-1 || getKelvin()==-1;
    }
    public void updateEmptyWith(HSBK hsbk) {
        if(getHue()==-1) setHue(hsbk.getHue());
        if(getSaturation()==-1) setSaturation(hsbk.getSaturation());
        if(getBrightness()==-1) setBrightness(hsbk.getBrightness());
        if(getKelvin()==-1) setKelvin(hsbk.getKelvin());
    }
    
    public int getHue() {
        return hue;
    }
    public int getHue(int scaleMax) {
        return (int)Math.round(hue/MAX_HUE*scaleMax);
    }
    public void setHue(int hue) {
        this.hue = hue;
    }
    public void setHue(int hue, int scaleMax) {
        this.hue = (int) (hue/(double)scaleMax * MAX_HUE);
    }

    public int getSaturation() {
            return saturation;
    }
    public int getSaturation(int scaleMax) {
        return (int)Math.round(saturation/MAX_SAT*scaleMax);
    }
    public void setSaturation(int saturation) {
        this.saturation = saturation;
    }
    public void setSaturation(int saturation, int scaleMax) {
        this.saturation = (int) (saturation/(double)scaleMax * MAX_SAT);
    }

    public int getBrightness() {
            return brightness;
    }
    public int getBrightness(int scaleMax) {
        return (int)Math.round(brightness/MAX_BRI*scaleMax);
    }
    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }
    public void setBrightness(int brightness, int scaleMax) {
        this.brightness = (int) (brightness/(double)scaleMax * MAX_BRI);
    }

    public int getKelvin() {
        return kelvin;
    }
    public void setKelvin(int kelvin) {
        this.kelvin = kelvin;
    }

    @Override
    public String toString() {
        return "(" + hue + ", " + saturation + ", " + brightness + ", " + kelvin + ')';
    }
	
}
