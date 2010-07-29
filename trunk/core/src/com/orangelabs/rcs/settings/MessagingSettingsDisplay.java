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
package com.orangelabs.rcs.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * Messaging settings display
 * 
 * @author jexa7410
 */
public class MessagingSettingsDisplay extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
	private CheckBoxPreference vibrate;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.rcs_settings_messaging_preferences);
        setTitle(R.string.title_settings);
        
        vibrate = (CheckBoxPreference)findPreference("filetransfer_invitation_vibration");
        vibrate.setPersistent(false);
        vibrate.setOnPreferenceChangeListener(this);
        vibrate.setChecked(RcsSettings.getInstance().isPhoneVibrateForFileTransferInvitation());        
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference.getKey().equals("filetransfer_invitation_vibration")) {
        	Boolean state = (Boolean)objValue;
        	RcsSettings.getInstance().setPhoneVibrateForFileTransferInvitation(state.booleanValue());
        }
        return true;
    }    
}
