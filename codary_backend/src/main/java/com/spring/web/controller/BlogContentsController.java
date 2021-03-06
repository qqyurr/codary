package com.spring.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.web.dto.BlogContentsDto;
import com.spring.web.dto.BlogContentsLikeDto;
import com.spring.web.dto.BlogHashtagDto;
import com.spring.web.dto.BlogPostDto;
import com.spring.web.dto.HashtagDto;
import com.spring.web.dto.UserInfoDto;
import com.spring.web.service.BlogContentsService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api("BlogContentsController V1")
@RestController
@RequestMapping("/blog")
@CrossOrigin(origins = { "*" }, maxAge = 6000)
public class BlogContentsController {
	@Autowired
	private BlogContentsService contentsService;

	public static final Logger logger = LoggerFactory.getLogger(BlogContentsController.class);

	/**
	 * 다른 사람 블로그의 특정 블로그 글 가져오기(조회수 증가)
	 * 
	 * @param blogId, blogContentsId
	 * @return BlogContentsDto
	 */
	@ApiOperation(value = "다른 사람 블로그의 특정 블로그 글 가져오기(조회수 증가)", notes = "@param blogId, blogContentsId  </br> @return BlogContentsDto")
	@GetMapping("{blogId}/{blogContentsId}")
	public ResponseEntity<Map<String, Object>> get(@PathVariable String blogId, @PathVariable int blogContentsId)
			throws Exception {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("data", contentsService.getContent(blogContentsId));
			map.put("hashtag", contentsService.selectHashOfPost(blogContentsId));
			contentsService.increaseContentsView(blogContentsId);
			return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}
	
	/**
	 * 블로그 글 작성
	 * 
	 * 1. 블로그 컨텐츠 생성
	 * 2. 해시태그 생성
	 * 3. 블로그해시태그 생성
	 * 
	 * @param BlogContentsDto(blogId, blogContentsTitle, blogContents,
	 * @return int(blogContentsId)
	 */

