package com.spring.web.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "UserInfoDto : 유저 상세정보", description = "UserInfoDto 회원 개인정보를 나타낸다.")
public class UserInfoDto {

	@ApiModelProperty(value = "회원 고유번호 13자리")
	private String uid;
	@ApiModelProperty(value = "닉네임")
	private String nickname;
	@ApiModelProperty(value = "회원명")
	private String username;
	@ApiModelProperty(value = "프로필 사진")
	private String profile;
	@ApiModelProperty(value = "직업")
	private String job;
	@ApiModelProperty(value = "소개글")
	private String intro;
	

	public UserInfoDto() {
	}

	public UserInfoDto(String uid, String nickname, String username, String profile) {
		this.uid = uid;
		this.nickname = nickname;
		this.username = username;
		this.profile = profile;
	}

	public UserInfoDto(String uid, String nickname, String profile, String job, String intro) {
		this.uid = uid;
		this.nickname = nickname;
		this.profile = profile;
		this.job = job;
		this.intro = intro;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}
	
	

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	@Override
	public String toString() {
		return "UserInfoDto [uid=" + uid + ", nickname=" + nickname + ", username=" + username + ", profile=" + profile
				+ ", job=" + job + ", intro=" + intro + "]";
	}

	
}
