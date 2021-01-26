package com.spring.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.spring.web.dto.MemoContentsDto;
import com.spring.web.service.MemoService;

@RestController
@RequestMapping("/memo")
@CrossOrigin(origins = { "*" }, maxAge = 6000)
public class MemoController {
	
	@Autowired
	private MemoService memoService;
	
	@PostMapping
	public ResponseEntity<List<MemoContentsDto>> writeMemo(@RequestBody MemoContentsDto memo) throws Exception{
		memoService.writeMemo(memo);
		return new ResponseEntity<List<MemoContentsDto>>(memoService.listMemo(memo.getMemoId()), HttpStatus.OK);
	}
	
	@GetMapping("{memoId}")
	public ResponseEntity<List<MemoContentsDto>> listMemo(@PathVariable String memoId) throws Exception {
		return new ResponseEntity<List<MemoContentsDto>>(memoService.listMemo(memoId), HttpStatus.OK);
	}
	
	@PutMapping
	public ResponseEntity<List<MemoContentsDto>> modifyMemo(@RequestBody MemoContentsDto memo) throws Exception {
		memoService.modifyMemo(memo);
		return new ResponseEntity<List<MemoContentsDto>>(memoService.listMemo(memo.getMemoId()), HttpStatus.OK);
	}
	
	@DeleteMapping("{memoId}/{memoNum}")
	public ResponseEntity<List<MemoContentsDto>> deleteMemo(@PathVariable String memoId, @PathVariable int memoNum) throws Exception {
		memoService.deleteMemo(memoNum);
		return new ResponseEntity<List<MemoContentsDto>>(memoService.listMemo(memoId), HttpStatus.OK);
	}
}
