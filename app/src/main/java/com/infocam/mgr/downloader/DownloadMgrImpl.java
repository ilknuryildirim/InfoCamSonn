/*
 * Copyright (C) 2010- Peer internet solutions
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
package com.infocam.mgr.downloader;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.infocam.MixContext;
import com.infocam.MixView;
import com.infocam.data.convert.DataConvertor;
import com.infocam.lib.marker.Marker;
import com.infocam.mgr.HttpTools;

import android.util.Log;

class DownloadMgrImpl implements Runnable, DownloadManager {

	private boolean stop = false;
	private MixContext ctx;
	private DownloadManagerState state = DownloadManagerState.Confused;
	private LinkedBlockingQueue<ManagedDownloadRequest> todoList = new LinkedBlockingQueue<ManagedDownloadRequest>();
	private ConcurrentHashMap<String, DownloadResult> doneList = new ConcurrentHashMap<String, DownloadResult>();
	private Executor executor = Executors.newSingleThreadExecutor();
	

	public DownloadMgrImpl(MixContext ctx) {
		if (ctx == null) {
			throw new IllegalArgumentException("Mix Context IS NULL");
		}
		this.ctx = ctx;
		state=DownloadManagerState.OffLine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infocam.mgr.downloader.DownloadManager#run()
	 */
	@Override
	public void run() {
		ManagedDownloadRequest mRequest;
		DownloadResult result;
		Log.v("dmgrimp", "dmgrrun calısıyor");
		stop = false;
		while (!stop) {
			Log.v("dmgrimp", "stop hep false");
			state=DownloadManagerState.OnLine;
			// Wait for proceed
			while (!stop) {
				try {
					mRequest = todoList.take();
					Log.v("dmgrimp", mRequest.toString());
					state=DownloadManagerState.Downloading;
					Log.v("dmgrimp", state.toString());
					result = processRequest(mRequest);
					Log.v("dmgrimp", result.toString());
				} catch (InterruptedException e) {
					Log.v("downmanager", "interrupted exception");
					result = new DownloadResult();
					result.setError(e, null);
				}
				doneList.put(result.getIdOfDownloadRequest(), result);
				state=DownloadManagerState.OnLine;
			}
		}
		state=DownloadManagerState.OffLine;
	}

	private DownloadResult processRequest(ManagedDownloadRequest mRequest) {
		DownloadRequest request = mRequest.getOriginalRequest();
		final DownloadResult result = new DownloadResult();
		try {
			if (request == null) {
				throw new Exception("Request is null");
			}
			
			if (!request.getSource().isWellFormed()) {
				throw new Exception("Datasource in not WellFormed");
			}
			Log.v("pppppp", "getPagecontent çağrıldı");
			String pageContent = HttpTools.getPageContent(request,
					ctx.getContentResolver());

			if (pageContent != null) {
				Log.v("ttttttt", pageContent);
				// try loading Marker data
				List<Marker> markers = DataConvertor.getInstance().load(
						request.getSource().getUrl(), pageContent,
						request.getSource());
				result.setAccomplish(mRequest.getUniqueKey(), markers,
						request.getSource());
			}
			else
				Log.v("dmgrimp", "pagecontent null");
		} catch (Exception ex) {
			result.setError(ex, request);
			Log.w(MixContext.TAG, "ERROR ON DOWNLOAD REQUEST", ex);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infocam.mgr.downloader.DownloadManager#purgeLists()
	 */
	public synchronized void resetActivity() {
		todoList.clear();
		doneList.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.infocam.mgr.downloader.DownloadManager#submitJob(com.infocam.mgr.downloader
	 * .DownloadRequest)
	 */
	public String submitJob(DownloadRequest job) {
		String jobId = null;
		Log.v("dmgr", "submit job çağrıldı." + job.toString());
		if (job != null && job.getSource().isWellFormed()) {
			ManagedDownloadRequest mJob;
			if (!todoList.contains(job)) {
				mJob = new ManagedDownloadRequest(job);
				todoList.add(mJob);
				Log.i(MixView.TAG, "Submitted " + job.toString());
				jobId = mJob.getUniqueKey();
			}
		}
		else
			Log.v("dmgrimp", "job null");
		return jobId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.infocam.mgr.downloader.DownloadManager#getReqResult(java.lang.String)
	 */
	public DownloadResult getReqResult(String jobId) {
		DownloadResult result = doneList.get(jobId);
		doneList.remove(jobId);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infocam.mgr.downloader.DownloadManager#getNextResult()
	 */
	public synchronized DownloadResult getNextResult() {
		DownloadResult result = null;
		if (!doneList.isEmpty()) {
			String nextId = doneList.keySet().iterator().next();
			result = doneList.get(nextId);
			doneList.remove(nextId);
		}
		else
			Log.v("dmgrimp", "doneList empty");
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infocam.mgr.downloader.DownloadManager#getResultSize()
	 */
	public int getResultSize(){
		return doneList.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infocam.mgr.downloader.DownloadManager#isDone()
	 */
	public Boolean isDone() {
		return todoList.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infocam.mgr.downloader.DownloadManager#goOnline()
	 */
	public void switchOn() {
		if (DownloadManagerState.OffLine.equals(getState())){
			Log.v("dmgr", "executor basladi");
		    executor.execute(this);


		}else{
			Log.i(MixView.TAG, "DownloadManager already started");
		}
	}

	public void switchOff() {
		Log.i("qwer", "switch off yapıldı");
		stop=true;
	}

	@Override
	public DownloadManagerState getState() {
		return state;
	}

	

}
