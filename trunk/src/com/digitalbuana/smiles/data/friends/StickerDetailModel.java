package com.digitalbuana.smiles.data.friends;

public class StickerDetailModel {

	private Long id;
	private String stickerID;
	private String name;
	private String url;
	
	public StickerDetailModel(
			Long id,
			String stickerID,
			String name,
			String url
			){
		this.id=id;
		this.stickerID=stickerID;
		this.name = name;
		this.url = url;
	}
	
	public Long getID(){
		return id;
	}
	public String getStickerID(){
		return stickerID;
	}
	public String getname(){
		return name;
	}
	public String getURL(){
		return url;
	}
}
