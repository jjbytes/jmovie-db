/**
 * 
 */
package com.lars_albrecht.mdb.main.extras.outputItems;

import java.util.ArrayList;
import java.util.Arrays;

import com.lars_albrecht.general.utilities.Helper;
import com.lars_albrecht.mdb.main.core.interfaces.web.pages.abstracts.AbstractFileDetailsOutputItem;
import com.lars_albrecht.mdb.main.core.models.Key;
import com.lars_albrecht.mdb.main.core.models.KeyValue;

/**
 * @author lalbrecht
 * 
 */
public class MovieFileDetailsOutputItem extends AbstractFileDetailsOutputItem {

	@Override
	public String getValue(final String infoType, final String sectionName, final KeyValue<String, Object> keyValue, final String value) {
		if (infoType.equalsIgnoreCase("themoviedb") && sectionName.equalsIgnoreCase("video")) {
			final ArrayList<String> trailerParams = Helper.explode(value, ",");
			String trailerUrl = null;
			if (Helper.explode(value, ",").get(3).trim().equalsIgnoreCase("youtube")) {
				trailerUrl = this.getYoutubeLink(trailerParams);
			}
			return "<a href=\"" + trailerUrl + "\">" + trailerParams.get(0).trim() + " (" + trailerParams.get(1).trim() + ")</a>";

		} else if (keyValue.getKey().getKey().equalsIgnoreCase("homepage")) {
			return (value != null && value.length() > 0) ? "<a href=\"" + value + "\">" + value + "</a>" : null;
		} else {
			return this.getDefaultValue(infoType, sectionName, keyValue, value);
		}
	}

	/**
	 * Create a youtube link from videoParams (comma separated list of strings).
	 * 
	 * @param videoParams
	 * @return a youtube link
	 */
	private String getYoutubeLink(final ArrayList<String> videoParams) {
		return "http://www.youtube.com/watch?v=" + videoParams.get(2).trim();
	}

	@Override
	public boolean keyAllowed(final Key<String> key) {
		final String[] disallowList = {};
		if (Arrays.asList(disallowList).contains(key.getKey())) {
			return false;
		}
		return true;
	}

	@Override
	public String getKey(final String infoType, final String sectionName, final KeyValue<String, Object> keyValue) {
		return this.getDefaultKey(infoType, sectionName, keyValue);
	}
}
