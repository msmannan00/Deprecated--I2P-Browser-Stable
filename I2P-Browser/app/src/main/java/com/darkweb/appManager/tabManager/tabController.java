package com.darkweb.appManager.tabManager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.darkweb.appManager.activityContextManager;
import com.darkweb.appManager.homeManager.homeController;
import com.darkweb.constants.enums;
import com.darkweb.constants.keys;
import com.darkweb.constants.status;
import com.darkweb.constants.strings;
import com.darkweb.dataManager.dataController;
import com.darkweb.helperManager.eventObserver;
import com.darkweb.helperManager.helperMethod;
import com.darkweb.pluginManager.pluginController;
import com.example.myapplication.R;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("FieldCanBeLocal")
public class tabController extends AppCompatActivity
{
    /*Private Variables*/

    private tabModel mListModel;
    private homeController mHomeController;
    private activityContextManager mContextManager;

    private ImageView mEmptyListNotifier;
    private EditText mSearchBar;
    private RecyclerView mListView;
    private Button mClearButton;

    private tabViewController mtabViewController;

    /*Initializations*/

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_view);
        initializeListModel();
        initializeViews();
        initializeList();
        onEditorInvoked();
    }

    public void initializeListModel(){
        mListModel = new tabModel();
        mListModel.setList(dataController.getInstance().getTab());
        mContextManager = activityContextManager.getInstance();
        mHomeController = activityContextManager.getInstance().getHomeController();
        mContextManager.setTabController(this);
        pluginController.getInstance().logEvent(strings.TAB_OPENED);
    }
    public void initializeViews(){
        mEmptyListNotifier = findViewById(R.id.empty_list);
        mSearchBar = findViewById(R.id.search);
        mListView = findViewById(R.id.listview);
        mClearButton = findViewById(R.id.clearButton);
        mtabViewController = new tabViewController(mEmptyListNotifier, mListView, mClearButton,this);
        mClearButton.setText(R.string.tab_view_clear_tab);
    }
    public void initializeList(){
        tabAdapter adapter = new tabAdapter(mListModel.getList(),new adapterCallback());
        adapter.invokeFilter(false);
        mListView.setAdapter(adapter);
        mListView.setLayoutManager(new LinearLayoutManager(this));
        mtabViewController.updateIfListEmpty(mListModel.getList().size(),0);
    }

    /*View Handlers*/

    public void onEditorInvoked(){

        mSearchBar.setOnEditorActionListener((v, actionId, event) ->{
            if (actionId == EditorInfo.IME_ACTION_NEXT)
            {
                helperMethod.hideKeyboard(this);
                return true;
            }
            return false;
        });

        mSearchBar.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                if(mListView!=null){
                    ((tabAdapter) Objects.requireNonNull(mListView.getAdapter())).setFilter(mSearchBar.getText().toString());
                    ((tabAdapter) mListView.getAdapter()).invokeFilter(true);
                }
            }
        });
    }

    public void reset(){
        if(mListView!=null){
            mListView.setAdapter(null);
        }
        mListView = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        reset();
    }

    public void onBackPressed(View view){
        this.finish();
        reset();
    }

    public void onclearDataTrigger(View view){
        pluginController.getInstance().MessageManagerHandler(this, Collections.singletonList(strings.EMPTY_STR),enums.etype.clear_tab);
    }

    @Override
    public void onTrimMemory(int level)
    {
        if(status.sIsAppPaused && (level==80 || level==15))
        {
            dataController.getInstance().setBool(keys.LOW_MEMORY,true);
            finish();
        }
    }

    public void onNewTabInvoked(View view)
    {
        mHomeController.onNewTab(false);
        finish();
    }

    @Override
    public void onResume()
    {
        activityContextManager.getInstance().setCurrentActivity(this);
        status.sIsAppPaused = false;
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause()
    {
        status.sIsAppPaused = true;
        super.onPause();
    }

    /*Event Observer*/

    public class adapterCallback implements eventObserver.eventListener{

        @Override
        public void invokeObserver(List<Object> data, enums.etype e_type)
        {
            if(e_type.equals(enums.etype.clear_recycler)){
                mListView.getRecycledViewPool().clear();
            }
            if(e_type.equals(enums.etype.url_triggered)){
                tabRowModel model = (tabRowModel)data.get(0);
                pluginController.getInstance().logEvent(strings.TAB_TRIGGERED);
                mHomeController.onLoadTab(model.getSession(),false);
                tabController.this.finish();
            }
            else if(e_type.equals(enums.etype.url_clear)){
                mListModel.onManualClear((int)data.get(0));
                mHomeController.initTabCount();
            }
            else if(e_type.equals(enums.etype.is_empty)){

                mtabViewController.removeFromList((int)data.get(0));
                mtabViewController.updateIfListEmpty(mListModel.getList().size(),300);
                mHomeController.releaseSession();
                if(dataController.getInstance().getTotalTabs()<1){
                    mHomeController.onNewTab(false);
                    finish();
                }else {
                    mHomeController.loadExistingTab();
                }
                mHomeController.initTabCount();
            }
        }
    }
}