	@CacheEvict(cacheNames = {"get_Contents"}, allEntries = true)
	@ApiOperation(value = "블로그 글 작성", notes = "@param BlogContentsDto(blogId, blogContentsTitle, blogContents, blogContentsCover)  </br> @return int(blogContentsId)")
	@PostMapping
	public ResponseEntity<Integer> write(@RequestBody BlogContentsDto content) throws Exception {
		System.out.println("#글작성 호출 ");
		try {
			// 1. 블로그 컨텐츠 테이블 insert
			int blogContentsId = contentsService.writeBlogContent(content);
//			System.out.println("#블로그 컨텐츠 번호: " + content.getBlogContentsId());
			// 2. 해시태그 테이블 insert
			List<Map<String, String>> hashTag = content.getHashTag();
			if(hashTag != null) {
				for (int i = 0; i < hashTag.size(); i++) {
					// 2-1. 해시태그값 파싱
					int key = Integer.parseInt(hashTag.get(i).get("key"));
					String value = hashTag.get(i).get("value");
	
					HashtagDto hash = new HashtagDto(key, value);
					BlogHashtagDto blogHash = null;
					// 2-2. 해시태그 테이블에 존재하지 않는 태그라면 insert
					if (key < 0) {
						contentsService.writeHash(hash);
					}
					// 3. 블로그해시태그 테이블에 insert
					blogHash = new BlogHashtagDto(hash.getHashtagId(), 
							blogContentsId, content.getBlogId());
					contentsService.writeBlogHash(blogHash);
					
					System.out.println("#해시태그 key: " + hash.getHashtagId() + " value:" + value);
				}
			}
			return new ResponseEntity<Integer>(blogContentsId, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

//	@GetMapping("{blogId}")
//	public ResponseEntity<List<BlogContentsDto>> list(@PathVariable int blogId) throws Exception{
//		return new ResponseEntity<List<BlogContentsDto>>(contentsService.listBlogContents(blogId), HttpStatus.OK);
//	}

	/**
	 * 블로그 글 수정
	 * 
	 * @param BlogContentsDto(blogContents, blogContentsTitle, blogContentsCover,
	 *                                      blogId, blogContentsId)
	 * @return BlogContentsDto
	 */
	@CacheEvict(cacheNames = {"get_Contents"}, allEntries = true)
	@ApiOperation(value = "블로그 글 수정", notes = "@param BlogContentsDto(blogContents, blogContentsTitle, blogContentsCover, blogId, blogContentsId)  </br> @return BlogContentsDto")
	@PutMapping
	public ResponseEntity<BlogContentsDto> modify(@RequestBody BlogContentsDto content) throws Exception {
		System.out.println("#글수정 호출 ");
		System.out.println(content.toString());
		
		// 1. 게시글 수정
		int result = contentsService.modifyBlogContent(content);
		
		// 2. 기존 해시태그 정보 삭제
		contentsService.deleteOldHash(content);
		
		// 3. 해시태그 내용 삽입
		List<Map<String, String>> hashTag = content.getHashTag();
		if(hashTag != null) {
			for (int i = 0; i < hashTag.size(); i++) {
				// 2-1. 해시태그값 파싱
				int key = Integer.parseInt(hashTag.get(i).get("key"));
				String value = hashTag.get(i).get("value");

				HashtagDto hash = new HashtagDto(key, value);
				BlogHashtagDto blogHash = null;
				// 2-2. 해시태그 테이블에 존재하지 않는 태그라면 insert
				if (key < 0) {
					contentsService.writeHash(hash);
				}
				// 3. 블로그해시태그 테이블에 insert
				blogHash = new BlogHashtagDto(hash.getHashtagId(), 
						content.getBlogContentsId(), content.getBlogId());
				contentsService.writeBlogHash(blogHash);
				
				System.out.println("#해시태그 key: " + hash.getHashtagId() + " value:" + value);
			}
		}
		
		if (result == 1)
			return new ResponseEntity<BlogContentsDto>(contentsService.getContent(content.getBlogContentsId()), HttpStatus.OK);
		else
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
	}

	/**
	 * 블로그 글 삭제
	 * 
	 * @param blogId, blogContentsId
	 * @return List<BlogContentsDto>
	 */
	@CacheEvict(cacheNames = {"get_Contents"}, allEntries = true)
	@ApiOperation(value = "블로그 글 삭제", notes = "@param blogId, blogContentsId  </br> @return List<BlogContentsDto>")
	@DeleteMapping("{blogId}/{blogContentsId}")
	public ResponseEntity<List<BlogContentsDto>> delete(@PathVariable String blogId, @PathVariable int blogContentsId)
			throws Exception {
		int result = contentsService.deleteBlogContent(blogContentsId);
		if (result == 1)
			return new ResponseEntity<List<BlogContentsDto>>(contentsService.listBlogContents(blogId), HttpStatus.OK);
		else
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
	}

	/**
	 * 내 블로그 글 클릭 시 Log남기고 글 가져오기
	 * 
	 * @param blogId, blogContentsId
	 * @return BlogContentsDto
	 */
	@ApiOperation(value = "내 블로그 글 클릭 시 Log남기고 글 가져오기", notes = "@param uid, blogId, blogContentsId  </br> @return BlogContentsDto")
	@GetMapping("log/{uid}/{blogId}/{blogContentsId}")
	public ResponseEntity<Map<String, Object>> writeLog(@PathVariable String uid, @PathVariable String blogId,
			@PathVariable int blogContentsId) throws Exception {
		logger.info("=======내 블로그 글 클릭 시 Log남기고 글 가져오기=======");
		ResponseEntity<Map<String, Object>> resEntity = null;
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			BlogContentsDto data = contentsService.writeLog(uid, blogId, blogContentsId);
			map.put("msg", "success");
			map.put("data", data);
			map.put("hashtag", contentsService.selectHashOfPost(blogContentsId));
			resEntity = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			map.put("msg", "fail");
			resEntity = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
			e.printStackTrace();
		}
		return resEntity;
	}

//	/**
//	 * 블로그 글 조회수 증가
//	 * 
//	 * @param BlogContentsId
//	 * @return 
//	 */
//	@ApiOperation(value = "블로그 글 클릭 시 조회 수 증가하기", notes ="@param blogContentsId @return ")
//	@GetMapping("increase/view/{blogContentsId}")
//	public ResponseEntity<Map<String, String>> increaseContentsView(@PathVariable int blogContentsId){
//		Map<String, String> map = new HashMap<>();
//		try {
//			contentsService.increaseContentsView(blogContentsId);
//			map.put("msg", "success");
//			return new ResponseEntity<>(map, HttpStatus.OK);
//		}catch(Exception e) {
//			e.printStackTrace();
//			map.put("msg", "fail");
//			return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
//		}
//	}

	/**
	 * 블로그 글 추천
	 * 
	 * @param
	 * @return List<Map<String, Object>>
	 */
	@ApiOperation(value = "블로그 글 추천", notes = "@param </br> @return List<Map<String, Object>>")
	@GetMapping("recommend")
	public ResponseEntity<List<Map<String, Object>>> recommend() throws Exception {
		try {
			return new ResponseEntity<List<Map<String, Object>>>(contentsService.recommendBlogContents(),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		}
	}

	/**
	 * 블로그 글 좋아요 눌렀는지 여부
	 * 
	 * @param BlogContentLikeDto
	 * @return msg가 "yet"이면 안 누른 상태, "like"이면 누른 상태
	 */
	@ApiOperation(value = "블로그 글 좋아요 눌렀는지 여부", notes = "@param BlogContentLikeDto </br> @return msg가 \"yet\"이면 안 누른 상태, \"like\"이면 누른 상태")
	@PostMapping("checkContentsLike")
	public ResponseEntity<Map<String, String>> readBlogContentsLike(@RequestBody BlogContentsLikeDto like)
			throws Exception {
		Map<String, String> map = new HashMap<>();
		try {
			BlogContentsLikeDto res = contentsService.readBlogContentsLike(like);
			if (res == null) {
				map.put("msg", "yet");
				return new ResponseEntity<Map<String, String>>(map, HttpStatus.OK);
			} else {
				map.put("msg", "like");
				return new ResponseEntity<Map<String, String>>(map, HttpStatus.OK);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * 블로그 글 좋아요
	 * 
	 * @param BlogContentLikeDto
	 * @return
	 */
	@ApiOperation(value = "블로그 글 좋아요 누르기", notes = "@param BlogContentsLikeDto </br> @return ")
	@PostMapping("contentsLike")
	public ResponseEntity<Map<String, String>> contentsLike(@RequestBody BlogContentsLikeDto like) throws Exception {
		Map<String, String> map = new HashMap<>();
		try {
			contentsService.contentLike(like);
			map.put("msg", "success");
			return new ResponseEntity<Map<String, String>>(map, HttpStatus.OK);
		} catch (Exception e) {
			map.put("msg", "fail");
			e.printStackTrace();
			return new ResponseEntity<Map<String, String>>(map, HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * 블로그 글 좋아요 취소
	 * 
	 * @param BlogContentLikeDto
	 * @return
	 */
	@ApiOperation(value = "블로그 글 좋아요 취소하기", notes = "@param BlogContentLikeDto </br> @return ")
	@PostMapping("contentsUnlike")
	public ResponseEntity<Map<String, String>> contentsUnlike(@RequestBody BlogContentsLikeDto like) throws Exception {
		Map<String, String> map = new HashMap<>();
		try {
			contentsService.contentUnlike(like);
			map.put("msg", "success");
			return new ResponseEntity<Map<String, String>>(map, HttpStatus.OK);
		} catch (Exception e) {
			map.put("msg", "fail");
			e.printStackTrace();
			return new ResponseEntity<Map<String, String>>(map, HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * 
	 * 블로그 아이디로 user 정보 가져오기.
	 * 
	 * @param blog_id
	 * @return List<CommentDto>
	 */
	@ApiOperation(value = "블로그 아이디로 user 정보 가져오기.", notes = "@param : blogId  </br> @return : UserInfo")
	@GetMapping("blogUserInfo/{blogId}")
	public ResponseEntity<Map<String, Object>> userInfo(@PathVariable String blogId) {
		logger.info("=======유저 정보 가져오기=======");

		ResponseEntity<Map<String, Object>> resEntity = null;
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			UserInfoDto data = contentsService.userInfo(blogId);
			map.put("msg", "success");
			map.put("data", data);
			resEntity = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			map.put("msg", "fail");
			resEntity = new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
			e.printStackTrace();
		}
		return resEntity;
	}

	@GetMapping("/getHashtag")
	public ResponseEntity<Map<String, Object>> getHashtag(String keyword) {

		System.out.println("#hashtag 정보 읽어오기");
		keyword = keyword != null ? keyword : "";
		System.out.println("#검색어 " + keyword);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<HashtagDto> selectHash = null;
		try {
			selectHash = contentsService.selectHash(keyword);
			resultMap.put("list", selectHash);
			resultMap.put("msg", "success");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			resultMap.put("msg", "fail");
			e.printStackTrace();
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
	}
	
	
	/**
	 * 해시태그 value 값에 대응하는 key값을 반환한다.
	 * 
	 */
	@ApiOperation(value = "해시태그 value 값에 대응하는 key값을 반환한다.")
	@PostMapping("/getTagKey")
	public ResponseEntity<Map<String, Object>> getTagKey(@RequestBody Map<String, String> map) {

		String value = map.get("value");
		System.out.println("#해시태그 " + value +"의 key값 찾기 ");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashtagDto hash = null;
		
		// 1. value값으로 해시태그 키값 찾기 
		try {
			
			if(contentsService.findTagByValue(value) != null) {
				hash = contentsService.findTagByValue(value);
			}else {
				hash = new HashtagDto(-1, value);
			}
			System.out.println(hash.toString());
			resultMap.put("tag", hash);
			resultMap.put("msg", "success");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			resultMap.put("msg", "fail");
			e.printStackTrace();
		}
		
		return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
	}

}
