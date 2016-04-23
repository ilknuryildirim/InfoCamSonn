/*
 * Copyright (C) 2012- Peer internet solutions & Finalist IT Group
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
package com.infocam.lib.marker;

import com.infocam.lib.MixContextInterface;
import com.infocam.lib.MixStateInterface;
import com.infocam.lib.gui.Label;
import com.infocam.lib.gui.PaintScreen;
import com.infocam.lib.marker.draw.ParcelableProperty;
import com.infocam.lib.marker.draw.PrimitiveProperty;
import com.infocam.lib.render.Camera;
import com.infocam.lib.render.MixVector;

import android.location.Location;

/**
 * The marker interface.
 * @author A. Egal
 */
public interface Marker extends Comparable<Marker>{

	String getTitle();

	String getURL();

	double getLatitude();

	double getLongitude();

	double getAltitude();

	MixVector getLocationVector();

	void update(Location curGPSFix);

	void calcPaint(Camera viewCam, float addX, float addY);

	void draw(PaintScreen dw);

	double getDistance();

	void setDistance(double distance);

	String getID();

	void setID(String iD);

	boolean isActive();

	void setActive(boolean active);

	int getColour();
	
	public void setTxtLab(Label txtLab);

	Label getTxtLab();

	public boolean fClick(float x, float y, MixContextInterface ctx, MixStateInterface state);

	int getMaxObjects();
	
	void setExtras(String name, ParcelableProperty parcelableProperty);
	
	void setExtras(String name, PrimitiveProperty primitiveProperty);

}