/*
 * Copyright (C) 2012- Peer internet solutions 
 * 
 * This file is part of infocam.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package com.infocam.mgr;



import android.content.ContentResolver;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.infocam.MixContext;
import com.infocam.mgr.downloader.DownloadRequest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public final class HttpTools {


	/**
	 * Prefered To use InputStream managed!
	 *
	 * @param request
	 * @param cr
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static String getPageContent(DownloadRequest request, ContentResolver cr) throws Exception {
		String pageContent;
		InputStream is = null;
		if(!request.getSource().getUrl().startsWith("file://")){
			is = HttpTools.getHttpGETInputStream(request.getSource().getUrl() + request.getParams(), cr);
			/*BufferedReader r = new BufferedReader(new InputStreamReader(is));
			StringBuilder total = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
				total.append(line);
			}
			Log.v("inputString3", line);*/
		}else{
			is = HttpTools.getHttpGETInputStream(request.getSource().getUrl(), cr);

		}
		pageContent = HttpTools.getHttpInputString(is);
		Log.v("pageConttt", pageContent);
		/*BufferedReader r = new BufferedReader(new InputStreamReader(is));
		StringBuilder total = new StringBuilder();
		String line;
		while ((line = r.readLine()) != null) {
			total.append(line);
		}
		Log.v("inputString2", line);*/
		HttpTools.returnHttpInputStream(is);
		return pageContent;
	}


	public static String getHttpInputString(InputStream is) {
		//BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8 * 1024);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			Log.v("", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}



	/**
	 * Input Stream with unsafe close 
	 */
	@Deprecated
	public static InputStream getHttpGETInputStream(String urlStr, ContentResolver cr ) throws Exception {
		InputStream is = null;
		URLConnection conn = null;
		Log.v("urlstr", urlStr);
		// HTTP connection reuse which was buggy pre-froyo
		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}

		if (urlStr.startsWith("file://"))
			return new FileInputStream(urlStr.replace("file://", ""));

		if (urlStr.startsWith("content://"))
			return getContentInputStream(urlStr, null, cr);

		if (urlStr.startsWith("https://")) {
			HttpsURLConnection
					.setDefaultHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String hostname,
											  SSLSession session) {
							return true;
						}
					});
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[] { new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain,
											   String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain,
											   String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			} }, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context
					.getSocketFactory());
		}

		try {
			URL url = new URL(urlStr);
			conn = url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);

			is = conn.getInputStream();
			BufferedReader r =new BufferedReader(new InputStreamReader(is));
			StringBuilder total = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
				total.append(line);
			}
			//Log.v("inputString1", total.toString());
			//String str = "{\"geonames\":[{\"elevation\":1080,\"feature\":\"city\",\"lng\":32.7492509,\"distance\":\"0.78\",\"countryCode\":\"TR\",\"rank\":19,\"lang\":\"tr\",\"title\":\"Bilkent Kutuphane\",\"lat\":39.8701803,\"wikipediaUrl\":\"tr.wikipedia.org/wiki/Bilkent_%C3%9Cniversitesi_K%C3%BCt%C3%BCphanesi\"}]}";
			String str = ",{\"elevation\":1300,\"lng\":32.7492509,\"distance\":\"0.78\",\"lang\":\"tr\",\"title\":\"Bilkent Kutuphane\",\"lat\":39.8701803,\"wikipediaUrl\":\"tr.wikipedia.org/wiki/Bilkent_%C3%9Cniversitesi_K%C3%BCt%C3%BCphanesi\"},{\"elevation\":1200,\"feature\":\"city\",\"lng\":32.7612508,\"distance\":\"3.3\",\"countryCode\":\"TR\",\"rank\":19,\"lang\":\"tr\",\"title\":\"Ali Pasa Camii\",\"lat\":39.8852739,\"wikipediaUrl\":\"tr.wikipedia.org/wiki/Do%C4%9Framac%C4%B1zade_Ali_Sami_Pa%C5%9Fa_Camii\"},{\"elevation\":1080,\"feature\":\"city\",\"lng\":32.7525412,\"distance\":\"0.3253\",\"countryCode\":\"TR\",\"rank\":19,\"lang\":\"tr\",\"title\":\"Bilkent Odeon\",\"lat\":39.8753577,\"wikipediaUrl\":\"tr.wikipedia.org/1\"},"
					+ "{\"elevation\":1200,\"feature\":\"edu\",\"lng\":32.7508973,\"distance\":\"1.2952\",\"countryCode\":\"TR\",\"rank\":89,\"lang\":\"tr\",\"title\":\"Meteksan Market\",\"lat\":39.8715564,\"wikipediaUrl\":\"tr.wikipedia.org/2\"},"
					+ "{\"elevation\":1180,\"feature\":\"edu\",\"lng\":32.7502294,\"distance\":\"1.2952\",\"countryCode\":\"TR\",\"rank\":89,\"lang\":\"tr\",\"title\":\"Rektörlük Binası\",\"lat\":39.8709157,\"wikipediaUrl\":\"tr.wikipedia.org/3\"},"
					+ "{\"elevation\":1190,\"feature\":\"edu\",\"lng\":32.749872,\"distance\":\"1.2952\",\"countryCode\":\"TR\",\"rank\":89,\"lang\":\"tr\",\"title\":\"Sağlık Merkezi\",\"lat\":39.8679056,\"wikipediaUrl\":\"tr.wikipedia.org/4\"},"
					+ "{\"elevation\":1200,\"feature\":\"edu\",\"lng\":32.7492433,\"distance\":\"1.2952\",\"countryCode\":\"TR\",\"rank\":89,\"lang\":\"tr\",\"title\":\"Speed Cafe\",\"lat\":39.8661404,\"wikipediaUrl\":\"tr.wikipedia.org/5\"},"
					+ "{\"elevation\":1220,\"feature\":\"edu\",\"lng\":32.7488792,\"distance\":\"1.2952\",\"countryCode\":\"TR\",\"rank\":89,\"lang\":\"tr\",\"title\":\"Sofa Bilkent\",\"lat\":39.8647688,\"wikipediaUrl\":\"tr.wikipedia.org/6\"},"
					+ "{\"elevation\":1180,\"feature\":\"edu\",\"lng\":32.7481678,\"distance\":\"1.2952\",\"countryCode\":\"TR\",\"rank\":89,\"lang\":\"tr\",\"title\":\"İşletme Fakültesi\",\"lat\":39.8668071,\"wikipediaUrl\":\"tr.wikipedia.org/7\"},"
					+ "{\"elevation\":1180,\"feature\":\"edu\",\"lng\":32.74878,\"distance\":\"1.2952\",\"countryCode\":\"TR\",\"rank\":89,\"lang\":\"tr\",\"title\":\"Hukuk Fakültesi\",\"lat\":39.8686486,\"wikipediaUrl\":\"tr.wikipedia.org/8\"},"
					+ "{\"elevation\":1190,\"feature\":\"edu\",\"lng\":32.7551231,\"distance\":\"1.2952\",\"countryCode\":\"TR\",\"rank\":89,\"lang\":\"tr\",\"title\":\"Müzik ve Sahne Sanatları Fakültesi\",\"lat\":39.8689185,\"wikipediaUrl\":\"tr.wikipedia.org/9\"},"
					+ "{\"elevation\":1180,\"feature\":\"edu\",\"lng\":32.7592357,\"distance\":\"1.2952\",\"countryCode\":\"TR\",\"rank\":89,\"lang\":\"tr\",\"title\":\"Bilkent Otel\",\"lat\":39.8754728,\"wikipediaUrl\":\"tr.wikipedia.org/10\"}"
					+ "]}";
			total.deleteCharAt(total.length()-1);
			total.deleteCharAt(total.length()-1);
			total.append(str);

			InputStream stream = new ByteArrayInputStream(total.toString().getBytes());
			/*r = new BufferedReader(new InputStreamReader(stream));

			StringBuilder total2 = new StringBuilder();
			while ((line = r.readLine()) != null) {
				total2.append(line);
			}
			Log.v("inputString1", total2.toString());*/
			return stream;
		} catch (Exception ex) {
			try {
				is.close();
			} catch (Exception ignore) {
				Log.w(MixContext.TAG, "Error on url "+urlStr, ignore);
			}
			try {
				if (conn instanceof HttpURLConnection)
					((HttpURLConnection) conn).disconnect();
			} catch (Exception ignore) {

			}
			throw ex;
		}
	}

	/**
	 * Input Stream with unsafe close 
	 */
	@Deprecated
	public static InputStream getHttpPOSTInputStream(String urlStr, String params,ContentResolver cr )
			throws Exception {
		InputStream is = null;
		OutputStream os = null;
		HttpURLConnection conn = null;

		if (urlStr.startsWith("content://"))
			return getContentInputStream(urlStr, params,cr);

		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);

			if (params != null) {
				conn.setDoOutput(true);
				os = conn.getOutputStream();
				OutputStreamWriter wr = new OutputStreamWriter(os);
				wr.write(params);
				wr.close();
			}

			is = conn.getInputStream();

			return is;
		} catch (Exception ex) {

			try {
				is.close();
			} catch (Exception ignore) {

			}
			try {
				os.close();
			} catch (Exception ignore) {

			}
			try {
				conn.disconnect();
			} catch (Exception ignore) {
			}

			if (conn != null && conn.getResponseCode() == 405) {
				return getHttpGETInputStream(urlStr,cr);
			} else {
				throw ex;
			}
		}
	}

	/**
	 * Input Stream with unsafe close 
	 */
	@Deprecated
	public static InputStream getContentInputStream(String urlStr, String params,ContentResolver cr)
			throws Exception {
		//ContentResolver cr = mixView.getContentResolver();
		Cursor cur = cr.query(Uri.parse(urlStr), null, params, null, null);

		cur.moveToFirst();
		int mode = cur.getInt(cur.getColumnIndex("MODE"));

		if (mode == 1) {
			String result = cur.getString(cur.getColumnIndex("RESULT"));
			cur.deactivate();

			return new ByteArrayInputStream(result.getBytes());
		} else {
			cur.deactivate();

			throw new Exception("Invalid content:// mode " + mode);
		}
	}

	/**
	 * Input Stream management not safe  
	 */
	@Deprecated
	public static void returnHttpInputStream(InputStream is) throws Exception {
		if (is != null) {
			is.close();
		}
	}


	/**
	 * Input Stream management not safe  
	 */
	@Deprecated
	public InputStream getResourceInputStream(String name,AssetManager mgr) throws Exception {
		//AssetManager mgr = mixView.getAssets();
		return mgr.open(name);
	}


	/**
	 * Input Stream management not safe  
	 */
	@Deprecated
	public static void returnResourceInputStream(InputStream is) throws Exception {
		if (is != null)
			is.close();
	}


}
