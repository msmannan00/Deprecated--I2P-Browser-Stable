package com.darkweb.appManager.bookmarkManager;

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
import com.darkweb.appManager.databaseManager.databaseController;
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
public class bookmarkController extends AppCompatActivity
{
    /*Private Variables*/

    private bookmarkModel mListModel;
    private homeController mHomeController;
    private activityContextManager mContextManager;

    private ImageView mEmptyListNotifier;
    private EditText mSearchBar;
    private RecyclerView mListView;
    private Button mClearButton;

    private bookmarkViewController mBookmarkViewController;

    /*Initializations*/

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark_view);
        initializeListModel();
        initializeViews();
        initializeList();
        onEditorInvoked();
    }

    public void initializeListModel(){
        mListModel = new bookmarkModel();
        mListModel.setList(dataController.getInstance().getBookmark());
        mContextManager = activityContextManager.getInstance();
        mHomeController = activityContextManager.getInstance().getHomeController();
        mContextManager.setBookmarkController(this);
        pluginController.getInstance().logEvent(strings.BOOKMARK_OPENED);
    }
    public void initializeViews(){
        mEmptyListNotifier = findViewById(R.id.empty_list);
        mSearchBar = findViewById(R.id.search);
        mListView = findViewById(R.id.listview);
        mClearButton = findViewById(R.id.clearButton);
        mBookmarkViewController = new bookmarkViewController(mEmptyListNotifier, mSearchBar, mListView, mClearButton,this);
        mClearButton.setText(R.string.tab_view_clear_bookmark);
    }
    public void initializeList(){
        bookmarkAdapter adapter = new bookmarkAdapter(mListModel.getList(),new adapterCallback());
        adapter.invokeFilter(false);
        mListView.setAdapter(adapter);
        mListView.setLayoutManager(new LinearLayoutManager(this));
        mBookmarkViewController.updateIfListEmpty(mListModel.getList().size(),0);
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
                ((bookmarkAdapter) Objects.requireNonNull(mListView.getAdapter())).setFilter(mSearchBar.getText().toString());
                ((bookmarkAdapter) mListView.getAdapter()).invokeFilter(true);
            }
        });
    }

    public void onBackPressed(View view){
        this.finish();
    }
    public void onclearDataTrigger(View view){
        pluginController.getInstance().MessageManagerHandler(this, Collections.singletonList(strings.EMPTY_STR),enums.etype.clear_bookmark);
    }
    public void onclearData(){
        mListModel.clearList();
        ((bookmarkAdapter) Objects.requireNonNull(mListView.getAdapter())).invokeFilter(true );
        mBookmarkViewController.clearList();
        databaseController.getInstance().execSQL("delete from bookmark where 1",null);
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

    @Override
    public void onResume()
    {
        activityContextManager.getInstance().setCurrentActivity(this);
        status.sIsAppPaused = false;
        super.onResume();
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
            if(e_type.equals(enums.etype.url_triggered)){
                String url_temp = helperMethod.completeURL(data.get(0).toString());
                pluginController.getInstance().logEvent(strings.BOOKMARK_TRIGGERED);
                mHomeController.onLoadURL(url_temp);
                finish();
            }
            else if(e_type.equals(enums.etype.url_clear)){
                mListModel.onManualClear((int)data.get(0));
            }
            else if(e_type.equals(enums.etype.is_empty)){
                mBookmarkViewController.removeFromList((int)data.get(0));
                mBookmarkViewController.updateIfListEmpty(mListModel.getList().size(),300);
            }
            else if(e_type.equals(enums.etype.remove_from_database)){
                databaseController.getInstance().deleteFromList((int)data.get(0),"bookmark");
            }
        }

    }

}
