/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                          www.pekall.com
 *
 * ----------------------------------------------------------------------------------------------
 */
package com.nodejs.comic.models;

import java.util.Arrays;
import java.util.Date;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.nodejs.comic.utils.Utility;

public class SoftwareInfo implements Parcelable {

	public static final String INSTALLS_TAG = "installs";
	public static final String UNINSTALLS_TAG = "uninstalls";
	
	private String apkId;
	private String apkUrl;
	private String packageName;
	private int versionCode;
	private String versionName;
	private String name;
	private String size;
	private String downloadCount;
	private String lastModifyTime;
	private String author;
	private String icon;
	private String intro;
	private String battery;
	private String security;
	private String dataTraffic;
	private String report;
	private String[] previews;
	private Drawable localIcon;

	// add field author:haoanbang
	private boolean hasSoftwareUpdate;
	private boolean hasIconUpdate;
	private boolean softwareIsInstalled;

	public String getApkId() {
		return apkId;
	}

	public void setApkId(String apkId) {
		this.apkId = apkId;
	}

	public String getApkUrl() {
		return apkUrl;
	}

	public void setApkUrl(String apkUrl) {
		this.apkUrl = apkUrl;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShowSize() {
		return Utility.formatSize(size);
	}
	
	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getDownloadCount() {
		if (downloadCount == null || "null".equals(downloadCount)) {
			return "0";
		}
		return downloadCount;
	}

	public void setDownloadCount(String downloadCount) {
		this.downloadCount = downloadCount;
	}

	public String getLastModifyTime() {
		try {
			long time = Long.parseLong(lastModifyTime);
			Date date = new Date(time);
			return date.toLocaleString();
		} catch (NumberFormatException e) {
			return lastModifyTime;
		}
	}

	public void setLastModifyTime(String lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public String getBattery() {
		return battery;
	}

	public void setBattery(String battery) {
		this.battery = battery;
	}

	public String getSecurity() {
		return security;
	}

	public void setSecurity(String security) {
		this.security = security;
	}

	public String getDataTraffic() {
		return dataTraffic;
	}

	public void setDataTraffic(String dataTraffic) {
		this.dataTraffic = dataTraffic;
	}

	public String[] getPreviews() {
		return previews == null ? new String[0] : previews;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public void setPreviews(String[] previews) {
		this.previews = previews;
	}

	public boolean isHasSoftwareUpdate() {
		return hasSoftwareUpdate;
	}

	public void setHasSoftwareUpdate(boolean hasSoftwareUpdate) {
		this.hasSoftwareUpdate = hasSoftwareUpdate;
	}

	public boolean isHasIconUpdate() {
		return hasIconUpdate;
	}

	public void setHasIconUpdate(boolean hasIconUpdate) {
		this.hasIconUpdate = hasIconUpdate;
	}

	public boolean isSoftwareIsInstalled() {
		return softwareIsInstalled;
	}

	public void setSoftwareIsInstalled(boolean softwareIsInstalled) {
		this.softwareIsInstalled = softwareIsInstalled;
	}
	
	public boolean isNetSoftwareInstalled(SoftwareInfo netInfo) {
		if (this.packageName.equals(netInfo.getPackageName()) && this.versionCode >= netInfo.getVersionCode()) {
			return true;
		}
		return false;
	}
	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public Drawable getLocalIcon() {
		return localIcon;
	}

	public void setLocalIcon(Drawable localIcon) {
		this.localIcon = localIcon;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SoftwareInfo other = (SoftwareInfo) obj;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		return true;
	}

	public static final Parcelable.Creator<SoftwareInfo> CREATOR = new Creator<SoftwareInfo>() {  
        public SoftwareInfo createFromParcel(Parcel source) {  
            SoftwareInfo mSoftwareInfo = new SoftwareInfo();  
            mSoftwareInfo.apkId = source.readString() ;
        	mSoftwareInfo.apkUrl = source.readString();
        	mSoftwareInfo.packageName = source.readString();
        	mSoftwareInfo.versionCode = source.readInt();
        	mSoftwareInfo.versionName = source.readString();
        	mSoftwareInfo.name = source.readString();
        	mSoftwareInfo.author = source.readString();
        	mSoftwareInfo.battery = source.readString();
        	mSoftwareInfo.dataTraffic = source.readString();
        	mSoftwareInfo.downloadCount = source.readString();
        	mSoftwareInfo.icon = source.readString();
        	mSoftwareInfo.intro = source.readString();
        	mSoftwareInfo.lastModifyTime = source.readString();
        	mSoftwareInfo.previews = source.createStringArray();
        	mSoftwareInfo.report = source.readString();
        	mSoftwareInfo.security = source.readString();
        	mSoftwareInfo.size = source.readString();
            return mSoftwareInfo;  
        }  
        public SoftwareInfo[] newArray(int size) {  
            return new SoftwareInfo[size];  
        }  
    };  
    
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(apkId);
		dest.writeString(apkUrl);
		dest.writeString(packageName);
		dest.writeInt(versionCode);
		dest.writeString(versionName);
		dest.writeString(name);
		dest.writeString(author);
		dest.writeString(battery);
		dest.writeString(dataTraffic);
		dest.writeString(downloadCount);
		dest.writeString(icon);
		dest.writeString(intro);
		dest.writeString(lastModifyTime);
		dest.writeStringArray(previews);
		dest.writeString(report);
		dest.writeString(security);
		dest.writeString(size);
	}
	
	public void setDetail(SoftwareInfo softwareInfo) {
		this.author = softwareInfo.getAuthor();
		this.battery = softwareInfo.getBattery();
		this.dataTraffic = softwareInfo.getDataTraffic();
		this.security = softwareInfo.getSecurity();
		this.intro = softwareInfo.getIntro();
		this.lastModifyTime = softwareInfo.getLastModifyTime();
		this.report = softwareInfo.getReport();
		this.previews = softwareInfo.getPreviews();
	}

	@Override
	public String toString() {
		return "SoftwareInfo [apkId=" + apkId + ", apkUrl=" + apkUrl + ", author=" + author + ", battery=" + battery
				+ ", dataTraffic=" + dataTraffic + ", downloadCount=" + downloadCount + ", hasIconUpdate="
				+ hasIconUpdate + ", hasSoftwareUpdate=" + hasSoftwareUpdate + ", icon=" + icon + ", intro=" + intro
				+ ", lastModifyTime=" + lastModifyTime + ", localIcon=" + localIcon + ", name=" + name
				+ ", packageName=" + packageName + ", previews=" + Arrays.toString(previews) + ", report=" + report
				+ ", security=" + security + ", size=" + size + ", softwareIsInstalled=" + softwareIsInstalled
				+ ", versionCode=" + versionCode + ", versionName=" + versionName + "]";
	}

}
