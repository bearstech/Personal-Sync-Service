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

package com.bearstech.android.myownsync.platform;

import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.text.TextUtils;
import android.util.Log;

import com.bearstech.android.myownsync.Constants;
import com.bearstech.android.myownsync.R;
import com.bearstech.android.myownsync.client.User;

/**
 * Helper class for storing data in the platform content providers.
 */
public class ContactOperations {

    private final ContentValues mValues;
    private ContentProviderOperation.Builder mBuilder;
    private final BatchOperation mBatchOperation;
    private final Context mContext;
    private boolean mYield;
    private long mRawContactId;
    private int mBackReference;
    private boolean mIsNewContact;

    /**
     * Returns an instance of ContactOperations instance for adding new contact
     * to the platform contacts provider.
     * 
     * @param context the Authenticator Activity context
     * @param userId the userId of the sample SyncAdapter user object
     * @param accountName the username of the current login
     * @return instance of ContactOperations
     */
    public static ContactOperations createNewContact(Context context,
        int userId, String accountName, BatchOperation batchOperation) {
        return new ContactOperations(context, userId, accountName,
            batchOperation);
    }

    /**
     * Returns an instance of ContactOperations for updating existing contact in
     * the platform contacts provider.
     * 
     * @param context the Authenticator Activity context
     * @param rawContactId the unique Id of the existing rawContact
     * @return instance of ContactOperations
     */
    public static ContactOperations updateExistingContact(Context context,
        long rawContactId, BatchOperation batchOperation) {
        return new ContactOperations(context, rawContactId, batchOperation);
    }

    public ContactOperations(Context context, BatchOperation batchOperation) {
        mValues = new ContentValues();
        mYield = true;
        mContext = context;
        mBatchOperation = batchOperation;
    }

