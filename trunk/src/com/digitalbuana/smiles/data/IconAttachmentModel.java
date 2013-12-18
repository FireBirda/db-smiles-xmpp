package com.digitalbuana.smiles.data;

public class IconAttachmentModel {
	String title;
	int resource;
	
	public IconAttachmentModel(String title, int resource)
	{
		this.title=title;
		this.resource = resource;
	}
	
	public String getTitle(){
		return title;
	}
	public int getResource(){
		return resource;
	}
}
