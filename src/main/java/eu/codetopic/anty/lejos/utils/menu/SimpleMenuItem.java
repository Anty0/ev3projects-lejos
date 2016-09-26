package eu.codetopic.anty.lejos.utils.menu;

public class SimpleMenuItem implements MenuItem {

    private final String name;
    private final OnSelected onSelected;

    public SimpleMenuItem(String name, OnSelected onSelected) {
        this.name = name;
        this.onSelected = onSelected;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean onSelected(Menu menu, int itemIndex) {
        return onSelected.onSelected(menu, itemIndex);
    }

    public interface OnSelected {
        boolean onSelected(Menu menu, int itemIndex);
    }
}
