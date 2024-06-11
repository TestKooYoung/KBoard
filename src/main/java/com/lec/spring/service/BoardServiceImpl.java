package com.lec.spring.service;

import com.lec.spring.domain.Attachment;
import com.lec.spring.domain.Post;
import com.lec.spring.domain.User;
import com.lec.spring.repository.AttachmentRepository;
import com.lec.spring.repository.PostRepository;
import com.lec.spring.repository.UserRepository;
import com.lec.spring.util.U;
import jakarta.servlet.http.HttpSession;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@Service
public class BoardServiceImpl implements BoardService {

    @Value("${app.upload.path}")
    private String uploadDir;

    @Value("${app.pagination.write_pages}")
    private int WRITE_PAGES;

    @Value("${app.pagination.page_rows}")
    private int PAGE_ROWS;

    private PostRepository postRepository;
    private UserRepository userRepository;
    private AttachmentRepository attachmentRepository;

    @Autowired
    public BoardServiceImpl(SqlSession sqlSession){
        //MyBatis 가 생성한 SqlSession 빈(bean) 객체 주입
        postRepository = sqlSession.getMapper(PostRepository.class);
        userRepository = sqlSession.getMapper(UserRepository.class);
        attachmentRepository = sqlSession.getMapper(AttachmentRepository.class);

        System.out.println("BoradService() 생성");
    }

    @Override
    public int write(Post post, Map<String, MultipartFile> files) {
        User user = U.getLoggedUser();
        // 현재 로그인 한 작성자 정보
        // 위정보는 session 의 정보

        userRepository.findById(user.getId());
        // DB에서 다시 읽어온다.
        post.setUser(user);
        // 글 작성자 셋팅

        int cnt = postRepository.save(post);
        // 글먼저 저장 (그래야 AI된 pk post_id값을 받아온다.)

        // 첨부파일 추가
        addFiles(files, post.getId());
        //첨부파일 추가

        return cnt;
    }

    //특정 글(id) 에 첨부파일(들) 추가
    private void addFiles(Map<String, MultipartFile> files, Long id) {
        if (files == null) return;

        for (Map.Entry<String, MultipartFile> e : files.entrySet()){
            if (!e.getKey().startsWith("upfile")) continue;
            // name="upfile##" 인 경우만 첨부파일 등록. (이유, 다른 웹에디터와 섞이지 않도록..ex: summernote)

            //첨부파일 정보 출력
            System.out.println("\n 첨부파일 정보: " + e.getKey());
            // name 값

            U.printFileInfo(e.getValue());
            //MultipartFile 정보 - 학습목적 작성.
            System.out.println();

            Attachment file = upload(e.getValue());
            //물리적인 파일 저장

            // 성공하면 DB에도 저장

            if (file != null){
                file.setPost_id(id);
                attachmentRepository.save(file);
            }
        }
    }


    private Attachment upload(MultipartFile multipartFile) {
        // 물리적으로 파일을 서버에 저장
        // 중복된 파일이름 -> rename 처리.
        Attachment attachment = null;

        String originalFilename = multipartFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) return null;
        // 첨부되 파일 없으면 pass

        String sourceName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        //원본파일명
        // cleanPath 의 기능 -> C:\Users\aaa\bbbb/dsaf/asdfsafd.ddd
        //                          "\" -> "/" 로 변경

        String fileName = sourceName;
        // 저장될 파일 명

        File file = new File(uploadDir, fileName);
        if (file.exists()){
            //파일이 중복되는지 확인
            // 이미 존재하는 파일명, 중복된다면 다른 이름으로 변경 하여 저장

            int pos = fileName.lastIndexOf(".");
            // a.txt => a_2378142783946.txt  : time stamp 값을 활용할거다!
            // "a" => "a_2378142783946"  : 확장자 없는 경우
            if (pos > -1){
                // 확장자가 있는 경우

                String name = fileName.substring(0, pos);
                // 파일 '이름'

                String ext =fileName.substring(pos+1);
                // 파일 '확장자'

                fileName = name + "_" + System.currentTimeMillis() + "." + ext;
                // 중복방지 회피를 위해 새로운 이름 (타임스탬프, 현재시간 ms)  를 파일명에 추가

            }else {
                // 확장자가 없는 파일의 경우
                fileName += "_" + System.currentTimeMillis();
            }

        }
        System.out.println("fileName: " + fileName);

        //java.io.*
        //java.nio.*

        Path copyOfLocation = Paths.get(new File(uploadDir, fileName).getAbsolutePath());
        System.out.println(copyOfLocation);

