package eu.codetopic.anty.lejos.car.menu;

import eu.codetopic.anty.lejos.car.hardware.Hardware;
import eu.codetopic.anty.lejos.car.menu.mode.BeaconFollowMode;
import eu.codetopic.anty.lejos.car.menu.mode.DriveMode;
import eu.codetopic.anty.lejos.car.menu.mode.GraphicsScanMode;
import eu.codetopic.anty.lejos.car.menu.mode.MapperMode;
import eu.codetopic.anty.lejos.utils.Utils;
import eu.codetopic.anty.lejos.utils.draw.canvas.Canvas;
import eu.codetopic.anty.lejos.utils.menu.LEDLightMenu;
import eu.codetopic.anty.lejos.utils.menu.MenuItem;
import lejos.hardware.BrickFinder;
import lejos.internal.ev3.EV3LED;

public class ModesMenu extends LEDLightMenu {

    public Hardware hardware;

    public ModesMenu() {
        super((EV3LED) BrickFinder.getDefault().getLED(), Canvas.obtain(true), "EV3Project -> CAR modes");
        // TODO: 21.9.16 create two canvas one for exit button and one for menu
        hardware = new Hardware(getLed());
    }

    @Override
    protected MenuItem[] loadItems() {
        hardware.initialize();
        return new MenuItem[]{new MapperMode(), new GraphicsScanMode(), new DriveMode(), new BeaconFollowMode()};
    }

    @Override
    protected void destroyItems() {
        Utils.tryIt(hardware::close);
        hardware = null;
        super.destroyItems();
    }
}
