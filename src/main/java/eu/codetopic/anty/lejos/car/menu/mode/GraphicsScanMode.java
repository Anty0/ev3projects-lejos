package eu.codetopic.anty.lejos.car.menu.mode;

import eu.codetopic.anty.lejos.car.Constants;
import eu.codetopic.anty.lejos.car.hardware.scan.FastRangeScanner;
import eu.codetopic.anty.lejos.car.menu.ModesMenu;
import eu.codetopic.anty.lejos.utils.draw.canvas.Canvas;
import eu.codetopic.anty.lejos.utils.menu.LEDLightMenu;
import eu.codetopic.anty.lejos.utils.menu.Menu;
import eu.codetopic.anty.lejos.utils.menu.MenuItem;
import eu.codetopic.anty.lejos.utils.menu.SimpleMenuItem;
import lejos.hardware.BrickFinder;
import lejos.hardware.LED;
import lejos.internal.ev3.EV3LED;

public final class GraphicsScanMode implements MenuItem {

    private float speed = -1;
    private Boolean lines = null;

    @Override
    public String getName() {
        return "GraphicsScan";
    }

    @Override
    public boolean onSelected(Menu menu, int index) {
        speed = -1;
        lines = null;
        EV3LED led = ((ModesMenu) menu).getLed();
        Canvas canvas = menu.generateSubmenuCanvas(index, Menu.calculateMinMenuHeight(5));
        new SelectSpeedMenu(led, canvas).start();
        if (speed < 0) {
            canvas.removeSelf();
            return true;
        }
        new SelectDrawingModeMenu(led, canvas).start();
        if (lines == null) {
            canvas.removeSelf();
            return true;
        }
        led.setPattern(EV3LED.COLOR_GREEN, EV3LED.PATTERN_HEARTBEAT);
        FastRangeScanner.graphicsAroundScan(canvas, ((ModesMenu) menu).hardware, speed, lines);
        canvas.removeSelf();
        return true;
    }

    private final class SelectSpeedMenu extends LEDLightMenu {

        public SelectSpeedMenu(EV3LED led, Canvas canvas) {
            super(led, canvas, "Select speed:");
        }

        @Override
        protected MenuItem[] loadItems() {
            return new MenuItem[] {new SimpleMenuItem("FastSpeed", (menu, itemIndex) -> {
                speed = Constants.SCAN_SPEED_GRAPHICS_FAST;
                menu.exit(true);
                return true;
            }), new SimpleMenuItem("SlowSpeed", (menu, itemIndex) -> {
                speed = Constants.SCAN_SPEED_GRAPHICS_SLOW;
                menu.exit(true);
                return true;
            })};
        }
    }

    private final class SelectDrawingModeMenu extends LEDLightMenu {

        public SelectDrawingModeMenu(EV3LED led, Canvas canvas) {
            super(led, canvas, "Select drawing mode:");
        }

        @Override
        protected MenuItem[] loadItems() {
            return new MenuItem[] {new SimpleMenuItem("DrawLines", (menu, itemIndex) -> {
                lines = true;
                menu.exit(true);
                return true;
            }), new SimpleMenuItem("DrawDots", (menu, itemIndex) -> {
                lines = false;
                menu.exit(true);
                return true;
            })};
        }
    }
}