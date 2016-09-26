package eu.codetopic.anty.lejos.car.menu.mode;

import eu.codetopic.anty.lejos.car.hardware.Mapper;
import eu.codetopic.anty.lejos.car.menu.ModesMenu;
import eu.codetopic.anty.lejos.utils.draw.canvas.Canvas;
import eu.codetopic.anty.lejos.utils.draw.drawer.GraphicsDrawer;
import eu.codetopic.anty.lejos.utils.menu.LEDLightMenu;
import eu.codetopic.anty.lejos.utils.menu.Menu;
import eu.codetopic.anty.lejos.utils.menu.MenuItem;

public class MapperMode implements MenuItem {

    @Override
    public String getName() {
        return "Mapper";
    }

    @Override
    public boolean onSelected(Menu menu, int itemIndex) {
        Canvas canvas = menu.generateSubmenuCanvas(itemIndex, Menu.calculateMinMenuHeight(5));
        GraphicsDrawer drawer = canvas.getGraphicsDrawer();
        drawer.drawString("Starting...", drawer.getWidth() / 2, drawer.getHeight() / 2,
                GraphicsDrawer.HCENTER | GraphicsDrawer.VCENTER);
        canvas.apply();
        drawer.clear();
        Mapper.start(canvas, ((ModesMenu) menu).hardware);
        canvas.removeSelf();
        return true;
    }
}
