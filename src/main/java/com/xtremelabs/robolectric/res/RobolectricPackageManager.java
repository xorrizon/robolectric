package com.xtremelabs.robolectric.res;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;

import com.xtremelabs.robolectric.RobolectricConfig;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.tester.android.content.pm.StubPackageManager;

public class RobolectricPackageManager extends StubPackageManager {
	
    private Map<String, PackageInfo> packageList;
    private Map<Intent, List<ResolveInfo>> resolveList = new HashMap<Intent, List<ResolveInfo>>();
    private Map<ComponentName, ComponentState> componentList = new HashMap<ComponentName,ComponentState>();
    
    private ContextWrapper contextWrapper;
    private RobolectricConfig config;
    private ApplicationInfo applicationInfo;

    public RobolectricPackageManager(ContextWrapper contextWrapper, RobolectricConfig config) {
        this.contextWrapper = contextWrapper;
        this.config = config;
        initializePackageInfo();
    }

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
        if (packageList.containsKey(packageName)) {
        	return packageList.get(packageName);
        }
        
        throw new NameNotFoundException();
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {

        if (config.getPackageName().equals(packageName)) {
            if (applicationInfo == null) {
                applicationInfo = new ApplicationInfo();
                applicationInfo.flags = config.getApplicationFlags();
                applicationInfo.targetSdkVersion = config.getSdkVersion();
                applicationInfo.packageName = config.getPackageName();
                applicationInfo.processName = config.getProcessName();
            }
            return applicationInfo;
        }
        
        PackageInfo info;
        if ((info = packageList.get(packageName)) != null) {
        	return info.applicationInfo;
        }

        throw new NameNotFoundException();
    }

    @Override
    public List<PackageInfo> getInstalledPackages(int flags) {
        return new ArrayList<PackageInfo>(packageList.values());
    }

    @Override 
    public List<ResolveInfo> queryIntentActivities( Intent intent, int flags ) {
    	return resolveList.get( intent );
    }
    
    @Override
    public ResolveInfo resolveActivity(Intent intent, int flags) {
    	List<ResolveInfo> candidates = queryIntentActivities(intent, flags);
    	if (candidates == null) { return null; }
    	return candidates.get(0);
    }
    
    public void addResolveInfoForIntent( Intent intent, List<ResolveInfo> info ) {
    	resolveList.put( intent, info );
    }
    
	@Override
	public Intent getLaunchIntentForPackage(String packageName) {
		Intent i = new Intent();
		i.setComponent( new ComponentName(packageName, "") );
		return i;
	}
	
	@Override
	public CharSequence getApplicationLabel(ApplicationInfo info) {
		return info.name;
	}
	
	@Override
	public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
		componentList.put(componentName, new ComponentState(newState, flags));
	}
	
	/**
	 * Non-Android accessor.  Use to make assertions on values passed to
	 * setComponentEnabledSetting.
	 * 
	 * @param componentName
	 * @return
	 */
	public RobolectricPackageManager.ComponentState getComponentState(ComponentName componentName) {
		return componentList.get(componentName);
	}
    
    /**
     * Non-Android accessor.  Used to add a package to the list of those
     * already 'installed' on system.
     * 
     * @param packageInfo
     */
    public void addPackage( PackageInfo packageInfo ) {
    	 packageList.put(packageInfo.packageName, packageInfo);
    }
    
    public void addPackage( String packageName ) {
    	PackageInfo info = new PackageInfo();
    	info.packageName = packageName;
    	addPackage( info );
    }    
    
    private void initializePackageInfo() {
    	if (packageList != null) { return; }

        PackageInfo packageInfo = new PackageInfo();
        packageInfo.packageName = contextWrapper.getPackageName();
        packageInfo.versionName = "1.0";
        
        packageList = new HashMap<String, PackageInfo>();
        addPackage( packageInfo );
    }
    
    public class ComponentState {
    	public int newState;
    	public int flags;
    	
		public ComponentState(int newState, int flags) {
			this.newState = newState;
			this.flags = flags;
		}
    }
}
