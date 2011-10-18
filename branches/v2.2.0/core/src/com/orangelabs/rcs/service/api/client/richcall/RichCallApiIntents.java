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
package com.orangelabs.rcs.service.api.client.richcall;

/**
 * Rich call API intents
 * 
 * @author jexa7410
 */
public class RichCallApiIntents {
	/**
     * Intent broadcasted when content sharing capabilities have been exchanged
     */
	public final static String SHARING_CAPABILITIES = "com.orangelabs.rcs.richcall.CONTENT_SHARING_CAPABILITIES";

	/**
     * Intent broadcasted when a new image sharing invitation has been received
     */
	public final static String IMAGE_SHARING_INVITATION = "com.orangelabs.rcs.richcall.IMAGE_SHARING_INVITATION";

    /**
     * Intent broadcasted when a new video sharing invitation has been received
     */
	public final static String VIDEO_SHARING_INVITATION = "com.orangelabs.rcs.richcall.VIDEO_SHARING_INVITATION";
}