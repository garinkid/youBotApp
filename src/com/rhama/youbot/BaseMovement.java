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

package com.rhama.youbot;

public class BaseMovement {
	double longitudinalVelocity;
	double transversalVelocity;
	double angularVelocity;
	
	
	public void addLongitudinal(){
		if(this.longitudinalVelocity < 76){
			this.longitudinalVelocity = this.longitudinalVelocity + 25;
		}
	}
	
	public void reduceLongitudinal(){
		if(this.longitudinalVelocity > -76){
			this.longitudinalVelocity = this.longitudinalVelocity - 25; 
		}
	}
	
	public void addTransversal(){
		if(this.transversalVelocity < 76){
			this.transversalVelocity = this.transversalVelocity + 25;
		}
	}
	
	public void reduceTransversal(){
		if(this.transversalVelocity > -76){
			this.transversalVelocity = this.transversalVelocity - 25; 
		}
	}
	
	public void setStop(){
		this.longitudinalVelocity = 0 ;
		this.transversalVelocity = 0 ;
		this.angularVelocity = 0 ;
	}
	
	public void setTransversal(double transversalValue){
		this.transversalVelocity = (double)transversalValue;
	}

	public void setAngular(double angularValue){
		this.angularVelocity = (double)angularValue;
	}

	public void setLongitudinal(double longitudinalValue){
	this.longitudinalVelocity = (double) longitudinalValue;
	}		

	
}
