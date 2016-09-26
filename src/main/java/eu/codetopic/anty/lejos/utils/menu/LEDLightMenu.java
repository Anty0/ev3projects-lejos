package eu.codetopic.anty.lejos.utils.menu;

import eu.codetopic.anty.lejos.utils.Utils;
import eu.codetopic.anty.lejos.utils.draw.canvas.Canvas;
import lejos.hardware.BrickFinder;
import lejos.hardware.LED;
import lejos.internal.ev3.EV3LED;

public abstract class LEDLightMenu extends Menu {

    private EV3LED led;

    public LEDLightMenu(EV3LED led, Canvas canvas, String title) {
        super(canvas, title);
        this.led = led;
    }

    public EV3LED getLed() {
        return led;
    }

    @Override
    protected MenuItem[] onCreate() {
        setLedPattern(EV3LED.COLOR_ORANGE, EV3LED.PATTERN_HEARTBEAT);
        MenuItem[] items = loadItems();
        setLedPattern(EV3LED.COLOR_RED, EV3LED.PATTERN_ON);
        return items;
    }

    protected abstract MenuItem[] loadItems();

    @Override
    protected void onSelected(MenuItem item, int index) {
        setLedPattern(EV3LED.COLOR_GREEN, EV3LED.PATTERN_HEARTBEAT);
        super.onSelected(item, index);
        setLedPattern(EV3LED.COLOR_RED, EV3LED.PATTERN_ON);
    }

    private void setLedPattern(int color, int pattern) {
        if (led == null) return;
        led.setPattern(color, pattern);
    }

    protected void destroyItems() {
    }

    @Override
    protected void onDestroy() {
        setLedPattern(EV3LED.COLOR_ORANGE, EV3LED.PATTERN_HEARTBEAT);
        destroyItems();
        super.onDestroy();
        setLedPattern(EV3LED.COLOR_NONE, EV3LED.PATTERN_ON);
    }
}