    public ContactOperations(Context context, int userId, String accountName,
        BatchOperation batchOperation) {
        this(context, batchOperation);
        mBackReference = mBatchOperation.size();
        mIsNewContact = true;
        mValues.put(RawContacts.SOURCE_ID, userId);
        mValues.put(RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        mValues.put(RawContacts.ACCOUNT_NAME, accountName);
        mBuilder =
            newInsertCpo(RawContacts.CONTENT_URI, true).withValues(mValues);
        mBatchOperation.add(mBuilder.build());
    }

    public ContactOperations(Context context, long rawContactId,
        BatchOperation batchOperation) {
        this(context, batchOperation);
        mIsNewContact = false;
        mRawContactId = rawContactId;
    }


	public ContactOperations addUser(User user) {
		Log.d("ContactOperations", "add user to contact: "+user.getFirstName()+" "+user.getLastName());
        mValues.clear();
        
        if (!TextUtils.isEmpty(user.getMiddleName())) {
            mValues.put(StructuredName.MIDDLE_NAME, user.getMiddleName());
            mValues.put(StructuredName.MIMETYPE,
                StructuredName.CONTENT_ITEM_TYPE);
        }
        if (!TextUtils.isEmpty(user.getNameSuffix())) {
            mValues.put(StructuredName.SUFFIX, user.getNameSuffix());
            mValues.put(StructuredName.MIMETYPE,
                StructuredName.CONTENT_ITEM_TYPE);
        }
        if (!TextUtils.isEmpty(user.getPGivenName())) {
            mValues.put(StructuredName.PHONETIC_GIVEN_NAME, user.getPGivenName());
            mValues.put(StructuredName.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        }
        if (!TextUtils.isEmpty(user.getPMiddleName())) {
            mValues.put(StructuredName.PHONETIC_MIDDLE_NAME, user.getPMiddleName());
            mValues.put(StructuredName.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        }
        if (!TextUtils.isEmpty(user.getPFamilyName())) {
            mValues.put(StructuredName.PHONETIC_FAMILY_NAME, user.getPFamilyName());
            mValues.put(StructuredName.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        }
        if (!TextUtils.isEmpty(user.getFirstName())) {
            mValues.put(StructuredName.GIVEN_NAME, user.getFirstName());
            mValues.put(StructuredName.MIMETYPE,
                StructuredName.CONTENT_ITEM_TYPE);
        }
        if (!TextUtils.isEmpty(user.getLastName())) {
            mValues.put(StructuredName.FAMILY_NAME, user.getLastName());
            mValues.put(StructuredName.MIMETYPE,
                StructuredName.CONTENT_ITEM_TYPE);
        }
        
        if (mValues.size() > 0) {
            addInsertOp();
        }
        
        return this;
	}
	
	public ContactOperations addNickname(String nickname) {
		
		mValues.clear();
		
        if (!TextUtils.isEmpty(nickname)) {
            mValues.put(Nickname.NAME, nickname);
            mValues.put(Nickname.MIMETYPE, Nickname.CONTENT_ITEM_TYPE);
        }
        
        if (mValues.size() > 0) {
        	addInsertOp();
        }
		
		return this;
	}
    
    /**
     * Adds a contact name
     * 
     * @param name Name of contact
     * @param nameType type of name: family name, given name, etc.
     * @return instance of ContactOperations
     */
    public ContactOperations addName(String firstName, String lastName) {
    	Log.d("ContactOperations", "add name "+firstName+" "+lastName);
        mValues.clear();
        if (!TextUtils.isEmpty(firstName)) {
            mValues.put(StructuredName.GIVEN_NAME, firstName);
            mValues.put(StructuredName.MIMETYPE,
                StructuredName.CONTENT_ITEM_TYPE);
        }
        if (!TextUtils.isEmpty(lastName)) {
            mValues.put(StructuredName.FAMILY_NAME, lastName);
            mValues.put(StructuredName.MIMETYPE,
                StructuredName.CONTENT_ITEM_TYPE);
        }
        if (mValues.size() > 0) {
            addInsertOp();
        }
        return this;
    }

    /**
     * Adds an email
     * 
     * @param new email for user
     * @return instance of ContactOperations
     */
    public ContactOperations addEmail(Map<String, String> emails) {
    	Log.d("ContactOperations", "add email "+emails);
        mValues.clear();
        for (Entry<String, String> email : emails.entrySet()) {
            mValues.put(Email.DATA, email.getValue());
            if (email.getKey() == "work")
            	mValues.put(Email.TYPE, Email.TYPE_WORK);
            else if (email.getKey() == "home")
            	mValues.put(Email.TYPE, Email.TYPE_HOME);
            else {
            	mValues.put(Email.TYPE, Email.TYPE_CUSTOM);
            	mValues.put(Email.LABEL, email.getKey());
            }
            mValues.put(Email.MIMETYPE, Email.CONTENT_ITEM_TYPE);
            addInsertOp();
        }
        return this;
    }

    /**
     * Adds a phone number
     * 
     * @param phone new phone number for the contact
     * @param phoneType the type: cell, home, etc.
     * @return instance of ContactOperations
     */
    public ContactOperations addPhone(Map<String, String> phones) {
    	
    	for (Entry<String,String> p : phones.entrySet()) {
    		String phone = p.getValue(), phoneType = p.getKey();
    		Log.d("ContactOperations", "add phone "+phone+" type:"+phoneType);
    		mValues.clear();
    		if (!TextUtils.isEmpty(phone)) {
    			if (phoneType == "cell")
    				mValues.put(Phone.TYPE, Phone.TYPE_MOBILE);
    			else if (phoneType == "home")
    				mValues.put(Phone.TYPE, Phone.TYPE_HOME);
    			else if (phoneType == "work")
    				mValues.put(Phone.TYPE, Phone.TYPE_WORK);
    			else if (phoneType == "company_main")
    				mValues.put(Phone.TYPE, Phone.TYPE_COMPANY_MAIN);
    			else if (phoneType == "fax_home")
    				mValues.put(Phone.TYPE, Phone.TYPE_FAX_HOME);
    			else if (phoneType == "fax_work")
    				mValues.put(Phone.TYPE, Phone.TYPE_FAX_WORK);
    			else if (phoneType == "other")
    				mValues.put(Phone.TYPE, Phone.TYPE_OTHER);
    			else if (phoneType == "main")
    				mValues.put(Phone.TYPE, Phone.TYPE_MAIN);
    			else if (phoneType == "work_cell")
    				mValues.put(Phone.TYPE, Phone.TYPE_WORK_MOBILE);
    			else {
    				mValues.put(Phone.TYPE, Phone.TYPE_CUSTOM);
    				mValues.put(Phone.LABEL, phoneType);
    			}
    			mValues.put(Phone.NUMBER, phone);
    			mValues.put(Phone.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
    			addInsertOp();
    		}
    	}
        return this;
    }
    
    /**
     * Adds a addresses
     * 
     * @param phone new phone number for the contact
     * @param phoneType the type: cell, home, etc.
     * @return instance of ContactOperations
     */
    public ContactOperations addAddresses(Map<String, Map<String,String>> addresses) {
    	
    	for (Entry<String,Map<String,String>> p : addresses.entrySet()) {
    			Log.d("ContactOperations", "add address ");
    			mValues.clear();
    			mValues.put(StructuredPostal.STREET, p.getValue().get("street"));
    			mValues.put(StructuredPostal.CITY, p.getValue().get("city"));
    			mValues.put(StructuredPostal.POSTCODE, p.getValue().get("postcode"));
    			mValues.put(StructuredPostal.COUNTRY, p.getValue().get("country"));
    			mValues.put(StructuredPostal.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE);
    			if (p.getKey() == "other")
    				mValues.put(StructuredPostal.TYPE, StructuredPostal.TYPE_OTHER);
    			else if (p.getKey() == "home")
    				mValues.put(StructuredPostal.TYPE, StructuredPostal.TYPE_HOME);
    			else if (p.getKey() == "work")
    				mValues.put(StructuredPostal.TYPE, StructuredPostal.TYPE_WORK);
    			else {
    				mValues.put(StructuredPostal.LABEL, p.getKey());
        			mValues.put(StructuredPostal.TYPE, StructuredPostal.TYPE_CUSTOM);
    				
    			}
    			
    			addInsertOp();
    	}
        return this;
    }

    /**
     * Adds a profile action
     * 
     * @param userId the userId of the sample SyncAdapter user object
     * @return instance of ContactOperations
     */
    public ContactOperations addProfileAction(long userId) {
        mValues.clear();
        if (userId != 0) {
            mValues.put(SampleSyncAdapterColumns.DATA_PID, userId);
            mValues.put(SampleSyncAdapterColumns.DATA_SUMMARY, mContext
                .getString(R.string.profile_action));
            mValues.put(SampleSyncAdapterColumns.DATA_DETAIL, mContext
                .getString(R.string.view_profile));
            mValues.put(Data.MIMETYPE, SampleSyncAdapterColumns.MIME_PROFILE);
            addInsertOp();
        }
        return this;
    }

    /**
     * Updates contact's email
     * 
     * @param email email id of the sample SyncAdapter user
     * @param uri Uri for the existing raw contact to be updated
     * @return instance of ContactOperations
     */
    public ContactOperations updateEmails(Map<String,String> emails, String existingEmail,
        Uri uri) {
    	mValues.clear();
    	for (Entry<String, String> email : emails.entrySet()) {
    		if (!TextUtils.equals(existingEmail, email.getValue())) {
    			Log.d("ContactOperations", "update email "+email+" over "+existingEmail+" uri:"+uri.toString());
    			mValues.put(Email.DATA, email.getValue());
    			if (email.getKey() == "work")
    				mValues.put(Email.TYPE, Email.TYPE_WORK);
    			else if (email.getKey() == "home")
    				mValues.put(Email.TYPE, Email.TYPE_HOME);
    			else {
    				mValues.put(Email.TYPE, Email.TYPE_CUSTOM);
    				mValues.put(Email.LABEL, email.getKey());
    			}
    			mValues.put(Email.MIMETYPE, Email.CONTENT_ITEM_TYPE);
    			addUpdateOp(uri);
    		}
        }
        return this;
    }

    /**
     * Updates contact's name
     * 
     * @param name Name of contact
     * @param existingName Name of contact stored in provider
     * @param nameType type of name: family name, given name, etc.
     * @param uri Uri for the existing raw contact to be updated
     * @return instance of ContactOperations
     */
    public ContactOperations updateName(Uri uri, String existingFirstName,
        String existingLastName, String firstName, String lastName) {
        Log.i("ContactOperations", "u;ef=" + existingFirstName + ";el="
            + existingLastName + ";f=" + firstName + ";l=" + lastName);

        mValues.clear();
        if (!TextUtils.equals(existingFirstName,firstName)) {
            mValues.put(StructuredName.GIVEN_NAME, firstName);
            mValues.put(StructuredName.MIMETYPE,
                StructuredName.CONTENT_ITEM_TYPE);
        }
        if (!TextUtils.equals(existingLastName, lastName)) {
            mValues.put(StructuredName.FAMILY_NAME, lastName);
            mValues.put(StructuredName.MIMETYPE,
                StructuredName.CONTENT_ITEM_TYPE);
        }
        if (mValues.size() > 0) {
            addUpdateOp(uri);
        }
        return this;
    }

    /**
     * Updates contact's phone
     * 
     * @param existingNumber phone number stored in contacts provider
     * @param phones new phone number for the contact
     * @param uri Uri for the existing raw contact to be updated
     * @return instance of ContactOperations
     */
    public ContactOperations updatePhones(String existingNumber, Map<String, String> phones,
        Uri uri) {
    	for (Entry<String,String> p : phones.entrySet()) {
    		String phone = p.getValue(), phoneType = p.getKey();
    		mValues.clear();
    		Log.d("ContactOperations", "update phone "+phones+" uri:"+uri.toString());
    		if (!TextUtils.isEmpty(phone)) {
    			if (phoneType == "cell")
    				mValues.put(Phone.TYPE, Phone.TYPE_MOBILE);
    			else if (phoneType == "home")
    				mValues.put(Phone.TYPE, Phone.TYPE_HOME);
    			else if (phoneType == "work")
    				mValues.put(Phone.TYPE, Phone.TYPE_WORK);
    			else if (phoneType == "company_main")
    				mValues.put(Phone.TYPE, Phone.TYPE_COMPANY_MAIN);
    			else if (phoneType == "fax_home")
    				mValues.put(Phone.TYPE, Phone.TYPE_FAX_HOME);
    			else if (phoneType == "fax_work")
    				mValues.put(Phone.TYPE, Phone.TYPE_FAX_WORK);
    			else if (phoneType == "other")
    				mValues.put(Phone.TYPE, Phone.TYPE_OTHER);
    			else if (phoneType == "main")
    				mValues.put(Phone.TYPE, Phone.TYPE_MAIN);
    			else if (phoneType == "work_cell")
    				mValues.put(Phone.TYPE, Phone.TYPE_WORK_MOBILE);
    			else {
    				mValues.put(Phone.TYPE, Phone.TYPE_CUSTOM);
    				mValues.put(Phone.LABEL, phoneType);
    			}
    			mValues.put(Phone.NUMBER, phone);
    			mValues.put(Phone.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
    			addUpdateOp(uri);
    		}
    	}
        return this;
    }

    /**
     * Updates contact's profile action
     * 
     * @param userId sample SyncAdapter user id
     * @param uri Uri for the existing raw contact to be updated
     * @return instance of ContactOperations
     */
    public ContactOperations updateProfileAction(Integer userId, Uri uri) {
        Log.d("ContactOperations", "update profile action "+userId+" "+uri);
    	mValues.clear();
        mValues.put(SampleSyncAdapterColumns.DATA_PID, userId);
        addUpdateOp(uri);
        return this;
    }
    
public ContactOperations updateNickname(String existingNickName, String nickname, Uri uri) {
		mValues.clear();
		
        if (!TextUtils.equals(existingNickName,nickname)) {
            mValues.put(Nickname.NAME, nickname);
            mValues.put(Nickname.MIMETYPE, Nickname.CONTENT_ITEM_TYPE);
        }
        
        if (mValues.size() > 0) {
        	addUpdateOp(uri);
        }
		
		return this;
	}

    /**
     * Adds an insert operation into the batch
     */
    private void addInsertOp() {
        if (!mIsNewContact) {
            mValues.put(Phone.RAW_CONTACT_ID, mRawContactId);
        }
        mBuilder =
            newInsertCpo(addCallerIsSyncAdapterParameter(Data.CONTENT_URI),
                mYield);
        mBuilder.withValues(mValues);
        if (mIsNewContact) {
            mBuilder
                .withValueBackReference(Data.RAW_CONTACT_ID, mBackReference);
        }
        mYield = false;
        mBatchOperation.add(mBuilder.build());
    }

    /**
     * Adds an update operation into the batch
     */
    private void addUpdateOp(Uri uri) {
        mBuilder = newUpdateCpo(uri, mYield).withValues(mValues);
        mYield = false;
        mBatchOperation.add(mBuilder.build());
    }

    public static ContentProviderOperation.Builder newInsertCpo(Uri uri,
        boolean yield) {
        return ContentProviderOperation.newInsert(
            addCallerIsSyncAdapterParameter(uri)).withYieldAllowed(yield);
    }

    public static ContentProviderOperation.Builder newUpdateCpo(Uri uri,
        boolean yield) {
        return ContentProviderOperation.newUpdate(
            addCallerIsSyncAdapterParameter(uri)).withYieldAllowed(yield);
    }

    public static ContentProviderOperation.Builder newDeleteCpo(Uri uri,
        boolean yield) {
        return ContentProviderOperation.newDelete(
            addCallerIsSyncAdapterParameter(uri)).withYieldAllowed(yield);

    }

    private static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(
            ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
    }

}
