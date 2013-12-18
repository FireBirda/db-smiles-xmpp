///**
// * Copyright (c) 2013, Redsolution LTD. All rights reserved.
// * 
// * This file is part of Xabber project; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License, Version 3.
// * 
// * Xabber is distributed in the hope that it will be useful, but
// * WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// * See the GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License,
// * along with this program. If not, see http://www.gnu.org/licenses/.
// */
//package com.digitalbuana.smiles.data.extension.filetransfer;
//
//import java.io.File;
//
//import org.jivesoftware.smack.Connection;
//import org.jivesoftware.smack.XMPPException;
//import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
//import org.jivesoftware.smackx.filetransfer.FileTransferListener;
//import org.jivesoftware.smackx.filetransfer.FileTransferManager;
//import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
//import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
//import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
//import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
//
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.media.AudioManager;
//import android.net.Uri;
//import android.os.Environment;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.app.NotificationCompat.Builder;
//import android.util.Log;
//
//import com.digitalbuana.smiles.R;
//import com.digitalbuana.smiles.data.AppConstants;
//import com.digitalbuana.smiles.data.Application;
//import com.digitalbuana.smiles.data.OnLoadListener;
//import com.digitalbuana.smiles.data.SettingsManager;
//import com.digitalbuana.smiles.data.account.AccountManager;
//import com.digitalbuana.smiles.data.message.ChatAction;
//import com.digitalbuana.smiles.data.message.MessageManager;
//import com.digitalbuana.smiles.data.notification.EntityNotificationProvider;
//import com.digitalbuana.smiles.data.notification.NotificationManager;
//import com.digitalbuana.smiles.data.roster.PresenceManager;
//import com.digitalbuana.smiles.ui.adapter.ChatMessageAdapter;
//import com.digitalbuana.smiles.utils.StringUtils;
//import com.digitalbuana.smiles.xmpp.address.Jid;
//
//public class FileTransferKuManager
//implements
//OnLoadListener,
//FileTransferListener
//{
//
//	private final static FileTransferKuManager instance;
//	private static FileTransferManager filesTransfer;
//	final Builder mBuilder;
//	
//	static {
//		instance = new FileTransferKuManager();
//		Application.getInstance().addManager(instance);
//	}
//
//	public static FileTransferKuManager getInstance() {
//		return instance;
//	}
//
//	private final EntityNotificationProvider<FileTransferKuRequest> attentionRequestProvider = new EntityNotificationProvider<FileTransferKuRequest>(
//			R.drawable.ic_stat_attention) {
//
//		@Override
//		public Uri getSound() {
//			return SettingsManager.chatsAttentionSound();
//		}
//
//		@Override
//		public int getStreamType() {
//			return AudioManager.STREAM_NOTIFICATION;
//		}
//	};
//
//	public FileTransferKuManager() {
//		mBuilder = new NotificationCompat.Builder(Application.getInstance().getApplicationContext());
//	}
//	
//	public void removeFileTransferManager(){
//		filesTransfer.removeFileTransferListener(this);
//		filesTransfer = null;
//	}
//	
//	public void setFileTransferManager(Connection connection){
//		Log.e(AppConstants.TAG,"Creating FileManager");
//		filesTransfer = new FileTransferManager(connection);
//		FileTransferNegotiator.setServiceEnabled(connection, true);
//		filesTransfer.addFileTransferListener(this);
//	}
//	
//	public FileTransferManager getFileTransferManager(){
//		return filesTransfer;
//	}
//	
//	@Override
//	public void onLoad() {
//		NotificationManager.getInstance().registerNotificationProvider(attentionRequestProvider);
//	}
//
//	public void removeAccountNotifications(String account, String user) {
//		attentionRequestProvider.remove(account, user);
//	}
//
//
//	public void sendFile(final String jid, final String filePath, final String description){
//		mBuilder.setContentIntent(null);
//		Application.getInstance().runInBackground(new Runnable() {
//			
//			@Override
//			public void run() {
//				String sendToUser = jid;
//				String account = AccountManager.getInstance().getAccountKu();
//				String vurbose = PresenceManager.getInstance().getVurbose(account, jid);
//				if(sendToUser!=""){
//					sendToUser = vurbose;
//				} else {
//					sendToUser = sendToUser+"/SmilesAndroid";
//				}
//				OutgoingFileTransfer transfer = filesTransfer.createOutgoingFileTransfer(sendToUser);
//				
//				File file = new File(filePath);
//				mBuilder.setContentTitle("Sending File.. ").setContentText(file.getName()).setSmallIcon(R.drawable.ic_launcher);
//				
//				try {
//					transfer.sendFile(file, description);
//				} catch (XMPPException e) {
//				   e.printStackTrace();
//				}
//				while(!transfer.isDone()) {
//					double prog = (double)transfer.getProgress()*100;
//					int progInt = (int)prog;
//					mBuilder.setContentTitle(progInt+" % Sending File..  ").setContentText(file.getName()).setSmallIcon(R.drawable.ic_launcher);
//					mBuilder.setProgress(100, progInt, false);
//					NotificationManager.getInstance().notify(0, mBuilder.build());
//					Log.e(AppConstants.TAG, "Progress Send : "+prog);
//				   if(transfer.getStatus().equals(Status.error)) {
////					   Log.e(AppConstants.TAG,"File 1:"+transfer.getError());
//					   mBuilder.setContentTitle("Sending Error..").setContentText(file.getName()).setSmallIcon(R.drawable.ic_launcher);
//					   mBuilder.setProgress(0, 0, false);
//					   NotificationManager.getInstance().notify(0, mBuilder.build());
//				   } else if (transfer.getStatus().equals(Status.cancelled) || transfer.getStatus().equals(Status.refused)) {
////					   Log.e(AppConstants.TAG,"File 2:"+transfer.getError());
//					   mBuilder.setContentTitle("Sending Failed..").setContentText(file.getName()).setSmallIcon(R.drawable.ic_launcher);
//					   mBuilder.setProgress(0, 0, false);
//					   NotificationManager.getInstance().notify(0, mBuilder.build());
//				   }
//				   try {
//				      Thread.sleep(1000);
//				   } catch (InterruptedException e) {
//				      e.printStackTrace();
//				   }
//				  
//				   if(transfer.getStatus().equals(Status.refused) || transfer.getStatus().equals(Status.error) || transfer.getStatus().equals(Status.cancelled)){
////				     Log.e(AppConstants.TAG,"File 3:"+transfer.getError());
//				     transfer.cancel();
//				     mBuilder.setContentTitle("Sending Failed..").setContentText(file.getName()).setSmallIcon(R.drawable.ic_launcher);
//					 mBuilder.setProgress(0, 0, false);
//					 NotificationManager.getInstance().notify(0, mBuilder.build());
//				  } else  if(transfer.getStatus().equals(Status.complete)){
//					  mBuilder.setContentTitle("Sending complete..").setContentText(file.getName()).setSmallIcon(R.drawable.ic_launcher);
//					  mBuilder.setProgress(0, 0, false);
//					  NotificationManager.getInstance().notify(0, mBuilder.build());
//					  openMedia(file.getAbsolutePath(),ChatMessageAdapter.TYPE_IMAGE);
//				  }else {
////				     Log.e(AppConstants.TAG,"File 4: Succes Send");
//
//				  }
//				}
//			}
//		});
//		
//	}
//	
//	@Override
//	public void fileTransferRequest(final FileTransferRequest request) {
//		mBuilder.setContentIntent(null);
//		Application.getInstance().runInBackground(new Runnable() {
//			@Override
//			public void run() {
//				
//				IncomingFileTransfer transfer = request.accept();
//				String account = AccountManager.getInstance().getAccountKu();
//				String jid =  Jid.getBareAddress(request.getRequestor());
//				File folderSmiles = new File(AppConstants.FileSavedURL);
//	            if (!folderSmiles.exists()) {
//	            	folderSmiles.mkdir();
//	            	folderSmiles.mkdirs();
//	            }
//	            File file = new File(folderSmiles, transfer.getFileName());
//	            Log.e(AppConstants.TAG, file.getAbsolutePath());
//	            
//	            try{
//	                transfer.recieveFile(file);
//	                while(!transfer.isDone()) {
//	                	double prog = (double)transfer.getProgress()*100;
//						int progInt = (int)prog;
//					    int file_size = Integer.parseInt(String.valueOf(file.length()/1024));
//						mBuilder.setContentTitle(progInt+" %  "+"Receive File from "+StringUtils.replaceStringEquals(account)).setContentText(file.getName()+"   "+file_size+"Byte").setSmallIcon(R.drawable.ic_launcher);
//						mBuilder.setProgress(100, progInt, false);
//						NotificationManager.getInstance().notify(0, mBuilder.build());
////	                	Log.e(AppConstants.TAG, "Progress Receive : "+prog);
//	                   try{
//	                      Thread.sleep(1000L);
//	                   }catch (Exception e) {
//	                      Log.e(AppConstants.TAG, e.getMessage());
//	                   }
//	                   if(transfer.getStatus().equals(Status.error)) {
//	                      Log.e(AppConstants.TAG, transfer.getError() + "");
//	                   }
//	                   if(transfer.getException() != null) {
//	                      transfer.getException().printStackTrace();
//	                   }
//	                   
//	                   if(transfer.getStatus().equals(Status.refused) || transfer.getStatus().equals(Status.error) || transfer.getStatus().equals(Status.cancelled)){
////	  				     Log.e(AppConstants.TAG,"File 3:"+transfer.getError());
//	                	   transfer.cancel();
//	                	   mBuilder.setContentTitle("Error Recive File from "+StringUtils.replaceStringEquals(jid)).setContentText(file.getName()).setSmallIcon(R.drawable.ic_launcher);
//			   				 mBuilder.setProgress(0, 0, false);
//			   				 NotificationManager.getInstance().notify(0, mBuilder.build());
//			   				 openMedia(file.getAbsolutePath(),ChatMessageAdapter.TYPE_IMAGE);
//	  				  } else  if(transfer.getStatus().equals(Status.complete)){
//	  					MessageManager.getInstance().sendMessage(
//								account,
//								jid,
//								"F1L3K03@TYPE:IMAGE/DESC:"
//								+request.getDescription()
//								+"/URL:"+request.getFileName()
//								);
//	   				   mBuilder.setContentTitle("Finish Receive File from "+StringUtils.replaceStringEquals(jid)).setContentText(file.getName()).setSmallIcon(R.drawable.ic_launcher);
//	   				   mBuilder.setProgress(0, 0, false);
//	   				   NotificationManager.getInstance().notify(0, mBuilder.build());
//	  				  }else {
////	  				     Log.e(AppConstants.TAG,"File 4: Succes Send");
//
//	  				  }
//	                } 
//	             }catch (Exception e) {
//	                Log.e(AppConstants.TAG, e.getMessage());
//	            }
//			}
//		});
//	}
//	private void openMedia(String path, int type){
//		Intent newIntent = new Intent(android.content.Intent.ACTION_VIEW);
//		Log.e(AppConstants.TAG, "Opening : "+path);
//		if(type==ChatMessageAdapter.TYPE_IMAGE){
//			File file = new File(path);  
//			newIntent.setDataAndType(Uri.fromFile(file),"image/*");
//			Application.getInstance().startActivity(newIntent);
//		}
//		PendingIntent intent =PendingIntent.getActivity(Application.getInstance().getApplicationContext(), 0, newIntent, 0);
//		mBuilder.setContentIntent(intent);
//		NotificationManager.getInstance().notify(0, mBuilder.build());
//	}
//
//
//}
