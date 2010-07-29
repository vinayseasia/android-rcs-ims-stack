/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
 * 
 * Copyright � 2010 France Telecom S.A.
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
package com.orangelabs.rcs.core.ims.service.capability;

import gov.nist.core.NameValue;
import gov.nist.core.sip.header.ContactHeader;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.Format;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.GeolocFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.VideoFormat;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaAttribute;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.SessionAuthenticationAgent;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.utils.MimeManager;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Capability service
 * 
 * @author jexa7410
 */
public class CapabilityService extends ImsService {
    /**
     * Default capabilities
     */
    private Capabilities localCapabilities = new Capabilities();

    /**
     * Additional capabilities
     */
    private Hashtable<String, String> otherCapabilities = new Hashtable<String, String>();
    
    /**
     * Image config
     */
    private String imageConfig;

    /**
     * Video config
     */
    private String videoConfig;

    /**
	 * Authentication agent
	 */
	private SessionAuthenticationAgent authenticationAgent = new SessionAuthenticationAgent();

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 * 
	 * @param parent IMS module
	 * @param activated Activation flag
	 * @throws CoreException
	 */
	public CapabilityService(ImsModule parent, boolean activated) throws CoreException {
		super(parent, "capabilities_service.xml", activated);
		
		// Read the default supported capabilities
		localCapabilities.setCsVideoSupport(getConfig().getBoolean("CsVideoCall", false));
		localCapabilities.setFileTransferSupport(getConfig().getBoolean("FileTransfer", false));
		localCapabilities.setImageSharingSupport(getConfig().getBoolean("ImageSharing", false));
		localCapabilities.setVideoSharingSupport(getConfig().getBoolean("VideoSharing", false));
		localCapabilities.setImSessionSupport(getConfig().getBoolean("ImSession", false));
		
		// Read the additional supported capabilities
		String others = getConfig().getString("OtherCapabilities", "");
		if (others.length() != 0) {
			String[] other = others.split(" ");
			for(int i=0; i < other.length; i++) {
				String key = other[i];
				otherCapabilities.put(key, key);
		    	if (logger.isActivated()) {
		    		logger.debug("Service " + key + " is supported");
		    	}
			}
		}
		
    	// Get supported MIME types
    	String supportedMimeTypes = ""; 
		for (Enumeration<String> e = MimeManager.getSupportedMimeTypes().elements() ; e.hasMoreElements() ;) {
			String mime = e.nextElement();
			if (supportedMimeTypes.indexOf(mime) == -1) {
				supportedMimeTypes += mime + " ";
			}
	    }
		supportedMimeTypes = supportedMimeTypes.trim();

    	// Get video config
    	Vector<VideoFormat> videoFormats = MediaRegistry.getSupportedVideoFormats();
    	videoConfig = "";
    	for(int i=0; i < videoFormats.size(); i++) {
    		VideoFormat fmt = videoFormats.elementAt(i);
    		videoConfig += "m=video 0 RTP/AVP " + fmt.getPayload() + SipUtils.CRLF;
    		videoConfig += "a=rtpmap:" + fmt.getPayload() + " " + fmt.getCodec() + SipUtils.CRLF;
    	}
    	
    	// Get the image config
    	imageConfig = "m=message 0 TCP/MSRP *"  + SipUtils.CRLF +
    		"a=accept-types:" + supportedMimeTypes + SipUtils.CRLF +
    		"a=max-size:" + ContentSharingTransferSession.MAX_CONTENT_SIZE + SipUtils.CRLF;    	
	}

	/**
	 * Start the IMS service
	 */
	public void start() {
	}

	/**
	 * Stop the IMS service 
	 */
	public void stop() {
	}
    
	/**
	 * Check the IMS service 
	 */
	public void check() {
	}

	/**
     * Returns the default capabilities
     * 
     * @return Capabilities
     */
	public Capabilities getDefaultCapabilities() {
		return localCapabilities;
	}

