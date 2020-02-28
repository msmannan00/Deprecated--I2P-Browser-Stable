package com.darkweb.helperManager;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import com.darkweb.constants.keys;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;

public class helperMethod
{
    /*Helper Methods General*/

    public static String completeURL(String url){

        if(!url.startsWith("www.")&& !url.startsWith("http://")&& !url.startsWith("https://")){
            url = ""+url;
        }
        if(!url.startsWith("http://")&&!url.startsWith("https://")){
            url = "http://"+url;
        }
        return url;
    }

    public static SpannableString urlDesigner(String url){

        if (url.contains("https://"))
        {
            SpannableString ss = new SpannableString(url);
            ss.setSpan(new ForegroundColorSpan(Color.argb(255, 0, 123, 43)), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(Color.GRAY), 5, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return ss;
        } else if (url.contains("http://"))
        {
            SpannableString ss = new SpannableString(url);
            ss.setSpan(new ForegroundColorSpan(Color.argb(255, 0, 128, 43)), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(Color.GRAY), 4, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return ss;
        } else
        {
            SpannableString ss = new SpannableString(url);
            ss.setSpan(new ForegroundColorSpan(Color.BLACK), 0, url.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return ss;
        }
    }

    public static void sendRateEmail(Context context){
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "gamesolstudios@gmail.com", null));
        i.putExtra(Intent.EXTRA_SUBJECT, "Issue Report");
        i.putExtra(Intent.EXTRA_TEXT   , "");
        try {
            if (i.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(i);
            }
        } catch (android.content.ActivityNotFoundException ignored) {
        }
    }

    public static void hideKeyboard(AppCompatActivity context) {
        View view = context.findViewById(android.R.id.content);
        if (view != null)
        {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void shareApp(AppCompatActivity context) {
        ShareCompat.IntentBuilder.from(context)
                .setType("text/plain")
                .setChooserTitle("Hi! Check out this Awesome App")
                .setSubject("Hi! Check out this Awesome App")
                .setText("Genesis | Onion Search | http://play.google.com/store/apps/details?id=" + context.getPackageName())
                .startChooser();
    }

    public static void openDownloadFolder(AppCompatActivity context)
    {
        Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        if(intent.resolveActivity(context.getPackageManager()) != null)
        {
            context.startActivity(intent);
        }else {
            helperMethod.showToastMessage("Download Folder Not Found",context);
        }
    }

    static String getHost(String link){
        URL url;
        try
        {
            url = new URL(link);
            return url.getHost();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            return "";
        }

    }

    public static String removeLastSlash(String url){
        if(url.length()>2){
            if(url.charAt(url.length()-1)=='/'){
                return url.substring(0,url.length()-1);
            }
        }
        return url;
    }

    public static String urlWithoutPrefix(String url){
        try{
            url = url.substring(url.indexOf(getHost(url))).replace("www.","").replace("m.","");
            return url;
        }catch (Exception ex){
            return url;
        }
    }

    public static void openActivity( Class<?> cls,int type,AppCompatActivity context,boolean animation){
        Intent myIntent = new Intent(context, cls);
        myIntent.putExtra(keys.list_type, type);
        if(!animation){
            myIntent.addFlags(FLAG_ACTIVITY_NO_ANIMATION);
        }
        context.startActivity(myIntent);
    }

    public static void onMinimizeApp(AppCompatActivity context){
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);
    }

    public static int screenWidth()
    {
        return (Resources.getSystem().getDisplayMetrics().widthPixels);
    }

    public static RotateAnimation getRotationAnimation(){
        RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        rotate.setDuration(2000);
        rotate.setRepeatCount(Animation.INFINITE);
        return rotate;
    }

    public static String getDomainName(String url)
    {
        try{
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        }catch (Exception ex){
            return url;
        }
    }

    public static ViewGroup.MarginLayoutParams getCenterScreenPoint(ViewGroup.LayoutParams itemLayoutParams) {
        double heightloader = Resources.getSystem().getDisplayMetrics().heightPixels*0.78;
        ViewGroup.MarginLayoutParams params_loading = (ViewGroup.MarginLayoutParams) itemLayoutParams;
        params_loading.topMargin = (int)(heightloader);

        return params_loading;
    }


    public static void openPlayStore(String packageName,AppCompatActivity context)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id="+packageName));

        if(intent.resolveActivity(context.getPackageManager()) != null)
        {
            context.startActivity(intent);
        }else {
            helperMethod.showToastMessage("Playstore Not Found",context);
        }

    }

    public static int pxFromDp(int dp){
        return   (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenHeight(AppCompatActivity context) {
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return size.y;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    public static void copyURL(String url,Context context){

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("link", url);
        clipboard.setPrimaryClip(clip);

        showToastMessage("Copied to Clipboard",context);

    }

    public static void showToastMessage(String message,Context context){
        Toast toast=Toast.makeText(context.getApplicationContext(),message,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    public static boolean checkPermissions(AppCompatActivity context) {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };

        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(context, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

}
