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
package com.orangelabs.rcs.core.ims.protocol.rtp.core;

/**
 * RTP source
 * 
 * @author jexa7410
 */
public class RtpSource {
	/**
	 * CNAME value
	 */
    public static String CNAME = "anonymous@127.0.0.1";

	/**
	 * SSRC
	 */
    public static int SSRC = RtpSource.generateSSRC();

    /**
     * Generate a unique SSRC value
     * 
     * @return Integer
     */
    private static int generateSSRC() {
        return (int)System.currentTimeMillis();
    }
}
