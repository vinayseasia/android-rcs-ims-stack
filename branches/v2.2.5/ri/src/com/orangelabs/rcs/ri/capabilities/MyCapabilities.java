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

package com.orangelabs.rcs.ri.capabilities;

import java.util.List;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;

import com.orangelabs.rcs.service.api.client.capability.Capabilities;
import com.orangelabs.rcs.service.api.client.capability.CapabilityApi;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;

/**
 * My capabilities
 */
public class MyCapabilities extends Activity {

	/**
	 * Capability API
	 */
    private CapabilityApi capabilityApi;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.capabilities_mine);
        
        // Set title
        setTitle(R.string.menu_my_capabilities);
        
        // Instanciate contacts API
        capabilityApi = new CapabilityApi(getApplicationContext());
        
		// Get the current capabilities from the RCS contacts API
        Capabilities capabilities = capabilityApi.getMyCapabilities();
    	
    	// Set timestamp
    	TextView lastModified = (TextView)findViewById(R.id.last_modified);
    	lastModified.setText(Utils.formatDateToString(capabilities.getTimestamp()));
    	
    	// Set capabilities
        CheckBox imageCSh = (CheckBox)findViewById(R.id.image_sharing);
        imageCSh.setChecked(capabilities.isImageSharingSupported());
        CheckBox videoCSh = (CheckBox)findViewById(R.id.video_sharing);
        videoCSh.setChecked(capabilities.isVideoSharingSupported());
        CheckBox ft = (CheckBox)findViewById(R.id.file_transfer);
        ft.setChecked(capabilities.isFileTransferSupported());
        CheckBox im = (CheckBox)findViewById(R.id.im);
        im.setChecked(capabilities.isImSessionSupported());
        CheckBox csVideo = (CheckBox)findViewById(R.id.cs_video);
        csVideo.setChecked(capabilities.isCsVideoSupported());
        CheckBox presenceDiscovery = (CheckBox)findViewById(R.id.presence_discovery);
        presenceDiscovery.setChecked(capabilities.isPresenceDiscoverySupported());
        CheckBox socialPresence = (CheckBox)findViewById(R.id.social_presence);
        socialPresence.setChecked(capabilities.isSocialPresenceSupported());

        // Set extensions
        TextView extensions = (TextView)findViewById(R.id.extensions);
        StringBuffer stringBuff = new StringBuffer();
        List<String> extensionList = capabilities.getSupportedExtensions();
        for(int i=0;i<extensionList.size();i++) {
        	stringBuff.append(extensionList.get(i) + "\n");
        }
        extensions.setText(stringBuff.toString());
    }
}