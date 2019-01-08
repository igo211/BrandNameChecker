package com.zero211.brandnamechecker;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public enum Site
{
    FB(R.id.txtFacebook, "https://m.facebook.com/" + MainActivity.USER_NAME_PLACEHOLDER + "/", "Page Not Found"),
    INSTA(R.id.txtInstagram, "https://www.instagram.com/" + MainActivity.USER_NAME_PLACEHOLDER + "/", "Page Not Found"),
    TWITTER(R.id.txtTwitter, "https://www.twitter.com/" + MainActivity.USER_NAME_PLACEHOLDER + "/", "Page Not Found"),
    YOUTUBE(R.id.txtYouTube, "https://www.youtube.com/user/" + MainActivity.USER_NAME_PLACEHOLDER + "/", "channel-empty-message")
    ;

    private int viewID;
    private String urlPatternStr;
    private Pattern failedRegexPatt;

    private class CheckSiteTask extends AsyncTask<Void, Void, Integer>
    {
        private String brandNameStr;
        private TextView textView;

        CheckSiteTask(TextView textView, String brandNameStr)
        {
            this.textView = textView;
            this.brandNameStr = brandNameStr;
        }

        @Override
        protected Integer doInBackground(Void... params)
        {
            String urlStr = Site.this.urlPatternStr.replace(MainActivity.USER_NAME_PLACEHOLDER, brandNameStr);

            Log.i("Main:CheckSiteTask", "downloading " + urlStr);

            HttpsURLConnection conn = null;
            try
            {
                URL url = new URL(urlStr);
                conn = (HttpsURLConnection) url.openConnection();

                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream())))
                {
                    char[] buf = new char[1024];
                    StringBuilder sb = new StringBuilder();
                    int charsRead;

                    while((charsRead = in.read(buf,0, buf.length)) != -1)
                    {
                        sb.append(buf,0, charsRead);
                    }

                    String htmlResult = sb.toString();
                    Log.i("Main:checkSite", "download result is: " + htmlResult);

                    if (htmlResult.isEmpty())
                    {
                        return Color.YELLOW;
                    }
                    else if (Site.this.failedRegexPatt.matcher(htmlResult).matches())
                    {
                        return Color.GREEN;
                    }
                    else
                    {
                        return Color.RED;
                    }

                }
                catch (FileNotFoundException fe)
                {
                    Log.i("Main:CheckSiteTask", "Got a FileNotFoundException for url: " + urlStr + " , so it's probably available.");
                    return Color.GREEN;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return Color.YELLOW;
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
                return Color.YELLOW;
            }
            finally
            {
                if (conn != null)
                {
                    conn.disconnect();
                }
            }

        }

        @Override
        protected void onPostExecute(Integer result)
        {
            textView.setBackgroundColor(result);
        }
    }

    Site(int viewID, String urlPatternStr, String failedRegexStr)
    {
        this.viewID = viewID;
        this.urlPatternStr = urlPatternStr;
        this.failedRegexPatt = Pattern.compile(failedRegexStr);
    }

    public void resetView(Activity activity)
    {
        TextView textView = (TextView) activity.findViewById(Site.this.viewID);
        textView.setBackgroundColor(Color.WHITE);
    }

    public void checkBrandName(Activity activity, String brandName)
    {
        TextView textView = (TextView) activity.findViewById(Site.this.viewID);
        CheckSiteTask checkTLDTask = new CheckSiteTask(textView, brandName);
        checkTLDTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
