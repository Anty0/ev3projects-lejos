package eu.codetopic.anty.lejos.utils.menu;

import eu.codetopic.anty.lejos.utils.Rectangle2DInt;
import eu.codetopic.anty.lejos.utils.Utils;
import eu.codetopic.anty.lejos.utils.draw.canvas.Canvas;
import eu.codetopic.anty.lejos.utils.draw.drawer.GraphicsDrawer;
import lejos.hardware.Button;
import lejos.hardware.lcd.Font;

public abstract class Menu {

    protected static final Font TITLE_FONT = Font.getDefaultFont();
    protected static final Font ITEMS_FONT = Font.getDefaultFont();

    private Thread thread = null;
    private final Canvas canvas;
    private final String title;
    private MenuItem[] items;
    private int selectedItem;

    public Menu(Canvas canvas, String title) {
        this.canvas = canvas;
        this.title = title;
    }

    public static int calculateMinMenuHeight(int itemsLen) {
        return (ITEMS_FONT.getHeight() * itemsLen) + TITLE_FONT.getHeight() + 2;
    }

    protected Canvas getCanvas() {
        return canvas;
    }

    public Canvas generateSubmenuCanvas(int itemIndex, int height) {
        int availableHeight = getCanvas().getDrawer().getHeight();
        if (height + 6 > availableHeight)
            throw new IllegalArgumentException("There is not enough space for your submenu (too many lines or small space)");
        int yCenter = calculateMinMenuHeight(itemIndex) - (ITEMS_FONT.getHeight() / 2);
        int y = yCenter - (height / 2);
        if (y + height + 3 > availableHeight) y = availableHeight - height - 3;
        else if (y - 3 < 0) y = 3;
        int x = getCanvas().getDrawer().getWidth() / 4;
        int width = (int) ((getCanvas().getDrawer().getWidth() - x) * 0.9);
        return canvas.createRestrictedCanvas(new Rectangle2DInt(x, y, width, height));
    }

    public String getTitle() {
        return title;
    }

    public MenuItem[] getItems() {
        return items;
    }

    public int getSelectedItemIndex() {
        return selectedItem;
    }

    protected abstract MenuItem[] onCreate();

    protected void onSelected(MenuItem item, int index) {
        item.onSelected(this, index);
    }

    public void start() {
        if (thread != null) return;
        thread = Thread.currentThread();

        items = onCreate();
        if (this.items.length <= 0) throw new IllegalArgumentException("Can't create empty menu");
        if (calculateMinMenuHeight(this.items.length) > canvas.getDrawer().getHeight())
            throw new IllegalArgumentException("There is not enough space for your menu (too many lines or small space)");

        drawMenu();
        while (!Thread.interrupted()) {
            switch (Button.waitForAnyPress()) {
                case Button.ID_UP:
                    if (selectedItem > 0) selectedItem--;
                    break;
                case Button.ID_DOWN:
                    if (selectedItem < items.length - 1) selectedItem++;
                    break;
                case Button.ID_ENTER:
                    onSelected(items[selectedItem], selectedItem);
                    break;
            }
            drawMenu();
            Thread.yield();
        }
        onDestroy();
        thread = null;
    }

    public void exit(boolean immediateReturn) {
        if (thread == null) return;
        if (immediateReturn) {
            thread.interrupt();
            return;
        }
        Utils.stopThread(thread);
    }

    protected void onDestroy() {
        items = null;
        selectedItem = 0;
    }

    protected void drawMenu() {
        GraphicsDrawer drawer = canvas.getGraphicsDrawer();
        drawer.clear();

        drawer.setFont(TITLE_FONT);
        drawer.drawString(title, 0, 0, 0);

        int lineY = TITLE_FONT.getHeight();
        drawer.drawLine(0, lineY, drawer.getWidth(), lineY++);
        drawer.drawLine(0, lineY, drawer.getWidth(), lineY);

        String pointer = "->";
        int x = ITEMS_FONT.stringWidth(pointer);
        drawer.setFont(ITEMS_FONT);
        for (int i = 0, len = items.length; i < len; i++)
            drawer.drawString(items[i].getName(), x,
                    lineY + (ITEMS_FONT.getHeight() * (i + 1)), 0);
        drawer.drawString(pointer, 0, lineY + (ITEMS_FONT.getHeight() * (selectedItem + 1)), 0);

        canvas.apply();
    }
}