        try {
            // inputStream을 가져와서
            // copyOfLocation (저장위치)로 파일을 쓴다.
            // copy의 옵션은 기존에 존재하면 REPLACE(대체한다), 오버라이딩 한다

            // java.nio.file.Files

            Files.copy(
                    multipartFile.getInputStream(),
                    copyOfLocation,
                    StandardCopyOption.REPLACE_EXISTING
                    // 기존에 존재하면 덮어쓰기
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        attachment = Attachment.builder()
                .filename(fileName)
                .sourcename(sourceName)
                .build();

        return attachment;

    }

    @Override
    @Transactional
    public Post detail(Long id) {
        postRepository.incViewCnt(id);
//        조회수 증가(update)
        Post post = postRepository.findById(id);

        if (post != null){
            // 첨부파일(들) 정보 가져오기
            List<Attachment> fileList = attachmentRepository.findByPost(post.getId());

            // 이미지 파일 여부 세팅
            setImage(fileList);

            post.setFileList(fileList);

        }
        return post;
    }

    private void setImage(List<Attachment> fileList) {
        // 이미지 파일 여부 세팅

        String realPath = new File(uploadDir).getAbsolutePath();
        // upload 실제 물리적인 경로

        for (Attachment attachment : fileList){
            BufferedImage imgData = null;
            File f = new File(realPath, attachment.getFilename());
            // 저장된 첨부파일에 대한 File객체

            try {
                imgData = ImageIO.read(f);
                //   ↑ 이미지가 아닌 경우는 null 리턴
                // ※ ↑ 파일이 존재 하지 않으면 IOExcepion 발생한다

                if (imgData != null) attachment.setImage(true);
            } catch (IOException e) {
                System.out.println("파일존재안함: " + f.getAbsolutePath() + "[" + e.getMessage()+ "]" );

            }

        }

    }

    @Override
    public List<Post> list() {
        return postRepository.findAll();
    }

    @Override
    public List<Post> list(Integer page, Model model) {
        //현재 페이지
        if (page == null || page < 1) page = 1;
        // 디폴트 1 페이지

        // 페이징
        // writePages: 한 [페이징] 당 몇개의 페이지가 표시되나
        // pageRows: 한 '페이지'에 몇개의 글을 리스트 할것인가?

        HttpSession session = U.getSession();
        Integer writePages = (Integer) session.getAttribute("writePages");
        if (writePages == null) writePages = WRITE_PAGES;

        Integer pageRows =(Integer) session.getAttribute("pageRows");
        if (pageRows == null) pageRows = PAGE_ROWS;

        session.setAttribute("page", page);
        // 현재페이지 번호  session에 저장.

        long cnt = postRepository.countAll();
        // 글 목록 전체의 개수.

        int totalPage = (int)Math.ceil(cnt / (double)pageRows);
        // 총 몇 '페이지' 분량인지 계산.

        int startPage = 0;
        int endPage = 0;
        //페이징 에 표시할 '시작페이지' 와 '마지막페이지'

        List<Post> list = null;
        //해당 '페이지' 의 글 목록

        if (cnt > 0){
            // 데이터가 최소 1개 이상 있는 경우만 페이징

            if(page > totalPage) page = totalPage;
            //page 값 보정

            int fromRow = (page - 1) * pageRows;
            //fromRow : 몇번째 데이터 부터

            startPage = (((page - 1) / writePages) * writePages) + 1;
            endPage = startPage + writePages - 1;
            if (endPage >= totalPage) endPage = totalPage;
            // [페이징] 에 표시할 '시작페이지' 와 '마지막페이지' 계산

            list = postRepository.selectFromRow(fromRow, pageRows);
            //해당 페이지의 글 목록 읽어오기
            model.addAttribute("list",list);

        }else {
            page = 0;
        }
        model.addAttribute("cnt", cnt);  // 전체 글 개수
        model.addAttribute("page", page); // 현재 페이지
        model.addAttribute("totalPage", totalPage);  // 총 '페이지' 수
        model.addAttribute("pageRows", pageRows);  // 한 '페이지' 에 표시할 글 개수

        // [페이징]
        model.addAttribute("url", U.getRequest().getRequestURI());  // 목록 url
        model.addAttribute("writePages", writePages); // [페이징] 에 표시할 숫자 개수
        model.addAttribute("startPage", startPage);  // [페이징] 에 표시할 시작 페이지
        model.addAttribute("endPage", endPage);   // [페이징] 에 표시할 마지막 페이지

        return list;
    }

    public Post selectById(Long id){
        Post post = postRepository.findById(id);

        if(post != null){
            // 첨부파일 정보 가져오기
            List<Attachment> fileList = attachmentRepository.findByPost(post.getId());
            setImage(fileList);   // 이미지 파일 여부 세팅
            post.setFileList(fileList);
        }

        return post;
    }

    public int update(Post post, Map<String, MultipartFile> files, Long[] delfile){

        int result = 0;

        result = postRepository.update(post);

        addFiles(files, post.getId());
        // 새로운 첨부파일 추가

        if (delfile != null){
            for (long fileId : delfile){
                Attachment file = attachmentRepository.findById(fileId);
                if (file != null){
                    delFile(file);
                    // 물리적으로 파일 삭제
                    attachmentRepository.delete(file);
                    // DB 에서 삭제
                }
            }
        }
        // 삭제할 첨부파일들은 삭제하기

        return result;
    }

    private void delFile(Attachment file) {
        // 특정첨부파일 물리적으로 삭제
        String saveDirectory = new File(uploadDir).getAbsolutePath();

        File f = new File(saveDirectory, file.getFilename());
        // 저장된 파일명 (rename 된 파일명)
        // 물리적으로 저장된 파일
        System.out.println("삭제시도 --> " + f.getAbsolutePath());

        if (f.exists()){

            if (f.delete()){
                System.out.println("삭제성공");
            }else {
                System.out.println("삭제실패 ");
            }

        }else {
            System.out.println("파일이 존재하지 않습니다.");
        }

    }

    @Override
    public int deleteById(Long id) {
        int result = 0;

        Post post = postRepository.findById(id);
//        존재하는 데이터인지 확인.
//        post.getUser().getId()

        if(post != null ){
//            존재한다면 삭제진행
            // 물리적으로 저장된 첨부 파일(들) 부터 삭제

            List<Attachment> fileList = attachmentRepository.findByPost(id);

            if(fileList != null && fileList.size() > 0){
                for (Attachment file : fileList){
                    delFile(file);
                }
            }

            // 글 삭제 (참조하는 첨부파일, 댓글 등도 같이 삭제 될것이다.
            // ON DELETE CASCADE)
            result = postRepository.delete(post);
        }


        return result;
    }

}
