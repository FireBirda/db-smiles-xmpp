package com.digitalbuana.smiles.data.friends;

public class StickerPackageModel {

	private long id;
	private String packageID;
	private String name;
	private String description;
	private String price;
	private String thumbnail;
	private boolean allowUse;
	private boolean isSave;
	
	public StickerPackageModel(
			Long id,
			String packageId,
			String name,
			String desctiption,
			String price,
			String thumbnail,
			boolean allowUse
			)
	
	{
		this.id=id;
		this.packageID=packageId;
		this.name=name;
		this.description=desctiption;
		this.price=price;
		this.thumbnail=thumbnail;
		this.allowUse=allowUse;
	}
	

	public Long getID(){
		return id;
	}
	public String getPackageID(){
		return packageID;
	}
	public String getname(){
		return name;
	}
	public String getDescription(){
		return description;
	}
	public String getPrice(){
		return price;
	}
	public String getThumbnail(){
		return thumbnail;
	}
	public boolean getAllowUse(){
		return allowUse;
	}
	public boolean getIsSave(){
		return isSave;
	}
	public void setIsSave(boolean isSave){
		this.isSave=isSave;
	}
}
