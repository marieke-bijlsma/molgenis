package org.molgenis.ui;

import org.molgenis.framework.server.MolgenisSettings;

public class AppTrackingCodeImpl implements AppTrackingCode
{
	private String googleAnalytics = null;
	private String piwik = null;
	private final static String START_TRACKINGCODE = "(function(){if('true' === $.cookie('permissionforcookies')){";
	private final static String END_TRACKINGCODE = "}})();";

	public AppTrackingCodeImpl()
	{
	}

	public AppTrackingCodeImpl(MolgenisSettings molgenisSettings)
	{
		this(molgenisSettings.getProperty(AppTrackingCode.KEY_APP_TRACKING_CODE_PIWIK), molgenisSettings
				.getProperty(AppTrackingCode.KEY_APP_TRACKING_CODE_GOOGLEANALYTICS));
	}

	public AppTrackingCodeImpl(String piwik, String googleAnalytics)
	{
		if (piwik != null)
		{
			this.setPiwik(START_TRACKINGCODE + piwik + END_TRACKINGCODE);
		}
		if (googleAnalytics != null)
		{
			this.setGoogleAnalytics(START_TRACKINGCODE + googleAnalytics + END_TRACKINGCODE);
		}
	}

	/**
	 * Return the Google Analytics Tracking Code from data base
	 * 
	 * @return
	 */
	@Override
	public String getGoogleAnalytics()
	{
		return this.googleAnalytics;
	}

	@Override
	public String getPiwik()
	{
		return this.piwik;
	}
	
	private void setGoogleAnalytics(String googleAnalytics)
	{
		this.googleAnalytics = googleAnalytics;
	}

	private void setPiwik(String piwik)
	{
		this.piwik = piwik;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.piwik == null) ? 0 : this.piwik.hashCode());
		result = prime * result + ((this.googleAnalytics == null) ? 0 : this.googleAnalytics.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AppTrackingCode other = (AppTrackingCode) obj;
		if (this.piwik == null)
		{
			if (other.getPiwik() != null) return false;
		}
		else if (!this.piwik.equals(other.getPiwik())) return false;
		if (this.googleAnalytics == null)
		{
			if (other.getGoogleAnalytics() != null) return false;
		}
		else if (!this.googleAnalytics.equals(other.getGoogleAnalytics())) return false;
		return true;
	}
}
