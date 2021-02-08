package com.spring.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.web.dto.LoginCallBackDto;
import com.spring.web.dto.UserDto;
import com.spring.web.dto.UserInfoDto;
import com.spring.web.service.BlogContentsService;
import com.spring.web.service.JwtServiceImpl;
import com.spring.web.service.UserService;
import com.spring.web.service.oauth.GoogleOauthService;
import com.spring.web.service.oauth.KakaoOauthService;
import com.spring.web.service.oauth.NaverOauthService;
import com.spring.web.service.oauth.SocialOauth;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = { "*" })
@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private KakaoOauthService kakaoOauthService;
	@Autowired
	private NaverOauthService naverOauthService;
	@Autowired
	private GoogleOauthService googleOauthService;
	@Autowired
	private JwtServiceImpl jwtService;

	public static final Logger logger = LoggerFactory.getLogger(UserController.class);
	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";

	
	/**
	 * 소셜로그인 access_token을 전달 받아 정보 전달 
	 * 
	 * @param -
	 * @return List<BlogPostDto>
	 */
	@ApiOperation(value = "소셜사이트로 부터 읽어온 access_token값", notes ="@param : access_token  </br> @return : access_token, message, user")
	@PostMapping("/login/{socialLoginType}")
	public ResponseEntity<Map<String, Object>> access(@PathVariable("socialLoginType") String socialLoginType,
			@RequestBody String accessTocken) {
		System.out.println("#" + socialLoginType + "로그인 요청됨!!");
		System.out.println("# accessTocken: " + accessTocken);
		SocialOauth socialOauth = getSocialOauth(socialLoginType);

		HashMap<String, Object> userInfo = socialOauth.getUserInfoFromOauth(accessTocken);
		logger.info("#Get userInfo: {}", userInfo);

		UserDto user = null;
		Map<String, Object> resultMap = new HashMap<>();
		LoginCallBackDto loginCallBackDto = new LoginCallBackDto();

		try {
			user = userService.findByProvider(userInfo);
			if (user == null) {
				logger.info("#최초 로그인입니다.");
				loginCallBackDto = userService.save(userInfo);
			} else {
				logger.info("#기존회원입니다.");
				loginCallBackDto = getLoginCallBackByUid(user.getUid());
			}
			
			resultMap.put("message", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("message", e.getMessage());
		}

		System.out.println(loginCallBackDto.toString());
		String token = jwtService.create("uid", loginCallBackDto.getUid(), "access_token");
		logger.debug("#토큰정보: " + token);
		resultMap.put("access_token", token);
		resultMap.put("user", loginCallBackDto);

		return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
	}
	/**
	 * JWT 토큰을 이용한 유저 정보 반환
	 * 
	 * @param -
	 * @return 
	 */
	@ApiOperation(value = "헤더에 담긴 JWT 토큰 값을 확인, <br> 이를 이용한 유저 정보 검색 및 반환", notes ="@param :  </br> @return : message, user")
	@GetMapping("/getUserInfo")
	public ResponseEntity<Map<String, Object>> getUserInfo(
			HttpServletRequest request ) {
		final String jwt = request.getHeader("access_token");
		String uid = null;
		uid = jwtService.getUserId(jwt);
		
		Map<String, Object> resultMap = new HashMap<>();
		LoginCallBackDto loginCallBackDto = null;
		try {
			loginCallBackDto = getLoginCallBackByUid(uid);
			resultMap.put("message", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("message", e.getMessage());
		}
		
		logger.info("#Get userInfo: {}", loginCallBackDto);
//		String token = jwtService.create("uid", uid, "access_token");
//		logger.debug("#토큰정보: " + token);
//		resultMap.put("access_token", token);
		resultMap.put("user", loginCallBackDto);

		return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
	}
	
	/**
	 * JWT 토큰을 이용한 유저 정보 반환
	 * 
	 * @param -
	 * @return 
	 */
	@GetMapping("/getRefreshToken")
	public ResponseEntity<Map<String, Object>> getRefreshToken(	HttpServletRequest request ) {

		final String jwt = request.getHeader("access_token");
		Map<String, Object> resultMap = new HashMap<>();
		String uid = jwtService.getUserId(jwt);
		
		if(uid == null ) {
			resultMap.put("message", FAIL);
			return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
		}
		
		String token = jwtService.create("uid", uid, "access_token");
		logger.debug("#토큰정보: " + token);
		resultMap.put("access_token", token);
		resultMap.put("message", SUCCESS);
		
		return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
	}
	

	/**
	 * 유저 닉네임 업데이트
	 * 
	 * @param uid, nickname
	 * @return LoginCallBackDto
	 */
	@ApiOperation(value = "닉네임 변경", notes ="@param : uid, 변경될 nickname  </br> @return : uid, userInfoDto, blodId, memoId")
	@PostMapping("/update/Nickname")
	public ResponseEntity<Map<String, Object>> updateNickname(@RequestParam("uid") String uid,
			@RequestParam("nickname") String nickname) {

		Map<String, String> map = new HashMap<String, String>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		map.put("uid", uid);
		map.put("nickname", nickname);
		LoginCallBackDto loginCallBackDto = null;
	
		try {
			// 유저 닉네임 정보 업데이트
			userService.updateNickname(map);
			// 업데이트된 정보 다시 불러오기
			loginCallBackDto = getLoginCallBackByUid(uid);
			resultMap.put("message", SUCCESS);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultMap.put("message", e.getMessage());
		}
		resultMap.put("user", loginCallBackDto);

		return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
	}

	@ApiOperation(value = "회원 정보 삭제", notes ="@param : 삭제할 유저 아이디(uid)  </br> @return : ")
	@PostMapping("/delete/user")
	public ResponseEntity<Map<String, Object>> deleteUser(
//			HttpServletRequest request,
			@RequestParam("uid") String uid) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		System.out.println(uid+" 삭제 요청");
//		final String jwt = request.getHeader("access_token");
//		String uidCheck = null;
//		uidCheck = jwtService.getUserId(jwt);
//		if(uid != uidCheck) {
//			System.out.println("# uid가 일치하지 않습니다. 잘못된 요청입니다.");
//		}
		
		try {
			// 해당 유저 정보 읽어오기.
			UserDto user = userService.findById(uid);

			// User, 블로그, 블로그 글, 댓글 테이블 삭제
			userService.delete(user.getUid(), user.getBlogId());
			resultMap.put("message", "success");

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultMap.put("message", e.getMessage());
		}

		return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
	}

	
	/**
	 * Frontend에 리턴할 유저정보 객체 생성
	 * 
	 * @param uid
	 * @return LoginCallBackDto
	 */
	public LoginCallBackDto getLoginCallBackByUid(String uid) {
		LoginCallBackDto loginCallBackDto = null;
		try {
			UserDto user = userService.findById(uid);
			UserInfoDto info = userService.findUserInfoById(uid);
			
			loginCallBackDto = new LoginCallBackDto(uid, user.getMemoId(), user.getBlogId(),
										info.getNickname(), info.getProfile(), user.getProvider());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return loginCallBackDto;
	}
	

	public SocialOauth getSocialOauth(String socialLoginType) {
		switch (socialLoginType) {
		case "kakao":
			return kakaoOauthService;
		case "naver":
			return naverOauthService;
		case "google":
			return googleOauthService;
		default:
			return null;
		}
	}

}
