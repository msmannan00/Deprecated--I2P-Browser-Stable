package com.darkweb.helperManager;

import androidx.appcompat.app.AppCompatActivity;

import com.darkweb.constants.strings;
import com.example.myapplication.R;

import org.mozilla.geckoview.WebRequestError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class errorHandler
{
    private AppCompatActivity mContext;
    private String mErrorTemplate;
    public String createErrorPage(final int category, final int error,AppCompatActivity mContext,String url) {
        this.mContext = mContext;
        if (mErrorTemplate == null) {
            InputStream stream = null;
            BufferedReader reader = null;
            StringBuilder builder = new StringBuilder();
            try {
                stream = mContext.getResources().getAssets().open("error.html");
                reader = new BufferedReader(new InputStreamReader(stream));

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    builder.append("\n");
                }

                mErrorTemplate = builder.toString();
            } catch (IOException e) {
                return null;
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ignored) {
                    }
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        String title = helperMethod.getHost(url);

        if(url==null){
            url = "Hidden Error";
        }
        if(title==null){
            title = "Hidden Error";
        }

        String replaceUrl = errorToString(error).replace("$URL",url);
        // String replaceUrl = errorToString(error).replace("$URL",url);
        // if(replaceUrl==null){
        //     replaceUrl = "Hidden Error";
        // }

        String errorPage = createErrorPage("CODE : " + categoryToString(category) + " <br>TYPE : " + replaceUrl.replace("$TITLE",title),url);
        errorPage = translateMessage(errorPage,"CODE : " + categoryToString(category));
        return errorPage;
    }

    private String translateMessage(String message,String error){
        message = message.replace("$ERROR_M1",mContext.getString(R.string.error_m1));
        message = message.replace("$ERROR_M2",mContext.getString(R.string.error_m2));
        message = message.replace("$ERROR_M3",mContext.getString(R.string.error_m3));
        message = message.replace("$ERROR_M4",mContext.getString(R.string.error_m4));
        message = message.replace("$ERROR_M5",mContext.getString(R.string.error_m5));
        message = message.replace("$ERROR_M6",mContext.getString(R.string.error_m6));
        message = message.replace("$ERROR", error);

        return message;
    }

    private String errorToString(final int error) {
        switch (error) {
            case WebRequestError.ERROR_UNKNOWN:
                return "ERROR_UNKNOWN"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ;
            case WebRequestError.ERROR_SECURITY_SSL:
                return "ERROR_SECURITY_SSL";
            case WebRequestError.ERROR_SECURITY_BAD_CERT:
                return "ERROR_SECURITY_BAD_CERT";
            case WebRequestError.ERROR_NET_RESET:
                return "ERROR_NET_RESET";
            case WebRequestError.ERROR_NET_INTERRUPT:
                return "ERROR_NET_INTERRUPT";
            case WebRequestError.ERROR_NET_TIMEOUT:
                return "ERROR_NET_TIMEOUT";
            case WebRequestError.ERROR_CONNECTION_REFUSED:
                return "ERROR_CONNECTION_REFUSED";
            case WebRequestError.ERROR_UNKNOWN_PROTOCOL:
                return "ERROR_UNKNOWN_PROTOCOL";
            case WebRequestError.ERROR_UNKNOWN_HOST:
                return "ERROR_UNKNOWN_HOST";
            case WebRequestError.ERROR_UNKNOWN_SOCKET_TYPE:
                return "ERROR_UNKNOWN_SOCKET_TYPE";
            case WebRequestError.ERROR_UNKNOWN_PROXY_HOST:
                return "ERROR_UNKNOWN_PROXY_HOST";
            case WebRequestError.ERROR_MALFORMED_URI:
                return "ERROR_MALFORMED_URI";
            case WebRequestError.ERROR_REDIRECT_LOOP:
                return "ERROR_REDIRECT_LOOP";
            case WebRequestError.ERROR_SAFEBROWSING_PHISHING_URI:
                return "ERROR_SAFEBROWSING_PHISHING_URI";
            case WebRequestError.ERROR_SAFEBROWSING_MALWARE_URI:
                return "ERROR_SAFEBROWSING_MALWARE_URI";
            case WebRequestError.ERROR_SAFEBROWSING_UNWANTED_URI:
                return "ERROR_SAFEBROWSING_UNWANTED_URI";
            case WebRequestError.ERROR_SAFEBROWSING_HARMFUL_URI:
                return "ERROR_SAFEBROWSING_HARMFUL_URI";
            case WebRequestError.ERROR_CONTENT_CRASHED:
                return "ERROR_CONTENT_CRASHED";
            case WebRequestError.ERROR_OFFLINE:
                return "ERROR_OFFLINE";
            case WebRequestError.ERROR_PORT_BLOCKED:
                return "ERROR_PORT_BLOCKED";
            case WebRequestError.ERROR_PROXY_CONNECTION_REFUSED:
                return "ERROR_PROXY_CONNECTION_REFUSED";
            case WebRequestError.ERROR_FILE_NOT_FOUND:
                return "ERROR_FILE_NOT_FOUND";
            case WebRequestError.ERROR_FILE_ACCESS_DENIED:
                return "ERROR_FILE_ACCESS_DENIED";
            case WebRequestError.ERROR_INVALID_CONTENT_ENCODING:
                return "ERROR_INVALID_CONTENT_ENCODING";
            case WebRequestError.ERROR_UNSAFE_CONTENT_TYPE:
                return "ERROR_UNSAFE_CONTENT_TYPE";
            case WebRequestError.ERROR_CORRUPTED_CONTENT:
                return "ERROR_CORRUPTED_CONTENT";
            default:
                return "UNKNOWN";
        }
    }
    private String categoryToString(final int category)
    {
        switch (category)
        {
            case WebRequestError.ERROR_CATEGORY_UNKNOWN:
                return "ERROR_CATEGORY_UNKNOWN";
            case WebRequestError.ERROR_CATEGORY_SECURITY:
                return "ERROR_CATEGORY_SECURITY";
            case WebRequestError.ERROR_CATEGORY_NETWORK:
                return "ERROR_CATEGORY_NETWORK";
            case WebRequestError.ERROR_CATEGORY_CONTENT:
                return "ERROR_CATEGORY_CONTENT";
            case WebRequestError.ERROR_CATEGORY_URI:
                return "ERROR_CATEGORY_URI";
            case WebRequestError.ERROR_CATEGORY_PROXY:
                return "ERROR_CATEGORY_PROXY";
            case WebRequestError.ERROR_CATEGORY_SAFEBROWSING:
                return "ERROR_CATEGORY_SAFEBROWSING";
            default:
                return "UNKNOWN";
        }
    }
    private String createErrorPage(final String error,String url) {
        if(error==null){
            return strings.EMPTY_STR;
        }

        if (mErrorTemplate == null) {
            InputStream stream = null;
            BufferedReader reader = null;
            StringBuilder builder = new StringBuilder();

            try {
                stream = mContext.getResources().getAssets().open("error.html");
                reader = new BufferedReader(new InputStreamReader(stream));

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    builder.append("\n");
                }

                mErrorTemplate = builder.toString();
            } catch (IOException e) {
                return null;
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ignored) {
                    }
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }

        return mErrorTemplate.replace("$URL",url);
    }

}
