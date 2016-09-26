package eu.codetopic.anty.lejos.utils.menu;

public interface MenuItem {

    String getName();

    boolean onSelected(Menu menu, int itemIndex);
}
