package com.darkweb.helperManager;

import com.darkweb.constants.enums;

import java.util.List;

public class eventObserver
{
    public interface eventListener
    {
        void invokeObserver(List<Object> data, enums.etype event_type);
    }
}
