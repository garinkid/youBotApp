/****************************************************************
 *
 * Copyright (c) 2011
 * All rights reserved.
 *
 * Hochschule Bonn-Rhein-Sieg
 * University of Applied Sciences
 * Computer Science Department
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * Author : Rhama Dwiputra
 * Contributor : Azamat Shakhimardanov
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * This sofware is published under a dual-license: GNU Lesser General Public 
 * License LGPL 2.1 and ASL2.0 license. The dual-license implies that users of this
 * code may choose which terms they prefer.
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Hochschule Bonn-Rhein-Sieg nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License LGPL as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version or the ASL2.0 license.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License LGPL and the ASL2.0 license for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License LGPL and ASL2.0 license along with this program.
 *
 ****************************************************************/

package com.youbot.app;

import android.view.View;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.content.Context;

public class ViewCartesian extends View{


			private float center_x;
			private float center_y;
			private float lineDistance;
			private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			
			public ViewCartesian(Context context){
				super(context);
			}
			
			@Override 
			//create a circular view for controller
			protected void onDraw(Canvas canvas){
				super.onDraw(canvas);
				center_x = getHeight() / 2;
				center_y = getWidth() / 2;
				float distance_x = getHeight();
				float distance_y = getWidth();
				
				int numberOfLines = 8;
				float space = getHeight() / numberOfLines;
				
				this.paint.setColor(Color.WHITE);
				canvas.drawCircle(center_x , center_y , center_x / 14, paint);
				canvas.drawLine(0, center_y, distance_x, center_y, paint);
				canvas.drawLine(center_x, 0, center_x, distance_y, paint);
				
				
				this.paint.setColor(Color.GRAY);
				for(int i = 1; i< numberOfLines; i++){
					float xy = space * (i);
					canvas.drawLine(0, xy, distance_x, xy, paint);
					canvas.drawLine(xy, 0 , xy , distance_y, paint);
				}
				
				/*
				canvas.drawLine(center_x + lineDistance, center_y, center_x * 2 - lineDistance, center_y, paint);
				canvas.drawLine(center_x, 0 + lineDistance, center_x, center_y - lineDistance, paint);
				canvas.drawLine(center_x, center_y + lineDistance, center_x, center_y * 2  - lineDistance, paint);
				*/
			}
			
}

