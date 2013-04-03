package eu.novi.im.core.impl;

import java.math.BigInteger;

import eu.novi.im.core.CPU;

public class CPUImpl extends NodeComponentImpl implements CPU {
	
	private BigInteger hasAvailableCores;
	private Float hasCPUSpeed;
	private Float hasCPUtil;
	private BigInteger hasCores;
	
	public CPUImpl(String uri)
	{
		super(uri);
	}

	@Override
	public BigInteger getHasAvailableCores() {
		return this.hasAvailableCores;
	}

	@Override
	public void setHasAvailableCores(BigInteger hasAvailableCores) {
		this.hasAvailableCores = hasAvailableCores;

	}

	@Override
	public Float getHasCPUSpeed() {
		return this.hasCPUSpeed;
	}

	@Override
	public void setHasCPUSpeed(Float hasCPUSpeed) {
		this.hasCPUSpeed = hasCPUSpeed;

	}

	@Override
	public BigInteger getHasCores() {
		return this.hasCores;
	}

	@Override
	public void setHasCores(BigInteger hasCores) {
		this.hasCores = hasCores;

	}

	@Override
	public Float getHasCPUtil() {
		return this.hasCPUtil;
	}

	@Override
	public void setHasCPUtil(Float hasCPUtil) {
		this.hasCPUtil = hasCPUtil;
		
	}

}