	/**
     * Send a capability request
     * 
     * @param contact Remote contact
     */
    public void requestCapability(String contact) {
    	if (logger.isActivated()) {
    		logger.info("Send an OPTIONS message to " + contact);                
    	}
    	
        try {
        	// Create a dialog path
        	String contactUri = PhoneUtils.formatNumberToSipAddress(contact);
        	SipDialogPath dialog = new SipDialogPath(
        					getImsModule().getSipManager().getSipStack(),
        					getImsModule().getSipManager().generateCallId(),
            				1,
            				contactUri,
            				ImsModule.IMS_USER_PROFILE.getNameAddress(),
            				contactUri,
            				getImsModule().getSipManager().getSipStack().getDefaultRoutePath());        	
        	
	        // Create the SIP request
        	if (logger.isActivated()) {
        		logger.info("Send first OPTIONS");
        	}
	        SipRequest options = SipMessageFactory.createOptions(dialog);
	        
	        // Send message
	        SipTransactionContext ctx = getImsModule().getSipManager().sendSipMessageAndWait(options);
	
	        // Wait response
        	if (logger.isActivated()) {
        		logger.info("Wait response");
        	}
	        ctx.waitResponse(SipManager.TIMEOUT);
	
	        // Analyze received message
            if (ctx.getStatusCode() == 407) {
                // 407 response received
            	if (logger.isActivated()) {
            		logger.info("407 response received");
            	}

    	        // Set the Proxy-Authorization header
            	authenticationAgent.readProxyAuthenticateHeader(ctx.getSipResponse());

                // Increment the Cseq number of the dialog path
                dialog.incrementCseq();

                // Send a second PUBLISH with the right token
                if (logger.isActivated()) {
                	logger.info("Send second OPTIONS");
                }
    	        options = SipMessageFactory.createOptions(dialog);
                
    	        // Set the Authorization header
                authenticationAgent.setProxyAuthorizationHeader(options);
                
                // Send message
    	        ctx = getImsModule().getSipManager().sendSipMessageAndWait(options);

                // Wait response
                if (logger.isActivated()) {
                	logger.info("Wait response");
                }
                ctx.waitResponse(SipManager.TIMEOUT);

                // Analyze received message
                if (ctx.getStatusCode() == 200) {
                    // 200 OK response
                	if (logger.isActivated()) {
                		logger.info("200 OK response received");
                	}

                	// Analyze response
                	parseCapabilities(contact, ctx.getSipResponse().getContent());
                } else {
                    // Error
                    if (logger.isActivated()) {
                    	logger.info("OPTIONS has failed");
                    }
                }
            } else if (ctx.getStatusCode() == 200) {
	            // 200 OK received
            	if (logger.isActivated()) {
            		logger.info("200 OK response received");
            	}
            	
            	// Analyse response
            	parseCapabilities(contact, ctx.getSipResponse().getContent());
	        } else {
	            // Error responses
            	if (logger.isActivated()) {
            		logger.info("No response received");
            	}
	        }
        } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("OPTIONS request has failed", e);
        	}
        }        
    }
    	
    /**
     * Receive a capability request
     * 
     * @param options Received options messsage
     */
    public void receiveCapabilityRequest(SipRequest options) {
    	if (logger.isActivated()) {
    		logger.info("Receive an OPTIONS request");
    	}

		// Notify the call manager
		getImsModule().getCore().getCallManager().handleReceiveCapabilities(options.getFromUri());		

		try {
	    	// Build the local SDP document
	    	String ipAddress = getImsModule().getCurrentNetworkInterface().getNetworkAccess().getIpAddress();
	    	String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
	        String localSdp = "v=0" + SipUtils.CRLF +
			           "o=" + ImsModule.IMS_USER_PROFILE.getUsername() + " "
							+ ntpTime + " " + ntpTime + " IN IP4 "
							+ ipAddress + SipUtils.CRLF +
			           "s=-" + SipUtils.CRLF +
			           "c=IN IP4 " + ipAddress + SipUtils.CRLF +
			           "t=0 0" + SipUtils.CRLF;

	        if (localCapabilities.isVideoSharingSupported()) {
		    	if (logger.isActivated()) {
		    		logger.debug("Video sharing supported");
		    	}
		    	localSdp += videoConfig;
	        }
	            
	        if (localCapabilities.isImageSharingSupported()) {
		    	if (logger.isActivated()) {
		    		logger.debug("Image sharing supported");
		    	}
		    	localSdp += imageConfig;
	        }
	                
	        if (otherCapabilities.containsKey("geoloc")) {
		    	if (logger.isActivated()) {
		    		logger.debug("Geoloc supported");
		    	}
	    		Format fmt = MediaRegistry.generateFormat(GeolocFormat.ENCODING);
	    		if (fmt != null) {
		    		String geoloc = "m=application 0 RTP/AVP " + fmt.getPayload() + SipUtils.CRLF +
		    			"a=rtpmap:" + fmt.getPayload() + " " + fmt.getCodec() + SipUtils.CRLF;
		    		localSdp += geoloc;
	    		}
	        }
	    	
	        // Create 200 OK response
	        SipResponse resp = SipMessageFactory.create200OkOptionsResponse(options, localSdp);
	        
			// Add a Contact header
	        ContactHeader contact = SipUtils.buildContactHeader(getImsModule().getSipManager().getSipStack());
	        contact.setParameter(new NameValue(SipUtils.FEATURE_3GPP_CS_VOICE, null));
	        contact.setParameter(new NameValue(SipUtils.FEATURE_GSMA_CS_IMAGE, null));
	        resp.getStackMessage().addHeader(contact);
	        
	    	// Send 200 OK
	        getImsModule().getSipManager().sendSipMessage(resp);
	        
	    } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Can't send 200 OK for OPTIONS: " + e.getMessage());
        	}
	    }
    }
    
    /**
     * Parse the received capabilities
     * 
     * @param contact Remote contact
     * @param content Content
     */
	private void parseCapabilities(String contact, String content) {
		boolean videoSharingSupported = false;
		boolean imageSharingSupported = false;
		Vector<String> others = new Vector<String>();
		
		if (content != null) {
	    	// Parse the SDP part
	    	SdpParser parser = new SdpParser(content.getBytes());
			Vector<MediaDescription> medias = parser.getMediaDescriptions();
			for(int i=0; i < medias.size(); i++) {
				MediaDescription desc = medias.elementAt(i);
				if (desc.name.equals("video")) {
					videoSharingSupported = true;
				} else
				if (desc.name.equals("message")) {
					imageSharingSupported = true;
				} else
				if (desc.name.equals("application")) {
					MediaAttribute mediaAttr = desc.getMediaAttribute("rtpmap");
					if (mediaAttr != null) {
						String rtpmap = mediaAttr.getValue();
						if (rtpmap != null) {
							String[] encoding = rtpmap.split(" ");
							if ((encoding != null) && (encoding.length > 1)) {
								String[] value = encoding[1].split("/"); 
								others.add(value[0]);
							}
						}
					}
				}
			}
		}
		
		if (logger.isActivated()) {
    		logger.debug("Parsed capabilities: image=" + imageSharingSupported + ", video=" + videoSharingSupported + ", others=" + others.size() + " elts");
    	}
		
    	// Notify listeners
		getImsModule().getCore().getListener().handleContentSharingCapabilitiesIndication(
				contact, imageSharingSupported, videoSharingSupported, others);
	}
}
