package eu.codetopic.anty.lejos.car.menu.mode;

import eu.codetopic.anty.lejos.car.hardware.Driver;
import eu.codetopic.anty.lejos.car.menu.ModesMenu;
import eu.codetopic.anty.lejos.utils.draw.canvas.Canvas;
import eu.codetopic.anty.lejos.utils.menu.Menu;
import eu.codetopic.anty.lejos.utils.menu.MenuItem;

@Deprecated
@SuppressWarnings("deprecation")
public final class DriveMode implements MenuItem {

    @Override
    public String getName() {
        return "Drive";
    }

    @Override
    public boolean onSelected(Menu menu, int itemIndex) {
        Canvas canvas = menu.generateSubmenuCanvas(itemIndex, Menu.calculateMinMenuHeight(5));
        canvas.apply();
        Driver.drive(canvas, ((ModesMenu) menu).hardware);
        canvas.removeSelf();
        return true;
    }
}
