package com.itservices.gpxanalyzer.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.utils.SingleLiveEvent;

import java.util.List;

public class MainMenuViewModel extends ViewModel implements MenuItemHandler {

    private final MutableLiveData<List<MenuItem>> menuItemsLiveData = new MutableLiveData<>();

    private final MutableLiveData<MenuItem> menuItemClickedEvent = new SingleLiveEvent<>();

    public MainMenuViewModel() {

    }
    public void setMenuItems(List<MenuItem> items) {
        menuItemsLiveData.setValue(items);
    }


    /**
     * Returns the LiveData of menu item list.
     */
    public LiveData<List<MenuItem>> getMenuItems() {
        return menuItemsLiveData;
    }

    /**
     * Event triggered when a menu item is clicked.
     */
    public LiveData<MenuItem> getMenuItemClickedEvent() {
        return menuItemClickedEvent;
    }

    /**
     * Implementation of MenuItemHandler.
     * Called by data binding when a menu item button is clicked.
     */
    @Override
    public void onMenuItemClicked(MenuItem item) {
        menuItemClickedEvent.setValue(item);
    }
}