package com.darkweb.appManager.tabManager;

import com.darkweb.appManager.homeManager.geckoSession;

public class tabRowModel
{
    /*Private Variables*/

    private geckoSession mSession;
    private int mId;

    /*Initializations*/

    public tabRowModel(geckoSession mSession,int mId) {
        this.mSession = mSession;
        this.mId = mId;
    }

    /*Getter Setters*/

    public geckoSession getSession()
    {
        return mSession;
    }
    public int getmId() {
        return mId;
    }
    public void setId(int id) {
        mId = id;
    }

}
