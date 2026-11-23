package wardmonitor;

import HES.Feeds.HumidityFeed;
import HES.Feeds.TemperatureFeed;
import HES.Feeds.PollutionFeed;
import HES.Feeds.TemperatureFeed;
import HES.Systems.EnvironmentalSystem;
import java.util.Locale;

final class EnvironmentalController {
    private static final String[] WARDS={"North Ward", "Central Ward", "South Ward"};
    private static final float[] TARGETS={23.0f,22.0f,20.0f};
    private static final float TEMPERATURE_MARGIN=0.5f;
    private static final int HUMIDITY_DIFFERENCE_LIMIT=10;
    private static final float POLLUTION_LIMIT=5.0f;

    private final EnvironmentalSystem system=new EnvironmentalSystem();
    private final TemperatureFeed temperatureFeed=system.getTempFeed();
    private final HumidityFeed humidityFeed=system.getHumidityFeed();
    private final PollutionFeed pollutionFeed=system.getPollutionFeed();

    static final class Reading {
        private final String text;
        private final boolean warning;

        Reading(String text, boolean warning) {
            this.text=text;
            this.warning=warning;
        }

        String getText() {
            return text;
        }

        boolean hasWarning() {
            return warning;
        }
    }

    Reading[] sample() {
        Reading[] readings=new Reading[WARDS.length+2];
        for (int ward=0; ward<WARDS.length; ward++) {
            readings[ward]=sampleWard(ward);
        }
        readings[WARDS.length]=sampleHumidity();
        readings[WARDS.length+1]=samplePollution();
        return readings;
    }

    private Reading sampleWard(int ward){
        float current=temperatureFeed.getTemperature(ward);
        float target=TARGETS[ward];
        float lower=target-TEMPERATURE_MARGIN;
        float upper=target+TEMPERATURE_MARGIN;
        boolean heating=system.isHeatingOn(ward);

        if (current<=lower && !heating){
            system.turnHeatingOn(ward,true);
    }else if (current>=upper && !heating){
            system.turnHeatingOn(ward,false);
        }
        boolean warning=current<lower || current>upper;
        String text=String.format(Locale.ROOT,
                "%s\nCurrent: %.1f °C\nIdeal: %.1f °C\nHeating: %s",
                WARDS[ward], current,target, onOff(system.isHeatingOn(ward)));
        return new Reading(text,warning);
    }
    private Reading sampleHumidity() {
        int inside=humidityFeed.getInsideHumidity();
        int outside=humidityFeed.getOutsideHumidity();

        int difference=inside-outside;
        boolean warning=difference>HUMIDITY_DIFFERENCE_LIMIT;
        if (system.isDehumidifierOn()!=warning) {
            system.turnDehumidifierOn(warning);
        }
        String text=String.format(Locale.ROOT,
                "Humidity\nInside: %d%%\nOutside: %d%%\nDifference: %d percentage points\nDehumidifier: %s",
                inside,outside, difference,onOff(system.isDehumidifierOn()));
        return new Reading(text,warning);
    }
    private Reading samplePollution() {
        float pollution=pollutionFeed.getPollution();

        boolean warning=pollution>POLLUTION_LIMIT;
        if (system.isAirPurifierOn()!=warning) {
            system.turnAirPurifierOn(warning);
        }
        String text="Air Pollution (PM2.5)\nCurrent: "+pollution
                +"\nAir purifier: "+onOff(system.isAirPurifierOn());
        return new Reading(text,warning);
    }

    String scannerReport() {
        return "CT scanner: "+system.getCTScannerStatus()
                +"\nUltrasound scanner: "+system.getUltrasoundScannerStatus();
    }

    private static String onOff(boolean on) {
        return on ? "On" : "Off";
    }

}


