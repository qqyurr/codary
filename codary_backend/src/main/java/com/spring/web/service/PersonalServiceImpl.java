package com.spring.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.web.dao.PersonalDao;
import com.spring.web.dto.BlogContentsDto;

@Service
public class PersonalServiceImpl implements PersonalService{

	@Autowired
	private PersonalDao personalDao;
	
	@Override
	public List<BlogContentsDto> personalContents(String uid) {
		return personalDao.selectPersonal(uid);
	}
	
}
