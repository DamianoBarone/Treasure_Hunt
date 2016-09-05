package com.example.damiano.treasurehunt;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.os.AsyncTask;
import java.io.IOException;

/**
 * Created by Lorenzo on 28/08/2016.
 */
public class BackgroundTask extends AsyncTask<Void, Void, Boolean> {

		String result=null;
		private String msg;
		private OkHttpClient client;
		private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
		private static final String URL  = "http://192.168.1.196:8080/Treasure_server/Servlet";
		BackgroundActions actions;

		BackgroundTask(String m) {
			this(m, null);
		}

		BackgroundTask(String m, BackgroundActions a) {
			result=new String();
			msg=m;
			client = new OkHttpClient();
			actions=a;
		}

		String getResult() {
			return result;
		}

		String send(String url, String json)
		{
			System.out.println("Background - send");
			String servletresponse;
			RequestBody body = RequestBody.create(JSON, json);
			Request request = new Request.Builder()
					.url(url)
					.post(body)
					.build();
			try (Response response = client.newCall(request).execute()) {

				servletresponse=response.body().string();
				return servletresponse;
			} catch (IOException e) {
				System.out.println("Background - eccezionale ");
				e.printStackTrace();
			}
			return "Background - vuoto";

		}

		@Override
		protected Boolean doInBackground(Void... params) {
			System.out.println("do in background");
			final String json =msg; /*"{'message_type':'9',\n}" +
                    "'idEvent': '"+ idEvent + "',\n }";*/
			result = send(URL, json);
			if (result==null || result.equals("Background - vuoto")) {
				System.out.println("errore nella send() di doInBackground");//activity successiva
				return false; // dovrebbe andare bene ***
			}
			System.out.println("doInBackground - Rispostaaaaaaaaaa: "+result);
			if (actions!=null)
				return actions.BackgroundActions(result);
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			if (actions!=null)
				actions.PostExecuteActions(success);
		}

		@Override
		protected void onCancelled() {
			if (actions!=null)
				actions.CancelledActions();
		}
	}
