package com.darkweb.appManager.historyManager;

import java.util.ArrayList;

class historyModel
{
    /*Private Variables*/

    private ArrayList<historyRowModel> mModelList = new ArrayList<>();

    /*Initializations*/

    void setList(ArrayList<historyRowModel> model)
    {
        mModelList = model;
    }
    ArrayList<historyRowModel> getList()
    {
        return mModelList;
    }
    private void removeFromMainList(int index)
    {
        mModelList.remove(index);
    }

    /*Manual Clear*/

    void onManualClear(int index){
         removeFromMainList(index);
    }
    void clearList(){
        mModelList.clear();
    }

}