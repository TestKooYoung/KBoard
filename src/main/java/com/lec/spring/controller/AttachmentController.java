package com.lec.spring.controller;

import com.lec.spring.domain.Attachment;
import com.lec.spring.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
// 데이터 response 하는 컨트롤러
public class AttachmentController {

    @Value("${app.upload.path}")
    private String uploadDir;

    private AttachmentService attachmentService;

    @Autowired
    public void setAttachmentService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }



    @RequestMapping("/board/download")
    public ResponseEntity<?> download(Long id){

        // 파일 다운로드
        // id: 첨부파일의 id
        // ResponseEntity<T> 를 사용하여
        // <?> 는 와일드카드 어떤 값이든 보낼수 있음.
        // '직접' Response data 를 구성

        if (id == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        // 400 error

        Attachment file = attachmentService.findById(id);
        if(file == null) return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        // 404 error

        String sourceName = file.getSourcename();
        // 원본 이름

        String fileName = file.getFilename();
        // 저장된 파일명

        String path = new File(uploadDir, fileName).getAbsolutePath();
        // 저장되 파일의 절대경로

        try {
            String mimeType = Files.probeContentType(Paths.get(path));
            // 파일 유형 (MIME type) 추출
            // ex) image/png

            if (mimeType == null){
                mimeType = "application/octet-stream";
                // 일련의 byte 스트림 타입
                // 유형이 알려지지 않은 경우 지정
            }
            // 파일 유형이 지정되지 않은경우

            Path filePath = Paths.get(path);
            // response body 준비

            Resource resource = new InputStreamResource(Files.newInputStream(filePath));
            // Resource <- InputStream <- 저장된 파일

            HttpHeaders headers = new HttpHeaders();
            // response header 세팅

            headers.setContentDisposition(
                    ContentDisposition.builder("attachment")
                            .filename(URLEncoder.encode(sourceName,"UTF-8"))
                            .build()
            );
            // 원본 파일 이름 (sourceName) 으로 다운로드 하게 하기 위한 세팅
            // 반드시 url 인코딩 해야함
            headers.setCacheControl("no-cache");
            // 캐쉬 없음.
            headers.setContentType(MediaType.parseMediaType(mimeType));
            // 유형지정

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
            // ResponseEntity<> 리턴 (body, header, status)
            //200
        } catch (IOException e) {
            return new ResponseEntity<>(null, null, HttpStatus.CONFLICT);
            // 409 error
        }

    }

}
