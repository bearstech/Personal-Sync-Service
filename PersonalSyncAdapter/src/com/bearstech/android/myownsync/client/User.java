/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.bearstech.android.myownsync.client;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a sample SyncAdapter user
 */
public class User {

    private final String mUserName;
    private final String mFirstName;
    private final String mLastName;
    private final boolean mDeleted;
    private final int mUserId;
	private final String mMiddleName;
	private final String mNameSuffix;
	private final String mPGivenName;
	private final String mPMiddleName;
	private final String mPFamilyName;
	private final String mNickname;
	private final String mWebsite;
	private final String mStatus;

    private final Map<String,String> mPhones;
    private final Map<String,String> mEmails;
    private final Map<String,Map<String,String>> mAddresses;
    private final Map<String,String> mIMs;
	private final HashMap<String, String> mOrgas;

    public User(String userName, String firstName, String lastName, String middleName, 
    			String nameSuffix, String pGivenName, String pMiddleName, String pFamilyName, 
    			String nickname, String website, String status, boolean deleted, int userId) {

        mUserName = userName; 
		mFirstName = firstName;
		mLastName = lastName;
		mMiddleName = middleName;
		mNameSuffix = nameSuffix;
		mPGivenName = pGivenName;
		mPMiddleName = pMiddleName;
		mPFamilyName = pFamilyName;
		mNickname = nickname;
		mWebsite = website;
		mStatus = status;
		mDeleted = deleted; 
		mUserId = userId;
		
		mOrgas = new HashMap<String, String>();
		mPhones = new HashMap<String, String>();
		mEmails = new HashMap<String, String>();
		mAddresses = new HashMap<String, Map<String,String>>();
		mIMs = new HashMap<String, String>();
    }
    private static String opt_get(JSONObject o, String s) throws JSONException {
		return o.has(s) ? o.getString(s) : null;
	}
    /**
     * Creates and returns an instance of the user from the provided JSON data.
     * 
     * @param user The JSONObject containing user data
     * @return user The new instance of Voiper user created from the JSON data.
     */
    public static User valueOf(JSONObject user) {
    try {
    		final int userId = user.getInt("user_id");
            final boolean deleted = user.has("d") ? user.getBoolean("d") : false;

            final String userName = user.getString("username");
            final String firstName = opt_get(user,"firstname");
            final String lastName = opt_get(user,"lastname");
            final String middleName = opt_get(user, "middlename");
            final String nameSuffix = opt_get(user, "namesuffix");
            final String pGivenName = opt_get(user, "phonetic_given_name");
            final String pMiddleName = opt_get(user, "phonetic_middle_name");
            final String pFamilyName = opt_get(user, "phonetic_family_name");
            final String nickname = opt_get(user, "nickname");
            final String website = opt_get(user, "website");
            final String status = opt_get(user, "status");
            
            User u = new User(userName, 
  				  firstName, 
  				  lastName, 
  				  middleName,
  				  nameSuffix,
  				  pGivenName,
  				  pMiddleName,
  				  pFamilyName,
  				  nickname,
  				  website,
  				  status,
  				  deleted, 
  				  userId);
            if (user.has("orga")) {
            	final JSONArray orgas = user.getJSONArray("orga");
            	for (int i=0;i<orgas.length();++i) {
            		JSONObject orga = orgas.getJSONObject(i);
            		u.addOrga(orga.getString("name"), orga.getString("role"));
            	}
            }
            if (user.has("phone")) {
            	final JSONArray phones = user.getJSONArray("phone");
            	for (int i=0;i<phones.length();++i) {
            		JSONObject phone = phones.getJSONObject(i);
            		u.addPhone(phone.getString("phonetype"),phone.getString("phone"));
            	}
            }
            if (user.has("email")) {
            	final JSONArray emails = user.getJSONArray("email");
            	for (int i=0;i<emails.length();++i) {
            		JSONObject email = emails.getJSONObject(i);
            		u.addEmail(email.getString("emailtype"),email.getString("email"));
            	}
            }
            if (user.has("address")) {
            	final JSONArray addresses = user.getJSONArray("address");
            	for (int i=0;i<addresses.length();++i) {
            		JSONObject address = addresses.getJSONObject(i);
            		u.addAddress(opt_get(address,"addrtype"),
            				opt_get(address,"street"),
            				opt_get(address,"postcode"),
            				opt_get(address,"city"),
            				opt_get(address,"country"));
            	}
            }
            if (user.has("IM")) {
            	final JSONArray IMs = user.getJSONArray("IM");
            	for (int i=0;i<IMs.length();++i) {
            		JSONObject IM = IMs.getJSONObject(i);
            		u.addIM(IM.getString("type"),IM.getString("handle"));
            	}
            }
            
            Log.d("User", "JSON object: "+user);
            Log.d("User","resulted user is "+u.getLastName());
            return u;
        } catch (final Exception ex) {
            Log.i("User", "Error parsing JSON user object" + ex.toString());

        }
        return null;

    }

	public String getNickname() {
		return mNickname;
	}

	public String getPMiddleName() {
		return mPMiddleName;
	}

	public String getMiddleName() {
		return mMiddleName;
	}

	public String getNameSuffix() {
		return mNameSuffix;
	}

	public String getPGivenName() {
		return mPGivenName;
	}

	public String getPFamilyName() {
		return mPFamilyName;
	}

	public String getStatus() {
		return mStatus;
	}

	public int getUserId() {
        return mUserId;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

	public String getWebsite() {
		return mWebsite;
	}
	
	public String getBirthday() {
		return null; // TODO implement me !
	}
	
	public String getPhoto() {
		return null; // TODO implement me !
	}
	
	public String getNotes() {
		return null; // TODO implement me !
	}

	public Map<String,String> getPhones() {
		return mPhones;
	}

	public Map<String,String> getEmails() {
		return mEmails;
	}

	public Map<String,Map<String,String>> getAddresses() {
		return mAddresses;
	}

	public Map<String,String> getIMs() {
		return mIMs;
	}
	
    public HashMap<String, String> getOrgas() {
		return mOrgas;
	}
	public boolean isDeleted() {
        return mDeleted;
    }
    
    private void addOrga(String name, String role) {
    	mOrgas.put(name, role);
	}

	private void addAddress(String type, String street, String postcode,
			String city, String country) {
		Map<String,String> detail = new HashMap<String,String>();
		detail.put("street",street);
		detail.put("postcode", postcode);
		detail.put("city", city);
		detail.put("country", country);
		mAddresses.put(type, detail);
	}

	private void addEmail(String type, String address) {
		mEmails.put(type, address);
	}

	private void addPhone(String type, String number) {
		mPhones.put(type, number);
	}
    
	private void addIM(String type, String handle) {
		mIMs.put(type, handle);
	}

	/**
     * Represents the User's status messages
     * 
     */
    public static class Status {
        private final Integer mUserId;
        private final String mStatus;

        public int getUserId() {
            return mUserId;
        }

        public String getStatus() {
            return mStatus;
        }

        public Status(Integer userId, String status) {
            mUserId = userId;
            mStatus = status;
        }

        public static User.Status valueOf(JSONObject userStatus) {
            try {
                final int userId = userStatus.getInt("user_id");
                final String status = userStatus.getString("status");
                return new User.Status(userId, status);
            } catch (final Exception ex) {
                Log.i("User.Status", "Error parsing JSON user object");
            }
            return null;
        }
    }

}
