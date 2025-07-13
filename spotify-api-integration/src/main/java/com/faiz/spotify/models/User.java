package com.faiz.spotify.models;

import java.util.List;

public class User {
	
	List<String> trackURIs;
	
	List<String> trackNames;

	String current_playing_track;
	
	String pause;
	
	String play;
	
	String productType;

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public List<String> getTrackURIs() {
		return trackURIs;
	}

	public void setTrackURIs(List<String> trackURIs) {
		this.trackURIs = trackURIs;
	}

	public List<String> getTrackNames() {
		return trackNames;
	}

	public void setTrackNames(List<String> trackNames) {
		this.trackNames = trackNames;
	}

	public String getCurrent_playing_track() {
		return current_playing_track;
	}

	public void setCurrent_playing_track(String current_playing_track) {
		this.current_playing_track = current_playing_track;
	}
	
	public String getPause() {
		return pause;
	}

	public void setPause(String pause) {
		this.pause = pause;
	}

	public String getPlay() {
		return play;
	}

	public void setPlay(String play) {
		this.play = play;
	}

	@Override
	public String toString() {
		return "User [trackURIs=" + trackURIs + ", trackNames=" + trackNames + ", current_playing_track="
				+ current_playing_track + ", pause=" + pause + ", play=" + play + ", productType=" + productType + "]";
	}

}
