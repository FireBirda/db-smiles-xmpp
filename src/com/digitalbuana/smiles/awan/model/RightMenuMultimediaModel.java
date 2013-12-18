package com.digitalbuana.smiles.awan.model;

public class RightMenuMultimediaModel {
	
	private String title;
	private String icon;
	private String type;
	private String detail;
	private String parm;
	
	public RightMenuMultimediaModel(String title, String icon, String type, String detail, String parm){
		this.title = title;
		this.icon = icon;
		this.type = type;
		this.detail = detail;
		this.parm = parm;
	}
	public RightMenuMultimediaModel(){}
	
	public void setTitle(String _title){
		this.title = _title;
	}
	public void setIcon(String _icon){
		this.icon = _icon;
	}
	public void setType(String _type){
		this.type = _type;
	}
	public void setDetail(String _detail){
		this.detail = _detail;
	}
	public void setParm(String _parm){
		this.parm = _parm;
	}
	
	public String getTitle(){
		return this.title;
	}
	public String getIcon(){
		return this.icon;
	}
	public String getType(){
		return this.type;
	}
	public String getDetail(){
		return this.detail;
	}
	public String getParm(){
		return this.parm;
	}
	
}
