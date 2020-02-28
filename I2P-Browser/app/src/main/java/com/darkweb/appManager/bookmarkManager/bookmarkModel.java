package com.darkweb.appManager.bookmarkManager;

import java.util.ArrayList;

class bookmarkModel
{
    /*Private Variables*/

    private ArrayList<bookmarkRowModel> mModelList = new ArrayList<>();

    /*Initializations*/

    void setList(ArrayList<bookmarkRowModel> model)
    {
        mModelList = model;
    }
    ArrayList<bookmarkRowModel> getList()
    {
        return mModelList;
    }
    private void removeFromMainList(int index)
    {
        mModelList.remove(index);
    }

    /*Clear List*/

    void onManualClear(int index){
         removeFromMainList(index);
    }
    void clearList(){
        mModelList.clear();
    }



}