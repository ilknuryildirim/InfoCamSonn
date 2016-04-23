/*
 * Copyleft 2012 - Peer internet solutions 
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
package com.infocam.mgr.datasource;

import android.util.Log;

import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.infocam.MixContext;
import com.infocam.data.DataSource;
import com.infocam.data.DataSourceStorage;
import com.infocam.mgr.downloader.DownloadRequest;

class DataSourceMgrImpl implements DataSourceManager {

	private final ConcurrentLinkedQueue<DataSource> allDataSources=new ConcurrentLinkedQueue<DataSource>(); 
	
	private final MixContext ctx;

	public DataSourceMgrImpl(MixContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public boolean isAtLeastOneDatasourceSelected() {
		boolean atLeastOneDatasourceSelected = false;
		for (DataSource ds : this.allDataSources) {
			if (ds.getEnabled())
				atLeastOneDatasourceSelected = true;
		}
		return atLeastOneDatasourceSelected;
	}



	public void setAllDataSourcesforLauncher(DataSource datasource) {
		this.allDataSources.clear(); // TODO WHY? CLEAN ALL
		this.allDataSources.add(datasource);
	}

	public void refreshDataSources() {
		this.allDataSources.clear();

		DataSourceStorage.getInstance(ctx).fillDefaultDataSources();

		int size = DataSourceStorage.getInstance().getSize();

		// copy the value from shared preference to adapter
		for (int i = 0; i < size; i++) {
			String fields[] = DataSourceStorage.getInstance().getFields(i);
			this.allDataSources.add(new DataSource(fields[0], fields[1],
					fields[2], fields[3], fields[4]));
		}
	}

	public void requestDataFromAllActiveDataSource(double lat, double lon,
			double alt, float radius) {
		Log.i("takip", "dmgr.reqdata from all active");
		for (DataSource ds : allDataSources) {
			Log.i("takipuniq", "dmgr.reqdata from all active ds var");
			/*
			 * when type is OpenStreetMap iterate the URL list and for selected
			 * URL send data request
			 */
			if (ds.getEnabled()) {
				requestData(ds, lat, lon, alt, radius, Locale.getDefault()
						.getLanguage());
			}
		}

	}

	private void requestData(DataSource datasource, double lat, double lon,
			double alt, float radius, String locale) {
		Log.v("Datasource", "reques data " + lat + " " + lon);
		DownloadRequest request = new DownloadRequest(datasource,
				datasource.createRequestParams(lat, lon, alt, radius, locale));
		ctx.getDownloadManager().submitJob(request);

	}

}
