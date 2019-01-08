package com.zero211.brandnamechecker;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.regex.Pattern;

public enum TLD
{
    COM(R.id.txtDotCom, "whois.verisign-grs.com", ".*No match for .*"),
    CO(R.id.txtDotCo, "whois.nic.co", ".*No Data Found.*"),
    ORG(R.id.txtDotOrg, "whois.pir.org", ".*Not a valid domain search pattern.*"),
    NET(R.id.txtDotNet, "whois.verisign-grs.com", ".*No match for .*"),
    AI(R.id.txtDotAI, "whois.ai", ".*No Object Found.*"),
    ;

    private int viewID;
    private String whoisServer;
    private Pattern failedRegexPatt;

    TLD(int viewID, String whoisServer, String failedRegexStr)
    {
        this.viewID = viewID;
        this.whoisServer = whoisServer;
        this.failedRegexPatt = Pattern.compile(failedRegexStr, Pattern.DOTALL);
    }

    private class CheckTLDTask extends AsyncTask<Void, Void, Integer>
    {

        private String brandNameStr;
        private TextView textView;

        String domquest;

        CheckTLDTask(TextView textView, String brandNameStr)
        {
            this.textView = textView;
            this.brandNameStr = brandNameStr;
        }

        @Override
        protected Integer doInBackground(Void... params)
        {
            domquest = brandNameStr + "." + TLD.this.name();
            Socket theSocket = null;
            StringBuilder sb = new StringBuilder();

            try
            {
                theSocket = new Socket(TLD.this.whoisServer, 43, true);
                Writer out = new OutputStreamWriter(theSocket.getOutputStream());
                out.write("=" + domquest + "\r\n");
                out.flush();
                BufferedReader inReader;
                inReader = new BufferedReader(new InputStreamReader(theSocket.getInputStream()));

                String s;
                while ((s = inReader.readLine()) != null)
                {
                    sb.append(s);
                    sb.append("\n");
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (theSocket != null)
                {
                    try
                    {
                        theSocket.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            String htmlStr = sb.toString();

            if (htmlStr.isEmpty())
            {
                Log.i("Main:CheckTLDTask", "\r\nResult for " + domquest + " is empty");
                return Color.YELLOW;
            }
            else
            {
                Log.i("Main:CheckTLDTask", "\r\nResult for " + domquest +  " is:\r\n\r\n" + htmlStr);
                if (TLD.this.failedRegexPatt.matcher(htmlStr).matches())
                {
                    return Color.GREEN;
                }
                else
                {
                    return Color.RED;
                }
            }
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            textView.setBackgroundColor(result);
        }
    }

    public void resetView(Activity activity)
    {
        TextView textView = (TextView) activity.findViewById(TLD.this.viewID);
        textView.setBackgroundColor(Color.WHITE);
    }

    public void checkBrandName(Activity activity, String brandName)
    {
        TextView textView = (TextView) activity.findViewById(TLD.this.viewID);
        CheckTLDTask checkTLDTask = new CheckTLDTask(textView, brandName);
        checkTLDTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
