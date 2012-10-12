/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright © 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.orangelabs.rcs.service.api.server.messaging;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSessionListener;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.provider.messaging.RichMessaging;
import com.orangelabs.rcs.service.api.client.eventslog.EventsLogApi;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferSession;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * File transfer session
 * 
 * @author jexa7410
 */
public class FileTransferSession extends IFileTransferSession.Stub implements ContentSharingTransferSessionListener {
	
	/**
	 * Core session
	 */
	private ContentSharingTransferSession session;

	/**
	 * File has been transfered
	 */
	private boolean transfered = false;
	
	/**
	 * List of listeners
	 */
	private RemoteCallbackList<IFileTransferEventListener> listeners = new RemoteCallbackList<IFileTransferEventListener>();

    /**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param session Session
	 */
	public FileTransferSession(ContentSharingTransferSession session) {
		this.session = session;
		session.addListener(this);
	}

	/**
	 * Get session ID
	 * 
	 * @return Session ID
	 */
	public String getSessionID() {
		return session.getSessionID();
	}
	
	/**
	 * Get remote contact
	 * 
	 * @return Contact
	 */
	public String getRemoteContact() {
		return session.getRemoteContact();
	}
	
	/**
	 * Get session state
	 * 
	 * @return State (-1: not started, 0: pending, 1: canceled, 2: established, 3: terminated) 
	 */
	public int getSessionState() {
		return session.getSessionState();
	}
	
	/**
     * Get filename
     *
     * @return Filename
     */
	public String getFilename() {
		return session.getContent().getName();
	}

	/**
     * Get file size
     *
     * @return Size in bytes
     */
	public long getFilesize() {
		return session.getContent().getSize();
	}	
	
	/**
	 * Accept the session invitation
	 */
	public void acceptSession() {
		if (logger.isActivated()) {
			logger.info("Accept session invitation");
		}
		
		// Accept invitation
		session.acceptSession();

	}
	
	/**
	 * Reject the session invitation
	 */
	public void rejectSession() {
		if (logger.isActivated()) {
			logger.info("Reject session invitation");
		}
		
		// Update rich messaging history
  		RichMessaging.getInstance().updateFileTransferStatus(session.getSessionID(), EventsLogApi.STATUS_FAILED);

  		// Reject invitation
		session.rejectSession(603);
	}

	/**
	 * Cancel the session
	 */
	public void cancelSession() {
		if (logger.isActivated()) {
			logger.info("Cancel session");
		}
		
		// Abort the session
		session.abortSession();

		// Update rich messaging history
  		RichMessaging.getInstance().updateFileTransferStatus(session.getSessionID(), EventsLogApi.STATUS_FAILED);
	}
	
	/**
	 * Add session listener
	 * 
	 * @param listener Listener
	 */
	public void addSessionListener(IFileTransferEventListener listener) {
		if (logger.isActivated()) {
			logger.info("Add an event listener");
		}

		listeners.register(listener);
	}
	
	/**
	 * Remove session listener
	 * 
	 * @param listener Listener
	 */
	public void removeSessionListener(IFileTransferEventListener listener) {
		if (logger.isActivated()) {
			logger.info("Remove an event listener");
		}

		listeners.unregister(listener);
	}
	
	/**
	 * Session is started
	 */
    public void handleSessionStarted() {
		if (logger.isActivated()) {
			logger.info("Session started");
		}

  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleSessionStarted();
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }

    /**
     * Session has been aborted
     */
    public void handleSessionAborted() {
		if (logger.isActivated()) {
			logger.info("Session aborted");
		}

		// Update rich messaging history
 		RichMessaging.getInstance().updateFileTransferStatus(session.getSessionID(), EventsLogApi.STATUS_FAILED);
		
  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleSessionAborted();
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();
        
        // Remove session from the list
        MessagingApiService.removeFileTransferSession(session.getSessionID());
    }
   
    /**
     * Session has been terminated by remote
     */
    public void handleSessionTerminatedByRemote() {
		if (logger.isActivated()) {
			logger.info("Session terminated by remote");
		}

  		// Notify listener only if the transfer has been aborted before the end of transfer 
  		if (!transfered) {  		
  			// Update rich messaging history
  	  		RichMessaging.getInstance().updateFileTransferStatus(session.getSessionID(), EventsLogApi.STATUS_FAILED);

  	  		// Notify event listeners
			final int N = listeners.beginBroadcast();
	        for (int i=0; i < N; i++) {
	            try {
	            	listeners.getBroadcastItem(i).handleSessionTerminatedByRemote();
	            } catch (RemoteException e) {
	            	if (logger.isActivated()) {
	            		logger.error("Can't notify listener", e);
	            	}
	            }
	        }
	        listeners.finishBroadcast();
  		}

        // Remove session from the list
        MessagingApiService.removeFileTransferSession(session.getSessionID());
    }
	
    /**
     * Content sharing error
     * 
     * @param error Error
     */
    public void handleSharingError(ContentSharingError error) {
		if (logger.isActivated()) {
			logger.info("Sharing error");
		}

		// Update rich messaging history
  		RichMessaging.getInstance().updateFileTransferStatus(session.getSessionID(), EventsLogApi.STATUS_FAILED);
		
  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleTransferError(error.getErrorCode());
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();
        
        // Remove session from the list
        MessagingApiService.removeFileTransferSession(session.getSessionID());
    }

    /**
	 * Content sharing progress
	 * 
	 * @param currentSize Data size transfered 
	 * @param totalSize Total size to be transfered
	 */
    public void handleSharingProgress(long currentSize, long totalSize) {
		if (logger.isActivated()) {
			logger.debug("Sharing progress");
		}
		
		// Update rich messaging history
  		RichMessaging.getInstance().updateFileTransferDownloadedSize(session.getSessionID(), currentSize, totalSize);
		
  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleTransferProgress(currentSize, totalSize);
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
     }
    
    /**
     * Content has been transfered
     * 
     * @param filename Filename associated to the received content
     */
    public void handleContentTransfered(String filename) {
		if (logger.isActivated()) {
			logger.info("Content transfered");
		}

		// Update transfer status
		transfered = true;
		
		// Update rich messaging history
		RichMessaging.getInstance().updateFileTransferUrl(session.getSessionID(), filename);

  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleFileTransfered(filename);
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }	
}