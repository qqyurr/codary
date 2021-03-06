package com.spring.web.service;

import java.util.List;

import com.spring.web.dto.CommentCountDto;
import com.spring.web.dto.CommentDto;
import com.spring.web.dto.CommentToLikeDto;
import com.spring.web.dto.UserDto;
import com.spring.web.dto.UserInfoDto;

public interface CommentService {

	public List<CommentDto> listComment(String blogId, int blogContentsId) throws Exception;

	public UserInfoDto getUserInfo(UserInfoDto info) throws Exception;

	public void writeComment(CommentDto comment) throws Exception;

	public void deleteComment(int commentNum) throws Exception;

	public void modifyComment(CommentDto comment) throws Exception;

	public boolean getCommentLike(CommentToLikeDto ctl) throws Exception;

	public void commentLike(CommentToLikeDto ctl) throws Exception;

	public void commentLikeCancle(CommentToLikeDto ctl)throws Exception;

	public List<CommentCountDto> commentCheck(UserDto user) throws Exception;

}
